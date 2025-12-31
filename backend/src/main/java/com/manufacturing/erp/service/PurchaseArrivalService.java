package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.PurchaseArrival;
import com.manufacturing.erp.domain.PurchaseArrivalCharge;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.PurchaseArrivalDtos;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.PurchaseArrivalChargeRepository;
import com.manufacturing.erp.repository.PurchaseArrivalRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                                VehicleRepository vehicleRepository) {
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
  }

  @Transactional
  public PurchaseArrival createArrival(PurchaseArrivalDtos.CreatePurchaseArrivalRequest request) {
    PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(request.purchaseOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    WeighbridgeTicket ticket = request.weighbridgeTicketId() != null
        ? weighbridgeTicketRepository.findById(request.weighbridgeTicketId())
            .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"))
        : null;
    Godown godown = godownRepository.findById(request.godownId())
        .orElseThrow(() -> new IllegalArgumentException("Godown not found"));

    BigDecimal unloadingCharges = defaultAmount(request.unloadingCharges());
    BigDecimal deductions = defaultAmount(request.deductions());
    BigDecimal tdsPercent = defaultAmount(request.tdsPercent());
    BigDecimal grossAmount = purchaseOrder.getTotalAmount() != null ? purchaseOrder.getTotalAmount() : BigDecimal.ZERO;
    BigDecimal tdsAmount = grossAmount.multiply(tdsPercent)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    ChargeComputationResult chargeResult = computeCharges(request.charges(), grossAmount);
    BigDecimal netPayable = grossAmount
        .add(unloadingCharges)
        .add(chargeResult.totalAdditions())
        .subtract(deductions)
        .subtract(chargeResult.totalDeductions())
        .subtract(tdsAmount);

    PurchaseArrival arrival = new PurchaseArrival();
    arrival.setPurchaseOrder(purchaseOrder);
    arrival.setWeighbridgeTicket(ticket);
    arrival.setGodown(godown);
    arrival.setUnloadingCharges(unloadingCharges);
    arrival.setDeductions(deductions);
    arrival.setTdsPercent(tdsPercent);
    arrival.setGrossAmount(grossAmount);
    arrival.setNetPayable(netPayable);
    arrival.setCreatedAt(Instant.now());

    PurchaseArrival saved = purchaseArrivalRepository.save(arrival);
    persistCharges(saved, chargeResult.charges());

    Ledger supplierLedger = purchaseOrder.getSupplier() != null ? purchaseOrder.getSupplier().getLedger() : null;
    if (supplierLedger == null && purchaseOrder.getSupplier() != null) {
      supplierLedger = ledgerService.createLedger(purchaseOrder.getSupplier().getName(), LedgerType.SUPPLIER,
          "SUPPLIER", purchaseOrder.getSupplier().getId());
      purchaseOrder.getSupplier().setLedger(supplierLedger);
      supplierRepository.save(purchaseOrder.getSupplier());
    }

    Ledger purchaseLedger = ledgerService.findOrCreateLedger(
        purchaseOrder.getPurchaseLedger() != null && !purchaseOrder.getPurchaseLedger().isBlank()
            ? purchaseOrder.getPurchaseLedger()
            : "Purchase",
        LedgerType.EXPENSE);
    Ledger unloadingLedger = ledgerService.findOrCreateLedger("Unloading Expense", LedgerType.EXPENSE);
    Ledger deductionLedger = ledgerService.findOrCreateLedger("Purchase Deductions", LedgerType.GENERAL);
    Ledger tdsLedger = ledgerService.findOrCreateLedger("TDS Payable", LedgerType.GENERAL);

    List<VoucherService.VoucherLineRequest> lines = new java.util.ArrayList<>();
    lines.add(new VoucherService.VoucherLineRequest(purchaseLedger, grossAmount, BigDecimal.ZERO));
    if (unloadingCharges.compareTo(BigDecimal.ZERO) > 0) {
      lines.add(new VoucherService.VoucherLineRequest(unloadingLedger, unloadingCharges, BigDecimal.ZERO));
    }
    if (deductions.compareTo(BigDecimal.ZERO) > 0) {
      lines.add(new VoucherService.VoucherLineRequest(deductionLedger, BigDecimal.ZERO, deductions));
    }
    for (ChargePosting chargePosting : chargeResult.postings()) {
      lines.add(new VoucherService.VoucherLineRequest(chargePosting.ledger(), chargePosting.drAmount(), chargePosting.crAmount()));
    }
    if (tdsAmount.compareTo(BigDecimal.ZERO) > 0) {
      lines.add(new VoucherService.VoucherLineRequest(tdsLedger, BigDecimal.ZERO, tdsAmount));
    }
    if (supplierLedger != null) {
      lines.add(new VoucherService.VoucherLineRequest(supplierLedger, BigDecimal.ZERO, netPayable));
    }

    if (supplierLedger != null) {
      voucherService.createVoucher("PURCHASE_ARRIVAL", saved.getId(), saved.getCreatedAt() != null
          ? saved.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
          : java.time.LocalDate.now(),
          "Purchase arrival posting", lines);
    }

    return saved;
  }

  private BigDecimal defaultAmount(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private ChargeComputationResult computeCharges(List<PurchaseArrivalDtos.PurchaseArrivalChargeRequest> requests,
                                                 BigDecimal baseAmount) {
    if (requests == null || requests.isEmpty()) {
      return new ChargeComputationResult(BigDecimal.ZERO, BigDecimal.ZERO, java.util.Collections.emptyList(),
          java.util.Collections.emptyList());
    }
    BigDecimal additions = BigDecimal.ZERO;
    BigDecimal deductions = BigDecimal.ZERO;
    List<PurchaseArrivalCharge> charges = new java.util.ArrayList<>();
    List<ChargePosting> postings = new java.util.ArrayList<>();

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

      Ledger partyLedger = ledgerForParty(charge.getPayablePartyType(), charge.getPayablePartyId());
      if (partyLedger != null && amount.compareTo(BigDecimal.ZERO) != 0) {
        if (isDeduction) {
          deductions = deductions.add(amount);
          postings.add(new ChargePosting(partyLedger, BigDecimal.ZERO, amount));
        } else {
          additions = additions.add(amount);
          postings.add(new ChargePosting(partyLedger, amount, BigDecimal.ZERO));
        }
      } else {
        if (isDeduction) {
          deductions = deductions.add(amount);
        } else {
          additions = additions.add(amount);
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

  private record ChargeComputationResult(BigDecimal totalAdditions, BigDecimal totalDeductions,
                                         List<PurchaseArrivalCharge> charges, List<ChargePosting> postings) {}

  private record ChargePosting(Ledger ledger, BigDecimal drAmount, BigDecimal crAmount) {}
}
