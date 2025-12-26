package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rfq_quote_lines")
public class RfqQuoteLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "quote_header_id")
  private RfqQuoteHeader quoteHeader;

  @ManyToOne
  @JoinColumn(name = "rfq_line_id")
  private RfqLine rfqLine;

  @Column(name = "quoted_qty")
  private BigDecimal quotedQty;

  @Column(name = "quoted_rate")
  private BigDecimal quotedRate;

  @Column(name = "delivery_date")
  private LocalDate deliveryDate;

  private String remarks;

  public RfqQuoteHeader getQuoteHeader() {
    return quoteHeader;
  }

  public void setQuoteHeader(RfqQuoteHeader quoteHeader) {
    this.quoteHeader = quoteHeader;
  }

  public RfqLine getRfqLine() {
    return rfqLine;
  }

  public void setRfqLine(RfqLine rfqLine) {
    this.rfqLine = rfqLine;
  }

  public BigDecimal getQuotedQty() {
    return quotedQty;
  }

  public void setQuotedQty(BigDecimal quotedQty) {
    this.quotedQty = quotedQty;
  }

  public BigDecimal getQuotedRate() {
    return quotedRate;
  }

  public void setQuotedRate(BigDecimal quotedRate) {
    this.quotedRate = quotedRate;
  }

  public LocalDate getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(LocalDate deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
