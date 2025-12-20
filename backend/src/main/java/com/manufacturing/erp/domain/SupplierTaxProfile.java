package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier_tax_profiles")
public class SupplierTaxProfile extends BaseEntity {
  @ManyToOne
  private Supplier supplier;

  @Column(nullable = false)
  private String defaultSection;

  @Column(nullable = false)
  private boolean tdsApplicable;

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public String getDefaultSection() {
    return defaultSection;
  }

  public void setDefaultSection(String defaultSection) {
    this.defaultSection = defaultSection;
  }

  public boolean isTdsApplicable() {
    return tdsApplicable;
  }

  public void setTdsApplicable(boolean tdsApplicable) {
    this.tdsApplicable = tdsApplicable;
  }
}
