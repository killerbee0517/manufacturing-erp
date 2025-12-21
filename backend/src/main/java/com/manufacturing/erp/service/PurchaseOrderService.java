package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
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
public class PurchaseOrderService {
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;

  public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                              SupplierRepository supplierRepository,
                              ItemRepository itemRepository,
                              UomRepository uomRepository) {
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
  }

  public Page<TransactionDtos.PurchaseOrderResponse> list(String q, String status, Pageable pageable) {
    Specification<PurchaseOrder> spec = Specification.where(null);
    if (q != null && !q.isBlank()) {
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("poNo")), "%" + q.toLowerCase() + "%"));
    }
    if (status != null && !status.isBlank()) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), DocumentStatus.valueOf(status.toUpperCase())));
    }
    return purchaseOrderRepository.findAll(spec, pageable).map(this::toResponse);
  }

  public TransactionDtos.PurchaseOrderResponse getById(Long id) {
    PurchaseOrder po = getPurchaseOrderOrThrow(id);
    return toResponse(po);
  }

  @Transactional
  public TransactionDtos.PurchaseOrderResponse create(TransactionDtos.PurchaseOrderRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    PurchaseOrder po = new PurchaseOrder();
    po.setPoNo(request.poNo());
    po.setSupplier(supplier);
    po.setPoDate(request.poDate());
    po.setRemarks(request.remarks());
    po.setStatus(DocumentStatus.DRAFT);

    request.lines().forEach(lineRequest -> po.getLines().add(toLineEntity(po, lineRequest)));
    po.setTotalAmount(calculateTotal(po.getLines()));

    return toResponse(purchaseOrderRepository.save(po));
  }

  @Transactional
  public TransactionDtos.PurchaseOrderResponse update(Long id, TransactionDtos.PurchaseOrderRequest request) {
    PurchaseOrder po = getPurchaseOrderOrThrow(id);
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

    po.setPoNo(request.poNo());
    po.setSupplier(supplier);
    po.setPoDate(request.poDate());
    po.setRemarks(request.remarks());

    Map<Long, PurchaseOrderLine> existingById = new HashMap<>();
    po.getLines().forEach(line -> existingById.put(line.getId(), line));

    po.getLines().clear();
    request.lines().forEach(lineRequest -> {
      PurchaseOrderLine line = lineRequest.id() != null ? existingById.get(lineRequest.id()) : null;
      if (line == null) {
        line = toLineEntity(po, lineRequest);
      } else {
        applyLineUpdates(line, lineRequest);
      }
      line.setPurchaseOrder(po);
      po.getLines().add(line);
    });

    po.setTotalAmount(calculateTotal(po.getLines()));
    return toResponse(purchaseOrderRepository.save(po));
  }

  @Transactional
  public TransactionDtos.PurchaseOrderResponse approve(Long id) {
    PurchaseOrder po = getPurchaseOrderOrThrow(id);
    if (po.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalArgumentException("Only DRAFT purchase orders can be approved");
    }
    po.setStatus(DocumentStatus.APPROVED);
    return toResponse(purchaseOrderRepository.save(po));
  }

  private PurchaseOrderLine toLineEntity(PurchaseOrder po, TransactionDtos.PurchaseOrderLineRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    PurchaseOrderLine line = new PurchaseOrderLine();
    line.setPurchaseOrder(po);
    line.setItem(item);
    line.setUom(uom);
    line.setQuantity(request.quantity());
    line.setRate(request.rate());
    line.setAmount(request.amount() != null ? request.amount() : request.rate().multiply(request.quantity()));
    line.setRemarks(request.remarks());
    return line;
  }

  private void applyLineUpdates(PurchaseOrderLine line, TransactionDtos.PurchaseOrderLineRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    line.setItem(item);
    line.setUom(uom);
    line.setQuantity(request.quantity());
    line.setRate(request.rate());
    line.setAmount(request.amount() != null ? request.amount() : request.rate().multiply(request.quantity()));
    line.setRemarks(request.remarks());
  }

  private BigDecimal calculateTotal(List<PurchaseOrderLine> lines) {
    return lines.stream()
        .map(line -> line.getAmount() != null ? line.getAmount() : line.getRate().multiply(line.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private TransactionDtos.PurchaseOrderResponse toResponse(PurchaseOrder po) {
    List<TransactionDtos.PurchaseOrderLineResponse> lines = po.getLines().stream()
        .map(line -> new TransactionDtos.PurchaseOrderLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getQuantity(),
            line.getRate(),
            line.getAmount(),
            line.getRemarks()))
        .toList();

    return new TransactionDtos.PurchaseOrderResponse(
        po.getId(),
        po.getPoNo(),
        po.getSupplier() != null ? po.getSupplier().getId() : null,
        po.getPoDate(),
        po.getRemarks(),
        po.getTotalAmount(),
        po.getStatus().name(),
        lines);
  }

  private PurchaseOrder getPurchaseOrderOrThrow(Long id) {
    return purchaseOrderRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
  }
}
