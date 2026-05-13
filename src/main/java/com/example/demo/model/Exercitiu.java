package com.example.demo.model;

import jakarta.persistence.*; // <--- IMPORT OBLIGATORIU PENTRU BAZA DE DATE

@Entity // <--- Spune-i lui Java ca asta e o tabela
@Table(name = "exercitii") 
public class Exercitiu {

    @Id // <--- Cheia primara
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <--- Se autogenereaza (1, 2, 3...)
    private Long id;

    private String nume;
    private String imagine; 
    private String grupaMare;      
    private String muschiSpecific; 

    // 1. CONSTRUCTOR GOL
    public Exercitiu() {
    }

    // 2. Constructorul tau vechi
    public Exercitiu(String nume, String imagine, String grupaMare, String muschiSpecific) {
        this.nume = nume;
        this.imagine = imagine;
        this.grupaMare = grupaMare;
        this.muschiSpecific = muschiSpecific;
    }

    // --- GETTERS ---
    
    // AICI ERA PROBLEMA: Lipsea acest Getter!
    public Long getId() { 
        return id; 
    }

    public String getNume() { return nume; }
    public String getImagine() { return imagine; }
    public String getGrupaMare() { return grupaMare; }
    public String getMuschiSpecific() { return muschiSpecific; }

    // --- SETTERS ---
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public void setNume(String nume) { this.nume = nume; }
    public void setImagine(String imagine) { this.imagine = imagine; }
    public void setGrupaMare(String grupaMare) { this.grupaMare = grupaMare; }
    public void setMuschiSpecific(String muschiSpecific) { this.muschiSpecific = muschiSpecific; }
}