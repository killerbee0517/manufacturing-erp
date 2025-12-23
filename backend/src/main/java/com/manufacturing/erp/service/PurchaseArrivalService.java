package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.PurchaseArrival;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.PurchaseArrivalDtos;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.PurchaseArrivalRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseArrivalService {
  private final PurchaseArrivalRepository purchaseArrivalRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final GodownRepository godownRepository;
  private final StockLedgerService stockLedgerService;

  public PurchaseArrivalService(PurchaseArrivalRepository purchaseArrivalRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                WeighbridgeTicketRepository weighbridgeTicketRepository,
                                GodownRepository godownRepository,
                                StockLedgerService stockLedgerService) {
    this.purchaseArrivalRepository = purchaseArrivalRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.godownRepository = godownRepository;
    this.stockLedgerService = stockLedgerService;
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
    BigDecimal netPayable = grossAmount.add(unloadingCharges).subtract(deductions).subtract(tdsAmount);

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

    for (PurchaseOrderLine line : purchaseOrder.getLines()) {
      BigDecimal quantity = line.getQuantity();
      BigDecimal weight = quantity != null ? quantity : BigDecimal.ZERO;
      stockLedgerService.postEntry("PURCHASE_ARRIVAL", saved.getId(), line.getId(), LedgerTxnType.IN,
          line.getItem(), line.getUom(), null, null, null, godown,
          quantity != null ? quantity : BigDecimal.ZERO,
          weight,
          StockStatus.UNRESTRICTED);
    }

    return saved;
  }

  private BigDecimal defaultAmount(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
