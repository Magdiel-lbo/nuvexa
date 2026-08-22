package com.nuvexa.core.identidade.repository;

import com.nuvexa.core.identidade.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByHashTokenRedefinicao(String hashTokenRedefinicao);
}
