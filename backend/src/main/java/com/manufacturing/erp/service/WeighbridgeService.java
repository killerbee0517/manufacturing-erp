package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manufacturing.erp.service.GrnService;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;
  private final VehicleRepository vehicleRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final GrnService grnService;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            SupplierRepository supplierRepository,
                            ItemRepository itemRepository,
                            VehicleRepository vehicleRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            GrnService grnService) {
    this.ticketRepository = ticketRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
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
    Item item = resolveItem(request.itemId());

    BigDecimal gross = request.grossWeight();
    BigDecimal unloaded = request.unloadedWeight() != null ? request.unloadedWeight() : BigDecimal.ZERO;
    BigDecimal net = unloaded.compareTo(BigDecimal.ZERO) > 0 ? gross.subtract(unloaded) : BigDecimal.ZERO;

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setSerialNo(resolveSerialNo(request.serialNo()));
    ticket.setVehicle(vehicle);
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setSecondDate(request.secondDate());
    ticket.setSecondTime(request.secondTime());
    ticket.setGrossWeight(gross);
    ticket.setUnloadedWeight(unloaded);
    ticket.setNetWeight(net);
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
    Item item = resolveItem(request.itemId());

    BigDecimal gross = request.grossWeight();
    BigDecimal unloaded = request.unloadedWeight() != null ? request.unloadedWeight() : BigDecimal.ZERO;
    BigDecimal net = unloaded.compareTo(BigDecimal.ZERO) > 0 ? gross.subtract(unloaded) : BigDecimal.ZERO;

    ticket.setSerialNo(resolveSerialNo(request.serialNo(), ticket.getSerialNo()));
    ticket.setVehicle(vehicle);
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setSecondDate(request.secondDate());
    ticket.setSecondTime(request.secondTime());
    ticket.setGrossWeight(gross);
    ticket.setUnloadedWeight(unloaded);
    ticket.setNetWeight(net);
    if (unloaded.compareTo(BigDecimal.ZERO) > 0) {
      ticket.setStatus(DocumentStatus.UNLOADED);
      grnService.createDraftFromWeighbridge(ticket);
    }

    return ticketRepository.save(ticket);
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

  private Item resolveItem(Long itemId) {
    if (itemId == null) {
      return null;
    }
    return itemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
  }
}
