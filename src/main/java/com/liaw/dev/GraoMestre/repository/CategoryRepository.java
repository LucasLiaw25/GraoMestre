package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
