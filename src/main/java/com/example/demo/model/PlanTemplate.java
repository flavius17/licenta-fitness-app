package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_templates")
public class PlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;

    // Folosim LONGTEXT pentru că planul generat de AI e lung și conține cod HTML
    @Column(columnDefinition = "LONGTEXT")
    private String continutHtml;

    // Constructori, Getteri și Setteri
    public PlanTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getContinutHtml() { return continutHtml; }
    public void setContinutHtml(String continutHtml) { this.continutHtml = continutHtml; }
}