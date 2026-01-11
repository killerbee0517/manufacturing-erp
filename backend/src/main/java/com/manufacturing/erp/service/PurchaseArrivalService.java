package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.PurchaseArrival;
import com.manufacturing.erp.domain.PurchaseArrivalCharge;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.PurchaseArrivalDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.PurchaseArrivalChargeRepository;
import com.manufacturing.erp.repository.PurchaseArrivalRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PurchaseArrivalService {
  private final PurchaseArrivalRepository purchaseArrivalRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final GodownRepository godownRepository;
  private final LedgerService ledgerService;
  private final VoucherService voucherService;
  private final SupplierRepository supplierRepository;
  private final DeductionChargeTypeRepository chargeTypeRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final PurchaseArrivalChargeRepository purchaseArrivalChargeRepository;
  private final BrokerRepository brokerRepository;
  private final VehicleRepository vehicleRepository;
  private final QcInspectionRepository qcInspectionRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public PurchaseArrivalService(PurchaseArrivalRepository purchaseArrivalRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                WeighbridgeTicketRepository weighbridgeTicketRepository,
                                GodownRepository godownRepository,
                                LedgerService ledgerService,
                                VoucherService voucherService,
                                SupplierRepository supplierRepository,
                                DeductionChargeTypeRepository chargeTypeRepository,
                                ExpensePartyRepository expensePartyRepository,
                                PurchaseArrivalChargeRepository purchaseArrivalChargeRepository,
                                BrokerRepository brokerRepository,
                                VehicleRepository vehicleRepository,
                                QcInspectionRepository qcInspectionRepository,
                                BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                                CompanyRepository companyRepository,
                                CompanyContext companyContext) {
    this.purchaseArrivalRepository = purchaseArrivalRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.godownRepository = godownRepository;
    this.ledgerService = ledgerService;
    this.voucherService = voucherService;
    this.supplierRepository = supplierRepository;
    this.chargeTypeRepository = chargeTypeRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.purchaseArrivalChargeRepository = purchaseArrivalChargeRepository;
    this.brokerRepository = brokerRepository;
    this.vehicleRepository = vehicleRepository;
    this.qcInspectionRepository = qcInspectionRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional
  public PurchaseArrival createArrival(PurchaseArrivalDtos.CreatePurchaseArrivalRequest request) {
    var company = requireCompany();
    PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndCompanyId(request.purchaseOrderId(), company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    if (request.weighbridgeTicketId() == null) {
      throw new IllegalArgumentException("Weighbridge ticket is required before purchase arrival");
    }
    WeighbridgeTicket ticket = weighbridgeTicketRepository.findById(request.weighbridgeTicketId())
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    if (ticket.getStatus() != DocumentStatus.UNLOADED) {
      throw new IllegalStateException("Weighbridge ticket must be unloaded before purchase arrival");
    }
    if (!qcInspectionRepository.existsByWeighbridgeTicketIdAndStatus(ticket.getId(), QcStatus.APPROVED)) {
      throw new IllegalStateException("QC approval is required before purchase arrival");
    }
    Godown godown = godownRepository.findById(request.godownId())
        .orElseThrow(() -> new IllegalArgumentException("Godown not found"));
    Broker broker = request.brokerId() != null
        ? brokerRepository.findById(request.brokerId())
            .orElseThrow(() -> new IllegalArgumentException("Broker not found"))
        : null;

    BigDecimal grossAmount = purchaseOrder.getTotalAmount() != null ? purchaseOrder.getTotalAmount() : BigDecimal.ZERO;
    Ledger chargeLedger = ledgerService.findOrCreateLedger("Purchase Charges", LedgerType.EXPENSE);
    Ledger deductionLedger = ledgerService.findOrCreateLedger("Purchase Deductions", LedgerType.GENERAL);
    ChargeComputationResult chargeResult = computeCharges(request.charges(), grossAmount, chargeLedger, deductionLedger);
    BigDecimal netPayable = grossAmount
        .add(chargeResult.totalAdditions())
        .subtract(chargeResult.totalDeductions());

    PurchaseArrival arrival = new PurchaseArrival();
    arrival.setPurchaseOrder(purchaseOrder);
    arrival.setWeighbridgeTicket(ticket);
    arrival.setBroker(broker);
    BigDecimal brokerageAmount = resolveBrokerageAmount(broker, grossAmount);
    arrival.setBrokerageAmount(brokerageAmount);
    arrival.setGodown(godown);
    arrival.setGrossAmount(grossAmount);
    arrival.setNetPayable(netPayable);
    arrival.setCreatedAt(Instant.now());

    PurchaseArrival saved = purchaseArrivalRepository.save(arrival);
    persistCharges(saved, chargeResult.charges());

    if (!chargeResult.postings().isEmpty()) {
      voucherService.createVoucher("PURCHASE_ARRIVAL", saved.getId(), saved.getCreatedAt() != null
          ? saved.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
          : java.time.LocalDate.now(),
          "Purchase arrival charges", chargeResult.postings().stream()
          .map(posting -> new VoucherService.VoucherLineRequest(posting.ledger(), posting.drAmount(), posting.crAmount()))
          .toList());
    }

    return saved;
  }

  private BigDecimal defaultAmount(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private ChargeComputationResult computeCharges(List<PurchaseArrivalDtos.PurchaseArrivalChargeRequest> requests,
                                                 BigDecimal baseAmount,
                                                 Ledger chargeLedger,
                                                 Ledger deductionLedger) {
    if (requests == null || requests.isEmpty()) {
      return new ChargeComputationResult(BigDecimal.ZERO, BigDecimal.ZERO, java.util.Collections.emptyList(),
          java.util.Collections.emptyList());
    }
    BigDecimal additions = BigDecimal.ZERO;
    BigDecimal deductions = BigDecimal.ZERO;
    List<PurchaseArrivalCharge> charges = new java.util.ArrayList<>();
    List<ChargePosting> postings = new java.util.ArrayList<>();

    Ledger chargePayableLedger = ledgerService.findOrCreateLedger("Purchase Charges Payable", LedgerType.GENERAL);
    Ledger deductionPayableLedger = ledgerService.findOrCreateLedger("Purchase Deductions Payable", LedgerType.GENERAL);

    for (PurchaseArrivalDtos.PurchaseArrivalChargeRequest req : requests) {
      var type = chargeTypeRepository.findById(req.chargeTypeId())
          .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
      var calcType = req.calcType() != null ? com.manufacturing.erp.domain.Enums.CalcType.valueOf(req.calcType().toUpperCase())
          : type.getDefaultCalcType();
      BigDecimal rate = req.rate() != null ? req.rate() : type.getDefaultRate();
      BigDecimal amount = req.amount();
      if (amount == null) {
        if (calcType == com.manufacturing.erp.domain.Enums.CalcType.PERCENT && rate != null) {
          amount = baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (rate != null) {
          amount = rate.setScale(2, RoundingMode.HALF_UP);
        } else {
          amount = BigDecimal.ZERO;
        }
      }
      boolean isDeduction = req.isDeduction() != null ? req.isDeduction() : type.isDeduction();

      PurchaseArrivalCharge charge = new PurchaseArrivalCharge();
      charge.setChargeType(type);
      charge.setCalcType(calcType);
      charge.setRate(rate);
      charge.setAmount(amount);
      charge.setDeduction(isDeduction);
      charge.setPayablePartyType(PayablePartyType.valueOf(req.payablePartyType().toUpperCase()));
      charge.setPayablePartyId(req.payablePartyId());
      charge.setRemarks(req.remarks());
      charges.add(charge);

      if (isDeduction) {
        deductions = deductions.add(amount);
      } else {
        additions = additions.add(amount);
      }
      if (amount.compareTo(BigDecimal.ZERO) != 0) {
        Ledger partyLedger = ledgerForParty(charge.getPayablePartyType(), charge.getPayablePartyId());
        if (partyLedger == null) {
          partyLedger = isDeduction ? deductionPayableLedger : chargePayableLedger;
        }
        Ledger expenseLedger = isDeduction ? deductionLedger : chargeLedger;
        if (isDeduction) {
          postings.add(new ChargePosting(partyLedger, amount, BigDecimal.ZERO));
          postings.add(new ChargePosting(expenseLedger, BigDecimal.ZERO, amount));
        } else {
          postings.add(new ChargePosting(expenseLedger, amount, BigDecimal.ZERO));
          postings.add(new ChargePosting(partyLedger, BigDecimal.ZERO, amount));
        }
      }
    }
    return new ChargeComputationResult(additions, deductions, charges, postings);
  }

  private void persistCharges(PurchaseArrival arrival, List<PurchaseArrivalCharge> charges) {
    if (charges == null || charges.isEmpty()) {
      return;
    }
    for (PurchaseArrivalCharge charge : charges) {
      charge.setPurchaseArrival(arrival);
    }
    purchaseArrivalChargeRepository.saveAll(charges);
    arrival.getCharges().clear();
    arrival.getCharges().addAll(charges);
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
      case CUSTOMER -> null;
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
      default -> null;
    };
  }

  private record ChargeComputationResult(BigDecimal totalAdditions, BigDecimal totalDeductions,
                                         List<PurchaseArrivalCharge> charges, List<ChargePosting> postings) {}

  private record ChargePosting(Ledger ledger, BigDecimal drAmount, BigDecimal crAmount) {}

  private BigDecimal resolveBrokerageAmount(Broker broker, BigDecimal baseAmount) {
    if (broker == null || baseAmount == null) {
      return BigDecimal.ZERO;
    }
    var rule = brokerCommissionRuleRepository.findFirstByBrokerId(broker.getId());
    if (rule.isEmpty() || rule.get().getRatePercent() == null) {
      return BigDecimal.ZERO;
    }
    return baseAmount.multiply(rule.get().getRatePercent())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  private com.manufacturing.erp.domain.Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }
}
