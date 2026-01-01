package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Enums.PaymentDirection;
import com.manufacturing.erp.domain.Enums.PaymentMode;
import com.manufacturing.erp.domain.Enums.PaymentStatus;
import com.manufacturing.erp.domain.ExpenseParty;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.PaymentVoucher;
import com.manufacturing.erp.domain.PaymentVoucherAllocation;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.PaymentVoucherDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.PaymentVoucherAllocationRepository;
import com.manufacturing.erp.repository.PaymentVoucherRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentVoucherService {
  private final PaymentVoucherRepository paymentVoucherRepository;
  private final PaymentVoucherAllocationRepository allocationRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final BankRepository bankRepository;
  private final LedgerService ledgerService;
  private final VoucherService voucherService;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public PaymentVoucherService(PaymentVoucherRepository paymentVoucherRepository,
                               PaymentVoucherAllocationRepository allocationRepository,
                               PurchaseInvoiceRepository purchaseInvoiceRepository,
                               SupplierRepository supplierRepository,
                               BrokerRepository brokerRepository,
                               ExpensePartyRepository expensePartyRepository,
                               BankRepository bankRepository,
                               LedgerService ledgerService,
                               VoucherService voucherService,
                               CompanyRepository companyRepository,
                               CompanyContext companyContext) {
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.allocationRepository = allocationRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.bankRepository = bankRepository;
    this.ledgerService = ledgerService;
    this.voucherService = voucherService;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional
  public PaymentVoucher create(PaymentVoucherDtos.PaymentVoucherRequest request) {
    Company company = requireCompany();
    PaymentVoucher voucher = new PaymentVoucher();
    voucher.setCompany(company);
    voucher.setVoucherNo(resolveVoucherNo(null));
    applyRequest(voucher, request);
    voucher.setStatus(PaymentStatus.DRAFT);
    PaymentVoucher saved = paymentVoucherRepository.save(voucher);
    persistAllocations(saved, request.allocations());
    return saved;
  }

  @Transactional
  public PaymentVoucher update(Long id, PaymentVoucherDtos.PaymentVoucherRequest request) {
    PaymentVoucher voucher = getVoucher(id);
    if (voucher.getStatus() != PaymentStatus.DRAFT) {
      throw new IllegalStateException("Only draft payment vouchers can be edited");
    }
    applyRequest(voucher, request);
    PaymentVoucher saved = paymentVoucherRepository.save(voucher);
    persistAllocations(saved, request.allocations());
    return saved;
  }

  @Transactional
  public PaymentVoucher post(Long id) {
    PaymentVoucher voucher = getVoucher(id);
    if (voucher.getStatus() == PaymentStatus.POSTED || voucher.getStatus() == PaymentStatus.PDC_CLEARED) {
      return voucher;
    }
    if (voucher.getPaymentMode() == PaymentMode.PDC) {
      voucher.setStatus(PaymentStatus.PDC_ISSUED);
      return paymentVoucherRepository.save(voucher);
    }
    postLedgerEntries(voucher, "PAYMENT_VOUCHER");
    voucher.setStatus(PaymentStatus.POSTED);
    return paymentVoucherRepository.save(voucher);
  }

  @Transactional
  public PaymentVoucher clearPdc(Long id) {
    PaymentVoucher voucher = getVoucher(id);
    if (voucher.getPaymentMode() != PaymentMode.PDC) {
      throw new IllegalStateException("Only PDC vouchers can be cleared");
    }
    if (voucher.getStatus() == PaymentStatus.PDC_CLEARED) {
      return voucher;
    }
    if (voucher.getStatus() != PaymentStatus.PDC_ISSUED) {
      throw new IllegalStateException("PDC voucher must be issued before clearance");
    }
    postLedgerEntries(voucher, "PAYMENT_PDC_CLEAR");
    voucher.setStatus(PaymentStatus.PDC_CLEARED);
    return paymentVoucherRepository.save(voucher);
  }

  private void applyRequest(PaymentVoucher voucher, PaymentVoucherDtos.PaymentVoucherRequest request) {
    if (request == null) {
      return;
    }
    voucher.setVoucherDate(request.voucherDate());
    voucher.setPartyType(PayablePartyType.valueOf(request.partyType().toUpperCase()));
    voucher.setPartyId(request.partyId());
    voucher.setPaymentDirection(PaymentDirection.valueOf(request.paymentDirection().toUpperCase()));
    voucher.setPaymentMode(PaymentMode.valueOf(request.paymentMode().toUpperCase()));
    voucher.setAmount(request.amount());
    voucher.setNarration(request.narration());
    voucher.setChequeNumber(request.chequeNumber());
    voucher.setChequeDate(request.chequeDate());
    if (voucher.getPaymentMode() != PaymentMode.CASH) {
      if (request.bankId() == null) {
        throw new IllegalArgumentException("Bank is required for bank or PDC payments");
      }
      Bank bank = bankRepository.findById(request.bankId())
          .orElseThrow(() -> new IllegalArgumentException("Bank not found"));
      voucher.setBank(bank);
    } else {
      voucher.setBank(null);
    }
    if (voucher.getPaymentMode() == PaymentMode.PDC) {
      if (voucher.getChequeNumber() == null || voucher.getChequeNumber().isBlank()) {
        throw new IllegalArgumentException("Cheque number is required for PDC");
      }
      if (voucher.getChequeDate() == null) {
        throw new IllegalArgumentException("Cheque date is required for PDC");
      }
    }
    validateParty(voucher.getPartyType(), voucher.getPartyId());
    validateAllocations(voucher, request.allocations());
  }

  private void validateAllocations(PaymentVoucher voucher, List<PaymentVoucherDtos.AllocationRequest> allocations) {
    if (allocations == null || allocations.isEmpty()) {
      return;
    }
    BigDecimal total = allocations.stream()
        .map(allocation -> allocation.allocatedAmount() != null ? allocation.allocatedAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (voucher.getAmount() != null && total.compareTo(voucher.getAmount()) > 0) {
      throw new IllegalArgumentException("Allocated amount cannot exceed voucher amount");
    }
  }

  private void persistAllocations(PaymentVoucher voucher, List<PaymentVoucherDtos.AllocationRequest> requests) {
    List<PaymentVoucherAllocation> existing = allocationRepository.findByPaymentVoucherId(voucher.getId());
    if (!existing.isEmpty()) {
      allocationRepository.deleteAll(existing);
    }
    if (requests == null || requests.isEmpty()) {
      voucher.getAllocations().clear();
      return;
    }
    List<PaymentVoucherAllocation> allocations = new ArrayList<>();
    for (PaymentVoucherDtos.AllocationRequest request : requests) {
      PaymentVoucherAllocation allocation = new PaymentVoucherAllocation();
      allocation.setPaymentVoucher(voucher);
      if (request.purchaseInvoiceId() != null) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(request.purchaseInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found"));
        if (voucher.getPartyType() == PayablePartyType.SUPPLIER
            && invoice.getSupplier() != null
            && !invoice.getSupplier().getId().equals(voucher.getPartyId())) {
          throw new IllegalArgumentException("Invoice supplier does not match payment party");
        }
        allocation.setPurchaseInvoice(invoice);
      }
      allocation.setAllocatedAmount(request.allocatedAmount() != null ? request.allocatedAmount() : BigDecimal.ZERO);
      allocation.setRemarks(request.remarks());
      allocations.add(allocation);
    }
    allocationRepository.saveAll(allocations);
    voucher.getAllocations().clear();
    voucher.getAllocations().addAll(allocations);
  }

  private void postLedgerEntries(PaymentVoucher voucher, String referenceType) {
    Ledger bankLedger = resolveBankLedger(voucher);
    Ledger partyLedger = resolvePartyLedger(voucher.getPartyType(), voucher.getPartyId());
    if (bankLedger == null || partyLedger == null) {
      throw new IllegalStateException("Bank and party ledger are required for posting");
    }
    BigDecimal amount = voucher.getAmount() != null ? voucher.getAmount() : BigDecimal.ZERO;
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Voucher amount must be positive");
    }
    List<VoucherService.VoucherLineRequest> lines = new ArrayList<>();
    if (voucher.getPaymentDirection() == PaymentDirection.PAYABLE) {
      lines.add(new VoucherService.VoucherLineRequest(partyLedger, amount, BigDecimal.ZERO));
      lines.add(new VoucherService.VoucherLineRequest(bankLedger, BigDecimal.ZERO, amount));
    } else {
      lines.add(new VoucherService.VoucherLineRequest(bankLedger, amount, BigDecimal.ZERO));
      lines.add(new VoucherService.VoucherLineRequest(partyLedger, BigDecimal.ZERO, amount));
    }
    voucherService.createVoucher(referenceType, voucher.getId(),
        voucher.getVoucherDate() != null ? voucher.getVoucherDate() : LocalDate.now(),
        "Payment voucher", lines);
  }

  private Ledger resolvePartyLedger(PayablePartyType partyType, Long partyId) {
    if (partyType == null || partyId == null) {
      return null;
    }
    return switch (partyType) {
      case SUPPLIER -> supplierRepository.findById(partyId)
          .map(this::ensureSupplierLedger)
          .orElse(null);
      case BROKER -> brokerRepository.findById(partyId)
          .map(broker -> ledgerService.findOrCreateLedger("Broker " + broker.getName(), LedgerType.GENERAL))
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(partyId)
          .map(this::ensureExpenseLedger)
          .orElse(null);
      case VEHICLE -> null;
    };
  }

  private void validateParty(PayablePartyType partyType, Long partyId) {
    if (partyType == null || partyId == null) {
      throw new IllegalArgumentException("Party is required");
    }
    switch (partyType) {
      case SUPPLIER -> supplierRepository.findById(partyId)
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
      case BROKER -> brokerRepository.findById(partyId)
          .orElseThrow(() -> new IllegalArgumentException("Broker not found"));
      case EXPENSE -> expensePartyRepository.findById(partyId)
          .orElseThrow(() -> new IllegalArgumentException("Expense party not found"));
      case VEHICLE -> throw new IllegalArgumentException("Vehicle payments are not supported yet");
    }
  }

  private Ledger resolveBankLedger(PaymentVoucher voucher) {
    if (voucher.getPaymentMode() == PaymentMode.CASH) {
      return ledgerService.findOrCreateLedger("Cash", LedgerType.GENERAL);
    }
    Bank bank = voucher.getBank();
    if (bank == null) {
      return null;
    }
    String name = bank.getName();
    if (bank.getBranch() != null && !bank.getBranch().isBlank()) {
      name = name + " - " + bank.getBranch();
    }
    return ledgerService.findOrCreateLedger(name, LedgerType.BANK);
  }

  private Ledger ensureSupplierLedger(Supplier supplier) {
    Ledger ledger = supplier.getLedger();
    if (ledger == null) {
      ledger = ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId());
      supplier.setLedger(ledger);
      supplierRepository.save(supplier);
    }
    return ledger;
  }

  private Ledger ensureExpenseLedger(ExpenseParty party) {
    if (party.getLedger() != null) {
      return party.getLedger();
    }
    Ledger ledger = ledgerService.findOrCreateLedger(party.getName(), LedgerType.EXPENSE);
    party.setLedger(ledger);
    expensePartyRepository.save(party);
    return ledger;
  }

  private PaymentVoucher getVoucher(Long id) {
    Company company = requireCompany();
    return paymentVoucherRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment voucher not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  private String resolveVoucherNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "PV-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        + "-" + System.nanoTime();
  }
}
