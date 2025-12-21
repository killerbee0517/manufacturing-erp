package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnService {
  private final GrnRepository grnRepository;
  private final GrnLineRepository grnLineRepository;
  private final SupplierRepository supplierRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final ItemRepository itemRepository;
  private final LocationRepository locationRepository;
  private final UomRepository uomRepository;
  private final StockLedgerService stockLedgerService;
  private final PurchaseOrderRepository purchaseOrderRepository;

  public GrnService(GrnRepository grnRepository,
                    GrnLineRepository grnLineRepository,
                    SupplierRepository supplierRepository,
                    WeighbridgeTicketRepository weighbridgeTicketRepository,
                    ItemRepository itemRepository,
                    LocationRepository locationRepository,
                    UomRepository uomRepository,
                    StockLedgerService stockLedgerService,
                    PurchaseOrderRepository purchaseOrderRepository) {
    this.grnRepository = grnRepository;
    this.grnLineRepository = grnLineRepository;
    this.supplierRepository = supplierRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.itemRepository = itemRepository;
    this.locationRepository = locationRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Transactional
  public Grn createGrn(GrnDtos.CreateGrnRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    PurchaseOrder purchaseOrder = request.purchaseOrderId() != null
        ? purchaseOrderRepository.findById(request.purchaseOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"))
        : null;
    WeighbridgeTicket ticket = request.weighbridgeTicketId() != null
        ? weighbridgeTicketRepository.findById(request.weighbridgeTicketId())
            .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"))
        : null;
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Location qcHold = locationRepository.findByCode("QC_HOLD")
        .orElseThrow(() -> new IllegalArgumentException("QC_HOLD location missing"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));

    Grn grn = new Grn();
    grn.setGrnNo(resolveGrnNo(request.grnNo()));
    grn.setSupplier(supplier);
    grn.setWeighbridgeTicket(ticket);
    grn.setPurchaseOrder(purchaseOrder);
    grn.setItem(item);
    grn.setUom(uom);
    grn.setGrnDate(request.grnDate());
    grn.setNarration(request.narration());
    grn.setFirstWeight(request.firstWeight());
    grn.setSecondWeight(request.secondWeight());
    grn.setNetWeight(resolveNetWeight(request));
    grn.setQuantity(request.quantity());
    grn.setStatus(DocumentStatus.POSTED);
    Grn saved = grnRepository.save(grn);

    GrnLine line = new GrnLine();
    line.setGrn(saved);
    line.setItem(item);
    line.setUom(uom);
    line.setBagType("N/A");
    line.setBagCount(0);
    line.setQuantity(request.quantity());
    line.setWeight(resolveNetWeight(request));
    GrnLine savedLine = grnLineRepository.save(line);

    stockLedgerService.postEntry("GRN", saved.getId(), savedLine.getId(), LedgerTxnType.IN,
        savedLine.getItem(), uom, null, qcHold, savedLine.getQuantity(), savedLine.getWeight(), StockStatus.QC_HOLD);

    return saved;
  }

  private String resolveGrnNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "GRN-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        + "-" + System.nanoTime();
  }

  private BigDecimal resolveNetWeight(GrnDtos.CreateGrnRequest request) {
    if (request.netWeight() != null) {
      return request.netWeight();
    }
    if (request.firstWeight() != null && request.secondWeight() != null) {
      return request.secondWeight().subtract(request.firstWeight()).abs();
    }
    return BigDecimal.ZERO;
  }
}
