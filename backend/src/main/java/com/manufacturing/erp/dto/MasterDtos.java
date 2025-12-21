package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MasterDtos {
  public record SupplierRequest(@NotBlank String name, @NotBlank String code, String pan) {}
  public record SupplierResponse(Long id, String name, String code, String pan) {}

  public record ItemRequest(@NotBlank String name, @NotBlank String sku, @NotNull Long uomId) {}
  public record ItemResponse(Long id, String name, String sku, Long uomId) {}

  public record LocationRequest(@NotBlank String name, @NotBlank String code, @NotBlank String locationType) {}
  public record LocationResponse(Long id, String name, String code, String locationType) {}

  public record CustomerRequest(@NotBlank String name, @NotBlank String code) {}
  public record CustomerResponse(Long id, String name, String code) {}

  public record BrokerRequest(@NotBlank String name, @NotBlank String code) {}
  public record BrokerResponse(Long id, String name, String code) {}

  public record VehicleRequest(@NotBlank String vehicleNo) {}
  public record VehicleResponse(Long id, String vehicleNo) {}

  public record TdsRuleRequest(
      @NotBlank String sectionCode,
      @NotNull BigDecimal ratePercent,
      @NotNull BigDecimal thresholdAmount,
      @NotNull LocalDate effectiveFrom,
      LocalDate effectiveTo) {}
  public record TdsRuleResponse(Long id, String sectionCode, BigDecimal ratePercent, BigDecimal thresholdAmount,
                                LocalDate effectiveFrom, LocalDate effectiveTo) {}

  public record UserRequest(@NotBlank String username, @NotBlank String password, @NotBlank String fullName, String roleName) {}
  public record UserResponse(Long id, String username, String fullName, List<String> roles) {}
}
