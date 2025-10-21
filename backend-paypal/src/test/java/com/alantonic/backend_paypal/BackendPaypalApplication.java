package com.alantonic.backend_paypal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot.
 * 
 * Esta clase arranca el servidor backend en el puerto 8080
 * y habilita la detección automática de:
 * - Controladores REST (@RestController)
 * - Servicios (@Service)
 * - Repositorios JPA (@Repository)
 * - Configuraciones (@Configuration)
 */
@SpringBootApplication
public class BackendPaypalApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendPaypalApplication.class, args);
    }
}