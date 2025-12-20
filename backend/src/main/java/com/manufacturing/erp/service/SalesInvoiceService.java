package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.BrokerCommission;
import com.manufacturing.erp.domain.BrokerCommissionRule;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.SalesInvoice;
import com.manufacturing.erp.repository.BrokerCommissionRepository;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.SalesInvoiceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesInvoiceService {
  private final SalesInvoiceRepository salesInvoiceRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final BrokerCommissionRepository brokerCommissionRepository;

  public SalesInvoiceService(SalesInvoiceRepository salesInvoiceRepository,
                             BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                             BrokerCommissionRepository brokerCommissionRepository) {
    this.salesInvoiceRepository = salesInvoiceRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
    this.brokerCommissionRepository = brokerCommissionRepository;
  }

  @Transactional
  public SalesInvoice postInvoice(SalesInvoice invoice) {
    invoice.setStatus(DocumentStatus.POSTED);
    SalesInvoice saved = salesInvoiceRepository.save(invoice);
    if (saved.getBroker() != null) {
      BrokerCommissionRule rule = brokerCommissionRuleRepository.findFirstByBrokerId(saved.getBroker().getId())
          .orElseThrow(() -> new IllegalArgumentException("Broker commission rule missing"));
      BigDecimal commission = saved.getTotalAmount()
          .multiply(rule.getRatePercent())
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      BrokerCommission record = new BrokerCommission();
      record.setSalesInvoice(saved);
      record.setBroker(saved.getBroker());
      record.setCommissionAmount(commission);
      brokerCommissionRepository.save(record);
    }
    return saved;
  }
}
