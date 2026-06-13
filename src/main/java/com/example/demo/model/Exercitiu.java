package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exercitii") 
public class Exercitiu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;
    private String imagine; 
    private String grupaMare;      
    private String muschiSpecific; 

    public Exercitiu() {
    }

    public Exercitiu(String nume, String imagine, String grupaMare, String muschiSpecific) {
        this.nume = nume;
        this.imagine = imagine;
        this.grupaMare = grupaMare;
        this.muschiSpecific = muschiSpecific;
    }

    public Long getId() { 
        return id; 
    }

    public String getNume() { return nume; }
    public String getImagine() { return imagine; }
    public String getGrupaMare() { return grupaMare; }
    public String getMuschiSpecific() { return muschiSpecific; }

    public void setId(Long id) { 
        this.id = id; 
    }

    public void setNume(String nume) { this.nume = nume; }
    public void setImagine(String imagine) { this.imagine = imagine; }
    public void setGrupaMare(String grupaMare) { this.grupaMare = grupaMare; }
    public void setMuschiSpecific(String muschiSpecific) { this.muschiSpecific = muschiSpecific; }
}