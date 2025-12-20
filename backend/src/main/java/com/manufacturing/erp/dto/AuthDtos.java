package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AuthDtos {
  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  public record LoginResponse(String token, String username, List<String> roles) {}
}
