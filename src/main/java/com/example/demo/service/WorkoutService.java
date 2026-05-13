package com.example.demo.service;

import com.example.demo.model.WorkoutSet;
import com.example.demo.model.User;
import com.example.demo.repository.WorkoutSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // <--- MODIFICAT AICI
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutSetRepository workoutSetRepository;

    public int calculeazaPersonalRecords(List<WorkoutSet> seturiNoi, User user) {
        int prCounter = 0;
        
        // <--- MODIFICAT AICI: Folosim LocalDateTime ca să se potrivească perfect cu noua bază de date!
        LocalDateTime azi = LocalDateTime.now(); 

        // MEMORIA SESIUNII CURENTE
        // Ținem minte cea mai mare greutate ridicată AZI pentru fiecare exercițiu (ID Exercițiu -> Greutate)
        Map<Long, Double> maxWeightSessionMap = new HashMap<>();
        // Ținem minte și repetările pentru greutatea aia maximă
        Map<Long, Integer> maxRepsSessionMap = new HashMap<>();

        for (WorkoutSet setNou : seturiNoi) {
            Long exercitiuId = setNou.getExercitiu().getId();
            // Dacă exercițiul nu are ID (e nou creat sau eroare), sărim peste el sau îl tratăm ca nou
            if (exercitiuId == null) continue; 

            Double greutateNoua = setNou.getGreutate();
            Integer repetariNoi = setNou.getRepetari();

            // 1. Aflăm Recordul ISTORIC (din Baza de Date, înainte de azi)
            Double maxWeightIstoric = workoutSetRepository.findMaxWeightHistory(exercitiuId, user.getId(), azi);
            if (maxWeightIstoric == null) maxWeightIstoric = 0.0;

            // 2. Aflăm Recordul SESIUNII (ce am făcut deja azi în seturile anterioare)
            Double maxWeightSesiune = maxWeightSessionMap.getOrDefault(exercitiuId, 0.0);

            // 3. Stabilim "ȘTACHETA" (Cea mai mare valoare dintre Istoric și Sesiune)
            // Ca să fie PR, trebuie să batem AMBELE valori!
            Double referintaGreutate = Math.max(maxWeightIstoric, maxWeightSesiune);

            boolean isPr = false;

            // CAZUL A: Greutate mai mare decât ORICE am făcut înainte (istoric sau azi)
            if (greutateNoua > referintaGreutate) {
                prCounter++;
                isPr = true;
            } 
            // CAZUL B: Greutate egală cu maximul, dar mai multe repetări
            else if (greutateNoua.equals(referintaGreutate)) {
                // Trebuie să vedem care era maximul de repetări pe greutatea asta
                // Luăm din istoric
                Integer maxRepsIstoric = workoutSetRepository.findMaxRepsHistory(exercitiuId, user.getId(), greutateNoua, azi);
                if (maxRepsIstoric == null) maxRepsIstoric = 0;

                // Luăm din sesiune
                Integer maxRepsSesiune = maxRepsSessionMap.getOrDefault(exercitiuId, 0);

                // Ștacheta la repetări
                Integer referintaReps = Math.max(maxRepsIstoric, maxRepsSesiune);

                if (repetariNoi > referintaReps) {
                    prCounter++;
                    isPr = true;
                }
            }

            // --- ACTUALIZĂM MEMORIA SESIUNII ---
            // Dacă setul curent este mai bun decât ce aveam în memorie pentru azi, actualizăm.
            // Astfel, seturile slabe următoare (ex: 5kg după 6kg) nu vor mai fi considerate PR.
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