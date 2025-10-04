package com.example.demo.config;

import com.example.demo.service.AuthService;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final AuthService authService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var signedJWT = authService.filterToken(token);
            var claims = signedJWT.getJWTClaimsSet();
            if (authService.isTokenInBlackList(token)){
                throw new JwtException("token is blacklist");
            }
            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(signedJWT.getHeader().toJSONObject()))
                    .claims(c -> c.putAll(claims.getClaims()))
                    .issuedAt(claims.getIssueTime().toInstant())
                    .expiresAt(claims.getExpirationTime().toInstant())
                    .build();
        } catch (Exception e) {
            throw new JwtException("Invalid token", e);
        }
    }
}
