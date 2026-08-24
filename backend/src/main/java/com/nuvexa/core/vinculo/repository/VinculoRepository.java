package com.nuvexa.core.vinculo.repository;

import com.nuvexa.core.vinculo.model.Vinculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VinculoRepository extends JpaRepository<Vinculo, Long> {

    List<Vinculo> findByUsuarioIdAndAtivoTrueOrderByIdAsc(Long usuarioId);
}
