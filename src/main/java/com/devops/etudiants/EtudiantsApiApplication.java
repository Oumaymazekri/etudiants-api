package com.devops.etudiants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EtudiantsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtudiantsApiApplication.class, args);
        System.out.println("Application démarrée !");
    }
}
