package com.micropymes.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ¿Que es DTO? es un patrón de diseño que se utiliza para transferir datos entre diferentes capas de una aplicación. 
// Un DTO es un objeto simple que contiene atributos y no tiene lógica de negocio. Su propósito principal es encapsular 
// los datos y transportarlos de manera eficiente, evitando exponer directamente las entidades del dominio o modelos de 
// la base de datos. Los DTOs son especialmente útiles en aplicaciones con arquitecturas multicapa, donde se necesita 
// separar la lógica de presentación, la lógica de negocio y el acceso a datos.
public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 150)
        String lastName,

        @NotBlank
        @Size(max = 150)
        String organizationName
) {
}