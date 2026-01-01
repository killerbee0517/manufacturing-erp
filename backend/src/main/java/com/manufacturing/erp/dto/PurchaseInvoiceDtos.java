package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseInvoiceDtos {
  public record LineRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal rate,
      BigDecimal amount) {}

  public record InvoiceChargeRequest(
      @NotNull Long chargeTypeId,
      String calcType,
      BigDecimal rate,
      BigDecimal amount,
      Boolean isDeduction,
      @NotNull String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record UpdateInvoiceRequest(
      String supplierInvoiceNo,
      @NotNull LocalDate invoiceDate,
      String narration,
      Long brokerId,
      List<InvoiceChargeRequest> charges) {}

  public record InvoiceLineResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal rate,
      BigDecimal amount) {}

  public record InvoiceChargeResponse(
      Long id,
      Long chargeTypeId,
      String chargeTypeName,
      String calcType,
      BigDecimal rate,
      BigDecimal amount,
      Boolean isDeduction,
      String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record InvoiceResponse(
      Long id,
      String invoiceNo,
      Long supplierId,
      String supplierName,
      Long brokerId,
      String brokerName,
      Long purchaseOrderId,
      String purchaseOrderNo,
      Long grnId,
      String grnNo,
      String supplierInvoiceNo,
      LocalDate invoiceDate,
      String narration,
      String status,
      BigDecimal totalAmount,
      BigDecimal subtotal,
      BigDecimal taxTotal,
      BigDecimal roundOff,
      BigDecimal grandTotal,
      BigDecimal tdsAmount,
      BigDecimal netPayable,
      BigDecimal brokerageAmount,
      List<InvoiceLineResponse> lines,
      List<InvoiceChargeResponse> charges) {}
}
