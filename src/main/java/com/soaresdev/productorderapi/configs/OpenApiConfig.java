package com.soaresdev.productorderapi.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
    info = @Info(
            contact = @Contact(
                    name = "Linkedin",
                    url = "https://www.linkedin.com/in/hiago-soares-96840a271/"
            ),
            description = "Documentation for orders and products API Restful.\n\nThe API supports JSON and XML.",
            title = "Orders and Products API Restful",
            version = "1.0"
    )
)
public class OpenApiConfig {
}