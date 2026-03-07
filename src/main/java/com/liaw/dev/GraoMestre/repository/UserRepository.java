package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
