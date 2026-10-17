package ch.bernet.trainingplatform.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        // Version steht im Pfad /api/v1/... (#NFA-7.16). Swagger und /error bleiben ohne Version.
        configurer.usePathSegment(1, path -> path.pathWithinApplication().value().startsWith("/api/"))
                .setVersionRequired(false);
    }

}
