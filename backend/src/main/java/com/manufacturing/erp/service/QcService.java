package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.QcInspection;
import com.manufacturing.erp.domain.QcInspectionLine;
import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.QcInspectionLineRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QcService {
  private final QcInspectionRepository qcInspectionRepository;
  private final QcInspectionLineRepository qcInspectionLineRepository;
  private final GrnRepository grnRepository;
  private final GrnLineRepository grnLineRepository;
  private final UomRepository uomRepository;

  public QcService(QcInspectionRepository qcInspectionRepository,
                   QcInspectionLineRepository qcInspectionLineRepository,
                   GrnRepository grnRepository,
                   GrnLineRepository grnLineRepository,
                   UomRepository uomRepository) {
    this.qcInspectionRepository = qcInspectionRepository;
    this.qcInspectionLineRepository = qcInspectionLineRepository;
    this.grnRepository = grnRepository;
    this.grnLineRepository = grnLineRepository;
    this.uomRepository = uomRepository;
  }

  @Transactional
  public QcInspection createDraftFromGrn(Long grnId) {
    List<QcInspection> existing = qcInspectionRepository.findByGrnId(grnId);
    if (!existing.isEmpty()) {
      return existing.get(0);
    }
    Grn grn = grnRepository.findById(grnId)
        .orElseThrow(() -> new IllegalArgumentException("GRN not found"));
    QcInspection inspection = new QcInspection();
    inspection.setGrn(grn);
    inspection.setStatus(QcStatus.DRAFT);
    inspection.setInspectionDate(LocalDate.now());
    inspection.setSampleQty(null);
    inspection.setSampleUom(null);
    inspection.setMethod(null);
    inspection.setRemarks(null);
    QcInspection saved = qcInspectionRepository.save(inspection);
    for (GrnLine grnLine : grn.getLines()) {
      QcInspectionLine line = new QcInspectionLine();
      line.setQcInspection(saved);
      line.setGrnLine(grnLine);
      line.setReceivedQty(defaultQty(grnLine.getReceivedQty(), grnLine.getQuantity()));
      line.setAcceptedQty(defaultQty(grnLine.getAcceptedQty(), grnLine.getReceivedQty()));
      line.setRejectedQty(defaultQty(grnLine.getRejectedQty(), BigDecimal.ZERO));
      qcInspectionLineRepository.save(line);
      saved.getLines().add(line);
    }
    return saved;
  }

  @Transactional(readOnly = true)
  public List<QcInspection> list(Long grnId) {
    if (grnId == null) {
      return qcInspectionRepository.findAll();
    }
    return qcInspectionRepository.findByGrnId(grnId);
  }

  @Transactional(readOnly = true)
  public QcInspection get(Long id) {
    return qcInspectionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("QC inspection not found"));
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
        .collect(Collectors.toMap(line -> line.getGrnLine().getId(), line -> line));
    inspection.getLines().clear();
    for (QcDtos.QcInspectionLineRequest lineRequest : request.lines()) {
      GrnLine grnLine = grnLineRepository.findById(lineRequest.grnLineId())
          .orElseThrow(() -> new IllegalArgumentException("GRN line not found"));
      if (!grnLine.getGrn().getId().equals(inspection.getGrn().getId())) {
        throw new IllegalArgumentException("GRN line does not belong to inspection GRN");
      }
      QcInspectionLine line = existing.getOrDefault(grnLine.getId(), new QcInspectionLine());
      line.setQcInspection(inspection);
      line.setGrnLine(grnLine);
      validateQuantities(lineRequest.receivedQty(), lineRequest.acceptedQty(), lineRequest.rejectedQty());
      line.setReceivedQty(lineRequest.receivedQty());
      line.setAcceptedQty(lineRequest.acceptedQty());
      line.setRejectedQty(lineRequest.rejectedQty());
      line.setReason(lineRequest.reason());
      qcInspectionLineRepository.save(line);
      inspection.getLines().add(line);
      syncGrnLineQuantities(grnLine, line);
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
    applyLineUpdatesToGrn(inspection);
    inspection.setStatus(QcStatus.SUBMITTED);
    return qcInspectionRepository.save(inspection);
  }

  @Transactional
  public QcInspection approve(Long id) {
    QcInspection inspection = get(id);
    if (inspection.getStatus() != QcStatus.SUBMITTED && inspection.getStatus() != QcStatus.DRAFT) {
      throw new IllegalStateException("Only submitted QC inspections can be approved");
    }
    applyLineUpdatesToGrn(inspection);
    inspection.setStatus(QcStatus.APPROVED);
    return qcInspectionRepository.save(inspection);
  }

  @Transactional
  public QcInspection reject(Long id) {
    QcInspection inspection = get(id);
    inspection.setStatus(QcStatus.REJECTED);
    for (QcInspectionLine line : inspection.getLines()) {
      line.setAcceptedQty(BigDecimal.ZERO);
      qcInspectionLineRepository.save(line);
      syncGrnLineQuantities(line.getGrnLine(), line);
    }
    return qcInspectionRepository.save(inspection);
  }

  private void applyLineUpdatesToGrn(QcInspection inspection) {
    for (QcInspectionLine line : inspection.getLines()) {
      validateQuantities(line.getReceivedQty(), line.getAcceptedQty(), line.getRejectedQty());
      syncGrnLineQuantities(line.getGrnLine(), line);
    }
  }

  private void syncGrnLineQuantities(GrnLine grnLine, QcInspectionLine qcLine) {
    grnLine.setAcceptedQty(qcLine.getAcceptedQty());
    grnLine.setRejectedQty(qcLine.getRejectedQty());
    grnLineRepository.save(grnLine);
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
}
