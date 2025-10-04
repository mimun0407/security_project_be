package com.example.demo.service;

import com.example.demo.common.GlobalExceptionHandler;
import com.example.demo.common.TokenExpiredException;
import com.example.demo.entity.*;
import com.example.demo.model.LoginResponse;
import com.example.demo.model.RefreshTokenRequest;
import com.example.demo.repository.InvalidatedTokenRepository;
import com.example.demo.repository.LogCRUDRepository;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    private final String secret= "h2jXchw5FloESb63Kc+DFhTARvpWL4jUGCwfGWxuG5SIf/1y/LgJxHnMqaF6A/gk";

    private final RefreshTokenRepository refreshTokenRepository;

    private final LogCRUDRepository logCRUDRepository;

    public Page<LogCrud> history(Pageable pageable) {
        return logCRUDRepository.findAll(pageable);
    }

    private String createJwt(UserEntity user) throws JOSEException {
        JWSHeader jweHeader = new JWSHeader(JWSAlgorithm.HS256);

        List<String> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getName)   // lấy tên role
                .toList();
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("DANG QUAN BAO")
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .expirationTime(new Date(System.currentTimeMillis() + 36000000))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSObject jwsObject = new JWSObject(jweHeader,jwtClaimsSet.toPayload());

        jwsObject.sign(new MACSigner(secret.getBytes()));

        return jwsObject.serialize();
    }

    private LoginResponse createRefreshToken(UserEntity user) throws JOSEException, ParseException {
        String refreshToken = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 24L * 60 * 60 * 1000);
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(refreshToken,user.getId(),expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);
        String token = createJwt(user);
        SignedJWT signedJWT = SignedJWT.parse(token);
        UserEntity userEntity = userRepository.findByUsername(signedJWT.getJWTClaimsSet().getSubject()).orElseThrow(()->new RuntimeException("cannot find user"));
        Set<RoleEntity> roles = userEntity.getRoles();
        Set<String> roleSet = roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
        return new LoginResponse( token,refreshToken,roleSet) ;
    }

    public LoginResponse login(String username, String password) throws JOSEException, ParseException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("nhap lai user name"));
        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new RuntimeException("nhap lai user password");
        }
        return createRefreshToken(userEntity);
    }

    public SignedJWT filterToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(secret.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new TokenExpiredException("Token expired or invalid");
        }
        return signedJWT;
    }

    public String logout(String token, RefreshTokenRequest request) throws ParseException, JOSEException {
        var signedJWT = filterToken(token);
        var claims = signedJWT.getJWTClaimsSet();
        InvalidatedTokenEntity invalidatedTokenEntity = new InvalidatedTokenEntity(token,claims.getExpirationTime());
        refreshTokenRepository.deleteById(request.refreshToken());
        invalidatedTokenRepository.save(invalidatedTokenEntity);
        return "Logout successful";
    }

    public Boolean isTokenInBlackList(String token) {
        return invalidatedTokenRepository.existsById(token);
    }

    public String refreshToken(String refreshToken) throws JOSEException {
        RefreshTokenEntity refreshTokenEntity = isRefreshTokenValid(refreshToken);

        UserEntity user = userRepository.findById(refreshTokenEntity.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return createJwt(user);
    }

    public RefreshTokenEntity isRefreshTokenValid(String token) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("refresh token not found"));

        if (!userRepository.existsById(refreshTokenEntity.getUserId())) {
            throw new RuntimeException("user of this token not found");
        }
        if (refreshTokenEntity.getExpiresTime().before(new Date())) {
            throw new RuntimeException("refresh token expired");
        }
        return refreshTokenEntity;
    }
}
