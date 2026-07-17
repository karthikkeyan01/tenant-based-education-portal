package com.fts.tenantbasededuportal.config;

import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title(SwaggerConstants.API_TITLE)
                        .version(SwaggerConstants.API_VERSION)
                        .description(SwaggerConstants.API_DESCRIPTION)
                        .contact(new Contact()
                                .name("Karthikkeyan K")
                                .url("https://github.com/karthikkeyan01"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(new Server()
                        .url(baseUrl)
                        .description(SwaggerConstants.LOCAL_SERVER_DESCRIPTION)))
                .components(new Components()
                        .addSecuritySchemes(SwaggerConstants.SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SwaggerConstants.SECURITY_SCHEME)
                                        .bearerFormat(SwaggerConstants.SECURITY_BEARER_FORMAT)));
    }

//    @Bean
//    public OpenApiCustomizer actuatorSecurityCustomizer() {
//
//        return new OpenApiCustomizer() {
//
//            @Override
//            public void customise(final OpenAPI openApi) {
//
//                if (openApi.getPaths() == null){
//                    return;
//                }
//
//                for (final Map.Entry<String, PathItem> entry :
//                        openApi.getPaths().entrySet()) {
//
//                    final String path = entry.getKey();
//                    final PathItem pathItem = entry.getValue();
//
//                    if(!path.startsWith("/actuator")){
//                        continue;
//                    }
//
//                    for (final Operation operation : pathItem.readOperations()) {
//
//                        operation.addSecurityItem(new SecurityRequirement()
//                                .addList(SwaggerConstants.SECURITY_SCHEME_NAME));
//                    }
//                }
//            }
//
//        };
//    }
}
