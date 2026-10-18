package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.service.user.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtUserIdConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    public JwtUserIdConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Benutzer-ID aus dem subject lesen, mit userService.exists prüfen, als Principal setzen
        Long id = Long.valueOf(jwt.getSubject());
        if(!userService.exists(id)){
            throw new BadCredentialsException(ErrorMessages.USER_NOT_FOUND);
        }

        return new UsernamePasswordAuthenticationToken(id, jwt, List.of());
    }

}
