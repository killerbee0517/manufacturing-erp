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
    CANCELLED
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

  public enum LocationType {
    GODOWN,
    BIN
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
}
