package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manufacturing.erp.service.GrnService;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final VehicleRepository vehicleRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final GrnService grnService;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            VehicleRepository vehicleRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            GrnService grnService) {
    this.ticketRepository = ticketRepository;
    this.vehicleRepository = vehicleRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.grnService = grnService;
  }

  @Transactional
  public WeighbridgeTicket createTicket(WeighbridgeDtos.CreateTicketRequest request) {
    PurchaseOrder po = purchaseOrderRepository.findById(request.poId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = po.getSupplier();

    Item item = po.getLines().isEmpty() ? null : po.getLines().get(0).getItem();

    BigDecimal gross = request.grossWeight();

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setSerialNo(resolveSerialNo(request.serialNo()));
    ticket.setVehicle(vehicle);
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setGrossWeight(gross);
    ticket.setStatus(DocumentStatus.IN_PROGRESS);

    return ticketRepository.save(ticket);
  }

  @Transactional
  public WeighbridgeTicket updateTicket(Long id, WeighbridgeDtos.CreateTicketRequest request) {
    WeighbridgeTicket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    PurchaseOrder po = purchaseOrderRepository.findById(request.poId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = po.getSupplier();
    Item item = po.getLines().isEmpty() ? null : po.getLines().get(0).getItem();

    ticket.setSerialNo(resolveSerialNo(request.serialNo(), ticket.getSerialNo()));
    ticket.setVehicle(vehicle);
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setGrossWeight(request.grossWeight());
    ticket.setNetWeight(ticket.getUnloadedWeight() != null ? ticket.getGrossWeight().subtract(ticket.getUnloadedWeight()) : null);

    return ticketRepository.save(ticket);
  }

  @Transactional
  public WeighbridgeTicket unload(Long id, WeighbridgeDtos.UnloadTicketRequest request) {
    WeighbridgeTicket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    PurchaseOrder po = purchaseOrderRepository.findById(request.poId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    ticket.setPurchaseOrder(po);
    ticket.setVehicle(vehicle);
    ticket.setSecondDate(request.secondDate());
    ticket.setSecondTime(request.secondTime());
    ticket.setUnloadedWeight(request.unloadedWeight());
    BigDecimal net = request.unloadedWeight() != null && ticket.getGrossWeight() != null
        ? ticket.getGrossWeight().subtract(request.unloadedWeight())
        : null;
    ticket.setNetWeight(net);
    ticket.setStatus(DocumentStatus.UNLOADED);
    WeighbridgeTicket saved = ticketRepository.save(ticket);
    grnService.createDraftFromWeighbridge(saved);
    return saved;
  }

  private String resolveSerialNo(String provided) {
    return resolveSerialNo(provided, null);
  }

  private String resolveSerialNo(String provided, String fallback) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    return "WB-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        + "-" + System.nanoTime();
  }

}
