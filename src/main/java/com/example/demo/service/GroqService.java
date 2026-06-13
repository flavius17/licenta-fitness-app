package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class GroqService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    public String genereazaPlan(String profilUtilizator, String listaExercitiiJson) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = "Ești un antrenor personal expert care se bazeaza strict cum sa alcatuiasca un plan de antrenament pe science-based lifters. Profilul clientului: " + profilUtilizator +
                ". Alege exerciții potrivite STRICT din această listă: " + listaExercitiiJson + ". " +
                "\nINSTRUCȚIUNI LOGICE PENTRU TINE: " +
                "1. Împarte logic grupele musculare în funcție de numărul de zile cerute în profil (ex: 2 zile = Full Body, 3 zile = Push/Pull/Legs, 4 zile = Upper/Lower etc). " +
                "2. Stabilește numărul de serii și repetări în funcție de obiectivul clientului (ex: 6-12 rep pentru Masă, 1-5 rep pentru Forță, 15-25 pentru Slăbire). " +
                "\nREGULĂ STRICTĂ DE FORMATARE: Trebuie să returnezi DOAR cod HTML pur. Fără markdown (```html). " +
                "Folosește EXACT și DOAR acest șablon pentru fiecare zi generată:\n\n" +
                "<h3>Ziua [X]: [Cum ai împărțit ziua - ex: Full Body / Push / Upper / Picioare]</h3>\n" +
                "<ul>\n" +
                "<li><strong>[Nume Exercițiu 1]</strong> - [Număr serii] serii x [Număr repetări] repetări</li>\n" +
                "<li><strong>[Nume Exercițiu 2]</strong> - [Număr serii] serii x [Număr repetări] repetări</li>\n" +
                "</ul>\n\n" +
                "Repetă acest bloc HTML pentru fiecare zi cerută. Nu adăuga texte explicative la început sau final, ci DOAR acest cod HTML strict!" +
                "REGULĂ STRICTĂ: Alege exercițiile DOAR din fișierul JSON furnizat. Trebuie să folosești EXACT numele exercițiului așa cum apare în JSON (fără să adaugi, să ștergi sau să modifici nicio literă). De exemplu, dacă în JSON scrie 'squat', nu scrie 'Barbell squat' sau 'Squats'. Baza de date a aplicației se va bloca dacă modifici numele!";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

            List choices = (List) response.get("choices");
            Map firstChoice = (Map) choices.get(0);
            Map messageObj = (Map) firstChoice.get("message");
            return (String) messageObj.get("content");

        } catch (Exception e) {
            System.out.println("Eroare API Groq: " + e.getMessage());
            return "<div class='text-center mt-5' style='color: white;'>" +
                   "<h3 style='color:#ff3333; font-family: \"Black Ops One\";'>Eroare de conexiune</h3>" +
                   "<p>" + e.getMessage() + "</p></div>";
        }
    }
}
