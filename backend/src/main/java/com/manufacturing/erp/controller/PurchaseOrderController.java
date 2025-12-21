package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final SupplierRepository supplierRepository;

  public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository) {
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.supplierRepository = supplierRepository;
  }

  @GetMapping
  public List<TransactionDtos.PurchaseOrderResponse> list() {
    return purchaseOrderRepository.findAll().stream()
        .map(po -> new TransactionDtos.PurchaseOrderResponse(
            po.getId(),
            po.getPoNo(),
            po.getSupplier() != null ? po.getSupplier().getId() : null,
            po.getStatus()))
        .toList();
  }

  @PostMapping
  public TransactionDtos.PurchaseOrderResponse create(@Valid @RequestBody TransactionDtos.PurchaseOrderRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    PurchaseOrder po = new PurchaseOrder();
    po.setPoNo(request.poNo());
    po.setSupplier(supplier);
    po.setStatus(request.status() != null ? request.status() : "DRAFT");
    PurchaseOrder saved = purchaseOrderRepository.save(po);
    return new TransactionDtos.PurchaseOrderResponse(saved.getId(), saved.getPoNo(), supplier.getId(), saved.getStatus());
  }
}
