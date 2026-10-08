package ch.bernet.trainingplatform.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        // TODO: Token aus dem Cookie JwtCookieService.COOKIE_NAME lesen, null wenn nicht vorhanden
        return null;
    }

}
