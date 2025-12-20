package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
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
                               BigDecimal quantity, BigDecimal weight, StockStatus status) {
    StockLedger ledger = new StockLedger();
    ledger.setDocType(docType);
    ledger.setDocId(docId);
    ledger.setDocLineId(docLineId);
    ledger.setTxnType(txnType);
    ledger.setItem(item);
    ledger.setUom(uom);
    ledger.setFromLocation(fromLocation);
    ledger.setToLocation(toLocation);
    ledger.setQuantity(quantity);
    ledger.setWeight(weight);
    ledger.setStatus(status);
    ledger.setPostedAt(Instant.now());
    return stockLedgerRepository.save(ledger);
  }
}
