package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.LedgerAccount;
import com.manufacturing.erp.domain.LedgerEntry;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.repository.LedgerAccountRepository;
import com.manufacturing.erp.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerAccountService {
  private final LedgerAccountRepository ledgerAccountRepository;
  private final LedgerEntryRepository ledgerEntryRepository;

  public LedgerAccountService(LedgerAccountRepository ledgerAccountRepository, LedgerEntryRepository ledgerEntryRepository) {
    this.ledgerAccountRepository = ledgerAccountRepository;
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  @Transactional
  public LedgerAccount ensurePartyLedger(Company company, Party party, LedgerType ledgerType, String name) {
    return ledgerAccountRepository
        .findByCompanyIdAndLedgerTypeAndPartyId(company.getId(), ledgerType, party.getId())
        .orElseGet(() -> {
          LedgerAccount account = new LedgerAccount();
          account.setCompany(company);
          account.setParty(party);
          account.setLedgerType(ledgerType);
          account.setName(name);
          return ledgerAccountRepository.save(account);
        });
  }

  @Transactional
  public LedgerAccount ensureBankLedger(Company company, Bank bank) {
    return ledgerAccountRepository
        .findByCompanyIdAndLedgerTypeAndBankId(company.getId(), LedgerType.BANK, bank.getId())
        .orElseGet(() -> {
          LedgerAccount account = new LedgerAccount();
          account.setCompany(company);
          account.setBank(bank);
          account.setLedgerType(LedgerType.BANK);
          account.setName(bank.getName());
          return ledgerAccountRepository.save(account);
        });
  }

  @Transactional
  public LedgerEntry postEntry(LedgerAccount account, LocalDate date, String txnType, String refTable, Long refId,
                               BigDecimal debit, BigDecimal credit, String narration) {
    LedgerEntry entry = new LedgerEntry();
    entry.setCompany(account.getCompany());
    entry.setLedgerAccount(account);
    entry.setTxnDate(date != null ? date : LocalDate.now());
    entry.setTxnType(txnType);
    entry.setRefTable(refTable);
    entry.setRefId(refId);
    entry.setDebit(debit == null ? BigDecimal.ZERO : debit);
    entry.setCredit(credit == null ? BigDecimal.ZERO : credit);
    entry.setNarration(narration);
    return ledgerEntryRepository.save(entry);
  }
}
