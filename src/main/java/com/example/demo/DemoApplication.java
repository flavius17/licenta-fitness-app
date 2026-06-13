package com.example.demo;

import com.example.demo.model.Exercitiu;
import com.example.demo.repository.ExercitiuRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(ExercitiuRepository repository) {
        return args -> {
            long count = repository.count();
            
            if (count == 0) {
                System.out.println("⏳ Baza de date e goală. Se încarcă exercițiile din JSON...");
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    InputStream inputStream = TypeReference.class.getResourceAsStream("/exercises.json");
                    
                    if (inputStream != null) {
                        List<Exercitiu> exercitiiJson = mapper.readValue(inputStream, new TypeReference<List<Exercitiu>>(){});
                        
                        repository.saveAll(exercitiiJson);
                        System.out.println("✅ SUCCES: S-au salvat " + exercitiiJson.size() + " exerciții în baza de date!");
                    } else {
                        System.out.println("❌ EROARE CRITICĂ: Nu am găsit fișierul exercises.json!");
                    }
                } catch (Exception e) {
                    System.out.println("❌ EROARE la citirea JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("ℹ️ Baza de date are deja " + count + " exerciții. Totul e OK.");
            }
        };
    }
}