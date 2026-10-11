package ch.bernet.trainingplatform.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(@NonNull HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, JwtCookieService.COOKIE_NAME);
        return cookie == null ? null : cookie.getValue();
    }

}
