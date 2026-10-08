package com.nuvexa.core.service;

import com.nuvexa.core.model.Paciente;
import com.nuvexa.core.model.Usuario;
import com.nuvexa.core.model.Vinculo;
import com.nuvexa.core.repository.PacienteRepository;
import com.nuvexa.core.repository.VinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Único ponto de decisão de "isso pertence à organização atual?" / "este usuário pode agir na
 * organização atual?" para os services de negócio.
 *
 * <p>Regra de produto confirmada: todo usuário com {@link Vinculo} ATIVO numa organização pode
 * acessar todos os pacientes daquela organização — o escopo de autorização é a organização
 * inteira, não uma carteira de pacientes por profissional. {@link com.nuvexa.core.model.PacienteProfissional}
 * existe como conceito de domínio (quem é o profissional responsável por um paciente), mas
 * deliberadamente NÃO é usado aqui nem em nenhum outro service como filtro de acesso — não
 * restringe leitura/escrita de Consulta/Prontuário/Avaliação/PlanoAlimentar/PerfilNutricional ao
 * profissional vinculado. Isolamento entre organizações continua obrigatório em toda query
 * (ver {@link #pacienteDaOrganizacao} e o padrão QueryDSL replicado nos services).
 */
@Component
@RequiredArgsConstructor
public class ValidadorOrganizacional {

    private final PacienteRepository pacienteRepository;
    private final VinculoRepository vinculoRepository;

    public Optional<Paciente> pacienteDaOrganizacao(Long pacienteId, Long organizacaoId) {
        return pacienteRepository.findByIdAndOrganizacaoId(pacienteId, organizacaoId);
    }

    public Optional<Usuario> usuarioAtivoNaOrganizacao(Long usuarioId, Long organizacaoId) {
        return vinculoRepository.findByUsuarioIdAndAtivoTrueOrderByIdAsc(usuarioId).stream()
                .filter(vinculo -> vinculo.getOrganizacao().getId().equals(organizacaoId))
                .map(Vinculo::getUsuario)
                .findFirst();
    }
}
