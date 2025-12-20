package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.ReadingType;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.WeighbridgeReading;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.WeighbridgeReadingRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final WeighbridgeReadingRepository readingRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            WeighbridgeReadingRepository readingRepository,
                            SupplierRepository supplierRepository,
                            ItemRepository itemRepository) {
    this.ticketRepository = ticketRepository;
    this.readingRepository = readingRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public WeighbridgeTicket createTicket(WeighbridgeDtos.CreateTicketRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));

    BigDecimal gross = extractMax(request.readings(), ReadingType.GROSS);
    BigDecimal tare = extractMax(request.readings(), ReadingType.TARE);
    BigDecimal net = gross.subtract(tare);

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setTicketNo(request.ticketNo());
    ticket.setVehicleNo(request.vehicleNo());
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn());
    ticket.setTimeIn(request.timeIn());
    ticket.setGrossWeight(gross);
    ticket.setTareWeight(tare);
    ticket.setNetWeight(net);
    ticket.setStatus(DocumentStatus.POSTED);

    WeighbridgeTicket saved = ticketRepository.save(ticket);

    request.readings().forEach(reading -> {
      WeighbridgeReading entity = new WeighbridgeReading();
      entity.setTicket(saved);
      entity.setReadingType(ReadingType.valueOf(reading.readingType().toUpperCase()));
      entity.setWeight(reading.weight());
      entity.setReadingTime(reading.readingTime());
      readingRepository.save(entity);
    });

    return saved;
  }

  private BigDecimal extractMax(List<WeighbridgeDtos.ReadingRequest> readings, ReadingType type) {
    return readings.stream()
        .filter(reading -> type.name().equalsIgnoreCase(reading.readingType()))
        .map(WeighbridgeDtos.ReadingRequest::weight)
        .max(Comparator.naturalOrder())
        .orElseThrow(() -> new IllegalArgumentException("Missing " + type + " reading"));
  }
}
