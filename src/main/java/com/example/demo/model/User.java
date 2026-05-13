package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // STANDARD: Tabelul se va numi 'users'
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;
    private String prenume;
    private String email;
    
    // STANDARD: Spring Security caută 'password', așa că îi dăm 'password'
    private String password; 

    // Date Profil
    private Double greutate;
    private Integer inaltime;
    private Integer varsta;

    public User() {}

    public User(String nume, String prenume, String email, String password) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.password = password;
    }

    // --- DATE NOI PENTRU NUTRITIE ---
    private Integer tintaCalorii;     // ex: 2500
    private String obiectivNutritie;  // ex: "SLĂBIRE"
    private String strategieMacro;    // ex: "HIGH PROTEIN"
    
    private Integer tintaProteine;
    private Integer tintaCarbohidrati;
    private Integer tintaGrasimi;

    // --- GETTERS ȘI SETTERS (Adaugă-i la finalul fișierului!) ---
    public Integer getTintaCalorii() { return tintaCalorii; }
    public void setTintaCalorii(Integer tintaCalorii) { this.tintaCalorii = tintaCalorii; }

    public String getObiectivNutritie() { return obiectivNutritie; }
    public void setObiectivNutritie(String obiectivNutritie) { this.obiectivNutritie = obiectivNutritie; }

    public String getStrategieMacro() { return strategieMacro; }
    public void setStrategieMacro(String strategieMacro) { this.strategieMacro = strategieMacro; }

    public Integer getTintaProteine() { return tintaProteine; }
    public void setTintaProteine(Integer tintaProteine) { this.tintaProteine = tintaProteine; }

    public Integer getTintaCarbohidrati() { return tintaCarbohidrati; }
    public void setTintaCarbohidrati(Integer tintaCarbohidrati) { this.tintaCarbohidrati = tintaCarbohidrati; }

    public Integer getTintaGrasimi() { return tintaGrasimi; }
    public void setTintaGrasimi(Integer tintaGrasimi) { this.tintaGrasimi = tintaGrasimi; }

    // --- GETTERS și SETTERS (Standard) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // AICI E SCHIMBAREA CHEIE: getPassword / setPassword
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Date Profil
    public Double getGreutate() { return greutate; }
    public void setGreutate(Double greutate) { this.greutate = greutate; }

    public Integer getInaltime() { return inaltime; }
    public void setInaltime(Integer inaltime) { this.inaltime = inaltime; }

    public Integer getVarsta() { return varsta; }
    public void setVarsta(Integer varsta) { this.varsta = varsta; }

    private String role;

    // --- ADAUGĂ GETTER ȘI SETTER ---
    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }
}