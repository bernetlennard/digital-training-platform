package ch.bernet.trainingplatform.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

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
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(o -> o
                        .bearerTokenResolver(cookieBearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtUserIdConverter)));
        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKeySpec sha256 = new SecretKeySpec(jwtProperties.secret().getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(sha256));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec sha256 = new SecretKeySpec(jwtProperties.secret().getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(sha256).macAlgorithm(MacAlgorithm.HS256).build();
    }

}
