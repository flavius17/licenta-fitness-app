package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuoteService {

    public String getRandomMotivationalQuote() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Folosim un API public și rapid pentru citate
            String url = "https://dummyjson.com/quotes/random";
            String response = restTemplate.getForObject(url, String.class);
            
            // Extragem textul citatului și autorul din JSON-ul primit
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String quote = root.path("quote").asText();
            String author = root.path("author").asText();
            
            return "\"" + quote + "\" - " + author;
            
        } catch (Exception e) {
            // Dacă pică internetul sau API-ul, avem un citat de rezervă ca să nu crape pagina
            return "\"Fiecare zi este o nouă șansă să fii mai bun.\" - 21 GYM"; 
        }
    }
}