package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            SupplierRepository supplierRepository,
                            ItemRepository itemRepository) {
    this.ticketRepository = ticketRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public WeighbridgeTicket createTicket(WeighbridgeDtos.CreateTicketRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

    BigDecimal gross = request.grossWeight();
    BigDecimal unloaded = request.unloadedWeight();
    BigDecimal net = gross.subtract(unloaded);

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setTicketNo(resolveTicketNo(request.ticketNo()));
    ticket.setVehicleNo(request.vehicleNo());
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setDateOut(request.dateOut());
    ticket.setTimeOut(request.timeOut());
    ticket.setGrossWeight(gross);
    ticket.setTareWeight(unloaded);
    ticket.setNetWeight(net);
    ticket.setStatus(DocumentStatus.POSTED);

    return ticketRepository.save(ticket);
  }

  private String resolveTicketNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "WB-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        + "-" + System.nanoTime();
  }
}
