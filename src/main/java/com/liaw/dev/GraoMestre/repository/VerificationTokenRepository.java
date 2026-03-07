package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
}
