package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.PaymentVoucher;
import com.manufacturing.erp.dto.PaymentVoucherDtos;
import com.manufacturing.erp.repository.PaymentVoucherAllocationRepository;
import com.manufacturing.erp.repository.PaymentVoucherRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.service.PaymentVoucherService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payment-vouchers")
public class PaymentVoucherController {
  private final PaymentVoucherService paymentVoucherService;
  private final PaymentVoucherRepository paymentVoucherRepository;
  private final PaymentVoucherAllocationRepository allocationRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final CompanyContext companyContext;

  public PaymentVoucherController(PaymentVoucherService paymentVoucherService,
                                  PaymentVoucherRepository paymentVoucherRepository,
                                  PaymentVoucherAllocationRepository allocationRepository,
                                  SupplierRepository supplierRepository,
                                  BrokerRepository brokerRepository,
                                  ExpensePartyRepository expensePartyRepository,
                                  CompanyContext companyContext) {
    this.paymentVoucherService = paymentVoucherService;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.allocationRepository = allocationRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.companyContext = companyContext;
  }

  @GetMapping
  public Page<PaymentVoucherDtos.PaymentVoucherResponse> list(Pageable pageable) {
    Long companyId = requireCompanyId();
    return paymentVoucherRepository.findByCompanyId(companyId, pageable).map(this::toResponse);
  }

  @GetMapping("/{id}")
  public PaymentVoucherDtos.PaymentVoucherResponse get(@PathVariable Long id) {
    Long companyId = requireCompanyId();
    PaymentVoucher voucher = paymentVoucherRepository.findByIdAndCompanyId(id, companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment voucher not found"));
    return toResponse(voucher);
  }

  @PostMapping
  public PaymentVoucherDtos.PaymentVoucherResponse create(@RequestBody PaymentVoucherDtos.PaymentVoucherRequest request) {
    return toResponse(paymentVoucherService.create(request));
  }

  @PutMapping("/{id}")
  public PaymentVoucherDtos.PaymentVoucherResponse update(@PathVariable Long id,
                                                          @RequestBody PaymentVoucherDtos.PaymentVoucherRequest request) {
    return toResponse(paymentVoucherService.update(id, request));
  }

  @PostMapping("/{id}/post")
  public PaymentVoucherDtos.PaymentVoucherResponse post(@PathVariable Long id) {
    return toResponse(paymentVoucherService.post(id));
  }

  @PostMapping("/{id}/clear-pdc")
  public PaymentVoucherDtos.PaymentVoucherResponse clearPdc(@PathVariable Long id) {
    return toResponse(paymentVoucherService.clearPdc(id));
  }

  private PaymentVoucherDtos.PaymentVoucherResponse toResponse(PaymentVoucher voucher) {
    var allocations = allocationRepository.findByPaymentVoucherId(voucher.getId());
    List<PaymentVoucherDtos.AllocationResponse> allocationResponses = allocations.stream()
        .map(allocation -> new PaymentVoucherDtos.AllocationResponse(
            allocation.getId(),
            allocation.getPurchaseInvoice() != null ? allocation.getPurchaseInvoice().getId() : null,
            allocation.getPurchaseInvoice() != null ? allocation.getPurchaseInvoice().getInvoiceNo() : null,
            allocation.getAllocatedAmount(),
            allocation.getRemarks()))
        .toList();
    return new PaymentVoucherDtos.PaymentVoucherResponse(
        voucher.getId(),
        voucher.getVoucherNo(),
        voucher.getVoucherDate(),
        voucher.getPartyType() != null ? voucher.getPartyType().name() : null,
        voucher.getPartyId(),
        resolvePartyName(voucher),
        voucher.getPaymentDirection() != null ? voucher.getPaymentDirection().name() : null,
        voucher.getPaymentMode() != null ? voucher.getPaymentMode().name() : null,
        voucher.getBank() != null ? voucher.getBank().getId() : null,
        voucher.getBank() != null ? voucher.getBank().getName() : null,
        voucher.getAmount(),
        voucher.getNarration(),
        voucher.getStatus() != null ? voucher.getStatus().name() : null,
        voucher.getChequeNumber(),
        voucher.getChequeDate(),
        allocationResponses
    );
  }

  private String resolvePartyName(PaymentVoucher voucher) {
    if (voucher.getPartyType() == null) {
      return null;
    }
    return switch (voucher.getPartyType()) {
      case SUPPLIER -> supplierRepository.findById(voucher.getPartyId())
          .map(supplier -> supplier.getName())
          .orElse(null);
      case BROKER -> brokerRepository.findById(voucher.getPartyId())
          .map(broker -> broker.getName())
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(voucher.getPartyId())
          .map(party -> party.getName())
          .orElse(null);
      case VEHICLE -> null;
    };
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyId;
  }
}
