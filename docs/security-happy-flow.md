# Spring Security: Happy Flow (Registrieren → Profil abfragen)

## Wie der Ablauf funktioniert

**Registrieren:** Das ist ein ganz normaler Controller ohne Spring-Security-Login.
`POST /api/auth/register` → `UserService.register` hasht das Passwort und speichert den User → `JwtCookieService` erstellt ein JWT mit `sub = userId` und setzt es als Cookie.

**Profil abfragen:** Hier arbeitet Spring Security, und zwar in dieser Kette:
`GET /api/users/me` → `CookieBearerTokenResolver` holt das Token aus dem Cookie → `JwtDecoder` prüft Signatur und Ablaufzeit → `JwtUserIdConverter` macht daraus eine `Authentication` mit Principal `Long userId` → `@AuthenticationPrincipal Long userId` im Controller.

Du baust also nur die Teile dieser Kette, Spring setzt sie zusammen.

## Was du für den Happy Flow brauchst (in dieser Reihenfolge)

### 1. `application.properties`

```properties
app.jwt.secret=${JWT_SECRET:ein-dev-secret-mit-mindestens-32-zeichen!!}
```

HS256 braucht mindestens 256 Bit, also mindestens 32 Bytes. Mit weniger wirft Nimbus einen Fehler.

### 2. `TrainingplatformApplication.clock()`

`Clock.system(ZoneId.of("Europe/Zurich"))`. Ohne diese Clock gibt es beim Token-Erstellen eine NPE.

### 3. `SecurityConfig`

- `passwordEncoder()`: `new BCryptPasswordEncoder()`
- Key: `new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256")`
- `jwtDecoder()`: `NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()`
- `jwtEncoder()`: `new NimbusJwtEncoder(new ImmutableSecret<>(key))`
- `securityFilterChain`: ungefähr so:

  ```java
  http.csrf(c -> c.disable())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
          .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
          .requestMatchers("/error").permitAll()
          .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o
          .bearerTokenResolver(cookieBearerTokenResolver)
          .jwt(j -> j.jwtAuthenticationConverter(jwtUserIdConverter)));
  return http.build();
  ```

  - **`/error` freigeben:** Sonst sieht jede Exception (auch eine NPE in einem freigegebenen Endpoint) wie ein 401 aus, und du suchst den Fehler am falschen Ort.
  - **CSRF ausschalten:** Ohne das bekommt jeder POST ein 403. Weil die Authentifizierung über ein Cookie läuft, ist das aber eine bewusste Entscheidung. `SameSite=Strict` ist dann dein CSRF-Schutz, das solltest du später dokumentieren.

### 4. `JwtCookieService`

- `createToken`: `JwtClaimsSet` mit `subject(userId.toString())`, `issuedAt(now)` und `expiresAt(now.plus(validity))`. Dann:

  ```java
  jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue()
  ```

  **Der Header mit HS256 muss explizit gesetzt sein.** Sonst nimmt der Encoder RS256 und meldet *"Failed to select a JWK signing key"*. Das ist der häufigste Stolperstein.
- `createAuthCookie`: `ResponseCookie.from(COOKIE_NAME, token).httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(validity).build()`

### 5. `CookieBearerTokenResolver`

`WebUtils.getCookie(request, COOKIE_NAME)` verwenden und `null` zurückgeben, wenn das Cookie fehlt. Wenn du `request.getCookies()` direkt nimmst: Das kann `null` sein.

### 6. `JwtUserIdConverter`

```java
Long id = Long.valueOf(jwt.getSubject());
return new UsernamePasswordAuthenticationToken(id, jwt, List.of());
```

- **Nicht `JwtAuthenticationToken` nehmen:** Dort ist der Principal das `Jwt`, und `@AuthenticationPrincipal Long` wäre dann `null`. Der Konstruktor mit drei Argumenten markiert das Token als authentifiziert.
- Die Prüfung mit `userService.exists` kannst du für den Happy Flow vorerst weglassen.

### 7. `UserService.register` und `AuthController.register`

- Im Service: E-Mail normalisieren, `AppUser.builder()...passwordHash(passwordEncoder.encode(pw)).build()` speichern und eine `UserResponse` zurückgeben. Die Prüfung auf doppelte E-Mail kann warten.
- Im Controller:

  ```java
  ResponseEntity.status(HttpStatus.CREATED)
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(user)
  ```

### 8. `UserService.getProfile`

Funktioniert schon so, wie es ist.

## Was du vorerst weglassen kannst

Login, Logout, `LoginLockService`, `SecurityErrorHandler` (ohne ihn schickt Spring einfach ein 401 ohne Body), Validierung und die übrigen `/me`-Endpoints.

## Testen

```bash
curl -i -c cookies.txt -H "Content-Type: application/json" \
     -d '{"email":"test@test.ch","password":"geheim123"}' \
     http://localhost:8080/api/auth/register

curl -i -b cookies.txt http://localhost:8080/api/users/me
```

- **Secure-Cookie über HTTP:** Neuere curl-Versionen und Browser schicken es an `localhost` trotzdem. Falls ein Tool das nicht tut, setz den Header manuell: `-H "Cookie: access_token=<token>"`.
- **Token anschauen:** Mit [jwt.io](https://jwt.io) siehst du, ob `sub`, `iat` und `exp` stimmen.

## Wo du bei Problemen suchst

In `application.properties` `logging.level.org.springframework.security=TRACE` setzen. Dann siehst du für jeden Request, welcher Filter was gemacht hat und warum er abgelehnt wurde.
