package prj.yong.first.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    private void addSecurityItems(OpenAPI openAPI) {
        swaggerAuthorizationHeaders.forEach(
                header -> openAPI.addSecurityItem(new SecurityRequirement().addList(header))
        );
    }

    @Bean
    public OpenAPI createOpenAPI() {
        OpenAPI openAPI = new OpenAPI().info(
                new Info()
                        .title(applicationName)
                        .version("v1.0")
                        .description("Swagger API Documentation"));

        if (!ObjectUtils.isEmpty(swaggerAuthorizationHeaders)) {
            this.addComponents(openAPI);
            this.addSecurityItems(openAPI);
        }

        return openAPI;
    }
}
