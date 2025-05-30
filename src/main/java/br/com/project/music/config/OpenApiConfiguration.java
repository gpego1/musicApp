package br.com.project.music.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Music Project API",
                version = "1.0",
                description = "API para o controle e cadastro de eventos musicais, generos musicais, e autentificacao de usuarios.",
                contact = @Contact(
                        name = "Gabriel Pego",
                        email = "gpego786@gmail.com"
                )
        )
)
public class OpenApiConfiguration {

}
