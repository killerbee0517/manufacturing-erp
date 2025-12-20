package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.util.List;
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

  public GrnService(GrnRepository grnRepository,
                    GrnLineRepository grnLineRepository,
                    SupplierRepository supplierRepository,
                    WeighbridgeTicketRepository weighbridgeTicketRepository,
                    ItemRepository itemRepository,
                    LocationRepository locationRepository,
                    UomRepository uomRepository,
                    StockLedgerService stockLedgerService) {
    this.grnRepository = grnRepository;
    this.grnLineRepository = grnLineRepository;
    this.supplierRepository = supplierRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.itemRepository = itemRepository;
    this.locationRepository = locationRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
  }

  @Transactional
  public Grn createGrn(GrnDtos.CreateGrnRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    WeighbridgeTicket ticket = weighbridgeTicketRepository.findById(request.weighbridgeTicketId())
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    Location qcHold = locationRepository.findByCode("QC_HOLD")
        .orElseThrow(() -> new IllegalArgumentException("QC_HOLD location missing"));
    Uom uom = uomRepository.findByCode("KG")
        .orElseThrow(() -> new IllegalArgumentException("UOM KG missing"));

    Grn grn = new Grn();
    grn.setGrnNo(request.grnNo());
    grn.setSupplier(supplier);
    grn.setWeighbridgeTicket(ticket);
    grn.setGrnDate(request.grnDate());
    grn.setStatus(DocumentStatus.POSTED);
    Grn saved = grnRepository.save(grn);

    List<GrnLine> lines = request.lines().stream().map(line -> {
      Item item = itemRepository.findById(line.itemId())
          .orElseThrow(() -> new IllegalArgumentException("Item not found"));
      GrnLine entity = new GrnLine();
      entity.setGrn(saved);
      entity.setItem(item);
      entity.setBagType(line.bagType());
      entity.setBagCount(line.bagCount());
      entity.setQuantity(line.quantity());
      entity.setWeight(line.weight());
      return grnLineRepository.save(entity);
    }).toList();

    for (GrnLine line : lines) {
      stockLedgerService.postEntry("GRN", saved.getId(), line.getId(), LedgerTxnType.IN,
          line.getItem(), uom, null, qcHold, line.getQuantity(), line.getWeight(), StockStatus.QC_HOLD);
    }

    return saved;
  }
}
