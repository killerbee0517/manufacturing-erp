package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.AuthDtos;
import com.manufacturing.erp.security.CustomUserDetailsService;
import com.manufacturing.erp.security.JwtService;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @PostMapping("/login")
  public AuthDtos.LoginResponse login(@RequestBody AuthDtos.LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(role -> role.replace("ROLE_", ""))
        .toList();
    String token = jwtService.generateAccessToken(authentication.getName(), roles);
    String refreshToken = jwtService.generateRefreshToken(authentication.getName());
    return new AuthDtos.LoginResponse(token, refreshToken, authentication.getName(), roles);
  }

  @PostMapping("/refresh")
  public AuthDtos.RefreshResponse refresh(@RequestBody AuthDtos.RefreshRequest request) {
    try {
      var claims = jwtService.parseToken(request.refreshToken());
      if (!jwtService.isRefreshToken(claims)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
      }
      String username = claims.getSubject();
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      List<String> roles = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .map(role -> role.replace("ROLE_", ""))
          .toList();
      String token = jwtService.generateAccessToken(username, roles);
      String refreshToken = jwtService.generateRefreshToken(username);
      return new AuthDtos.RefreshResponse(token, refreshToken);
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
  }
}
