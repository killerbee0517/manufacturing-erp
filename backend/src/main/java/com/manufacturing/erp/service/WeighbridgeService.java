package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;
  private final VehicleRepository vehicleRepository;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            SupplierRepository supplierRepository,
                            ItemRepository itemRepository,
                            VehicleRepository vehicleRepository) {
    this.ticketRepository = ticketRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional
  public WeighbridgeTicket createTicket(WeighbridgeDtos.CreateTicketRequest request) {
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

    BigDecimal gross = request.grossWeight();
    BigDecimal unloaded = request.unloadedWeight();
    BigDecimal net = gross.subtract(unloaded);

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setSerialNo(resolveSerialNo(request.serialNo()));
    ticket.setVehicle(vehicle);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setSecondDate(request.secondDate());
    ticket.setSecondTime(request.secondTime());
    ticket.setGrossWeight(gross);
    ticket.setUnloadedWeight(unloaded);
    ticket.setNetWeight(net);
    ticket.setStatus(DocumentStatus.POSTED);

    return ticketRepository.save(ticket);
  }

  @Transactional
  public WeighbridgeTicket updateTicket(Long id, WeighbridgeDtos.CreateTicketRequest request) {
    WeighbridgeTicket ticket = ticketRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

    BigDecimal gross = request.grossWeight();
    BigDecimal unloaded = request.unloadedWeight();
    BigDecimal net = gross.subtract(unloaded);

    ticket.setSerialNo(resolveSerialNo(request.serialNo(), ticket.getSerialNo()));
    ticket.setVehicle(vehicle);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setSecondDate(request.secondDate());
    ticket.setSecondTime(request.secondTime());
    ticket.setGrossWeight(gross);
    ticket.setUnloadedWeight(unloaded);
    ticket.setNetWeight(net);

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
}
