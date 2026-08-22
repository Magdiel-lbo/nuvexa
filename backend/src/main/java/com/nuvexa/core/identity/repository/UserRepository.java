package com.nuvexa.core.identity.repository;

import com.nuvexa.core.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByResetTokenHash(String resetTokenHash);
}
