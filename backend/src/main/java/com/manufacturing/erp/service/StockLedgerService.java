package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.domain.StockLedger;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.repository.StockLedgerRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class StockLedgerService {
  private final StockLedgerRepository stockLedgerRepository;

  public StockLedgerService(StockLedgerRepository stockLedgerRepository) {
    this.stockLedgerRepository = stockLedgerRepository;
  }

  public StockLedger postEntry(String docType, Long docId, Long docLineId, LedgerTxnType txnType,
                               Item item, Uom uom, Location fromLocation, Location toLocation,
                               Godown fromGodown, Godown toGodown,
                               BigDecimal quantity, BigDecimal weight, StockStatus status) {
    return postEntry(docType, docId, docLineId, txnType, item, uom, fromLocation, toLocation,
        fromGodown, toGodown, null, null, quantity, weight, status, null, null);
  }

  public StockLedger postEntry(String docType, Long docId, Long docLineId, LedgerTxnType txnType,
                               Item item, Uom uom, Location fromLocation, Location toLocation,
                               Godown fromGodown, Godown toGodown, Godown godown, Long batchId,
                               BigDecimal quantity, BigDecimal weight, StockStatus status,
                               BigDecimal rate, BigDecimal amount) {
    StockLedger ledger = new StockLedger();
    ledger.setDocType(docType);
    ledger.setDocId(docId);
    ledger.setDocLineId(docLineId);
    ledger.setTxnType(txnType);
    ledger.setItem(item);
    ledger.setUom(uom);
    ledger.setFromLocation(fromLocation);
    ledger.setToLocation(toLocation);
    ledger.setFromGodown(fromGodown);
    ledger.setToGodown(toGodown);
    ledger.setGodown(godown != null ? godown : resolveGodown(txnType, fromGodown, toGodown));
    ledger.setQuantity(defaultZero(quantity));
    ledger.setWeight(defaultZero(weight));
    ledger.setQtyIn(resolveQtyIn(txnType, defaultZero(quantity)));
    ledger.setQtyOut(resolveQtyOut(txnType, defaultZero(quantity)));
    ledger.setStatus(status);
    ledger.setPostedAt(Instant.now());
    ledger.setBatchId(batchId);
    ledger.setRate(rate);
    ledger.setAmount(amount);
    return stockLedgerRepository.save(ledger);
  }

  private Godown resolveGodown(LedgerTxnType txnType, Godown fromGodown, Godown toGodown) {
    return switch (txnType) {
      case IN -> toGodown != null ? toGodown : fromGodown;
      case OUT -> fromGodown != null ? fromGodown : toGodown;
      case MOVE -> null;
    };
  }

  private BigDecimal resolveQtyIn(LedgerTxnType txnType, BigDecimal quantity) {
    return switch (txnType) {
      case IN -> quantity;
      case MOVE -> quantity;
      case OUT -> BigDecimal.ZERO;
    };
  }

  private BigDecimal resolveQtyOut(LedgerTxnType txnType, BigDecimal quantity) {
    return switch (txnType) {
      case OUT -> quantity;
      case MOVE -> quantity;
      case IN -> BigDecimal.ZERO;
    };
  }

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
