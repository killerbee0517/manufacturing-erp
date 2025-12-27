package com.manufacturing.erp.domain;

public class Enums {
  private Enums() {}

  public enum DocumentStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    RELEASED,
    POSTED,
    CLOSED,
    CANCELLED,
    PARTIALLY_AWARDED,
    AWARDED,
    CLOSED_NOT_AWARDED,
    REVISED,
    REJECTED,
    IN_PROGRESS,
    UNLOADED
  }

  public enum QcStatus {
    PENDING,
    HOLD,
    ACCEPTED,
    REJECTED
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
    BYPRODUCT
  }

  public enum InventoryLocationType {
    GODOWN,
    WIP
  }
}
