package com.nuvexa.core.repository;

import com.nuvexa.core.model.Organizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    @Query("""
            select o from Organizacao o
            where :busca is null
               or lower(o.nome) like lower(concat('%', cast(:busca as string), '%'))
               or lower(o.razaoSocial) like lower(concat('%', cast(:busca as string), '%'))
            order by o.nome asc
            """)
    List<Organizacao> buscar(@Param("busca") String busca);
}
