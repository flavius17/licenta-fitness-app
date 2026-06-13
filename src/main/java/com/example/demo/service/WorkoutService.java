package com.example.demo.service;

import com.example.demo.model.WorkoutSet;
import com.example.demo.model.User;
import com.example.demo.repository.WorkoutSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutSetRepository workoutSetRepository;

    public int calculeazaPersonalRecords(List<WorkoutSet> seturiNoi, User user) {
        int prCounter = 0;
        
        LocalDateTime azi = LocalDateTime.now(ZoneId.of("Europe/Bucharest"));

        Map<Long, Double> maxWeightSessionMap = new HashMap<>();
        Map<Long, Integer> maxRepsSessionMap = new HashMap<>();

        for (WorkoutSet setNou : seturiNoi) {
            Long exercitiuId = setNou.getExercitiu().getId();
            if (exercitiuId == null) continue; 

            Double greutateNoua = setNou.getGreutate();
            Integer repetariNoi = setNou.getRepetari();

            Double maxWeightIstoric = workoutSetRepository.findMaxWeightHistory(exercitiuId, user.getId(), azi);
            if (maxWeightIstoric == null) maxWeightIstoric = 0.0;

            Double maxWeightSesiune = maxWeightSessionMap.getOrDefault(exercitiuId, 0.0);

            Double referintaGreutate = Math.max(maxWeightIstoric, maxWeightSesiune);

            boolean isPr = false;

            if (greutateNoua > referintaGreutate) {
                prCounter++;
                isPr = true;
            } 
            else if (greutateNoua.equals(referintaGreutate)) {
                Integer maxRepsIstoric = workoutSetRepository.findMaxRepsHistory(exercitiuId, user.getId(), greutateNoua, azi);
                if (maxRepsIstoric == null) maxRepsIstoric = 0;

                Integer maxRepsSesiune = maxRepsSessionMap.getOrDefault(exercitiuId, 0);

                Integer referintaReps = Math.max(maxRepsIstoric, maxRepsSesiune);

                if (repetariNoi > referintaReps) {
                    prCounter++;
                    isPr = true;
                }
            }

            if (greutateNoua > maxWeightSesiune) {
                maxWeightSessionMap.put(exercitiuId, greutateNoua);
                maxRepsSessionMap.put(exercitiuId, repetariNoi);
            } else if (greutateNoua.equals(maxWeightSesiune)) {
                Integer currentMaxReps = maxRepsSessionMap.getOrDefault(exercitiuId, 0);
                if (repetariNoi > currentMaxReps) {
                    maxRepsSessionMap.put(exercitiuId, repetariNoi);
                }
            }
        }

        return prCounter;
    }
}