package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.VoucherLine;
import com.manufacturing.erp.dto.LedgerDtos;
import com.manufacturing.erp.repository.LedgerRepository;
import com.manufacturing.erp.repository.VoucherLineRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
  private final LedgerRepository ledgerRepository;
  private final VoucherLineRepository voucherLineRepository;

  public LedgerService(LedgerRepository ledgerRepository, VoucherLineRepository voucherLineRepository) {
    this.ledgerRepository = ledgerRepository;
    this.voucherLineRepository = voucherLineRepository;
  }

  @Transactional
  public Ledger createLedger(String name, LedgerType type, String referenceType, Long referenceId) {
    Ledger ledger = new Ledger();
    ledger.setName(name);
    ledger.setType(type);
    ledger.setReferenceType(referenceType);
    ledger.setReferenceId(referenceId);
    ledger.setEnabled(true);
    return ledgerRepository.save(ledger);
  }

  @Transactional
  public Ledger findOrCreateLedger(String name, LedgerType type) {
    return ledgerRepository.findByNameAndType(name, type)
        .orElseGet(() -> createLedger(name, type, null, null));
  }

  public BigDecimal getBalance(Long ledgerId) {
    BigDecimal balance = voucherLineRepository.findBalanceForLedger(ledgerId);
    return balance != null ? balance : BigDecimal.ZERO;
  }

  public List<LedgerDtos.LedgerStatementEntry> getStatement(Long ledgerId, LocalDate fromDate, LocalDate toDate) {
    BigDecimal runningBalance = BigDecimal.ZERO;
    if (fromDate != null) {
      runningBalance = voucherLineRepository.findOpeningBalance(ledgerId, fromDate);
    }
    List<VoucherLine> lines = voucherLineRepository.findStatementLines(ledgerId, fromDate, toDate);
    BigDecimal currentBalance = runningBalance;
    List<LedgerDtos.LedgerStatementEntry> entries = new java.util.ArrayList<>();
    for (VoucherLine line : lines) {
      BigDecimal dr = line.getDrAmount();
      BigDecimal cr = line.getCrAmount();
      currentBalance = currentBalance.add(dr).subtract(cr);
      entries.add(new LedgerDtos.LedgerStatementEntry(
          line.getVoucher().getId(),
          line.getVoucher().getVoucherNo(),
          line.getVoucher().getVoucherDate(),
          line.getVoucher().getNarration(),
          line.getVoucher().getReferenceType(),
          line.getVoucher().getReferenceId(),
          dr,
          cr,
          currentBalance
      ));
    }
    return entries;
  }
}
