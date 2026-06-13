package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_templates")
public class PlanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;

    @Column(columnDefinition = "LONGTEXT")
    private String continutHtml;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public PlanTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getContinutHtml() { return continutHtml; }
    public void setContinutHtml(String continutHtml) { this.continutHtml = continutHtml; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}