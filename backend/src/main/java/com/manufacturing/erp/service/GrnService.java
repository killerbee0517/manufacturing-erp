package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
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
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnService {
  private final GrnRepository grnRepository;
  private final GrnLineRepository grnLineRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final ItemRepository itemRepository;
  private final LocationRepository locationRepository;
  private final UomRepository uomRepository;
  private final StockLedgerService stockLedgerService;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final GodownRepository godownRepository;

  public GrnService(GrnRepository grnRepository,
                    GrnLineRepository grnLineRepository,
                    WeighbridgeTicketRepository weighbridgeTicketRepository,
                    ItemRepository itemRepository,
                    LocationRepository locationRepository,
                    UomRepository uomRepository,
                    StockLedgerService stockLedgerService,
                    PurchaseOrderRepository purchaseOrderRepository,
                    GodownRepository godownRepository) {
    this.grnRepository = grnRepository;
    this.grnLineRepository = grnLineRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.itemRepository = itemRepository;
    this.locationRepository = locationRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.godownRepository = godownRepository;
  }

  @Transactional
  public Grn createGrn(GrnDtos.CreateGrnRequest request) {
    PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(request.purchaseOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));
    Supplier supplier = purchaseOrder.getSupplier();
    WeighbridgeTicket ticket = request.weighbridgeTicketId() != null
        ? weighbridgeTicketRepository.findById(request.weighbridgeTicketId())
            .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"))
        : null;
    Location qcHold = locationRepository.findByCode("QC_HOLD")
        .orElseThrow(() -> new IllegalArgumentException("QC_HOLD location missing"));
    Godown godown = godownRepository.findById(request.godownId())
        .orElseThrow(() -> new IllegalArgumentException("Godown not found"));

    Grn grn = new Grn();
    grn.setGrnNo(resolveGrnNo(request.grnNo()));
    grn.setSupplier(supplier);
    grn.setWeighbridgeTicket(ticket);
    grn.setPurchaseOrder(purchaseOrder);
    grn.setGodown(godown);
    grn.setGrnDate(request.grnDate());
    grn.setNarration(request.narration());
    grn.setFirstWeight(request.firstWeight());
    grn.setSecondWeight(request.secondWeight());
    grn.setNetWeight(resolveNetWeight(request));
    grn.setStatus(DocumentStatus.POSTED);
    Grn saved = grnRepository.save(grn);

    for (GrnDtos.GrnLineRequest lineRequest : request.lines()) {
      Item item = itemRepository.findById(lineRequest.itemId())
          .orElseThrow(() -> new IllegalArgumentException("Item not found"));
      Uom uom = uomRepository.findById(lineRequest.uomId())
          .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
      GrnLine line = new GrnLine();
      line.setGrn(saved);
      line.setItem(item);
      line.setUom(uom);
      line.setBagType("N/A");
      line.setBagCount(0);
      line.setQuantity(lineRequest.quantity());
      BigDecimal weight = lineRequest.weight() != null ? lineRequest.weight() : lineRequest.quantity();
      line.setWeight(weight);
      GrnLine savedLine = grnLineRepository.save(line);
      saved.getLines().add(savedLine);

      stockLedgerService.postEntry("GRN", saved.getId(), savedLine.getId(), LedgerTxnType.IN,
          savedLine.getItem(), uom, null, qcHold, null, godown,
          savedLine.getQuantity(), savedLine.getWeight(), StockStatus.QC_HOLD);
    }

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
