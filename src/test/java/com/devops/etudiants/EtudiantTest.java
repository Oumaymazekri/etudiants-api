package com.devops.etudiants;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EtudiantTest {
    @Test
    void testSetNom() {
        Etudiant e = new Etudiant();
        e.setNom("Ali");
        assertEquals("Ali", e.getNom());
    }
}
