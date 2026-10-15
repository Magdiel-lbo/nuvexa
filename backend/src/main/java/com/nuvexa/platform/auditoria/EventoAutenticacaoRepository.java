package com.nuvexa.platform.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EventoAutenticacaoRepository extends JpaRepository<EventoAutenticacao, Long> {

    long countByTipoEventoAndEmailInformadoAndCriadoEmAfter(TipoEventoAutenticacao tipoEvento, String emailInformado, LocalDateTime desde);

    long countByTipoEventoAndIpAndCriadoEmAfter(TipoEventoAutenticacao tipoEvento, String ip, LocalDateTime desde);
}
