package com.example.demo.repository;

import com.example.demo.model.Exercitiu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExercitiuRepository extends JpaRepository<Exercitiu, Long> {
    Exercitiu findByNume(String nume);
}