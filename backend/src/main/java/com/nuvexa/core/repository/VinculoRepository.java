package com.nuvexa.core.repository;

import com.nuvexa.core.model.Vinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VinculoRepository extends JpaRepository<Vinculo, Long> {

    List<Vinculo> findByUsuarioIdAndAtivoTrueOrderByIdAsc(Long usuarioId);

    List<Vinculo> findByOrganizacaoIdAndAtivoTrueOrderByUsuario_NomeAsc(Long organizacaoId);
}
