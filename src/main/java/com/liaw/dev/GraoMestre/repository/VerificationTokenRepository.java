package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.User;
import com.liaw.dev.GraoMestre.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(User user);
    Optional<VerificationToken> findByUser(User user);
}
