package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.QcStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qc_inspections")
public class QcInspection extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "grn_id")
  private Grn grn;

  @ManyToOne
  private GrnLine grnLine;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QcStatus status;

  @Column(nullable = false)
  private LocalDate inspectionDate;

  @Column(name = "inspector_user_id")
  private Long inspectorUserId;

  @Column(name = "sample_qty")
  private BigDecimal sampleQty;

  @ManyToOne
  @JoinColumn(name = "sample_uom_id")
  private Uom sampleUom;

  private String method;

  private String remarks;

  @OneToMany(mappedBy = "qcInspection", orphanRemoval = true)
  private List<QcInspectionLine> lines = new ArrayList<>();

  public Grn getGrn() {
    return grn;
  }

  public void setGrn(Grn grn) {
    this.grn = grn;
  }

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

  public Long getInspectorUserId() {
    return inspectorUserId;
  }

  public void setInspectorUserId(Long inspectorUserId) {
    this.inspectorUserId = inspectorUserId;
  }

  public BigDecimal getSampleQty() {
    return sampleQty;
  }

  public void setSampleQty(BigDecimal sampleQty) {
    this.sampleQty = sampleQty;
  }

  public Uom getSampleUom() {
    return sampleUom;
  }

  public void setSampleUom(Uom sampleUom) {
    this.sampleUom = sampleUom;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public List<QcInspectionLine> getLines() {
    return lines;
  }

  public void setLines(List<QcInspectionLine> lines) {
    this.lines = lines;
  }
}
