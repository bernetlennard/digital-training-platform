package ch.bernet.trainingplatform.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Set;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final Set<String> PUBLIC_PATHS =
            Set.of("/api/auth/register", "/api/auth/login", "/api/auth/logout");

    @Override
    public String resolve(@NonNull HttpServletRequest request) {
        if (PUBLIC_PATHS.contains(request.getServletPath())) {
            return null;
        }
        Cookie cookie = WebUtils.getCookie(request, JwtCookieService.COOKIE_NAME);
        return cookie == null ? null : cookie.getValue();
    }

}
