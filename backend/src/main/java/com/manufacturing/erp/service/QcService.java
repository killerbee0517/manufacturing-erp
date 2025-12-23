package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.domain.QcInspection;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.UomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QcService {
  private final QcInspectionRepository qcInspectionRepository;
  private final GrnLineRepository grnLineRepository;
  private final LocationRepository locationRepository;
  private final UomRepository uomRepository;
  private final StockLedgerService stockLedgerService;

  public QcService(QcInspectionRepository qcInspectionRepository,
                   GrnLineRepository grnLineRepository,
                   LocationRepository locationRepository,
                   UomRepository uomRepository,
                   StockLedgerService stockLedgerService) {
    this.qcInspectionRepository = qcInspectionRepository;
    this.grnLineRepository = grnLineRepository;
    this.locationRepository = locationRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
  }

  @Transactional
  public QcInspection updateStatus(QcDtos.QcUpdateRequest request) {
    GrnLine grnLine = grnLineRepository.findById(request.grnLineId())
        .orElseThrow(() -> new IllegalArgumentException("GRN line not found"));
    QcStatus status = QcStatus.valueOf(request.status().toUpperCase());

    QcInspection inspection = new QcInspection();
    inspection.setGrnLine(grnLine);
    inspection.setStatus(status);
    inspection.setInspectionDate(request.inspectionDate());
    QcInspection saved = qcInspectionRepository.save(inspection);

    if (status == QcStatus.ACCEPTED) {
      Location qcHold = locationRepository.findByCode("QC_HOLD")
          .orElseThrow(() -> new IllegalArgumentException("QC_HOLD location missing"));
      Location unrestricted = locationRepository.findByCode("UNRESTRICTED")
          .orElseThrow(() -> new IllegalArgumentException("UNRESTRICTED location missing"));
      Uom uom = uomRepository.findByCode("KG")
          .orElseThrow(() -> new IllegalArgumentException("UOM KG missing"));
      stockLedgerService.postEntry("QC", grnLine.getGrn().getId(), grnLine.getId(), LedgerTxnType.MOVE,
          grnLine.getItem(), uom, qcHold, unrestricted, null, null,
          grnLine.getQuantity(), grnLine.getWeight(), StockStatus.UNRESTRICTED);
    }

    return saved;
  }
}
