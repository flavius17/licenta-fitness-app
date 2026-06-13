package com.example.demo.repository;

import com.example.demo.model.PlanTemplate;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, Long> {
    List<PlanTemplate> findByUser(User user);
}