package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ReadingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "weighbridge_readings")
public class WeighbridgeReading extends BaseEntity {
  @ManyToOne
  private WeighbridgeTicket ticket;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReadingType readingType;

  @Column(nullable = false)
  private BigDecimal weight;

  @Column(nullable = false)
  private Instant readingTime;

  public WeighbridgeTicket getTicket() {
    return ticket;
  }

  public void setTicket(WeighbridgeTicket ticket) {
    this.ticket = ticket;
  }

  public ReadingType getReadingType() {
    return readingType;
  }

  public void setReadingType(ReadingType readingType) {
    this.readingType = readingType;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public Instant getReadingTime() {
    return readingTime;
  }

  public void setReadingTime(Instant readingTime) {
    this.readingTime = readingTime;
  }
}
