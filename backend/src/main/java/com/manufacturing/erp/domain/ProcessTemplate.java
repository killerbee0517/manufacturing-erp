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
@Table(name = "process_templates")
public class ProcessTemplate extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "output_item_id")
  private Item outputItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "output_uom_id")
  private Uom outputUom;

  @Column(nullable = false)
  private Boolean enabled = Boolean.TRUE;

  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProcessTemplateStep> steps = new ArrayList<>();

  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProcessTemplateInput> inputs = new ArrayList<>();

  @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProcessTemplateOutput> outputs = new ArrayList<>();

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

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

  public Item getOutputItem() {
    return outputItem;
  }

  public void setOutputItem(Item outputItem) {
    this.outputItem = outputItem;
  }

  public Uom getOutputUom() {
    return outputUom;
  }

  public void setOutputUom(Uom outputUom) {
    this.outputUom = outputUom;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public List<ProcessTemplateStep> getSteps() {
    return steps;
  }

  public void setSteps(List<ProcessTemplateStep> steps) {
    this.steps = steps;
  }

  public List<ProcessTemplateInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<ProcessTemplateInput> inputs) {
    this.inputs = inputs;
  }

  public List<ProcessTemplateOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<ProcessTemplateOutput> outputs) {
    this.outputs = outputs;
  }
}
