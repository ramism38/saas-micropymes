package com.micropymes.backend.auth.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//Ruta base para el controlador de autenticación
@RequestMapping("/api/v1/auth")
public class CsrfController {

    // Endpoint para obtener el token CSRF
    @GetMapping("/csrf")
    // ##Método que devuelve el token CSRF##
    // ¿Es este metodo generado por spring? sí, el token CSRF es generado automáticamente por Spring Security y se inyecta en 
    // el método a través del parámetro CsrfToken. Esto permite que el cliente obtenga el token CSRF necesario para realizar 
    // solicitudes seguras al servidor.
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}