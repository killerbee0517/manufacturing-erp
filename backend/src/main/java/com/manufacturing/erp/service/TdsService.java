package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.SupplierTaxProfile;
import com.manufacturing.erp.domain.TdsRule;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.SupplierTaxProfileRepository;
import com.manufacturing.erp.repository.TdsRuleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class TdsService {
  private final SupplierTaxProfileRepository supplierTaxProfileRepository;
  private final TdsRuleRepository tdsRuleRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;

  public TdsService(SupplierTaxProfileRepository supplierTaxProfileRepository,
                    TdsRuleRepository tdsRuleRepository,
                    PurchaseInvoiceRepository purchaseInvoiceRepository) {
    this.supplierTaxProfileRepository = supplierTaxProfileRepository;
    this.tdsRuleRepository = tdsRuleRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
  }

  public BigDecimal calculateTds(Long supplierId, LocalDate invoiceDate, BigDecimal invoiceAmount) {
    SupplierTaxProfile profile = supplierTaxProfileRepository.findBySupplierId(supplierId)
        .orElseThrow(() -> new IllegalArgumentException("Supplier tax profile missing"));
    if (!profile.isTdsApplicable()) {
      return BigDecimal.ZERO;
    }
    String section = profile.getDefaultSection();
    TdsRule rule = tdsRuleRepository.findFirstBySectionCodeAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            section, invoiceDate, invoiceDate)
        .orElseThrow(() -> new IllegalArgumentException("TDS rule not found"));

    LocalDate fyStart = LocalDate.of(invoiceDate.getMonthValue() >= 4 ? invoiceDate.getYear() : invoiceDate.getYear() - 1, 4, 1);
    LocalDate fyEnd = fyStart.plusYears(1).minusDays(1);
    BigDecimal cumulative = purchaseInvoiceRepository.findPostedTotalForSupplier(
        supplierId, fyStart, fyEnd, DocumentStatus.POSTED);
    BigDecimal projected = cumulative.add(invoiceAmount);
    if (projected.compareTo(rule.getThresholdAmount()) <= 0) {
      return BigDecimal.ZERO;
    }
    return invoiceAmount.multiply(rule.getRatePercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }
}
