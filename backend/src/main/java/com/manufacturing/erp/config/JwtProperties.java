package com.manufacturing.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
  private String issuer;
  private String secret;
  private long expiresMinutes;

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpiresMinutes() {
    return expiresMinutes;
  }

  public void setExpiresMinutes(long expiresMinutes) {
    this.expiresMinutes = expiresMinutes;
  }
}
