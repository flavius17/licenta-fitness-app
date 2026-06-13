package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String numeUtilizator) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adresa.ta@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Bine ai venit în 21 GYM!");
        
        String textBody = "Salutare " + numeUtilizator + ",\n\n" +
                          "Bine ai venit în 21 GYM! Ne bucurăm să te avem alături.\n" +
                          "Pregătește-te să îți distrugi recordurile și să îți atingi obiectivele!\n\n" +
                          "Echipa 21 GYM";
                          
        message.setText(textBody);
        
        try {
            mailSender.send(message);
            System.out.println("E-mail trimis cu succes către: " + toEmail);
        } catch (Exception e) {
            System.out.println("Eroare la trimiterea e-mailului: " + e.getMessage());
        }
    }
}