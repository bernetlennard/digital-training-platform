package ch.bernet.trainingplatform.security;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class JwtCookieService {

    public static final String COOKIE_NAME = "access_token";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtCookieService(JwtEncoder jwtEncoder, JwtProperties jwtProperties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public ResponseCookie createAuthCookie(Long userId) {
        // TODO: Cookie mit Token aus createToken, HttpOnly, Secure, SameSite=Strict, Max-Age = validity (#NFA-1.2)
        return null;
    }

    public ResponseCookie createLogoutCookie() {
        // TODO: leeres Cookie mit gleichen Attributen und Max-Age 0
        return null;
    }

    private String createToken(Long userId) {
        // TODO: JWT (HS256) nur mit Benutzer-ID, Ausstellungs- und Ablaufzeit (L15)
        return null;
    }

}
