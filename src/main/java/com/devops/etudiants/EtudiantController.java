package com.devops.etudiants;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/etudiants")
public class EtudiantController {

    private List<Etudiant> etudiants = new ArrayList<>();

    @GetMapping
    public List<Etudiant> getAllEtudiants() {
        return etudiants;
    }

    @PostMapping
    public Etudiant addEtudiant(@RequestBody Etudiant etudiant) {
        etudiants.add(etudiant);
        return etudiant;
    }
}
