package prj.yong.price.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfiguration {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${swagger.authorization.headers:#{null}}")
    private List<String> swaggerAuthorizationHeaders;

    private void addComponents(OpenAPI openAPI) {
        SecurityScheme jwtTokenSecuritySchema =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .name(HttpHeaders.AUTHORIZATION);

        Components components = new Components();

        swaggerAuthorizationHeaders.forEach(
                header -> {
                    if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(header)) {
                        components.addSecuritySchemes(
                                HttpHeaders.AUTHORIZATION, jwtTokenSecuritySchema);
                    } else {
                        components.addSecuritySchemes(
                                header,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(header));
                    }
                });

        openAPI.components(components);
    }
}
