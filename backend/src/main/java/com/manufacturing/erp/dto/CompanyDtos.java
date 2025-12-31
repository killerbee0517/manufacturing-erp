package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyDtos {
  public record CompanyRequest(
      @NotBlank @Size(max = 50) String code,
      @NotBlank @Size(max = 150) String name,
      String gstNo,
      String pan,
      String address,
      Boolean active,
      Long parentCompanyId) {}

  public record CompanyResponse(
      Long id,
      String code,
      String name,
      String gstNo,
      String pan,
      String address,
      boolean active,
      Long parentCompanyId) {}
}
