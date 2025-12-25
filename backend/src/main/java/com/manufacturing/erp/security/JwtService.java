package com.manufacturing.erp.security;

import com.manufacturing.erp.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String ACCESS_TOKEN = "access";
  private static final String REFRESH_TOKEN = "refresh";

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(String username, List<String> roles) {
    return buildToken(username, Map.of("roles", roles, TOKEN_TYPE_CLAIM, ACCESS_TOKEN), jwtProperties.getExpiresMinutes());
  }

  public String generateRefreshToken(String username) {
    return buildToken(username, Map.of(TOKEN_TYPE_CLAIM, REFRESH_TOKEN), jwtProperties.getRefreshExpiresMinutes());
  }

  public boolean isRefreshToken(Claims claims) {
    return REFRESH_TOKEN.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
  }

  public boolean isAccessToken(Claims claims) {
    String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
    return tokenType == null || ACCESS_TOKEN.equals(tokenType);
  }

  private String buildToken(String username, Map<String, Object> claims, long expiresMinutes) {
    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(expiresMinutes * 60);
    return Jwts.builder()
        .issuer(jwtProperties.getIssuer())
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .claims(claims)
        .signWith(secretKey)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
