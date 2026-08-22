package com.nuvexa.nucleo.identidade.repository;

import com.nuvexa.nucleo.identidade.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByResetTokenHash(String resetTokenHash);
}
