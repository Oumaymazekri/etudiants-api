package com.devops.etudiants;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/etudiants")
public class EtudiantController {

    private final EtudiantRepository repo;

    public EtudiantController(EtudiantRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Etudiant> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Etudiant getById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Etudiant create(@RequestBody Etudiant e) {
        return repo.save(e);
    }

    @PutMapping("/{id}")
    public Etudiant update(@PathVariable Long id, @RequestBody Etudiant e) {
        e.setId(id);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
