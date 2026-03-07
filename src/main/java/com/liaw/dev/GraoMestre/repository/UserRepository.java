package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
