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
  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String username, List<String> roles) {
    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(jwtProperties.getExpiresMinutes() * 60);
    return Jwts.builder()
        .issuer(jwtProperties.getIssuer())
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .claims(Map.of("roles", roles))
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
