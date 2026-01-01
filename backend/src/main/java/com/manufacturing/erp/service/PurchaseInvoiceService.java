package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.DeductionChargeType;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.PurchaseInvoiceCharge;
import com.manufacturing.erp.domain.PurchaseInvoiceLine;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceChargeRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceLineRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.PurchaseOrderLineRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PurchaseInvoiceService {
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
  private final PurchaseInvoiceChargeRepository purchaseInvoiceChargeRepository;
  private final GrnRepository grnRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PurchaseOrderLineRepository purchaseOrderLineRepository;
  private final DeductionChargeTypeRepository chargeTypeRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final VehicleRepository vehicleRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final LedgerService ledgerService;
  private final VoucherService voucherService;
  private final TdsService tdsService;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public PurchaseInvoiceService(PurchaseInvoiceRepository purchaseInvoiceRepository,
                                PurchaseInvoiceLineRepository purchaseInvoiceLineRepository,
                                PurchaseInvoiceChargeRepository purchaseInvoiceChargeRepository,
                                GrnRepository grnRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderLineRepository purchaseOrderLineRepository,
                                DeductionChargeTypeRepository chargeTypeRepository,
                                ExpensePartyRepository expensePartyRepository,
                                SupplierRepository supplierRepository,
                                BrokerRepository brokerRepository,
                                VehicleRepository vehicleRepository,
                                BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                                LedgerService ledgerService,
                                VoucherService voucherService,
                                TdsService tdsService,
                                CompanyRepository companyRepository,
                                CompanyContext companyContext) {
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.purchaseInvoiceLineRepository = purchaseInvoiceLineRepository;
    this.purchaseInvoiceChargeRepository = purchaseInvoiceChargeRepository;
    this.grnRepository = grnRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.chargeTypeRepository = chargeTypeRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.vehicleRepository = vehicleRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
    this.ledgerService = ledgerService;
    this.voucherService = voucherService;
    this.tdsService = tdsService;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional
  public PurchaseInvoice createFromGrn(Long grnId) {
    Company company = requireCompany();
    Grn grn = grnRepository.findByIdAndPurchaseOrderCompanyId(grnId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GRN not found"));
    if (grn.getStatus() != DocumentStatus.POSTED) {
      throw new IllegalStateException("GRN must be posted before creating purchase invoice");
    }
    var existing = purchaseInvoiceRepository.findFirstByGrnId(grnId);
    if (existing.isPresent()) {
      return existing.get();
    }

    PurchaseInvoice invoice = new PurchaseInvoice();
    invoice.setInvoiceNo(resolveInvoiceNo(null));
    invoice.setSupplier(grn.getSupplier());
    invoice.setPurchaseOrder(grn.getPurchaseOrder());
    invoice.setGrn(grn);
    invoice.setSupplierInvoiceNo(null);
    invoice.setInvoiceDate(LocalDate.now());
    invoice.setNarration(null);
    invoice.setStatus(DocumentStatus.DRAFT);

    BigDecimal subtotal = BigDecimal.ZERO;
    List<PurchaseInvoiceLine> lines = new ArrayList<>();
    for (GrnLine grnLine : grn.getLines()) {
      PurchaseInvoiceLine line = buildLine(invoice, grnLine);
      subtotal = subtotal.add(defaultAmount(line.getAmount()));
      lines.add(line);
    }

    applyTotals(invoice, subtotal, List.of());
    PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
    List<PurchaseInvoiceLine> savedLines = lines.stream().map(line -> {
      line.setPurchaseInvoice(saved);
      return purchaseInvoiceLineRepository.save(line);
    }).toList();
    saved.setLines(savedLines);
    return saved;
  }

  @Transactional
  public PurchaseInvoice createFromPo(Long purchaseOrderId) {
    PurchaseOrder purchaseOrder = getPurchaseOrder(purchaseOrderId);
    var existing = purchaseInvoiceRepository.findFirstByPurchaseOrderId(purchaseOrderId);
    if (existing.isPresent()) {
      return existing.get();
    }

    PurchaseInvoice invoice = new PurchaseInvoice();
    invoice.setInvoiceNo(resolveInvoiceNo(null));
    invoice.setSupplier(purchaseOrder.getSupplier());
    invoice.setPurchaseOrder(purchaseOrder);
    invoice.setGrn(null);
    invoice.setSupplierInvoiceNo(null);
    invoice.setInvoiceDate(LocalDate.now());
    invoice.setNarration(null);
    invoice.setStatus(DocumentStatus.DRAFT);

    BigDecimal subtotal = BigDecimal.ZERO;
    List<PurchaseInvoiceLine> lines = new ArrayList<>();
    for (PurchaseOrderLine poLine : purchaseOrderLineRepository.findByPurchaseOrderId(purchaseOrderId)) {
      PurchaseInvoiceLine line = buildLine(invoice, poLine);
      subtotal = subtotal.add(defaultAmount(line.getAmount()));
      lines.add(line);
    }

    applyTotals(invoice, subtotal, List.of());
    PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
    List<PurchaseInvoiceLine> savedLines = lines.stream().map(line -> {
      line.setPurchaseInvoice(saved);
      return purchaseInvoiceLineRepository.save(line);
    }).toList();
    saved.setLines(savedLines);
    return saved;
  }

  @Transactional
  public PurchaseInvoice updateDraft(Long id, PurchaseInvoiceDtos.UpdateInvoiceRequest request) {
    PurchaseInvoice invoice = getInvoice(id);
    if (invoice.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalStateException("Only draft invoices can be edited");
    }
    invoice.setSupplierInvoiceNo(request.supplierInvoiceNo());
    invoice.setInvoiceDate(request.invoiceDate());
    invoice.setNarration(request.narration());
    if (request.brokerId() != null) {
      Broker broker = brokerRepository.findById(request.brokerId())
          .orElseThrow(() -> new IllegalArgumentException("Broker not found"));
      invoice.setBroker(broker);
    } else {
      invoice.setBroker(null);
    }

    BigDecimal subtotal = resolveSubtotal(invoice.getId());
    List<PurchaseInvoiceCharge> charges = request.charges() != null
        ? buildCharges(request.charges(), subtotal)
        : purchaseInvoiceChargeRepository.findByPurchaseInvoiceId(invoice.getId());
    if (request.charges() != null) {
      persistCharges(invoice, charges);
    }

    applyTotals(invoice, subtotal, charges);
    return purchaseInvoiceRepository.save(invoice);
  }

  @Transactional
  public PurchaseInvoice post(Long id) {
    PurchaseInvoice invoice = getInvoice(id);
    if (invoice.getStatus() == DocumentStatus.POSTED) {
      return invoice;
    }
    if (invoice.getSupplier() == null) {
      throw new IllegalStateException("Supplier is required for posting");
    }
    BigDecimal subtotal = resolveSubtotal(invoice.getId());
    List<PurchaseInvoiceCharge> charges = purchaseInvoiceChargeRepository.findByPurchaseInvoiceId(invoice.getId());
    applyTotals(invoice, subtotal, charges);

    Ledger supplierLedger = ensureSupplierLedger(invoice.getSupplier());
    Ledger purchaseLedger = ledgerService.findOrCreateLedger(resolvePurchaseLedgerName(invoice), LedgerType.EXPENSE);
    Ledger deductionLedger = ledgerService.findOrCreateLedger("Purchase Deductions", LedgerType.GENERAL);
    Ledger chargeLedger = ledgerService.findOrCreateLedger("Purchase Charges", LedgerType.EXPENSE);
    Ledger tdsLedger = ledgerService.findOrCreateLedger("TDS Payable", LedgerType.GENERAL);
    Ledger brokerageLedger = ledgerService.findOrCreateLedger("Brokerage Expense", LedgerType.EXPENSE);

    List<VoucherService.VoucherLineRequest> lines = new ArrayList<>();
    lines.add(new VoucherService.VoucherLineRequest(purchaseLedger, invoice.getSubtotal(), BigDecimal.ZERO));

    for (var posting : buildChargePostings(charges, chargeLedger, deductionLedger)) {
      lines.add(posting);
    }

    if (defaultAmount(invoice.getTdsAmount()).compareTo(BigDecimal.ZERO) > 0) {
      lines.add(new VoucherService.VoucherLineRequest(tdsLedger, BigDecimal.ZERO, invoice.getTdsAmount()));
    }
    if (supplierLedger != null) {
      lines.add(new VoucherService.VoucherLineRequest(supplierLedger, BigDecimal.ZERO, invoice.getNetPayable()));
    }
    if (invoice.getBroker() != null && defaultAmount(invoice.getBrokerageAmount()).compareTo(BigDecimal.ZERO) > 0) {
      Ledger brokerLedger = ledgerService.findOrCreateLedger("Broker " + invoice.getBroker().getName(), LedgerType.GENERAL);
      lines.add(new VoucherService.VoucherLineRequest(brokerageLedger, invoice.getBrokerageAmount(), BigDecimal.ZERO));
      lines.add(new VoucherService.VoucherLineRequest(brokerLedger, BigDecimal.ZERO, invoice.getBrokerageAmount()));
    }

    voucherService.createVoucher("PURCHASE_INVOICE", invoice.getId(),
        invoice.getInvoiceDate(), "Purchase invoice posting", lines);
    invoice.setStatus(DocumentStatus.POSTED);
    return purchaseInvoiceRepository.save(invoice);
  }

  private BigDecimal resolveSubtotal(Long invoiceId) {
    return purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoiceId).stream()
        .map(line -> defaultAmount(line.getAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void applyTotals(PurchaseInvoice invoice, BigDecimal subtotal, List<PurchaseInvoiceCharge> charges) {
    ChargeTotals totals = computeChargeTotals(charges);
    BigDecimal totalAmount = subtotal.add(totals.additions()).subtract(totals.deductions());
    BigDecimal tdsAmount = BigDecimal.ZERO;
    if (invoice.getSupplier() != null && invoice.getInvoiceDate() != null) {
      tdsAmount = tdsService.calculateTds(invoice.getSupplier().getId(), invoice.getInvoiceDate(), totalAmount);
    }
    BigDecimal netPayable = totalAmount.subtract(defaultAmount(tdsAmount));
    BigDecimal brokerageAmount = resolveBrokerageAmount(invoice.getBroker(), totalAmount);

    invoice.setSubtotal(subtotal);
    invoice.setTaxTotal(BigDecimal.ZERO);
    invoice.setRoundOff(BigDecimal.ZERO);
    invoice.setTotalAmount(totalAmount);
    invoice.setGrandTotal(totalAmount);
    invoice.setTdsAmount(tdsAmount);
    invoice.setNetPayable(netPayable);
    invoice.setBrokerageAmount(brokerageAmount);
  }

  private ChargeTotals computeChargeTotals(List<PurchaseInvoiceCharge> charges) {
    BigDecimal additions = BigDecimal.ZERO;
    BigDecimal deductions = BigDecimal.ZERO;
    if (charges != null) {
      for (PurchaseInvoiceCharge charge : charges) {
        BigDecimal amount = defaultAmount(charge.getAmount());
        if (charge.isDeduction()) {
          deductions = deductions.add(amount);
        } else {
          additions = additions.add(amount);
        }
      }
    }
    return new ChargeTotals(additions, deductions);
  }

  private List<VoucherService.VoucherLineRequest> buildChargePostings(List<PurchaseInvoiceCharge> charges,
                                                                      Ledger chargeLedger,
                                                                      Ledger deductionLedger) {
    List<VoucherService.VoucherLineRequest> postings = new ArrayList<>();
    if (charges == null) {
      return postings;
    }
    for (PurchaseInvoiceCharge charge : charges) {
      BigDecimal amount = defaultAmount(charge.getAmount());
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      Ledger targetLedger = ledgerForParty(charge.getPayablePartyType(), charge.getPayablePartyId());
      if (targetLedger == null) {
        targetLedger = charge.isDeduction() ? deductionLedger : chargeLedger;
      }
      if (charge.isDeduction()) {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, BigDecimal.ZERO, amount));
      } else {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, amount, BigDecimal.ZERO));
      }
    }
    return postings;
  }

  private PurchaseInvoiceLine buildLine(PurchaseInvoice invoice, GrnLine grnLine) {
    BigDecimal qty = resolveQty(grnLine);
    BigDecimal rate = grnLine.getRate() != null ? grnLine.getRate()
        : grnLine.getPurchaseOrderLine() != null ? grnLine.getPurchaseOrderLine().getRate() : BigDecimal.ZERO;
    BigDecimal amount = qty.multiply(rate != null ? rate : BigDecimal.ZERO);

    PurchaseInvoiceLine line = new PurchaseInvoiceLine();
    line.setPurchaseInvoice(invoice);
    line.setItem(grnLine.getItem());
    line.setUom(grnLine.getUom());
    line.setQuantity(qty);
    line.setRate(rate);
    line.setAmount(amount);
    return line;
  }

  private PurchaseInvoiceLine buildLine(PurchaseInvoice invoice, PurchaseOrderLine poLine) {
    BigDecimal qty = poLine.getQuantity() != null ? poLine.getQuantity() : BigDecimal.ZERO;
    BigDecimal rate = poLine.getRate() != null ? poLine.getRate() : BigDecimal.ZERO;
    BigDecimal amount = qty.multiply(rate);

    PurchaseInvoiceLine line = new PurchaseInvoiceLine();
    line.setPurchaseInvoice(invoice);
    line.setItem(poLine.getItem());
    line.setUom(poLine.getUom());
    line.setQuantity(qty);
    line.setRate(rate);
    line.setAmount(amount);
    return line;
  }

  private List<PurchaseInvoiceCharge> buildCharges(List<PurchaseInvoiceDtos.InvoiceChargeRequest> requests,
                                                   BigDecimal baseAmount) {
    if (requests == null || requests.isEmpty()) {
      return List.of();
    }
    List<PurchaseInvoiceCharge> charges = new ArrayList<>();
    for (PurchaseInvoiceDtos.InvoiceChargeRequest req : requests) {
      DeductionChargeType type = chargeTypeRepository.findById(req.chargeTypeId())
          .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
      CalcType calcType = req.calcType() != null ? CalcType.valueOf(req.calcType().toUpperCase())
          : type.getDefaultCalcType();
      BigDecimal rate = req.rate() != null ? req.rate() : type.getDefaultRate();
      BigDecimal amount = req.amount();
      if (amount == null) {
        if (calcType == CalcType.PERCENT && rate != null) {
          amount = baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (rate != null) {
          amount = rate.setScale(2, RoundingMode.HALF_UP);
        } else {
          amount = BigDecimal.ZERO;
        }
      }
      boolean isDeduction = req.isDeduction() != null ? req.isDeduction() : type.isDeduction();

      PurchaseInvoiceCharge charge = new PurchaseInvoiceCharge();
      charge.setPurchaseInvoice(invoice);
      charge.setChargeType(type);
      charge.setCalcType(calcType);
      charge.setRate(rate);
      charge.setAmount(amount);
      charge.setDeduction(isDeduction);
      charge.setPayablePartyType(PayablePartyType.valueOf(req.payablePartyType().toUpperCase()));
      charge.setPayablePartyId(req.payablePartyId());
      charge.setRemarks(req.remarks());
      charges.add(charge);
    }
    return charges;
  }

  private void persistCharges(PurchaseInvoice invoice, List<PurchaseInvoiceCharge> charges) {
    List<PurchaseInvoiceCharge> existing = purchaseInvoiceChargeRepository.findByPurchaseInvoiceId(invoice.getId());
    if (!existing.isEmpty()) {
      purchaseInvoiceChargeRepository.deleteAll(existing);
    }
    if (charges == null || charges.isEmpty()) {
      invoice.getCharges().clear();
      return;
    }
    for (PurchaseInvoiceCharge charge : charges) {
      charge.setPurchaseInvoice(invoice);
    }
    purchaseInvoiceChargeRepository.saveAll(charges);
    invoice.getCharges().clear();
    invoice.getCharges().addAll(charges);
  }

  private Ledger ledgerForParty(PayablePartyType partyType, Long partyId) {
    if (partyType == null || partyId == null) {
      return null;
    }
    return switch (partyType) {
      case SUPPLIER -> supplierRepository.findById(partyId)
          .map(supplier -> {
            if (supplier.getLedger() == null) {
              supplier.setLedger(ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId()));
              supplierRepository.save(supplier);
            }
            return supplier.getLedger();
          })
          .orElse(null);
      case BROKER -> brokerRepository.findById(partyId)
          .map(broker -> ledgerService.findOrCreateLedger("Broker " + broker.getName(), LedgerType.GENERAL))
          .orElse(null);
      case VEHICLE -> vehicleRepository.findById(partyId)
          .map(vehicle -> ledgerService.findOrCreateLedger("Vehicle " + vehicle.getVehicleNo(), LedgerType.EXPENSE))
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(partyId)
          .map(party -> {
            if (party.getLedger() == null) {
              party.setLedger(ledgerService.findOrCreateLedger(party.getName(), LedgerType.EXPENSE));
              expensePartyRepository.save(party);
            }
            return party.getLedger();
          })
          .orElse(null);
    };
  }

  private Ledger ensureSupplierLedger(Supplier supplier) {
    if (supplier == null) {
      return null;
    }
    Ledger ledger = supplier.getLedger();
    if (ledger == null) {
      ledger = ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId());
      supplier.setLedger(ledger);
      supplierRepository.save(supplier);
    }
    return ledger;
  }

  private BigDecimal resolveBrokerageAmount(Broker broker, BigDecimal baseAmount) {
    if (broker == null) {
      return BigDecimal.ZERO;
    }
    var rule = brokerCommissionRuleRepository.findFirstByBrokerId(broker.getId());
    if (rule.isEmpty() || rule.get().getRatePercent() == null) {
      return BigDecimal.ZERO;
    }
    return baseAmount.multiply(rule.get().getRatePercent())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  private String resolveInvoiceNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "PINV-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.nanoTime();
  }

  private BigDecimal resolveQty(GrnLine line) {
    if (line.getAcceptedQty() != null) {
      return line.getAcceptedQty();
    }
    if (line.getReceivedQty() != null) {
      return line.getReceivedQty();
    }
    if (line.getQuantity() != null) {
      return line.getQuantity();
    }
    if (line.getExpectedQty() != null) {
      return line.getExpectedQty();
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal defaultAmount(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private PurchaseOrder getPurchaseOrder(Long purchaseOrderId) {
    Company company = requireCompany();
    return purchaseOrderRepository.findByIdAndCompanyId(purchaseOrderId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
  }

  private PurchaseInvoice getInvoice(Long invoiceId) {
    Company company = requireCompany();
    return purchaseInvoiceRepository.findByIdAndPurchaseOrderCompanyId(invoiceId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase invoice not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  private String resolvePurchaseLedgerName(PurchaseInvoice invoice) {
    if (invoice.getPurchaseOrder() != null && invoice.getPurchaseOrder().getPurchaseLedger() != null
        && !invoice.getPurchaseOrder().getPurchaseLedger().isBlank()) {
      return invoice.getPurchaseOrder().getPurchaseLedger();
    }
    return "Purchase";
  }

  private record ChargeTotals(BigDecimal additions, BigDecimal deductions) {}
}
