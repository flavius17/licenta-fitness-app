package com.example.demo.repository;

import com.example.demo.model.WorkoutSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    @Query("SELECT MAX(w.greutate) FROM WorkoutSet w WHERE w.exercitiu.id = :exercitiuId AND w.user.id = :userId AND w.data <= :azi")
    Double findMaxWeightHistory(@Param("exercitiuId") Long exercitiuId, 
                                @Param("userId") Long userId,
                                @Param("azi") LocalDateTime azi);

    @Query("SELECT MAX(w.repetari) FROM WorkoutSet w WHERE w.exercitiu.id = :exercitiuId AND w.user.id = :userId AND w.greutate = :greutate AND w.data <= :azi")
    Integer findMaxRepsHistory(@Param("exercitiuId") Long exercitiuId, 
                               @Param("userId") Long userId, 
                               @Param("greutate") Double greutate,
                               @Param("azi") LocalDateTime azi);
}