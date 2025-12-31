package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BankDtos {
  public record BankRequest(
      @NotBlank @Size(max = 150) String name,
      String branch,
      String accNo,
      String ifsc,
      String swiftCode,
      String type,
      Boolean active) {}

  public record BankResponse(
      Long id,
      String name,
      String branch,
      String accNo,
      String ifsc,
      String swiftCode,
      String type,
      boolean active) {}

  public record BankOption(Long id, String name) {}
}
