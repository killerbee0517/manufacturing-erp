package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.QcInspection;
import com.manufacturing.erp.domain.QcInspectionLine;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.PurchaseOrderLineRepository;
import com.manufacturing.erp.repository.QcInspectionLineRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class QcService {
  private final QcInspectionRepository qcInspectionRepository;
  private final QcInspectionLineRepository qcInspectionLineRepository;
  private final PurchaseOrderLineRepository purchaseOrderLineRepository;
  private final UomRepository uomRepository;
  private final GrnService grnService;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;

  public QcService(QcInspectionRepository qcInspectionRepository,
                   QcInspectionLineRepository qcInspectionLineRepository,
                   PurchaseOrderLineRepository purchaseOrderLineRepository,
                   UomRepository uomRepository,
                   GrnService grnService,
                   CompanyRepository companyRepository,
                   CompanyContext companyContext,
                   WeighbridgeTicketRepository weighbridgeTicketRepository) {
    this.qcInspectionRepository = qcInspectionRepository;
    this.qcInspectionLineRepository = qcInspectionLineRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.uomRepository = uomRepository;
    this.grnService = grnService;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
  }

  @Transactional
  public QcInspection createDraftFromWeighbridge(Long weighbridgeId) {
    Company company = requireCompany();
    WeighbridgeTicket ticket = weighbridgeTicketRepository.findByIdAndPurchaseOrderCompanyId(weighbridgeId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weighbridge ticket not found"));
    return createDraftFromWeighbridge(ticket);
  }

  @Transactional
  public QcInspection createDraftFromWeighbridge(WeighbridgeTicket ticket) {
    if (ticket == null) {
      throw new IllegalArgumentException("Weighbridge ticket is required");
    }
    if (ticket.getStatus() != com.manufacturing.erp.domain.Enums.DocumentStatus.UNLOADED) {
      throw new IllegalStateException("Weighbridge ticket must be unloaded before QC inspection");
    }
    ensureSameCompany(ticket.getPurchaseOrder());
    List<QcInspection> existing = qcInspectionRepository.findByWeighbridgeTicketId(ticket.getId());
    if (!existing.isEmpty()) {
      return existing.get(0);
    }
    PurchaseOrder purchaseOrder = ticket.getPurchaseOrder();
    if (purchaseOrder == null) {
      throw new IllegalArgumentException("Weighbridge ticket must reference a purchase order");
    }
    QcInspection inspection = new QcInspection();
    inspection.setPurchaseOrder(purchaseOrder);
    inspection.setWeighbridgeTicket(ticket);
    inspection.setStatus(QcStatus.DRAFT);
    inspection.setInspectionDate(LocalDate.now());
    inspection.setSampleQty(null);
    inspection.setSampleUom(null);
    inspection.setMethod(null);
    inspection.setRemarks(null);
    QcInspection saved = qcInspectionRepository.save(inspection);
    for (PurchaseOrderLine poLine : purchaseOrder.getLines()) {
      QcInspectionLine line = new QcInspectionLine();
      line.setQcInspection(saved);
      line.setPurchaseOrderLine(poLine);
      line.setReceivedQty(defaultQty(poLine.getQuantity(), BigDecimal.ZERO));
      line.setAcceptedQty(defaultQty(poLine.getQuantity(), BigDecimal.ZERO));
      line.setRejectedQty(BigDecimal.ZERO);
      qcInspectionLineRepository.save(line);
      saved.getLines().add(line);
    }
    return saved;
  }

  @Transactional(readOnly = true)
  public List<QcInspection> list(Long grnId, Long weighbridgeId, Long purchaseOrderId) {
    Company company = requireCompany();
    List<QcInspection> inspections;
    if (grnId != null) {
      inspections = qcInspectionRepository.findByGrnId(grnId);
    } else if (weighbridgeId != null) {
      inspections = qcInspectionRepository.findByWeighbridgeTicketId(weighbridgeId);
    } else if (purchaseOrderId != null) {
      inspections = qcInspectionRepository.findByPurchaseOrderId(purchaseOrderId);
    } else {
      inspections = qcInspectionRepository.findAll();
    }
    return inspections.stream()
        .filter(inspection -> resolveCompanyId(inspection) != null && resolveCompanyId(inspection).equals(company.getId()))
        .toList();
  }

  @Transactional(readOnly = true)
  public QcInspection get(Long id) {
    QcInspection inspection = qcInspectionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QC inspection not found"));
    Company company = requireCompany();
    Long inspectionCompanyId = resolveCompanyId(inspection);
    if (inspectionCompanyId != null && !inspectionCompanyId.equals(company.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "QC inspection not found");
    }
    return inspection;
  }

  @Transactional
  public QcInspection update(Long id, QcDtos.QcInspectionRequest request) {
    QcInspection inspection = get(id);
    if (inspection.getStatus() != QcStatus.DRAFT && inspection.getStatus() != QcStatus.REJECTED) {
      throw new IllegalStateException("Only draft QC inspections can be edited");
    }
    if (request.sampleUomId() != null) {
      inspection.setSampleUom(uomRepository.findById(request.sampleUomId())
          .orElseThrow(() -> new IllegalArgumentException("Sample UOM not found")));
    } else {
      inspection.setSampleUom(null);
    }
    inspection.setSampleQty(request.sampleQty());
    inspection.setMethod(request.method());
    inspection.setRemarks(request.remarks());
    Map<Long, QcInspectionLine> existing = inspection.getLines().stream()
        .filter(line -> line.getPurchaseOrderLine() != null)
        .collect(Collectors.toMap(line -> line.getPurchaseOrderLine().getId(), line -> line));
    inspection.getLines().clear();
    for (QcDtos.QcInspectionLineRequest lineRequest : request.lines()) {
      PurchaseOrderLine poLine = purchaseOrderLineRepository.findById(lineRequest.poLineId())
          .orElseThrow(() -> new IllegalArgumentException("PO line not found"));
      if (inspection.getPurchaseOrder() == null
          || poLine.getPurchaseOrder() == null
          || !poLine.getPurchaseOrder().getId().equals(inspection.getPurchaseOrder().getId())) {
        throw new IllegalArgumentException("PO line does not belong to inspection purchase order");
      }
      QcInspectionLine line = existing.getOrDefault(poLine.getId(), new QcInspectionLine());
      line.setQcInspection(inspection);
      line.setPurchaseOrderLine(poLine);
      BigDecimal received = lineRequest.receivedQty();
      BigDecimal rejected = lineRequest.rejectedQty();
      BigDecimal accepted = normalizeAccepted(received, rejected);
      validateQuantities(received, accepted, rejected);
      line.setReceivedQty(received);
      line.setAcceptedQty(accepted);
      line.setRejectedQty(rejected);
      line.setReason(lineRequest.reason());
      qcInspectionLineRepository.save(line);
      inspection.getLines().add(line);
    }
    inspection.setStatus(QcStatus.DRAFT);
    return qcInspectionRepository.save(inspection);
  }

  @Transactional
  public QcInspection submit(Long id) {
    QcInspection inspection = get(id);
    if (inspection.getStatus() != QcStatus.DRAFT && inspection.getStatus() != QcStatus.REJECTED) {
      throw new IllegalStateException("Only draft QC inspections can be submitted");
    }
    applyLineUpdates(inspection);
    inspection.setStatus(QcStatus.SUBMITTED);
    return qcInspectionRepository.save(inspection);
  }

  @Transactional
  public QcInspection approve(Long id) {
    QcInspection inspection = get(id);
    if (inspection.getStatus() != QcStatus.SUBMITTED && inspection.getStatus() != QcStatus.DRAFT) {
      throw new IllegalStateException("Only submitted QC inspections can be approved");
    }
    applyLineUpdates(inspection);
    inspection.setStatus(QcStatus.APPROVED);
    Grn grn = grnService.createDraftFromQc(inspection);
    inspection.setGrn(grn);
    return qcInspectionRepository.save(inspection);
  }

  @Transactional
  public QcInspection reject(Long id) {
    QcInspection inspection = get(id);
    inspection.setStatus(QcStatus.REJECTED);
    for (QcInspectionLine line : inspection.getLines()) {
      line.setAcceptedQty(BigDecimal.ZERO);
      qcInspectionLineRepository.save(line);
    }
    return qcInspectionRepository.save(inspection);
  }

  private void applyLineUpdates(QcInspection inspection) {
    for (QcInspectionLine line : inspection.getLines()) {
      BigDecimal received = line.getReceivedQty();
      BigDecimal rejected = line.getRejectedQty();
      BigDecimal accepted = normalizeAccepted(received, rejected);
      line.setAcceptedQty(accepted);
      validateQuantities(received, accepted, rejected);
    }
  }

  private void validateQuantities(BigDecimal received, BigDecimal accepted, BigDecimal rejected) {
    BigDecimal rec = defaultQty(received, BigDecimal.ZERO);
    BigDecimal acc = defaultQty(accepted, BigDecimal.ZERO);
    BigDecimal rej = defaultQty(rejected, BigDecimal.ZERO);
    if (acc.add(rej).compareTo(rec) > 0) {
      throw new IllegalArgumentException("Accepted + rejected cannot exceed received quantity");
    }
  }

  private BigDecimal defaultQty(BigDecimal value, BigDecimal fallback) {
    return Optional.ofNullable(value).orElse(fallback);
  }

  private BigDecimal normalizeAccepted(BigDecimal received, BigDecimal rejected) {
    BigDecimal rec = defaultQty(received, BigDecimal.ZERO);
    BigDecimal rej = defaultQty(rejected, BigDecimal.ZERO);
    BigDecimal acc = rec.subtract(rej);
    return acc.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : acc;
  }

  private void ensureSameCompany(PurchaseOrder purchaseOrder) {
    if (purchaseOrder == null) {
      return;
    }
    Company company = requireCompany();
    if (purchaseOrder.getCompany() == null || !purchaseOrder.getCompany().getId().equals(company.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found");
    }
  }

  private Long resolveCompanyId(QcInspection inspection) {
    if (inspection.getPurchaseOrder() != null && inspection.getPurchaseOrder().getCompany() != null) {
      return inspection.getPurchaseOrder().getCompany().getId();
    }
    if (inspection.getGrn() != null && inspection.getGrn().getPurchaseOrder() != null
        && inspection.getGrn().getPurchaseOrder().getCompany() != null) {
      return inspection.getGrn().getPurchaseOrder().getCompany().getId();
    }
    return null;
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
