package com.example.demo.controller;

import com.example.demo.model.Exercitiu;
import com.example.demo.model.PlanTemplate;
import com.example.demo.model.User;
import com.example.demo.model.WorkoutSet;
import com.example.demo.repository.ExercitiuRepository;
import com.example.demo.repository.PlanTemplateRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkoutSetRepository;
import com.example.demo.service.WorkoutService;
import com.example.demo.service.EmailService;
import com.example.demo.service.GroqService;
import com.example.demo.service.QuoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.MacroPlan;
import com.example.demo.model.PlanTemplate;
import com.example.demo.repository.PlanTemplateRepository;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private WorkoutSetRepository workoutSetRepository;

    @Autowired
    private ExercitiuRepository exercitiuRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GroqService groqService;

    @Autowired
    private PlanTemplateRepository templateRepository;

    @Autowired 
    private EmailService emailService;

    @Autowired 
    private QuoteService quoteService;

    // --- METODĂ HELPER PENTRU EMAIL ---
    private String getEmailFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken token) {
            return token.getPrincipal().getAttribute("email");
        }
        return principal.getName();
    }

    // --- PAGINA DE START ---
    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
    
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }

    // --- INREGISTRARE ---
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");
        
        // 1. Salvăm user-ul în baza de date
        userRepository.save(user);
        
        // 2. Trimitem mail-ul (AICI MODIFICI):
        // Am pus user.getEmail() de două ori ca să fim siguri că nu mai primești "null"
        emailService.sendWelcomeEmail(user.getEmail(), user.getEmail());
        
        return "redirect:/login";
    }

    // --- LOGARE ---
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            model.addAttribute("user", user);
        }
        return "dashboard";
    }
    
    // --- MEMBRI (ADMIN) ---
    @GetMapping("/members")
    public String showMembersPage(Model model) {
        List<User> useriiMei = userRepository.findAll();
        model.addAttribute("listaUseri", useriiMei);
        return "members";
    }

    // --- CALCULATOR TDEE ---
    @GetMapping("/tdee")
    public String showTdeePage() {
        return "tdee";
    }

    @PostMapping("/tdee")
    public String calculateTDEE(
            @RequestParam("gen") String gen,
            @RequestParam("varsta") int varsta,
            @RequestParam("greutate") double greutate,
            @RequestParam("inaltime") int inaltime,
            @RequestParam("activitate") double activitate,
            Model model) {

        double bmr;
        if (gen.equalsIgnoreCase("masculin")) {
            bmr = (10 * greutate) + (6.25 * inaltime) - (5 * varsta) + 5;
        } else {
            bmr = (10 * greutate) + (6.25 * inaltime) - (5 * varsta) - 161;
        }

        int tdee = (int) (bmr * activitate);
        int slabire = tdee - 500;
        int mentinere = tdee;
        int masa = tdee + 300;

        model.addAttribute("slabire", slabire);
        model.addAttribute("mentinere", mentinere);
        model.addAttribute("masa", masa);
        
        model.addAttribute("oldVarsta", varsta);
        model.addAttribute("oldGreutate", greutate);
        model.addAttribute("oldInaltime", inaltime);

        return "tdee";
    }

    @GetMapping("/macros")
    public String showMacros(@RequestParam("calorii") int calorii, 
                             @RequestParam("tip") String tip, 
                             Model model) {
        
        model.addAttribute("totalCalorii", calorii);
        model.addAttribute("tipObiectiv", tip);

        model.addAttribute("bal_prot", (int)(calorii * 0.25 / 4));
        model.addAttribute("bal_carb", (int)(calorii * 0.50 / 4));
        model.addAttribute("bal_gras", (int)(calorii * 0.25 / 9));

        model.addAttribute("low_prot", (int)(calorii * 0.35 / 4));
        model.addAttribute("low_carb", (int)(calorii * 0.20 / 4));
        model.addAttribute("low_gras", (int)(calorii * 0.45 / 9));

        model.addAttribute("high_prot", (int)(calorii * 0.35 / 4));
        model.addAttribute("high_carb", (int)(calorii * 0.40 / 4));
        model.addAttribute("high_gras", (int)(calorii * 0.25 / 9));

        return "macros"; 
    }

    // --- WORKOUTS ---
    @GetMapping("/workouts")
    public String showWorkoutsPage(Model model, Principal principal) {
        String citatMotivational = quoteService.getRandomMotivationalQuote();
        model.addAttribute("citat", citatMotivational);
        if (principal == null) return "redirect:/login";

        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            List<WorkoutSet> toateSeturile = workoutSetRepository.findAll().stream()
                    .filter(set -> set.getUser() != null && set.getUser().getId().equals(user.getId()))
                    .sorted((s1, s2) -> s2.getId().compareTo(s1.getId())) 
                    .toList();

            java.util.Map<java.time.LocalDateTime, java.util.Map<String, List<WorkoutSet>>> istoricDetaliat = new java.util.LinkedHashMap<>();
            
            for (WorkoutSet set : toateSeturile) {
                java.time.LocalDateTime dataTimp = set.getData();
                if (dataTimp == null) continue; 

                istoricDetaliat.putIfAbsent(dataTimp, new java.util.LinkedHashMap<>());
                
                String exName = (set.getExercitiu() != null && set.getExercitiu().getNume() != null) 
                                ? set.getExercitiu().getNume() : "Exercițiu Necunoscut";
                
                istoricDetaliat.get(dataTimp).putIfAbsent(exName, new java.util.ArrayList<>());
                istoricDetaliat.get(dataTimp).get(exName).add(set);
            }

            List<java.util.Map<String, Object>> istoricFinal = new java.util.ArrayList<>();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm");

            for (java.util.Map.Entry<java.time.LocalDateTime, java.util.Map<String, List<WorkoutSet>>> sessionEntry : istoricDetaliat.entrySet()) {
                java.util.Map<String, Object> ziNode = new java.util.HashMap<>();
                ziNode.put("data", sessionEntry.getKey().format(formatter)); 
                
                List<java.util.Map<String, Object>> exercitiiList = new java.util.ArrayList<>();
                for (java.util.Map.Entry<String, List<WorkoutSet>> exEntry : sessionEntry.getValue().entrySet()) {
                    java.util.Map<String, Object> exNode = new java.util.HashMap<>();
                    exNode.put("nume", exEntry.getKey());
                    
                    List<WorkoutSet> seturi = exEntry.getValue();
                    seturi.sort(java.util.Comparator.comparing(WorkoutSet::getId));
                    exNode.put("seturi", seturi);
                    
                    exercitiiList.add(exNode);
                }
                ziNode.put("exercitiiLucrate", exercitiiList);
                istoricFinal.add(ziNode);
            }
            model.addAttribute("istoric", istoricFinal);
        }
        return "workouts";
    }

    @PostMapping("/api/save-workout") 
    public String saveWorkout(@RequestBody List<WorkoutSet> seturi, Principal principal, Model model) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);

        double volumTotal = 0;
        int totalRepetari = 0;
        LocalDateTime timpulSesiunii = LocalDateTime.now(ZoneId.of("Europe/Bucharest")).withNano(0);

        for (WorkoutSet set : seturi) {
            set.setUser(user);
            set.setData(timpulSesiunii); 
            
            Exercitiu exercitiuReal = null;
            if (set.getExercitiu() != null && set.getExercitiu().getId() != null) {
                exercitiuReal = exercitiuRepository.findById(set.getExercitiu().getId()).orElse(null);
            }
            if (exercitiuReal == null && set.getExercitiu() != null && set.getExercitiu().getNume() != null) {
                exercitiuReal = exercitiuRepository.findByNume(set.getExercitiu().getNume());
            }
            if (exercitiuReal != null) {
                set.setExercitiu(exercitiuReal);
            } else {
                throw new RuntimeException("Exercițiul nu a fost găsit în baza de date!");
            }
            volumTotal += (set.getGreutate() * set.getRepetari());
            totalRepetari += set.getRepetari();
        }

        int prCount = workoutService.calculeazaPersonalRecords(seturi, user);
        workoutSetRepository.saveAll(seturi);

        model.addAttribute("totalVolum", (int) volumTotal);
        model.addAttribute("totalReps", totalRepetari);
        model.addAttribute("personalRecords", prCount); 

        return "workout-stats";
    }

    @GetMapping("/log-workout")
    public String showLogWorkout() {
        return "log-workout";
    }

    @GetMapping("/create-template")
    public String showCreateTemplate(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        model.addAttribute("userEmail", getEmailFromPrincipal(principal));
        return "create-template";
    }

    @GetMapping("/add-exercise")
    public String showAddExerciseList(Model model) {
        List<Exercitiu> listaExercitii = exercitiuRepository.findAll();
        if (listaExercitii.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = TypeReference.class.getResourceAsStream("/exercises.json");
                if(inputStream != null) {
                    List<Exercitiu> exercitiiDinJson = mapper.readValue(inputStream, new TypeReference<List<Exercitiu>>(){});
                    listaExercitii = exercitiuRepository.saveAll(exercitiiDinJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("exercitiiAll", listaExercitii);
        return "add-exercise";
    }

    @GetMapping("/finish-workout")
    public String finishWorkout() {
        return "finish-workout";
    }

    @GetMapping("/workout-stats")
    public String workoutStats() {
        return "workout-stats";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = getEmailFromPrincipal(principal); 
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            String numeAfisat = (user.getNume() != null) ? user.getNume() : "";
            if (user.getPrenume() != null && !user.getPrenume().isEmpty()) {
                numeAfisat += " " + user.getPrenume();
            }
            if (numeAfisat.trim().isEmpty()) {
                numeAfisat = user.getEmail();
            }
            model.addAttribute("user", user); 
            model.addAttribute("numeUtilizator", numeAfisat); 
            model.addAttribute("email", user.getEmail());
            model.addAttribute("greutate", user.getGreutate());
            model.addAttribute("inaltime", user.getInaltime());
            model.addAttribute("varsta", user.getVarsta());
        }
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedData, 
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                Principal principal, Model model) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setNume(updatedData.getNume());
            user.setGreutate(updatedData.getGreutate());
            user.setInaltime(updatedData.getInaltime());
            user.setVarsta(updatedData.getVarsta());
            if (newPassword != null && !newPassword.isEmpty()) {
                if (newPassword.equals(confirmPassword)) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    return "redirect:/profile?error=passwords_dont_match";
                }
            }
            userRepository.save(user);
        }
        return "redirect:/profile?success=updated";
    }

    @PostMapping("/api/save-nutrition")
    @ResponseBody
    public String saveNutrition(@RequestBody com.example.demo.dto.MacroPlan plan, java.security.Principal principal) {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) {
            user.setTintaCalorii(plan.calorii);
            user.setObiectivNutritie(plan.obiectiv);
            user.setStrategieMacro(plan.strategie);
            user.setTintaProteine(plan.proteine);
            user.setTintaCarbohidrati(plan.carbohidrati);
            user.setTintaGrasimi(plan.grasimi);
            userRepository.save(user); 
            return "Plan salvat cu succes!";
        }
        return "Eroare: Utilizator negăsit.";
    }

    // --- METODE NOI PENTRU GENERATOR AI ---

    @GetMapping("/generator")
    public String showGeneratorPage() {
        return "generator";
    }

    @PostMapping("/generate-plan")
    public String handleAiGeneration(@RequestParam String nivel, 
                                     @RequestParam String scop, 
                                     @RequestParam int zile,
                                     @RequestParam(required = false, defaultValue = "") String detalii, // PRELUĂM TEXTUL LIBER
                                     Model model) {
        
        // 1. Creăm profilul de bază
        String profil = "Utilizator cu nivel " + nivel + ", având scopul de " + scop + 
                         " și dorind să se antreneze " + zile + " zile pe săptămână.";
        
        // 2. DACĂ a scris ceva la probleme medicale, adăugăm un ordin strict pentru AI
        if (!detalii.trim().isEmpty()) {
            profil += " FOARTE IMPORTANT: Utilizatorul are următoarele condiții medicale, probleme sau preferințe: '" + detalii + "'. " +
                      "TE ROG SĂ ȚII CONT STRICT DE ACESTE RESTRICȚII. Exclude orice exercițiu care i-ar putea agrava situația descrisă și alege alternative sigure.";
        }
        
        // 3. Extragem lista de nume de exerciții din baza de date
        List<String> numeExercitii = exercitiuRepository.findAll().stream()
                .map(Exercitiu::getNume)
                .toList();
        
        // 4. Apelăm serviciul AI
        String planGenerat = groqService.genereazaPlan(profil, numeExercitii.toString());
        
        // 5. Trimitem rezultatul către pagina de vizualizare
        model.addAttribute("planAI", planGenerat);
        return "view-plan";
    }

    @PostMapping("/save-template")
    public String saveTemplate(@RequestParam("planHtml") String planHtml,
                               @RequestParam("numeTemplate") String numeTemplate,
                               Principal principal) {

        System.out.println("--- AM PRIMIT CEREREA DE SALVARE ---");

        String email = getEmailFromPrincipal(principal);
        User currentUser = userRepository.findByEmail(email);

        try {
            String[] zile = planHtml.split("<h3>");
            System.out.println("Am tăiat textul în " + (zile.length - 1) + " bucăți.");

            for (int i = 1; i < zile.length; i++) {
                String continutZi = "<h3>" + zile[i];
                String titluZi = "Ziua " + i;

                try {
                    int endTag = continutZi.indexOf("</h3>");
                    if (endTag != -1) {
                        titluZi = continutZi.substring(4, endTag);
                    }
                } catch (Exception e) {
                    System.out.println("Nu am putut extrage titlul exact, folosesc default.");
                }

                PlanTemplate template = new PlanTemplate();
                template.setNume(numeTemplate + " - " + titluZi);
                template.setContinutHtml(continutZi);
                template.setUser(currentUser);

                System.out.println("Încerc să salvez în baza de date: " + titluZi);
                templateRepository.save(template);
                System.out.println("Salvat cu succes: " + titluZi);
            }

            System.out.println("--- SALVARE COMPLETĂ! REDIRECȚIONEZ... ---");
            return "redirect:/my-templates";

        } catch (Exception e) {
            System.out.println("!!! EROARE CRITICĂ LA SALVARE !!! " + e.getMessage());
            e.printStackTrace();
            return "redirect:/my-templates";
        }
    }

    @PostMapping("/api/save-manual-template")
    @ResponseBody
    public ResponseEntity<String> saveManualTemplate(@RequestBody java.util.Map<String, String> payload,
                                                     Principal principal) {
        String nume = payload.get("nume");
        String continutHtml = payload.get("continutHtml");

        String email = getEmailFromPrincipal(principal);
        User currentUser = userRepository.findByEmail(email);

        PlanTemplate template = new PlanTemplate();
        template.setNume(nume);
        template.setContinutHtml(continutHtml);
        template.setUser(currentUser);

        templateRepository.save(template);

        return ResponseEntity.ok("Template salvat cu succes!");
    }

    @GetMapping("/my-templates")
    public String viewTemplates(Model model, Principal principal) {
        String email = getEmailFromPrincipal(principal);
        User currentUser = userRepository.findByEmail(email);
        model.addAttribute("templates", templateRepository.findByUser(currentUser));
        model.addAttribute("listaExercitii", exercitiuRepository.findAll());
        return "my-templates";
    }

    // 3. Metoda care șterge un template din baza de date
    @GetMapping("/delete-template/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        templateRepository.deleteById(id);
        return "redirect:/my-templates";
    }

    // --- EXPORT PDF ---
    @GetMapping("/export/pdf")
    public void exportWorkoutsToPDF(HttpServletResponse response, Principal principal) throws Exception {
        // 1. Verificăm cine este logat
        if (principal == null) return;
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        // 2. Setăm răspunsul să fie de tip fișier PDF care se descarcă automat
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Istoric_Antrenamente_21GYM.pdf";
        response.setHeader(headerKey, headerValue);

        // 3. Luăm și grupăm istoricul EXACT cum o faci pentru afișarea pe pagină
        List<WorkoutSet> toateSeturile = workoutSetRepository.findAll().stream()
                .filter(set -> set.getUser() != null && set.getUser().getId().equals(user.getId()))
                .sorted((s1, s2) -> s2.getId().compareTo(s1.getId()))
                .toList();

        java.util.Map<java.time.LocalDateTime, java.util.Map<String, List<WorkoutSet>>> istoricDetaliat = new java.util.LinkedHashMap<>();
        for (WorkoutSet set : toateSeturile) {
            java.time.LocalDateTime dataTimp = set.getData();
            if (dataTimp == null) continue;
            istoricDetaliat.putIfAbsent(dataTimp, new java.util.LinkedHashMap<>());
            
            String exName = (set.getExercitiu() != null && set.getExercitiu().getNume() != null) 
                            ? set.getExercitiu().getNume() : "Exercițiu Necunoscut";
            istoricDetaliat.get(dataTimp).putIfAbsent(exName, new java.util.ArrayList<>());
            istoricDetaliat.get(dataTimp).get(exName).add(set);
        }

        // 4. Creăm documentul PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // 5. Desenăm titlul
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Istoric Antrenamente - 21 GYM\n\n", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        // 6. Scriem Datele în PDF
        com.lowagie.text.Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        com.lowagie.text.Font exFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        com.lowagie.text.Font setFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm");

        if (istoricDetaliat.isEmpty()) {
            document.add(new Paragraph("Nu există niciun antrenament înregistrat încă.", setFont));
        } else {
            // Parcurgem zilele
            for (java.util.Map.Entry<java.time.LocalDateTime, java.util.Map<String, List<WorkoutSet>>> sessionEntry : istoricDetaliat.entrySet()) {
                document.add(new Paragraph("Data: " + sessionEntry.getKey().format(formatter), dateFont));
                document.add(new Paragraph(" ")); // rând liber

                // Parcurgem exercițiile din acea zi
                for (java.util.Map.Entry<String, List<WorkoutSet>> exEntry : sessionEntry.getValue().entrySet()) {
                    document.add(new Paragraph("  Exercițiu: " + exEntry.getKey(), exFont));
                    
                    List<WorkoutSet> seturi = exEntry.getValue();
                    seturi.sort(java.util.Comparator.comparing(WorkoutSet::getId));
                    
                    // Parcurgem seriile
                    int setNum = 1;
                    for (WorkoutSet set : seturi) {
                        document.add(new Paragraph("    Set " + setNum + " -> " + set.getGreutate() + " kg x " + set.getRepetari() + " repetări", setFont));
                        setNum++;
                    }
                    document.add(new Paragraph(" ")); // rând liber după un exercițiu
                }
                document.add(new Paragraph("--------------------------------------------------")); // Linie despărțitoare între antrenamente
                document.add(new Paragraph(" "));
            }
        }

        document.add(new Paragraph("\nGenerat automat din aplicația 21 GYM."));
        document.close();
    }
}