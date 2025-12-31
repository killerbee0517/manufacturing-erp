package com.manufacturing.erp.dto;

import com.manufacturing.erp.domain.Enums.BrokerCommissionType;
import com.manufacturing.erp.domain.Enums.BrokeragePaidBy;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Enums.SupplierType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class PartyDtos {
  public record PartyRoleRequest(
      @NotNull PartyRoleType roleType,
      Integer creditPeriodDays,
      SupplierType supplierType,
      BrokerCommissionType brokerCommissionType,
      BigDecimal brokerCommissionRate,
      BrokeragePaidBy brokeragePaidBy,
      Boolean active) {}

  public record PartyRequest(
      String partyCode,
      @NotBlank String name,
      String address,
      String state,
      String country,
      String pinCode,
      String pan,
      String gstNo,
      String contact,
      String email,
      Long bankId,
      PartyStatus status,
      @Valid List<PartyRoleRequest> roles) {}

  public record PartyRoleResponse(
      Long id,
      PartyRoleType roleType,
      Integer creditPeriodDays,
      SupplierType supplierType,
      BrokerCommissionType brokerCommissionType,
      BigDecimal brokerCommissionRate,
      BrokeragePaidBy brokeragePaidBy,
      boolean active) {}

  public record PartyResponse(
      Long id,
      String partyCode,
      String name,
      String address,
      String state,
      String country,
      String pinCode,
      String pan,
      String gstNo,
      String contact,
      String email,
      Long bankId,
      String bankName,
      PartyStatus status,
      List<PartyRoleResponse> roles) {}

  public record PartyOption(Long id, String code, String name) {}
}
