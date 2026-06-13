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
            String url = "https://dummyjson.com/quotes/random";
            String response = restTemplate.getForObject(url, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String quote = root.path("quote").asText();
            String author = root.path("author").asText();
            
            return "\"" + quote + "\" - " + author;
            
        } catch (Exception e) {
            return "\"Fiecare zi este o nouă șansă să fii mai bun.\" - 21 GYM"; 
        }
    }
}