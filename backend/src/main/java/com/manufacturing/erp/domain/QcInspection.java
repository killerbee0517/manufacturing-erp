package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.QcStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "qc_inspections")
public class QcInspection extends BaseEntity {
  @ManyToOne
  private GrnLine grnLine;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QcStatus status;

  @Column(nullable = false)
  private LocalDate inspectionDate;

  public GrnLine getGrnLine() {
    return grnLine;
  }

  public void setGrnLine(GrnLine grnLine) {
    this.grnLine = grnLine;
  }

  public QcStatus getStatus() {
    return status;
  }

  public void setStatus(QcStatus status) {
    this.status = status;
  }

  public LocalDate getInspectionDate() {
    return inspectionDate;
  }

  public void setInspectionDate(LocalDate inspectionDate) {
    this.inspectionDate = inspectionDate;
  }
}
