package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Rfq;
import com.manufacturing.erp.domain.RfqLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.RfqRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public RfqService(RfqRepository rfqRepository,
                    SupplierRepository supplierRepository,
                    ItemRepository itemRepository,
                    UomRepository uomRepository) {
    this.rfqRepository = rfqRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
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
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

    Rfq rfq = new Rfq();
    rfq.setRfqNo(request.rfqNo());
    rfq.setSupplier(supplier);
    rfq.setRfqDate(request.rfqDate());
    rfq.setRemarks(request.remarks());
    rfq.setStatus(DocumentStatus.DRAFT);

    request.lines().forEach(lineRequest -> rfq.getLines().add(toLineEntity(rfq, lineRequest)));

    return toResponse(rfqRepository.save(rfq));
  }

  @Transactional
  public TransactionDtos.RfqResponse update(Long id, TransactionDtos.RfqRequest request) {
    Rfq rfq = getRfqOrThrow(id);
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

    rfq.setRfqNo(request.rfqNo());
    rfq.setSupplier(supplier);
    rfq.setRfqDate(request.rfqDate());
    rfq.setRemarks(request.remarks());

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
    if (rfq.getStatus() != DocumentStatus.SUBMITTED) {
      throw new IllegalArgumentException("Only SUBMITTED RFQs can be approved");
    }
    rfq.setStatus(DocumentStatus.APPROVED);
    return toResponse(rfqRepository.save(rfq));
  }

  private RfqLine toLineEntity(Rfq rfq, TransactionDtos.RfqLineRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    RfqLine line = new RfqLine();
    line.setRfq(rfq);
    line.setItem(item);
    line.setUom(uom);
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
    line.setItem(item);
    line.setUom(uom);
    line.setQuantity(request.quantity());
    line.setRateExpected(request.rateExpected());
    line.setRemarks(request.remarks());
  }

  private TransactionDtos.RfqResponse toResponse(Rfq rfq) {
    List<TransactionDtos.RfqLineResponse> lines = rfq.getLines().stream()
        .map(line -> new TransactionDtos.RfqLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getQuantity(),
            line.getRateExpected(),
            line.getRemarks()))
        .toList();

    return new TransactionDtos.RfqResponse(
        rfq.getId(),
        rfq.getRfqNo(),
        rfq.getSupplier() != null ? rfq.getSupplier().getId() : null,
        rfq.getRfqDate(),
        rfq.getRemarks(),
        rfq.getStatus().name(),
        lines);
  }

  private Rfq getRfqOrThrow(Long id) {
    return rfqRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RFQ not found"));
  }
}
