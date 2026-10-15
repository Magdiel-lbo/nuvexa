package com.nuvexa.core.repository;

import com.nuvexa.core.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Optional<Sessao> findByHashRefreshToken(String hashRefreshToken);

    @Query("""
            select count(s) > 0 from Sessao s
            where s.id = :id and s.usuario.email = :email
              and s.revogadaEm is null and s.rotacionadaEm is null and s.expiraEm > :agora
            """)
    boolean existeAtiva(@Param("id") Long id, @Param("email") String email, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true)
    @Query("update Sessao s set s.revogadaEm = :agora where s.familiaId = :familiaId and s.revogadaEm is null")
    int revogarFamilia(@Param("familiaId") String familiaId, @Param("agora") LocalDateTime agora);

    @Modifying(clearAutomatically = true)
    @Query("update Sessao s set s.revogadaEm = :agora where s.usuario.id = :usuarioId and s.revogadaEm is null")
    int revogarTodasDoUsuario(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);
}
