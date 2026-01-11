package com.manufacturing.erp.domain;

public class Enums {
  private Enums() {}

  public enum DocumentStatus {
    DRAFT,
    QUOTING,
    SUBMITTED,
    APPROVED,
    RELEASED,
    POSTED,
    CLOSED,
    CANCELLED,
    PARTIALLY_AWARDED,
    AWARDED,
    AWARDED_PARTIAL,
    AWARDED_FULL,
    CLOSED_NOT_AWARDED,
    REVISED,
    REJECTED,
    IN_PROGRESS,
    UNLOADED
  }

  public enum QcStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    PENDING,
    HOLD,
    ACCEPTED
  }

  public enum StockStatus {
    QC_HOLD,
    UNRESTRICTED,
    REJECTED
  }

  public enum LedgerTxnType {
    IN,
    OUT,
    MOVE
  }

  public enum CalcType {
    FLAT,
    PERCENT
  }

  public enum LocationType {
    GODOWN,
    BIN
  }

  public enum PayablePartyType {
    SUPPLIER,
    CUSTOMER,
    BROKER,
    VEHICLE,
    EXPENSE
  }

  public enum ReadingType {
    GROSS,
    TARE
  }

  public enum DebitNoteReason {
    WEIGHT_DIFF,
    BAG_TYPE_DIFF,
    QUALITY_CLAIM,
    RATE_DIFF
  }

  public enum LedgerType {
    SUPPLIER,
    CUSTOMER,
    BROKER,
    EXPENSE,
    BANK,
    GENERAL
  }

  public enum ProductionStatus {
    DRAFT,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED
  }

  public enum ProcessInputSourceType {
    GODOWN,
    WIP
  }

  public enum ProcessOutputType {
    WIP,
    FG,
    BYPRODUCT,
    EMPTY_BAG
  }

  public enum InventoryLocationType {
    GODOWN,
    WIP
  }

  public enum PartyStatus {
    ACTIVE,
    INACTIVE
  }

  public enum PartyRoleType {
    SUPPLIER,
    CUSTOMER,
    BROKER,
    EXPENSE
  }

  public enum BrokerCommissionType {
    PERCENT,
    PER_QTY,
    FIXED
  }

  public enum BrokeragePaidBy {
    COMPANY,
    SUPPLIER
  }

  public enum PaymentDirection {
    PAYABLE,
    RECEIVABLE
  }

  public enum PaymentMode {
    BANK,
    CASH,
    PDC
  }

  public enum PaymentStatus {
    DRAFT,
    POSTED,
    PDC_ISSUED,
    PDC_CLEARED,
    CANCELLED
  }

  public enum PdcStatus {
    ISSUED,
    DEPOSITED,
    CLEARED,
    BOUNCED,
    CANCELLED
  }
}
