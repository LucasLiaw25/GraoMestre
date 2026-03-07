package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScopeRepository extends JpaRepository<Scope, Long> {
    Optional<Scope> findByName(String name);
}
