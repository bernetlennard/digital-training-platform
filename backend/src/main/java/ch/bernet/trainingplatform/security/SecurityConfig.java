package ch.bernet.trainingplatform.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final SecurityErrorHandler securityErrorHandler;
    private final JwtUserIdConverter jwtUserIdConverter;
    private final CookieBearerTokenResolver cookieBearerTokenResolver;

    public SecurityConfig(JwtProperties jwtProperties,
                          SecurityErrorHandler securityErrorHandler,
                          JwtUserIdConverter jwtUserIdConverter,
                          CookieBearerTokenResolver cookieBearerTokenResolver) {
        this.jwtProperties = jwtProperties;
        this.securityErrorHandler = securityErrorHandler;
        this.jwtUserIdConverter = jwtUserIdConverter;
        this.cookieBearerTokenResolver = cookieBearerTokenResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // TODO: Deny by default, offen nur Registrierung, Login und Health-Check; kein CORS
        // TODO: oauth2ResourceServer mit cookieBearerTokenResolver und jwtUserIdConverter konfigurieren
        // TODO: securityErrorHandler als AuthenticationEntryPoint und AccessDeniedHandler setzen
        return null;
    }

    // static, weil UserService den PasswordEncoder braucht und sonst ein Zyklus entsteht
    // (SecurityConfig -> JwtUserIdConverter -> UserService -> SecurityConfig)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        // TODO: PasswordEncoder zurückgeben (z. B. BCrypt)
        return null;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        // TODO: NimbusJwtEncoder mit HS256 und jwtProperties.secret()
        return null;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // TODO: NimbusJwtDecoder mit HS256 und jwtProperties.secret()
        return null;
    }

}
