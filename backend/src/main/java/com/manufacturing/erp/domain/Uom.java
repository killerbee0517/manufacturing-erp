package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "uoms")
public class Uom extends BaseEntity {
  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String description;

  @ManyToOne
  @JoinColumn(name = "base_uom_id")
  private Uom baseUom;

  @Column(name = "conversion_factor")
  private BigDecimal conversionFactor;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Uom getBaseUom() {
    return baseUom;
  }

  public void setBaseUom(Uom baseUom) {
    this.baseUom = baseUom;
  }

  public BigDecimal getConversionFactor() {
    return conversionFactor;
  }

  public void setConversionFactor(BigDecimal conversionFactor) {
    this.conversionFactor = conversionFactor;
  }
}
