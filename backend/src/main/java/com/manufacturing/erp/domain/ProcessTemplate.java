package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_templates")
public class ProcessTemplate extends BaseEntity {
  @Column(nullable = false)
  private String name;

  private String description;

  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProcessStep> steps = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ProcessStep> getSteps() {
    return steps;
  }

  public void setSteps(List<ProcessStep> steps) {
    this.steps = steps;
  }
}
