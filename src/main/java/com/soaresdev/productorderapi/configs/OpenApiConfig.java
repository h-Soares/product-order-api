package com.soaresdev.productorderapi.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
            contact = @Contact(
                    name = "Linkedin",
                    url = "https://www.linkedin.com/in/hiago-soares-de-araujo-96840a271"
            ),
            description = "Documentation for orders and products API Restful",
            title = "Orders and Products API Restful",
            version = "1.0"
    ),
    servers = {
        @Server(
            description = "Localhost server",
            url = "http://localhost:8080/"
        ),
        @Server(
            description = "Deploy server",
            url = "https://product-order-api.onrender.com"
        )
    }
)
public class OpenApiConfig {
}