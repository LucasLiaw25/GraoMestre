package com.liaw.dev.GraoMestre.repository;

import com.liaw.dev.GraoMestre.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
