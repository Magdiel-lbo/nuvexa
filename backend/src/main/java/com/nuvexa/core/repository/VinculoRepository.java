package com.nuvexa.core.repository;

import com.nuvexa.core.model.Vinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinculoRepository extends JpaRepository<Vinculo, Long> {

    List<Vinculo> findByUsuarioIdAndAtivoTrueOrderByIdAsc(Long usuarioId);

    @org.springframework.data.jpa.repository.Query("""
            select distinct v.organizacao.id from Vinculo v
            where v.papel = com.nuvexa.core.model.PapelOrganizacional.PROPRIETARIO and v.ativo = true
            """)
    java.util.Set<Long> idsDeOrganizacoesComProprietarioAtivo();

    Optional<Vinculo> findByUsuarioIdAndOrganizacaoId(Long usuarioId, Long organizacaoId);

    Optional<Vinculo> findByIdAndOrganizacaoId(Long id, Long organizacaoId);

    List<Vinculo> findByOrganizacaoIdOrderByAtivoDescUsuarioNomeAsc(Long organizacaoId);

    List<Vinculo> findByOrganizacaoIdAndAtivoTrueOrderByUsuario_NomeAsc(Long organizacaoId);
}
