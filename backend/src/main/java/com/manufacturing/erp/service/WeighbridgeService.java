package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class WeighbridgeService {
  private final WeighbridgeTicketRepository ticketRepository;
  private final VehicleRepository vehicleRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final QcService qcService;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public WeighbridgeService(WeighbridgeTicketRepository ticketRepository,
                            VehicleRepository vehicleRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            QcService qcService,
                            CompanyRepository companyRepository,
                            CompanyContext companyContext) {
    this.ticketRepository = ticketRepository;
    this.vehicleRepository = vehicleRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.qcService = qcService;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional(readOnly = true)
  public List<WeighbridgeTicket> list(String status) {
    Company company = requireCompany();
    List<WeighbridgeTicket> tickets = ticketRepository.findByPurchaseOrderCompanyId(company.getId());
    if (status != null && !status.isBlank()) {
      String normalized = status.equalsIgnoreCase("COMPLETED") ? DocumentStatus.UNLOADED.name() : status;
      DocumentStatus target = DocumentStatus.valueOf(normalized.toUpperCase());
      tickets = tickets.stream().filter(t -> t.getStatus() == target).toList();
    }
    return tickets;
  }

  @Transactional(readOnly = true)
  public WeighbridgeTicket getById(Long id) {
    return getTicketOrThrow(id);
  }

  @Transactional
  public WeighbridgeTicket createTicket(WeighbridgeDtos.CreateTicketRequest request) {
    PurchaseOrder po = getPurchaseOrder(request.poId());
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = po.getSupplier();

    Item item = po.getLines().size() == 1 ? po.getLines().get(0).getItem() : null;

    BigDecimal gross = request.grossWeight();

    WeighbridgeTicket ticket = new WeighbridgeTicket();
    ticket.setSerialNo(resolveSerialNo(request.serialNo()));
    ticket.setVehicle(vehicle);
    ticket.setVehicleNo(vehicle.getVehicleNo());
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn() != null ? request.dateIn() : ticket.getDateIn());
    ticket.setTimeIn(request.timeIn() != null ? request.timeIn() : ticket.getTimeIn());
    ticket.setGrossWeight(gross);
    ticket.setStatus(DocumentStatus.IN_PROGRESS);

    return ticketRepository.save(ticket);
  }

  @Transactional
  public WeighbridgeTicket updateTicket(Long id, WeighbridgeDtos.CreateTicketRequest request) {
    WeighbridgeTicket ticket = getTicketOrThrow(id);
    PurchaseOrder po = getPurchaseOrder(request.poId());
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    Supplier supplier = po.getSupplier();
    Item item = po.getLines().size() == 1 ? po.getLines().get(0).getItem() : null;

    ticket.setSerialNo(resolveSerialNo(request.serialNo(), ticket.getSerialNo()));
    ticket.setVehicle(vehicle);
    ticket.setVehicleNo(vehicle.getVehicleNo());
    ticket.setPurchaseOrder(po);
    ticket.setSupplier(supplier);
    ticket.setItem(item);
    ticket.setDateIn(request.dateIn() != null ? request.dateIn() : LocalDate.now());
    ticket.setTimeIn(request.timeIn() != null ? request.timeIn() : LocalTime.now());
    ticket.setGrossWeight(request.grossWeight());
    ticket.setNetWeight(ticket.getUnloadedWeight() != null ? ticket.getGrossWeight().subtract(ticket.getUnloadedWeight()) : null);

    return ticketRepository.save(ticket);
  }

  @Transactional
  public WeighbridgeTicket unload(Long id, WeighbridgeDtos.UnloadTicketRequest request) {
    WeighbridgeTicket ticket = getTicketOrThrow(id);
    PurchaseOrder po = getPurchaseOrder(request.poId());
    Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
        .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    ticket.setPurchaseOrder(po);
    ticket.setVehicle(vehicle);
    ticket.setVehicleNo(vehicle.getVehicleNo());
    ticket.setSupplier(po.getSupplier());
    ticket.setItem(po.getLines().size() == 1 ? po.getLines().get(0).getItem() : null);
    ticket.setSecondDate(request.secondDate() != null ? request.secondDate() : LocalDate.now());
    ticket.setSecondTime(request.secondTime() != null ? request.secondTime() : LocalTime.now());
    ticket.setUnloadedWeight(request.unloadedWeight());
    BigDecimal net = request.unloadedWeight() != null && ticket.getGrossWeight() != null
        ? ticket.getGrossWeight().subtract(request.unloadedWeight())
        : null;
    ticket.setNetWeight(net);
    ticket.setStatus(DocumentStatus.UNLOADED);
    WeighbridgeTicket saved = ticketRepository.save(ticket);
    qcService.createDraftFromWeighbridge(saved);
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

  private PurchaseOrder getPurchaseOrder(Long poId) {
    Company company = requireCompany();
    return purchaseOrderRepository.findByIdAndCompanyId(poId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
  }

  private WeighbridgeTicket getTicketOrThrow(Long id) {
    Company company = requireCompany();
    return ticketRepository.findByIdAndPurchaseOrderCompanyId(id, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weighbridge ticket not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }
}
