package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.service.user.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtUserIdConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    public JwtUserIdConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // TODO: Benutzer-ID aus dem subject lesen, mit userService.exists prüfen, als Principal setzen
        return null;
    }

}
