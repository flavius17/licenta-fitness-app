package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double greutate;
    private int repetari;
    
    private LocalDateTime data;

    @ManyToOne 
    @JoinColumn(name = "exercitiu_id", nullable = false)
    private Exercitiu exercitiu;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public WorkoutSet() {}

    public double getGreutate() { return greutate; }
    public void setGreutate(double greutate) { this.greutate = greutate; }

    public int getRepetari() { return repetari; }
    public void setRepetari(int repetari) { this.repetari = repetari; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    
    public Exercitiu getExercitiu() { return exercitiu; }
    public void setExercitiu(Exercitiu exercitiu) { this.exercitiu = exercitiu; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}