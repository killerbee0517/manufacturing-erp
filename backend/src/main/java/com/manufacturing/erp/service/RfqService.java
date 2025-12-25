package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Rfq;
import com.manufacturing.erp.domain.RfqAward;
import com.manufacturing.erp.domain.RfqLine;
import com.manufacturing.erp.domain.RfqSupplierQuote;
import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.PurchaseOrderLineRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.RfqAwardRepository;
import com.manufacturing.erp.repository.RfqRepository;
import com.manufacturing.erp.repository.RfqSupplierQuoteRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RfqService {
  private final RfqRepository rfqRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final BrokerRepository brokerRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PurchaseOrderLineRepository purchaseOrderLineRepository;
  private final RfqSupplierQuoteRepository rfqSupplierQuoteRepository;
  private final RfqAwardRepository rfqAwardRepository;

  public RfqService(RfqRepository rfqRepository,
                    SupplierRepository supplierRepository,
                    ItemRepository itemRepository,
                    UomRepository uomRepository,
                    BrokerRepository brokerRepository,
                    PurchaseOrderRepository purchaseOrderRepository,
                    PurchaseOrderLineRepository purchaseOrderLineRepository,
                    RfqSupplierQuoteRepository rfqSupplierQuoteRepository,
                    RfqAwardRepository rfqAwardRepository) {
    this.rfqRepository = rfqRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.brokerRepository = brokerRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.rfqSupplierQuoteRepository = rfqSupplierQuoteRepository;
    this.rfqAwardRepository = rfqAwardRepository;
  }

  public Page<TransactionDtos.RfqResponse> list(String q, String status, Pageable pageable) {
    Specification<Rfq> spec = Specification.where(null);
    if (q != null && !q.isBlank()) {
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("rfqNo")), "%" + q.toLowerCase() + "%"));
    }
    if (status != null && !status.isBlank()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), DocumentStatus.valueOf(status.toUpperCase())));
    }
    return rfqRepository.findAll(spec, pageable).map(this::toResponse);
  }

  public TransactionDtos.RfqResponse getById(Long id) {
    Rfq rfq = getRfqOrThrow(id);
    return toResponse(rfq);
  }

  @Transactional
  public TransactionDtos.RfqResponse create(TransactionDtos.RfqRequest request) {
    Rfq rfq = new Rfq();
    rfq.setRfqNo(resolveRfqNo(request.rfqNo()));
    rfq.setRfqDate(request.rfqDate());
    rfq.setPaymentTerms(request.paymentTerms());
    rfq.setNarration(request.narration());
    rfq.setStatus(DocumentStatus.DRAFT);

    request.lines().forEach(lineRequest -> rfq.getLines().add(toLineEntity(rfq, lineRequest)));
    attachSuppliers(rfq, request.supplierIds());

    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqResponse update(Long id, TransactionDtos.RfqRequest request) {
    Rfq rfq = getRfqOrThrow(id);
    if (rfq.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalArgumentException("Only DRAFT RFQs can be edited");
    }

    rfq.setRfqNo(resolveRfqNo(request.rfqNo(), rfq.getRfqNo()));
    rfq.setRfqDate(request.rfqDate());
    rfq.setPaymentTerms(request.paymentTerms());
    rfq.setNarration(request.narration());

    Map<Long, RfqLine> existingById = new HashMap<>();
    rfq.getLines().forEach(line -> existingById.put(line.getId(), line));

    rfq.getLines().clear();
    request.lines().forEach(lineRequest -> {
      RfqLine line = lineRequest.id() != null ? existingById.get(lineRequest.id()) : null;
      if (line == null) {
        line = toLineEntity(rfq, lineRequest);
      } else {
        applyLineUpdates(line, lineRequest);
      }
      line.setRfq(rfq);
      rfq.getLines().add(line);
    });
    rfq.getSuppliers().clear();
    attachSuppliers(rfq, request.supplierIds());

    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqResponse submit(Long id) {
    Rfq rfq = getRfqOrThrow(id);
    if (rfq.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalArgumentException("Only DRAFT RFQs can be submitted");
    }
    rfq.setStatus(DocumentStatus.SUBMITTED);
    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqResponse approve(Long id) {
    Rfq rfq = getRfqOrThrow(id);
    if (rfq.getStatus() != DocumentStatus.SUBMITTED && rfq.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalArgumentException("Only open RFQs can be approved");
    }
    rfq.setStatus(DocumentStatus.APPROVED);
    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqResponse award(Long id, TransactionDtos.RfqAwardRequest request) {
    Rfq rfq = getRfqOrThrow(id);
    if (rfq.getStatus() == DocumentStatus.REJECTED) {
      throw new IllegalStateException("Rejected RFQ cannot be awarded");
    }
    Map<Long, RfqLine> lineMap = rfq.getLines().stream()
        .collect(Collectors.toMap(RfqLine::getId, l -> l));

    Map<Long, BigDecimal> awardedPerLine = new HashMap<>();
    for (TransactionDtos.RfqAwardLine awardLine : request.awards()) {
      RfqLine line = lineMap.get(awardLine.rfqLineId());
      if (line == null) {
        throw new IllegalArgumentException("Invalid RFQ line: " + awardLine.rfqLineId());
      }
      awardedPerLine.merge(line.getId(), awardLine.quantity(), BigDecimal::add);
    }
    awardedPerLine.forEach((lineId, total) -> {
      BigDecimal requested = lineMap.get(lineId).getQuantity();
      if (total.compareTo(requested) > 0) {
        throw new IllegalArgumentException("Awarded qty exceeds requested for line " + lineId);
      }
    });

    rfq.getAwards().clear();
    List<RfqAward> awards = new ArrayList<>();
    for (TransactionDtos.RfqAwardLine awardLine : request.awards()) {
      Supplier supplier = supplierRepository.findById(awardLine.supplierId())
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + awardLine.supplierId()));
      RfqAward award = new RfqAward();
      award.setRfqLine(lineMap.get(awardLine.rfqLineId()));
      award.setSupplier(supplier);
      award.setAwardedQty(awardLine.quantity());
      award.setRate(awardLine.rate());
      award.setAwardStatus(DocumentStatus.AWARDED);
      award.setRfqLine(lineMap.get(awardLine.rfqLineId()));
      award.setSupplier(supplier);
      awards.add(award);
    }
    rfq.getAwards().addAll(awards);
    rfqAwardRepository.saveAll(awards);

    Map<Long, List<RfqAward>> awardsBySupplier = awards.stream()
        .filter(a -> a.getAwardedQty().compareTo(BigDecimal.ZERO) > 0)
        .collect(Collectors.groupingBy(a -> a.getSupplier().getId()));

    Map<Long, Long> createdPoIds = new HashMap<>();
    for (Map.Entry<Long, List<RfqAward>> entry : awardsBySupplier.entrySet()) {
      Supplier supplier = supplierRepository.findById(entry.getKey())
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + entry.getKey()));
      PurchaseOrder po = buildPurchaseOrderFromAwards(rfq, supplier, entry.getValue());
      purchaseOrderRepository.save(po);
      purchaseOrderLineRepository.saveAll(po.getLines());
      createdPoIds.put(supplier.getId(), po.getId());
    }

    rfq.getSuppliers().forEach(invite -> {
      if (awardsBySupplier.containsKey(invite.getSupplier().getId())) {
        invite.setStatus(DocumentStatus.AWARDED);
      } else {
        invite.setStatus(DocumentStatus.REJECTED);
      }
    });

    boolean fullyAwarded = awardedPerLine.values().stream().allMatch(qty -> qty.compareTo(BigDecimal.ZERO) > 0);
    rfq.setStatus(fullyAwarded ? DocumentStatus.AWARDED : DocumentStatus.PARTIALLY_AWARDED);
    Rfq saved = rfqRepository.save(rfq);
    return toResponse(saved, createdPoIds);
  }

  @Transactional
  public TransactionDtos.RfqResponse reject(Long id, String remarks) {
    Rfq rfq = getRfqOrThrow(id);
    rfq.setStatus(DocumentStatus.REJECTED);
    rfq.getSuppliers().forEach(s -> s.setStatus(DocumentStatus.REJECTED));
    if (remarks != null && !remarks.isBlank()) {
      rfq.setClosureReason(remarks);
    }
    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqCloseResponse close(Long id, TransactionDtos.RfqCloseRequest request) {
    Rfq rfq = getRfqOrThrow(id);
    if (request.closureReason() == null || request.closureReason().isBlank()) {
      throw new IllegalArgumentException("Closure reason is required");
    }
    rfq.setClosureReason(request.closureReason());
    rfq.setStatus(DocumentStatus.CLOSED);

    Long purchaseOrderId = null;
    if ("AWARDED_TO_SUPPLIER".equalsIgnoreCase(request.closureReason())) {
      PurchaseOrder po = createPurchaseOrderFromRfq(rfq);
      purchaseOrderId = po.getId();
    }

    rfqRepository.save(rfq);
    return new TransactionDtos.RfqCloseResponse(rfq.getId(), rfq.getStatus().name(), rfq.getClosureReason(), purchaseOrderId);
  }

  private RfqLine toLineEntity(Rfq rfq, TransactionDtos.RfqLineRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    Broker broker = request.brokerId() != null
        ? brokerRepository.findById(request.brokerId())
            .orElseThrow(() -> new IllegalArgumentException("Broker not found"))
        : null;
    RfqLine line = new RfqLine();
    line.setRfq(rfq);
    line.setItem(item);
    line.setUom(uom);
    line.setBroker(broker);
    line.setQuantity(request.quantity());
    line.setRateExpected(request.rateExpected());
    line.setRemarks(request.remarks());
    return line;
  }

  private void applyLineUpdates(RfqLine line, TransactionDtos.RfqLineRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    Broker broker = request.brokerId() != null
        ? brokerRepository.findById(request.brokerId())
            .orElseThrow(() -> new IllegalArgumentException("Broker not found"))
        : null;
    line.setItem(item);
    line.setUom(uom);
    line.setBroker(broker);
    line.setQuantity(request.quantity());
    line.setRateExpected(request.rateExpected());
    line.setRemarks(request.remarks());
  }

  private TransactionDtos.RfqResponse toResponse(Rfq rfq) {
    return toResponse(rfq, Map.of());
  }

  private TransactionDtos.RfqResponse toResponse(Rfq rfq, Map<Long, Long> poIdsBySupplier) {
    List<TransactionDtos.RfqLineResponse> lines = rfq.getLines().stream()
        .map(line -> new TransactionDtos.RfqLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getBroker() != null ? line.getBroker().getId() : null,
            line.getQuantity(),
            line.getRateExpected(),
            line.getRemarks()))
        .toList();

    List<TransactionDtos.RfqSupplierInvite> supplierInvites = rfq.getSuppliers().stream()
        .sorted(Comparator.comparing(inv -> inv.getSupplier().getId()))
        .map(inv -> new TransactionDtos.RfqSupplierInvite(
            inv.getSupplier() != null ? inv.getSupplier().getId() : null,
            inv.getStatus() != null ? inv.getStatus().name() : null,
            inv.getRemarks()))
        .toList();

    List<TransactionDtos.RfqAwardLine> awards = rfq.getAwards().stream()
        .map(a -> new TransactionDtos.RfqAwardLine(
            a.getRfqLine() != null ? a.getRfqLine().getId() : null,
            a.getSupplier() != null ? a.getSupplier().getId() : null,
            a.getAwardedQty(),
            a.getRate(),
            a.getAwardStatus() != null ? a.getAwardStatus().name() : null))
        .toList();

    return new TransactionDtos.RfqResponse(
        rfq.getId(),
        rfq.getRfqNo(),
        supplierInvites,
        rfq.getRfqDate(),
        rfq.getPaymentTerms(),
        rfq.getNarration(),
        rfq.getClosureReason(),
        rfq.getStatus().name(),
        lines,
        awards,
        poIdsBySupplier);
  }

  private String resolveRfqNo(String provided) {
    return resolveRfqNo(provided, null);
  }

  private String resolveRfqNo(String provided, String fallback) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    String stamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH));
    return "RFQ-" + stamp + "-" + System.nanoTime();
  }

  private PurchaseOrder createPurchaseOrderFromRfq(Rfq rfq) {
    PurchaseOrder po = new PurchaseOrder();
    po.setPoNo("PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)) + "-" + System.nanoTime());
    po.setSupplier(rfq.getSupplier());
    po.setPoDate(LocalDate.now());
    po.setRemarks(rfq.getNarration());
    po.setPurchaseLedger(null);
    po.setCurrentLedgerBalance(BigDecimal.ZERO);
    po.setStatus(DocumentStatus.DRAFT);
    po.setRfq(rfq);

    rfq.getLines().forEach(rfqLine -> {
      PurchaseOrderLine line = new PurchaseOrderLine();
      line.setPurchaseOrder(po);
      line.setItem(rfqLine.getItem());
      line.setUom(rfqLine.getUom());
      line.setQuantity(rfqLine.getQuantity());
      line.setRate(rfqLine.getRateExpected() != null ? rfqLine.getRateExpected() : BigDecimal.ZERO);
      line.setAmount(line.getQuantity().multiply(line.getRate()));
      line.setRemarks(rfqLine.getRemarks());
      po.getLines().add(line);
    });
    po.setTotalAmount(calculateTotal(po.getLines()));
    return purchaseOrderRepository.save(po);
  }

  private BigDecimal calculateTotal(List<PurchaseOrderLine> lines) {
    return lines.stream()
        .map(line -> line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Rfq getRfqOrThrow(Long id) {
    return rfqRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RFQ not found"));
  }

  private void attachSuppliers(Rfq rfq, List<Long> supplierIds) {
    if (supplierIds == null || supplierIds.isEmpty()) {
      throw new IllegalArgumentException("At least one supplier is required");
    }
    supplierIds.stream().distinct().forEach(supplierId -> {
      Supplier supplier = supplierRepository.findById(supplierId)
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));
      RfqSupplierQuote invite = new RfqSupplierQuote();
      invite.setRfq(rfq);
      invite.setSupplier(supplier);
      invite.setStatus(DocumentStatus.DRAFT);
      rfq.getSuppliers().add(invite);
    });
  }

  private PurchaseOrder buildPurchaseOrderFromAwards(Rfq rfq, Supplier supplier, List<RfqAward> awards) {
    PurchaseOrder po = new PurchaseOrder();
    po.setPoNo("PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)) + "-" + System.nanoTime());
    po.setSupplier(supplier);
    po.setPoDate(LocalDate.now());
    po.setRemarks(rfq.getNarration());
    po.setPurchaseLedger(null);
    po.setCurrentLedgerBalance(BigDecimal.ZERO);
    po.setStatus(DocumentStatus.DRAFT);
    po.setRfq(rfq);

    awards.forEach(award -> {
      PurchaseOrderLine line = new PurchaseOrderLine();
      line.setPurchaseOrder(po);
      line.setItem(award.getRfqLine().getItem());
      line.setUom(award.getRfqLine().getUom());
      line.setQuantity(award.getAwardedQty());
      line.setRate(award.getRate());
      line.setAmount(award.getAwardedQty().multiply(award.getRate()));
      line.setRemarks(award.getRfqLine().getRemarks());
      po.getLines().add(line);
    });
    po.setTotalAmount(calculateTotal(po.getLines()));
    return po;
  }
}
