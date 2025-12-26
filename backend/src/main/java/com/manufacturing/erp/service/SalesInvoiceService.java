package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.BrokerCommission;
import com.manufacturing.erp.domain.BrokerCommissionRule;
import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.SalesInvoice;
import com.manufacturing.erp.domain.SalesInvoiceLine;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.StockDtos;
import com.manufacturing.erp.repository.BrokerCommissionRepository;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SalesInvoiceRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesInvoiceService {
  private final SalesInvoiceRepository salesInvoiceRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final BrokerCommissionRepository brokerCommissionRepository;
  private final StockLedgerService stockLedgerService;
  private final VoucherService voucherService;
  private final LedgerService ledgerService;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final GodownRepository godownRepository;
  private final CustomerRepository customerRepository;
  private final BrokerRepository brokerRepository;

  public SalesInvoiceService(SalesInvoiceRepository salesInvoiceRepository,
                             BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                             BrokerCommissionRepository brokerCommissionRepository,
                             StockLedgerService stockLedgerService,
                             VoucherService voucherService,
                             LedgerService ledgerService,
                             ItemRepository itemRepository,
                             UomRepository uomRepository,
                             GodownRepository godownRepository,
                             CustomerRepository customerRepository,
                             BrokerRepository brokerRepository) {
    this.salesInvoiceRepository = salesInvoiceRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
    this.brokerCommissionRepository = brokerCommissionRepository;
    this.stockLedgerService = stockLedgerService;
    this.voucherService = voucherService;
    this.ledgerService = ledgerService;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.godownRepository = godownRepository;
    this.customerRepository = customerRepository;
    this.brokerRepository = brokerRepository;
  }

  @Transactional
  public SalesInvoice postInvoice(StockDtos.SalesInvoiceRequest request) {
    Customer customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    Broker broker = request.brokerId() != null
        ? brokerRepository.findById(request.brokerId()).orElse(null)
        : null;
    SalesInvoice invoice = new SalesInvoice();
    invoice.setInvoiceNo(request.invoiceNo());
    invoice.setCustomer(customer);
    invoice.setBroker(broker);
    invoice.setInvoiceDate(request.invoiceDate());
    invoice.setTotalAmount(request.totalAmount());

    invoice.setStatus(DocumentStatus.POSTED);
    List<SalesInvoiceLine> lines = new ArrayList<>();
    if (request.lines() != null) {
      for (StockDtos.SalesInvoiceLineRequest lineRequest : request.lines()) {
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setSalesInvoice(invoice);
        Item item = itemRepository.findById(lineRequest.itemId())
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        Uom uom = uomRepository.findById(lineRequest.uomId())
            .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
        Godown godown = lineRequest.godownId() != null
            ? godownRepository.findById(lineRequest.godownId())
                .orElseThrow(() -> new IllegalArgumentException("Godown not found"))
            : null;
        line.setItem(item);
        line.setUom(uom);
        line.setQuantity(lineRequest.quantity());
        BigDecimal rate = lineRequest.rate() != null ? lineRequest.rate() : BigDecimal.ZERO;
        BigDecimal amount = lineRequest.amount() != null
            ? lineRequest.amount()
            : rate.multiply(lineRequest.quantity());
        line.setLineAmount(amount);
        line.setRate(rate);
        line.setGodown(godown);
        lines.add(line);
      }
    }
    invoice.setLines(lines);
    SalesInvoice saved = salesInvoiceRepository.save(invoice);

    postStock(saved, lines);
    postCustomerLedger(saved);
    postBrokerCommission(saved);
    return saved;
  }

  private void postCustomerLedger(SalesInvoice invoice) {
    Ledger customerLedger = invoice.getCustomer() != null ? invoice.getCustomer().getLedger() : null;
    if (customerLedger == null && invoice.getCustomer() != null) {
      customerLedger = ledgerService.createLedger(invoice.getCustomer().getName(), LedgerType.CUSTOMER, "CUSTOMER", invoice.getCustomer().getId());
      invoice.getCustomer().setLedger(customerLedger);
      customerRepository.save(invoice.getCustomer());
    }
    if (customerLedger == null) {
      return;
    }
    Ledger salesLedger = ledgerService.findOrCreateLedger("Sales", LedgerType.GENERAL);
    List<VoucherService.VoucherLineRequest> lines = new ArrayList<>();
    lines.add(new VoucherService.VoucherLineRequest(customerLedger, invoice.getTotalAmount(), BigDecimal.ZERO));
    lines.add(new VoucherService.VoucherLineRequest(salesLedger, BigDecimal.ZERO, invoice.getTotalAmount()));
    voucherService.createVoucher("SALES_INVOICE", invoice.getId(), invoice.getInvoiceDate(), "Sales invoice", lines);
  }

  private void postStock(SalesInvoice invoice, List<SalesInvoiceLine> lines) {
    for (SalesInvoiceLine line : lines) {
      stockLedgerService.postEntry("SALES_INVOICE", invoice.getId(), line.getId(), LedgerTxnType.OUT,
          line.getItem(), line.getUom(), null, null, line.getGodown(), null,
          line.getQuantity(), line.getQuantity(), StockStatus.UNRESTRICTED,
          line.getRate(), line.getLineAmount());
    }
  }

  private void postBrokerCommission(SalesInvoice invoice) {
    if (invoice.getBroker() == null) {
      return;
    }
    BrokerCommissionRule rule = brokerCommissionRuleRepository.findFirstByBrokerId(invoice.getBroker().getId())
        .orElseThrow(() -> new IllegalArgumentException("Broker commission rule missing"));
    BigDecimal commission = invoice.getTotalAmount()
        .multiply(rule.getRatePercent())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    BrokerCommission record = new BrokerCommission();
    record.setSalesInvoice(invoice);
    record.setBroker(invoice.getBroker());
    record.setCommissionAmount(commission);
    brokerCommissionRepository.save(record);
  }
}
