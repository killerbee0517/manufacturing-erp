package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Rfq;
import com.manufacturing.erp.domain.RfqAward;
import com.manufacturing.erp.domain.RfqLine;
import com.manufacturing.erp.domain.RfqQuoteHeader;
import com.manufacturing.erp.domain.RfqQuoteLine;
import com.manufacturing.erp.domain.RfqSupplierQuote;
import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.dto.TransactionDtos.RfqAwardAllocation;
import com.manufacturing.erp.dto.TransactionDtos.RfqAwardLine;
import com.manufacturing.erp.dto.TransactionDtos.RfqSupplierAward;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.RfqAwardRepository;
import com.manufacturing.erp.repository.RfqRepository;
import com.manufacturing.erp.repository.RfqSupplierQuoteRepository;
import com.manufacturing.erp.repository.RfqQuoteHeaderRepository;
import com.manufacturing.erp.repository.RfqQuoteLineRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
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
  private final RfqSupplierQuoteRepository rfqSupplierQuoteRepository;
  private final RfqAwardRepository rfqAwardRepository;
  private final RfqQuoteHeaderRepository rfqQuoteHeaderRepository;
  private final RfqQuoteLineRepository rfqQuoteLineRepository;

  public RfqService(RfqRepository rfqRepository,
                    SupplierRepository supplierRepository,
                    ItemRepository itemRepository,
                    UomRepository uomRepository,
                    BrokerRepository brokerRepository,
                    PurchaseOrderRepository purchaseOrderRepository,
                    RfqSupplierQuoteRepository rfqSupplierQuoteRepository,
                    RfqAwardRepository rfqAwardRepository,
                    RfqQuoteHeaderRepository rfqQuoteHeaderRepository,
                    RfqQuoteLineRepository rfqQuoteLineRepository) {
    this.rfqRepository = rfqRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.brokerRepository = brokerRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.rfqSupplierQuoteRepository = rfqSupplierQuoteRepository;
    this.rfqAwardRepository = rfqAwardRepository;
    this.rfqQuoteHeaderRepository = rfqQuoteHeaderRepository;
    this.rfqQuoteLineRepository = rfqQuoteLineRepository;
  }

  @Transactional(readOnly = true)
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

  @Transactional(readOnly = true)
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

  @Transactional(readOnly = true)
  public List<TransactionDtos.RfqQuoteSupplierSummary> listQuotes(Long rfqId) {
    Rfq rfq = getRfqOrThrow(rfqId);
    return buildQuoteSummaries(rfq);
  }

  @Transactional
  public TransactionDtos.RfqQuoteResponse saveQuote(Long rfqId, Long supplierId, TransactionDtos.RfqQuoteSaveRequest request) {
    Rfq rfq = getRfqOrThrow(rfqId);
    if (rfq.getStatus() == DocumentStatus.AWARDED || rfq.getStatus() == DocumentStatus.PARTIALLY_AWARDED) {
      throw new IllegalStateException("Awarded RFQ cannot be edited");
    }
    Supplier supplier = supplierRepository.findById(supplierId)
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));
    ensureSupplierInvited(rfq, supplierId);

    RfqQuoteHeader header = rfqQuoteHeaderRepository.findByRfqIdAndSupplierId(rfqId, supplierId)
        .orElseGet(() -> createBlankQuoteHeader(rfq, supplier));
    Map<Long, RfqLine> lineMap = rfq.getLines().stream()
        .collect(Collectors.toMap(RfqLine::getId, l -> l));

    applyQuoteUpdates(header, request, lineMap);
    DocumentStatus nextStatus = header.getStatus() == DocumentStatus.SUBMITTED ? DocumentStatus.REVISED : DocumentStatus.DRAFT;
    header.setStatus(nextStatus);
    if (nextStatus == DocumentStatus.DRAFT) {
      header.setSubmittedAt(null);
    }
    rfqQuoteHeaderRepository.save(header);
    rfq.getSuppliers().stream()
        .filter(inv -> inv.getSupplier() != null && inv.getSupplier().getId().equals(supplierId))
        .forEach(inv -> inv.setStatus(nextStatus));
    rfqRepository.save(rfq);
    return toQuoteResponse(header);
  }

  @Transactional(readOnly = true)
  public TransactionDtos.RfqQuoteResponse getQuote(Long rfqId, Long supplierId) {
    Rfq rfq = getRfqOrThrow(rfqId);
    Supplier supplier = supplierRepository.findById(supplierId)
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));
    ensureSupplierInvited(rfq, supplierId);

    RfqQuoteHeader header = rfqQuoteHeaderRepository.findByRfqIdAndSupplierId(rfqId, supplierId)
        .orElseGet(() -> createBlankQuoteHeader(rfq, supplier));
    populateMissingLines(header, rfq.getLines());
    return toQuoteResponse(header);
  }

  @Transactional
  public TransactionDtos.RfqQuoteResponse submitQuote(Long rfqId, Long supplierId) {
    Rfq rfq = getRfqOrThrow(rfqId);
    if (rfq.getStatus() == DocumentStatus.AWARDED || rfq.getStatus() == DocumentStatus.PARTIALLY_AWARDED) {
      throw new IllegalStateException("Awarded RFQ cannot be edited");
    }
    ensureSupplierInvited(rfq, supplierId);
    RfqQuoteHeader header = rfqQuoteHeaderRepository.findByRfqIdAndSupplierId(rfqId, supplierId)
        .orElseThrow(() -> new IllegalStateException("Quote not found for supplier " + supplierId));
    header.setStatus(DocumentStatus.SUBMITTED);
    header.setSubmittedAt(Instant.now());
    rfqQuoteHeaderRepository.save(header);
    rfq.getSuppliers().stream()
        .filter(inv -> inv.getSupplier().getId().equals(supplierId))
        .forEach(inv -> inv.setStatus(DocumentStatus.SUBMITTED));
    rfqRepository.save(rfq);
    return toQuoteResponse(header);
  }

  @Transactional
  public TransactionDtos.RfqResponse award(Long id, TransactionDtos.RfqAwardRequest request) {
    Rfq rfq = getRfqOrThrow(id);
    if (rfq.getStatus() == DocumentStatus.REJECTED) {
      throw new IllegalStateException("Rejected RFQ cannot be awarded");
    }
    Map<Long, RfqLine> lineMap = rfq.getLines().stream()
        .collect(Collectors.toMap(RfqLine::getId, l -> l));

    Map<Long, BigDecimal> existingAwards = rfq.getAwards().stream()
        .collect(Collectors.toMap(a -> a.getRfqLine().getId(), RfqAward::getAwardedQty, BigDecimal::add));

    List<RfqAwardLine> normalizedAwards = normalizeAwardLines(request, lineMap.keySet());
    if (normalizedAwards.isEmpty()) {
      throw new IllegalArgumentException("At least one award allocation is required");
    }

    Map<Long, BigDecimal> awardedPerLine = new HashMap<>(existingAwards);
    for (RfqAwardLine awardLine : normalizedAwards) {
      RfqLine line = lineMap.get(awardLine.rfqLineId());
      if (line == null) {
        throw new IllegalArgumentException("Invalid RFQ line: " + awardLine.rfqLineId());
      }
      ensureSupplierInvited(rfq, awardLine.supplierId());
      if (awardLine.awardQty() == null || awardLine.awardQty().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Award quantity is required for line " + awardLine.rfqLineId());
      }
      awardedPerLine.merge(line.getId(), awardLine.awardQty(), BigDecimal::add);
      if (awardedPerLine.get(line.getId()).compareTo(line.getQuantity()) > 0) {
        throw new IllegalArgumentException("Awarded qty exceeds requested for line " + line.getId());
      }
      resolveSubmittedQuoteLine(rfq.getId(), awardLine.supplierId(), awardLine.rfqLineId());
    }

    List<RfqAward> newAwards = new ArrayList<>();
    for (RfqAwardLine awardLine : normalizedAwards) {
      Supplier supplier = supplierRepository.findById(awardLine.supplierId())
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + awardLine.supplierId()));
      RfqQuoteLine quoteLine = resolveSubmittedQuoteLine(rfq.getId(), awardLine.supplierId(), awardLine.rfqLineId());
      BigDecimal rate = awardLine.awardRate() != null ? awardLine.awardRate() : quoteLine.getQuotedRate();
      LocalDate delivery = awardLine.deliveryDate() != null ? awardLine.deliveryDate() : quoteLine.getDeliveryDate();
      if (rate == null) {
        throw new IllegalArgumentException("Award rate missing for line " + awardLine.rfqLineId());
      }
      RfqAward award = new RfqAward();
      award.setRfq(rfq);
      award.setRfqLine(lineMap.get(awardLine.rfqLineId()));
      award.setSupplier(supplier);
      award.setAwardedQty(awardLine.awardQty());
      award.setAwardedRate(rate);
      award.setAwardedDeliveryDate(delivery);
      award.setAwardStatus(DocumentStatus.AWARDED);
      newAwards.add(award);
    }

    rfq.getAwards().addAll(newAwards);
    rfqAwardRepository.saveAll(newAwards);

    Map<Long, List<RfqAward>> awardsBySupplier = newAwards.stream()
        .filter(a -> a.getAwardedQty().compareTo(BigDecimal.ZERO) > 0)
        .collect(Collectors.groupingBy(a -> a.getSupplier().getId()));

    Map<Long, List<Long>> createdPoIds = new HashMap<>();
    for (Map.Entry<Long, List<RfqAward>> entry : awardsBySupplier.entrySet()) {
      Supplier supplier = supplierRepository.findById(entry.getKey())
          .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + entry.getKey()));
      PurchaseOrder po = buildOrUpdatePurchaseOrderFromAwards(rfq, supplier, entry.getValue());
      po = purchaseOrderRepository.save(po);
      createdPoIds.computeIfAbsent(supplier.getId(), k -> new ArrayList<>()).add(po.getId());
    }

    Set<Long> suppliersAwarded = rfq.getAwards().stream()
        .filter(a -> a.getAwardedQty() != null && a.getAwardedQty().compareTo(BigDecimal.ZERO) > 0)
        .map(a -> a.getSupplier().getId())
        .collect(Collectors.toSet());

    boolean fullyAwarded = rfq.getLines().stream().allMatch(line ->
        awardedPerLine.getOrDefault(line.getId(), BigDecimal.ZERO)
            .compareTo(line.getQuantity()) >= 0);

    rfq.getSuppliers().forEach(invite -> {
      if (suppliersAwarded.contains(invite.getSupplier().getId())) {
        invite.setStatus(DocumentStatus.AWARDED);
      } else if (fullyAwarded) {
        invite.setStatus(DocumentStatus.CLOSED_NOT_AWARDED);
      }
    });

    if (fullyAwarded && (rfq.getClosureReason() == null || rfq.getClosureReason().isBlank())) {
      rfq.setClosureReason("AWARDED");
    }
    rfq.setStatus(fullyAwarded ? DocumentStatus.AWARDED : DocumentStatus.PARTIALLY_AWARDED);
    rfqRepository.save(rfq);
    Rfq refreshed = rfqRepository.findById(rfq.getId()).orElse(rfq);
    return toResponse(refreshed, createdPoIds);
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
    if (rfq.getStatus() == DocumentStatus.AWARDED) {
      throw new IllegalStateException("Awarded RFQ is already closed");
    }
    if (request.closureReason() == null || request.closureReason().isBlank()) {
      throw new IllegalArgumentException("Closure reason is required");
    }
    rfq.setClosureReason(request.closureReason());
    rfq.setStatus(DocumentStatus.CLOSED);
    rfq.getSuppliers().forEach(inv -> {
      if (inv.getStatus() != DocumentStatus.AWARDED) {
        inv.setStatus(DocumentStatus.CLOSED_NOT_AWARDED);
      }
    });

    rfqRepository.save(rfq);
    return new TransactionDtos.RfqCloseResponse(rfq.getId(), rfq.getStatus().name(), rfq.getClosureReason(), null);
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

  private void ensureSupplierInvited(Rfq rfq, Long supplierId) {
    boolean invited = rfq.getSuppliers().stream()
        .anyMatch(inv -> inv.getSupplier() != null && inv.getSupplier().getId().equals(supplierId));
    if (!invited) {
      throw new IllegalArgumentException("Supplier not invited to this RFQ");
    }
  }

  private RfqQuoteHeader createBlankQuoteHeader(Rfq rfq, Supplier supplier) {
    RfqQuoteHeader header = new RfqQuoteHeader();
    header.setRfq(rfq);
    header.setSupplier(supplier);
    header.setStatus(DocumentStatus.DRAFT);
    header.setPaymentTermsOverride(null);
    header.setRemarks(null);
    populateMissingLines(header, rfq.getLines());
    return header;
  }

  private void populateMissingLines(RfqQuoteHeader header, List<RfqLine> rfqLines) {
    Map<Long, RfqQuoteLine> existing = header.getLines().stream()
        .collect(Collectors.toMap(l -> l.getRfqLine().getId(), l -> l));
    for (RfqLine rfqLine : rfqLines) {
      if (!existing.containsKey(rfqLine.getId())) {
        RfqQuoteLine quoteLine = new RfqQuoteLine();
        quoteLine.setQuoteHeader(header);
        quoteLine.setRfqLine(rfqLine);
        header.getLines().add(quoteLine);
      }
    }
  }

  private void applyQuoteUpdates(RfqQuoteHeader header,
                                 TransactionDtos.RfqQuoteSaveRequest request,
                                 Map<Long, RfqLine> lineMap) {
    Map<Long, RfqQuoteLine> existing = header.getLines().stream()
        .collect(Collectors.toMap(l -> l.getRfqLine().getId(), l -> l));
    header.getLines().clear();
    for (TransactionDtos.RfqQuoteLineRequest lineRequest : request.lines()) {
      RfqLine rfqLine = lineMap.get(lineRequest.rfqLineId());
      if (rfqLine == null) {
        throw new IllegalArgumentException("Invalid RFQ line: " + lineRequest.rfqLineId());
      }
      RfqQuoteLine quoteLine = existing.getOrDefault(rfqLine.getId(), new RfqQuoteLine());
      quoteLine.setQuoteHeader(header);
      quoteLine.setRfqLine(rfqLine);
      quoteLine.setQuotedQty(lineRequest.quotedQty());
      quoteLine.setQuotedRate(lineRequest.quotedRate());
      quoteLine.setDeliveryDate(lineRequest.deliveryDate());
      quoteLine.setRemarks(lineRequest.remarks());
      header.getLines().add(quoteLine);
    }
    header.setPaymentTermsOverride(request.paymentTermsOverride());
    header.setRemarks(request.remarks());
  }

  private TransactionDtos.RfqQuoteResponse toQuoteResponse(RfqQuoteHeader header) {
    List<TransactionDtos.RfqQuoteLineResponse> lines = header.getLines().stream()
        .sorted(Comparator.comparing(l -> l.getRfqLine().getId()))
        .map(l -> new TransactionDtos.RfqQuoteLineResponse(
            l.getRfqLine() != null ? l.getRfqLine().getId() : null,
            l.getQuotedQty(),
            l.getQuotedRate(),
            l.getDeliveryDate(),
            l.getRemarks()))
        .toList();
    return new TransactionDtos.RfqQuoteResponse(
        header.getSupplier() != null ? header.getSupplier().getId() : null,
        header.getSupplier() != null ? header.getSupplier().getName() : null,
        header.getStatus() != null ? header.getStatus().name() : null,
        header.getPaymentTermsOverride(),
        header.getRemarks(),
        header.getSubmittedAt(),
        lines);
  }

  private List<TransactionDtos.RfqQuoteSupplierSummary> buildQuoteSummaries(Rfq rfq) {
    List<RfqQuoteHeader> headers = rfqQuoteHeaderRepository.findByRfqId(rfq.getId());
    Map<Long, RfqQuoteHeader> headerBySupplier = headers.stream()
        .filter(h -> h.getSupplier() != null)
        .collect(Collectors.toMap(h -> h.getSupplier().getId(), h -> h));
    return rfq.getSuppliers().stream()
        .filter(inv -> inv.getSupplier() != null)
        .map(inv -> {
          RfqQuoteHeader header = headerBySupplier.get(inv.getSupplier().getId());
          BigDecimal totalAmount = header != null
              ? header.getLines().stream()
                  .map(l -> {
                    BigDecimal qty = l.getQuotedQty() != null ? l.getQuotedQty() : BigDecimal.ZERO;
                    BigDecimal rate = l.getQuotedRate() != null ? l.getQuotedRate() : BigDecimal.ZERO;
                    return qty.multiply(rate);
                  })
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
              : BigDecimal.ZERO;
          BigDecimal totalQty = header != null
              ? header.getLines().stream()
                  .map(l -> l.getQuotedQty() != null ? l.getQuotedQty() : BigDecimal.ZERO)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
              : BigDecimal.ZERO;
          DocumentStatus status = header != null ? header.getStatus() : inv.getStatus();
          Instant submittedAt = header != null ? header.getSubmittedAt() : null;
          return new TransactionDtos.RfqQuoteSupplierSummary(
              inv.getSupplier().getId(),
              inv.getSupplier().getName(),
              status != null ? status.name() : null,
              totalAmount,
              totalQty,
              submittedAt);
        })
        .sorted(Comparator.comparing(TransactionDtos.RfqQuoteSupplierSummary::supplierId))
        .toList();
  }

  private RfqQuoteLine resolveSubmittedQuoteLine(Long rfqId, Long supplierId, Long rfqLineId) {
    RfqQuoteHeader header = rfqQuoteHeaderRepository.findByRfqIdAndSupplierId(rfqId, supplierId)
        .orElseThrow(() -> new IllegalArgumentException("Quote header not found for supplier " + supplierId));
    if (header.getStatus() != DocumentStatus.SUBMITTED) {
      throw new IllegalStateException("Quote for supplier " + supplierId + " is not submitted");
    }
    return header.getLines().stream()
        .filter(l -> l.getRfqLine() != null && l.getRfqLine().getId().equals(rfqLineId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Quote line not found for RFQ line " + rfqLineId));
  }

  private List<RfqAwardLine> normalizeAwardLines(TransactionDtos.RfqAwardRequest request, Set<Long> rfqLineIds) {
    List<RfqAwardLine> result = new ArrayList<>();
    if (request == null) {
      return result;
    }
    if (request.supplierAwards() != null) {
      for (RfqSupplierAward supplierAward : request.supplierAwards()) {
        if (supplierAward == null || supplierAward.allocations() == null) {
          continue;
        }
        for (RfqAwardAllocation allocation : supplierAward.allocations()) {
          if (allocation == null || allocation.awardQty() == null) {
            continue;
          }
          if (rfqLineIds != null && !rfqLineIds.contains(allocation.rfqLineId())) {
            continue;
          }
          result.add(new RfqAwardLine(
              allocation.rfqLineId(),
              supplierAward.supplierId(),
              allocation.awardQty(),
              allocation.awardRate(),
              allocation.deliveryDate(),
              null));
        }
      }
    }
    if (request.awards() != null) {
      for (RfqAwardLine awardLine : request.awards()) {
        if (awardLine == null) {
          continue;
        }
        if (rfqLineIds != null && !rfqLineIds.contains(awardLine.rfqLineId())) {
          continue;
        }
        result.add(awardLine);
      }
    }
    return result;
  }

  private TransactionDtos.RfqResponse toResponse(Rfq rfq) {
    return toResponse(rfq, resolvePoIds(rfq));
  }

  private TransactionDtos.RfqResponse toResponse(Rfq rfq, Map<Long, List<Long>> poIdsBySupplier) {
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
            a.getAwardedRate(),
            a.getAwardedDeliveryDate(),
            a.getAwardStatus() != null ? a.getAwardStatus().name() : null))
        .toList();

    List<TransactionDtos.RfqQuoteSupplierSummary> quoteSummaries = buildQuoteSummaries(rfq);

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
        quoteSummaries,
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

  private PurchaseOrder buildOrUpdatePurchaseOrderFromAwards(Rfq rfq, Supplier supplier, List<RfqAward> awards) {
    PurchaseOrder po = purchaseOrderRepository.findByRfqId(rfq.getId()).stream()
        .filter(existing -> existing.getSupplier() != null && existing.getSupplier().getId().equals(supplier.getId()))
        .findFirst()
        .orElseGet(() -> {
          PurchaseOrder fresh = new PurchaseOrder();
          fresh.setPoNo("PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH)) + "-" + System.nanoTime());
          fresh.setSupplier(supplier);
          fresh.setPoDate(LocalDate.now());
          fresh.setRemarks(rfq.getNarration());
          fresh.setPurchaseLedger(null);
          fresh.setCurrentLedgerBalance(BigDecimal.ZERO);
          fresh.setStatus(DocumentStatus.DRAFT);
          fresh.setRfq(rfq);
          return fresh;
        });

    LocalDate earliestAwardDate = awards.stream()
        .map(RfqAward::getAwardedDeliveryDate)
        .filter(Objects::nonNull)
        .sorted()
        .findFirst()
        .orElse(null);
    LocalDate currentDelivery = po.getDeliveryDate();
    if (currentDelivery == null || (earliestAwardDate != null && earliestAwardDate.isBefore(currentDelivery))) {
      po.setDeliveryDate(earliestAwardDate);
    }

    awards.forEach(award -> {
      PurchaseOrderLine line = new PurchaseOrderLine();
      line.setPurchaseOrder(po);
      line.setItem(award.getRfqLine().getItem());
      line.setUom(award.getRfqLine().getUom());
      line.setQuantity(award.getAwardedQty());
      line.setRate(award.getAwardedRate());
      line.setAmount(award.getAwardedQty().multiply(award.getAwardedRate()));
      line.setRemarks(award.getRfqLine().getRemarks());
      po.getLines().add(line);
    });
    po.setTotalAmount(calculateTotal(po.getLines()));
    return po;
  }

  private Map<Long, List<Long>> resolvePoIds(Rfq rfq) {
    if (rfq.getId() == null) {
      return Map.of();
    }
    return purchaseOrderRepository.findByRfqId(rfq.getId()).stream()
        .filter(po -> po.getSupplier() != null)
        .collect(Collectors.groupingBy(po -> po.getSupplier().getId(),
            Collectors.mapping(PurchaseOrder::getId, Collectors.toList())));
  }
}
