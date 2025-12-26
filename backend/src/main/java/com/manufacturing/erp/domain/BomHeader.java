package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bom_header")
public class BomHeader extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "finished_item_id", nullable = false)
  private Item finishedItem;

  @Column(nullable = false)
  private String name;

  private String version;

  @Column(nullable = false)
  private Boolean enabled = Boolean.TRUE;

  @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<BomLine> lines = new ArrayList<>();

  public Item getFinishedItem() {
    return finishedItem;
  }

  public void setFinishedItem(Item finishedItem) {
    this.finishedItem = finishedItem;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public List<BomLine> getLines() {
    return lines;
  }

  public void setLines(List<BomLine> lines) {
    this.lines = lines;
  }
}
