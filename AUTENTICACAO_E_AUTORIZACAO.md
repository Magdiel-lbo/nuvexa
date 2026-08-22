# Autenticação e Autorização no `nuvexa/backend`

Este documento explica, linha por linha, como funciona o fluxo de autenticação (JWT)
e autorização (Spring Security) neste projeto, seguido de uma explicação sobre a
diferença entre arquitetura **monolítica** e **modularizada**, e onde este projeto
se encaixa.

---

## 1. Visão geral do fluxo

```
Cliente (frontend)
   │
   │ POST /api/v1/auth/register  ou  POST /api/v1/auth/login
   ▼
AuthController  →  AuthService  →  UserRepository (banco) / JwtService (gera token)
   │
   ▼
Retorna AuthResponse { token, tokenType, role }
   │
   │ (em requests seguintes, o cliente manda o token)
   ▼
Header: Authorization: Bearer <token>
   │
   ▼
JwtAuthenticationFilter (intercepta TODA requisição antes de chegar no controller)
   │  → valida o token, carrega o usuário, popula o SecurityContext
   ▼
SecurityConfig (decide se a rota exige autenticação ou não)
   │
   ▼
Controller protegido (ex: PatientController) só é executado se autenticado
```

As peças envolvidas:

| Arquivo | Papel |
|---|---|
| `AuthController` | Endpoints HTTP públicos de auth (`/register`, `/login`, `/forgot-password`, `/reset-password`) |
| `AuthService` | Regra de negócio: cria usuário, autentica, gera token, fluxo de reset de senha |
| `JwtService` | Gera e valida o token JWT (assinatura, expiração) |
| `JwtAuthenticationFilter` | Filtro que roda em toda requisição, lê o header `Authorization` e autentica o usuário no contexto do Spring Security |
| `UserDetailsServiceImpl` | Ponte entre o Spring Security e o banco: busca o `User` pelo e-mail |
| `SecurityConfig` | Define quais rotas são públicas/privadas, registra o filtro JWT, configura CORS, CSRF, sessão stateless |
| `JwtAuthenticationEntryPoint` | Responde 401 quando não autenticado |
| `JwtAccessDeniedHandler` | Responde 403 quando autenticado mas sem permissão |
| `User` (model) | Entidade JPA que também implementa `UserDetails` (o "usuário" do Spring Security é o próprio usuário do domínio) |

---

## 2. `AuthController.java` — linha por linha

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
```
- `@RestController`: cada método retorna diretamente o corpo JSON da resposta (não uma view).
- `@RequestMapping("/api/v1/auth")`: prefixo comum de todas as rotas desta classe.
- `@RequiredArgsConstructor` (Lombok): gera um construtor com os campos `final`, ou seja, injeta `AuthService` via construtor automaticamente (sem precisar escrever `@Autowired`).

```java
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
```
- `POST /api/v1/auth/register`.
- `@Valid`: dispara as validações Bean Validation declaradas no `RegisterRequest` (`@NotBlank`, `@Email`, `@Size(min=8)`). Se falhar, o Spring lança `MethodArgumentNotValidException` antes mesmo de entrar no método.
- `@ResponseStatus(HttpStatus.CREATED)`: HTTP 201 em vez do padrão 200.
- Delega toda a lógica para `authService.register(...)` — o controller **não tem regra de negócio**, só orquestra HTTP.

```java
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
```
- Mesmo padrão, mas devolve 200 (padrão) e delega para `authService.login`.

```java
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) { ... }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) { ... }
```
- Fluxo de "esqueci minha senha" — dois passos: pedir o link (`forgot-password`) e efetivamente trocar a senha usando o token recebido (`reset-password`).

---

## 3. `AuthService.java` — linha por linha

### Constantes e dependências

```java
private static final Locale MESSAGE_LOCALE = Locale.of("pt", "BR");
private static final int RESET_TOKEN_BYTES = 32;
private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private final AuthenticationManager authenticationManager;
private final JwtService jwtService;
private final MessageSource messageSource;
private final SecureRandom secureRandom = new SecureRandom();

@Value("${app.cors.allowed-origin}")
private String frontendUrl;
```
- `RESET_TOKEN_BYTES = 32`: o token de reset de senha terá 32 bytes de entropia (256 bits) — bem forte contra brute force.
- `RESET_TOKEN_VALIDITY_MINUTES = 30`: o link de redefinição expira em 30 minutos.
- `PasswordEncoder` (BCrypt, ver `SecurityConfig`): usado para criar hash da senha, nunca guardamos senha em texto puro.
- `AuthenticationManager`: componente do Spring Security que efetivamente checa "esse e-mail/senha combinam?".
- `SecureRandom`: gerador de números aleatórios criptograficamente seguro (não é o `Random` comum), usado para o token de reset.
- `frontendUrl`: injetado do `application.yml` (`app.cors.allowed-origin`), usado para montar o link do e-mail de reset.

### Cenário 1 — Registro (`register`)

```java
public AuthResponse register(RegisterRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new CustomException(HttpStatus.CONFLICT, resolveMessage("auth.emailInUse", request.getEmail()));
    }

    User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .enabled(true)
            .build();

    User saved = userRepository.save(user);
    log.info("Usuário registrado com id={}", saved.getId());

    return buildAuthResponse(saved);
}
```
Passo a passo:
1. Verifica se já existe um usuário com aquele e-mail → se sim, `409 CONFLICT`.
2. Constrói o `User` com `Role.USER` fixo (ninguém se registra como `ADMIN` pela API pública — importante do ponto de vista de segurança).
3. **`passwordEncoder.encode(...)`** — a senha é transformada em hash BCrypt antes de ir para o banco. Nunca se armazena a senha original.
4. `enabled(true)` — conta já nasce habilitada (não há fluxo de confirmação de e-mail aqui).
5. Salva no banco.
6. `buildAuthResponse(saved)` — já gera um token JWT e devolve, ou seja, **registro já loga automaticamente o usuário** (não precisa fazer login separado depois).

### Cenário 2 — Login (`login`)

```java
public AuthResponse login(LoginRequest request) {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (AuthenticationException ex) {
        throw new CustomException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials"));
    }

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, resolveMessage("auth.invalidCredentials")));

    log.info("Usuário autenticado com id={}", user.getId());
    return buildAuthResponse(user);
}
```
1. Cria um `UsernamePasswordAuthenticationToken` "não autenticado" (e-mail + senha em texto puro digitados no login) e entrega para o `authenticationManager.authenticate(...)`.
2. Por baixo dos panos, o `AuthenticationManager` usa o `DaoAuthenticationProvider` (registrado em `SecurityConfig`), que:
   - Chama `UserDetailsServiceImpl.loadUserByUsername(email)` para buscar o usuário no banco.
   - Compara a senha digitada com o hash salvo, usando o mesmo `PasswordEncoder` (BCrypt) — `passwordEncoder.matches(senhaDigitada, hashSalvo)`.
   - Se não bater ou o usuário não existir → lança `AuthenticationException` (ex: `BadCredentialsException`).
3. Se a exceção for lançada, é convertida numa `CustomException` com **401 Unauthorized** — e note que a mensagem é genérica ("credenciais inválidas"), não diz se foi "e-mail não existe" ou "senha errada". Isso é proposital: evita que um atacante descubra quais e-mails estão cadastrados (User enumeration).
4. Se a autenticação passar, busca o `User` de novo (para montar a resposta) e gera o token.

### Cenário 3 — Esqueci a senha (`forgotPassword`)

```java
public MessageResponse forgotPassword(ForgotPasswordRequest request) {
    Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

    if (userOptional.isPresent()) {
        User user = userOptional.get();
        String rawToken = generateRawToken();

        user.setResetTokenHash(hashToken(rawToken));
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
        userRepository.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
        log.info("Link de recuperação de senha para {} (válido por {} min): {}", ...);
    } else {
        log.info("Recuperação de senha solicitada para e-mail não cadastrado: {}", request.getEmail());
    }

    return new MessageResponse(resolveMessage("auth.forgotPassword.sent"));
}
```
Pontos importantes de segurança:
- **A resposta HTTP é sempre a mesma**, exista ou não o e-mail (`auth.forgotPassword.sent`) — de novo, evita enumeração de usuários por e-mail.
- O token que vai no link (`rawToken`) é aleatório (32 bytes via `SecureRandom`), mas **o que é salvo no banco é o hash SHA-256 dele** (`resetTokenHash`), não o token em si. Assim, mesmo que o banco vaze, ninguém consegue usar os hashes para resetar senhas de outros usuários (o token original não é recuperável a partir do hash).
- `resetTokenExpiresAt` = agora + 30 minutos → expira o link.
- **Nota**: hoje o link é apenas logado (`log.info`) — não há envio real de e-mail implementado; em produção isso precisaria de um serviço de e-mail.

### Cenário 4 — Resetar a senha (`resetPassword`)

```java
public MessageResponse resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByResetTokenHash(hashToken(request.getToken()))
            .filter(u -> u.getResetTokenExpiresAt() != null && u.getResetTokenExpiresAt().isAfter(LocalDateTime.now()))
            .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, resolveMessage("auth.token.invalid")));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setResetTokenHash(null);
    user.setResetTokenExpiresAt(null);
    userRepository.save(user);

    return new MessageResponse(resolveMessage("auth.resetPassword.success"));
}
```
1. Recebe o token **em texto puro** do usuário (veio do link do e-mail), calcula o hash e busca no banco por `resetTokenHash`.
2. `.filter(...)` garante que o token não expirou (compara `resetTokenExpiresAt` com "agora").
3. Se token inválido ou expirado → `400 BAD_REQUEST`.
4. Se válido: define nova senha (com novo hash BCrypt), e **invalida o token** (`null` nos dois campos) para que não possa ser reutilizado.

### Helpers

```java
private String generateRawToken() {
    byte[] bytes = new byte[RESET_TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
}
```
Gera 32 bytes aleatórios seguros e codifica em Base64 URL-safe (sem `=` de padding) para poder ir direto na URL.

```java
private String hashToken(String rawToken) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(hash);
}
```
SHA-256 do token, representado em hexadecimal — é isso que fica salvo no banco.

```java
private AuthResponse buildAuthResponse(User user) {
    return AuthResponse.builder()
            .token(jwtService.generateToken(user))
            .tokenType("Bearer")
            .role(user.getRole().name())
            .build();
}
```
Gera o JWT (via `JwtService`) e monta a resposta padrão devolvida tanto no registro quanto no login.

---

## 4. `JwtService.java` — geração e validação do token

```java
@Value("${app.jwt.secret}")
private String secret;

@Value("${app.jwt.expiration-minutes}")
private long expirationMinutes;
```
Vêm do `application.yml`:
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:change-this-secret-change-this-secret-change-this-secret}
    expiration-minutes: ${JWT_EXPIRATION_MINUTES:60}
```
- `secret`: chave simétrica usada para assinar/validar o token (HMAC). **Em produção deve vir de variável de ambiente `JWT_SECRET`**, nunca usar o valor default.
- `expiration-minutes`: token expira em 60 minutos por padrão.

```java
public String generateToken(UserDetails userDetails) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationMinutes * 60_000);

    return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(getSigningKey())
            .compact();
}
```
- `userDetails.getUsername()` — no seu `User`, `getUsername()` retorna o **e-mail** (veja `User.java`). Então o `subject` (claim `sub`) do JWT é o e-mail do usuário.
- `issuedAt`/`expiration` — claims padrão `iat` e `exp`.
- `signWith(getSigningKey())` — assina o token com HMAC-SHA (a biblioteca `jjwt` escolhe o algoritmo compatível com o tamanho da chave).
- `.compact()` — serializa em `header.payload.signature` (o formato JWT: 3 partes separadas por `.`).

```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
}
```
Lê o `sub` (e-mail) de dentro do token.

```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
}
```
Válido se: o e-mail do token bate com o usuário carregado do banco **e** não expirou.

```java
private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return claimsResolver.apply(claims);
}
```
- `verifyWith(getSigningKey())` — aqui é onde a **assinatura é verificada**. Se alguém alterar o payload do token sem saber o `secret`, a assinatura não bate e `parseSignedClaims` lança `JwtException` (ex.: `SignatureException`).
- `getSigningKey()` converte a string `secret` em bytes e cria uma `SecretKey` HMAC (`Keys.hmacShaKeyFor`).

**Resumo de segurança do JWT aqui**: é um token *opaco só na aparência* — na verdade qualquer um pode decodificar o payload (é só Base64), mas **não pode forjar/alterar** porque não tem o `secret` para recalcular a assinatura.

---

## 5. `JwtAuthenticationFilter.java` — o "porteiro" de cada requisição

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```
- `OncePerRequestFilter`: garante que o filtro roda **uma única vez por requisição** (mesmo em forwards internos). É a classe base padrão do Spring para filtros de autenticação.

```java
String authHeader = request.getHeader("Authorization");

if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
    filterChain.doFilter(request, response);
    return;
}
```
- Se não tem header `Authorization` ou não começa com `"Bearer "`, **deixa passar sem autenticar** (não bloqueia aqui — quem decide se a rota exige autenticação é o `SecurityConfig`). Isso é importante: rotas públicas como `/api/v1/auth/login` passam por este filtro normalmente, só que sem popular o contexto de autenticação.

```java
String token = authHeader.substring(BEARER_PREFIX.length());

try {
    String email = jwtService.extractUsername(token);

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (jwtService.isTokenValid(token, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
} catch (JwtException | UsernameNotFoundException ex) {
    SecurityContextHolder.clearContext();
}

filterChain.doFilter(request, response);
```
Passo a passo:
1. Extrai o token (remove o prefixo `"Bearer "`).
2. `jwtService.extractUsername(token)` — aqui já valida a assinatura (se for inválida, lança `JwtException`, capturada no `catch`).
3. Se já existe uma autenticação no contexto, não refaz (evita trabalho duplicado dentro da mesma requisição).
4. Carrega o `UserDetails` (na prática, o `User` do banco) via `UserDetailsServiceImpl`.
5. `jwtService.isTokenValid(...)` — confere e-mail + expiração de novo (dupla checagem).
6. Se tudo certo, cria um `UsernamePasswordAuthenticationToken` **já autenticado** (`principal=userDetails`, `credentials=null`, `authorities=userDetails.getAuthorities()`) e registra no `SecurityContextHolder`.
7. A partir daqui, **para o resto da requisição, o Spring Security considera o usuário autenticado** e sabe suas `authorities` (roles).
8. Qualquer erro de token (expirado, assinatura inválida, usuário não existe mais) limpa o contexto — a requisição segue **sem autenticação**, e então o `SecurityConfig`/`JwtAuthenticationEntryPoint` decide o que fazer (normalmente 401, se a rota exigir autenticação).
9. `filterChain.doFilter(request, response)` sempre é chamado ao final — deixa a requisição seguir para os próximos filtros/controller.

---

## 6. `UserDetailsServiceImpl.java`

```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
}
```
- Interface padrão do Spring Security (`UserDetailsService`). O parâmetro se chama "username" por convenção, mas aqui é o e-mail.
- Como `User implements UserDetails` diretamente (ver seção 8), não precisa de nenhuma classe adaptadora — o próprio objeto de domínio já serve como usuário de segurança.

---

## 7. `SecurityConfig.java` — a "planta" de segurança da aplicação

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
Define o algoritmo de hash de senha: **BCrypt** (inclui salt automático, é lento de propósito para dificultar brute-force).

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}
```
Liga as duas peças: "para autenticar, busque o usuário via `userDetailsService` e compare a senha usando `passwordEncoder`". É esse provider que o `AuthenticationManager` usa por baixo dos panos no `login()`.

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```
Expõe o `AuthenticationManager` como bean para poder ser injetado no `AuthService`.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .anyRequest().authenticated())
            .exceptionHandling(handling -> handling
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```
Linha por linha:
- `.csrf(disable)` — CSRF é uma proteção pensada para autenticação **baseada em cookie/sessão**. Como aqui é **stateless com token JWT no header**, não faz sentido e é desabilitado (o token não é enviado automaticamente pelo browser como um cookie seria, então não há o mesmo risco).
- `.cors(Customizer.withDefaults())` — habilita CORS usando a configuração default do Spring (que por sua vez lê o `WebMvcConfigurer`/propriedades de CORS do projeto, aqui ligado a `app.cors.allowed-origin`, o endereço do frontend).
- `.sessionManagement(STATELESS)` — **o servidor não guarda sessão nenhuma**. Cada requisição precisa vir com o token; nada de `HttpSession`. É isso que torna a API escalável horizontalmente sem "sticky sessions".
- `.authorizeHttpRequests(...)`:
  - `/api/v1/auth/**` → **público** (`permitAll()`) — registro, login, forgot/reset password não exigem token (faz sentido, é exatamente onde você consegue o token).
  - `anyRequest().authenticated()` — **todo o resto exige estar autenticado** (ex: `PatientController`). Não há aqui diferenciação por `role` (não tem `hasRole("ADMIN")` nesse arquivo) — a autorização hoje é binária: autenticado ou não. Se no futuro quiserem restringir por papel (ex.: só `ADMIN` pode deletar paciente), normalmente se usaria `@PreAuthorize("hasRole('ADMIN')")` no método do controller/service (habilitando `@EnableMethodSecurity`), ou mais matchers específicos aqui em `authorizeHttpRequests`.
- `.exceptionHandling(...)` — troca o comportamento default do Spring Security por handlers customizados que devolvem um JSON de erro (`ApiError`) em vez da página HTML padrão:
  - `authenticationEntryPoint` → chamado quando a requisição **não está autenticada** e a rota exige (401).
  - `accessDeniedHandler` → chamado quando **está autenticado mas não tem permissão** (403) — hoje não é muito exercitado porque não há checagem de role.
- `.authenticationProvider(authenticationProvider())` — registra o provider BCrypt+banco.
- `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` — **insere o filtro JWT antes do filtro padrão de login por formulário** na cadeia de filtros do Spring Security. É isso que garante que, antes de qualquer decisão de autorização, o token já foi lido e o usuário (se válido) já está no `SecurityContext`.

---

## 8. `User.java` — o modelo que também é `UserDetails`

```java
public class User extends AbstractModel implements UserDetails {
```
O `User` (entidade JPA, mapeada para a tabela `users`) implementa diretamente a interface `UserDetails` do Spring Security. Isso evita ter uma classe `UserPrincipal` separada — só funciona bem em projetos simples como este.

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
}
```
Transforma o `Role` (enum `ADMIN`/`USER`) na authority que o Spring Security entende, com o prefixo `ROLE_` (convenção do framework — é o que permite usar `hasRole("ADMIN")` em vez de `hasAuthority("ROLE_ADMIN")`).

```java
@Override
public String getUsername() {
    return email;
}
```
O "username" do Spring Security, neste projeto, **é o e-mail**.

```java
@Override
public boolean isAccountNonExpired() { return true; }
@Override
public boolean isAccountNonLocked() { return true; }
@Override
public boolean isCredentialsNonExpired() { return true; }
@Override
public boolean isEnabled() { return enabled; }
```
Flags padrão de conta exigidas pela interface. Só `enabled` é realmente controlado pelo banco (`enabled` column); os outros três estão fixos em `true` — ou seja, hoje não há suporte a "conta bloqueada" ou "credenciais expiradas", só "conta ativa/inativa".

---

## 9. `JwtAuthenticationEntryPoint` e `JwtAccessDeniedHandler`

Ambos seguem o mesmo padrão: constroem um `ApiError` (timestamp, status, mensagem, path) e escrevem como JSON na resposta.

- `JwtAuthenticationEntryPoint.commence(...)` → disparado quando falta autenticação → **401**.
- `JwtAccessDeniedHandler.handle(...)` → disparado quando há autenticação mas falta permissão → **403**.

Isso garante que erros de segurança tenham o **mesmo formato JSON** que os outros erros da API (em vez da página de erro HTML default do Spring Security/Tomcat).

---

## 10. Resumo dos cenários

### Criar conta (Sign up)
1. `POST /api/v1/auth/register` com `{name, email, password}` — validado por Bean Validation.
2. `AuthService.register` checa duplicidade de e-mail, faz hash BCrypt da senha, salva `role=USER`, `enabled=true`.
3. Gera JWT e devolve `{token, tokenType: "Bearer", role: "USER"}` — usuário já sai logado.

### Login
1. `POST /api/v1/auth/login` com `{email, password}`.
2. `AuthenticationManager` (via `DaoAuthenticationProvider`) busca o usuário e compara hash BCrypt.
3. Se válido, gera novo JWT e devolve `{token, tokenType, role}`.
4. Se inválido, `401` com mensagem genérica (não revela se foi e-mail ou senha).

### Acesso a rota protegida (ex.: `PatientController`)
1. Cliente manda `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter` valida o token, carrega o `User` do banco, popula `SecurityContext`.
3. `SecurityConfig` exige `authenticated()` para qualquer rota fora de `/api/v1/auth/**` → se autenticado, deixa passar; senão, `401` via `JwtAuthenticationEntryPoint`.
4. (Autorização por *role* específica não está implementada ainda — hoje é tudo ou nada: autenticado ou não.)

### Esqueci a senha
1. `POST /forgot-password` com o e-mail → gera token aleatório, salva só o hash SHA-256 dele + expiração de 30 min, loga o link (não envia e-mail de fato ainda).
2. `POST /reset-password` com `{token, newPassword}` → valida hash + expiração, troca a senha (novo hash BCrypt), invalida o token.

---

## 11. Monolito vs. Modularizado — e onde este projeto se encaixa

### Monolito (o que este backend é hoje)

Um **monolito** é uma aplicação empacotada e implantada como **uma única unidade** — um único processo, um único artefato (aqui, `backend-0.0.1-SNAPSHOT.jar`), um único banco de dados, um único deploy.

Estrutura real deste projeto:
```
com.nuvexa
 ├── controller/     ← todos os controllers (Auth, Patient, ...)
 ├── service/        ← toda a lógica de negócio
 ├── repository/     ← acesso a dados (JPA)
 ├── model/          ← entidades JPA
 ├── dto/            ← objetos de request/response
 ├── security/       ← JWT, filtros, config de segurança
 ├── mapper/
 ├── exception/
 └── configuration/
```
Isso é o padrão clássico de **"pacote por camada" (package-by-layer)** dentro de um monolito: os pacotes separam por **tipo técnico** (controller, service, repository), não por **domínio de negócio**. Ou seja, `AuthController` e `PatientController` moram lado a lado no mesmo pacote `controller`, `AuthService` e qualquer `PatientService` moram lado a lado em `service`, etc.

Características do monolito:
- ✅ Simples de rodar, testar e debugar (tudo num processo só, `mvn spring-boot:run`).
- ✅ Transações fáceis (um `@Transactional` cobre tudo, mesmo banco).
- ✅ Deploy único, sem precisar orquestrar múltiplos serviços.
- ❌ Tudo escala junto (não dá pra escalar só o módulo de "pacientes" sem escalar o de "auth" também).
- ❌ Conforme cresce, o acoplamento entre "camadas técnicas" pode virar acoplamento entre "domínios de negócio" sem querer (ex: `PatientService` podendo depender de detalhes internos de `AuthService`), porque não há fronteira explícita de módulo.
- ❌ Times grandes tendem a pisar no calo uns dos outros no mesmo código-base.

### Modularizado (dois sentidos possíveis)

O termo "modularizado" pode significar duas coisas bem diferentes — vale distinguir:

**(a) Monolito modular** (ainda um processo só, mas organizado por domínio)
Em vez de agrupar por camada técnica, agrupa-se por **domínio/feature**, e cada módulo tem sua própria fatia de controller/service/repository:
```
com.nuvexa
 ├── auth/
 │    ├── AuthController.java
 │    ├── AuthService.java
 │    ├── JwtService.java
 │    ├── SecurityConfig.java
 │    └── UserRepository.java
 ├── patient/
 │    ├── PatientController.java
 │    ├── PatientService.java
 │    └── PatientRepository.java
 └── shared/  (ex: exception, dto comuns)
```
Isso ainda é **um único deploy/processo** (continua "monolito" em termos de infraestrutura), mas o código fica organizado por **fronteiras de domínio**, o que:
- Deixa claro o que pertence a "auth" vs "patient".
- Facilita, no futuro, extrair um módulo inteiro para um microsserviço separado, se necessário — porque as dependências já estão isoladas.
- É o passo intermediário mais comum e recomendado antes de partir para microsserviços.

**(b) Microsserviços** (múltiplos processos/deploys independentes)
Cada módulo vira um **serviço separado**, com seu próprio processo, seu próprio banco (idealmente), seu próprio deploy, comunicando-se via rede (REST, mensageria, etc.):
```
auth-service  (seu próprio jar, seu próprio banco de usuários, seu próprio deploy)
patient-service (idem, chama auth-service via HTTP/JWT para validar token)
```
- ✅ Escala e faz deploy de cada serviço independentemente.
- ✅ Times diferentes podem trabalhar sem conflito de código.
- ❌ Muito mais complexidade operacional: rede, latência, consistência eventual, observabilidade distribuída, versionamento de contrato entre serviços, etc.
- ❌ Autenticação passa a precisar de um serviço central (ou um Auth Server tipo OAuth2/OpenID Connect) que os outros serviços confiam — geralmente validando o mesmo JWT (só a assinatura, sem precisar chamar o auth-service a cada request), o que é inclusive uma das vantagens do JWT ser *stateless* como já está implementado aqui.

### Onde este projeto está agora

Hoje o `nuvexa/backend` é um **monolito organizado por camada técnica** (não por domínio ainda) — só existem dois "domínios" reais (`auth` e `patient`), então a dor de organização ainda é pequena. Se o projeto crescer (mais entidades, mais controllers), migrar para "monolito modular por domínio" (opção *a* acima) tende a compensar bem antes de cogitar microsserviços — a base de segurança já implementada (JWT stateless, sem sessão em memória) é justamente o tipo de decisão que **facilita** uma futura extração para múltiplos serviços, caso um dia seja necessário.
