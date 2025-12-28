package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.PurchaseOrderLineRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrnService {
  private final GrnRepository grnRepository;
  private final GrnLineRepository grnLineRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final StockLedgerService stockLedgerService;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final GodownRepository godownRepository;
  private final PurchaseOrderLineRepository purchaseOrderLineRepository;

  public GrnService(GrnRepository grnRepository,
                    GrnLineRepository grnLineRepository,
                    WeighbridgeTicketRepository weighbridgeTicketRepository,
                    ItemRepository itemRepository,
                    UomRepository uomRepository,
                    StockLedgerService stockLedgerService,
                    PurchaseOrderRepository purchaseOrderRepository,
                    GodownRepository godownRepository,
                    PurchaseOrderLineRepository purchaseOrderLineRepository) {
    this.grnRepository = grnRepository;
    this.grnLineRepository = grnLineRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.godownRepository = godownRepository;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
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
    Godown godown = request.godownId() != null
        ? godownRepository.findById(request.godownId()).orElseThrow(() -> new IllegalArgumentException("Godown not found"))
        : null;

    Grn grn = new Grn();
    grn.setGrnNo(resolveGrnNo(request.grnNo()));
    grn.setSupplier(supplier);
    grn.setWeighbridgeTicket(ticket);
    grn.setPurchaseOrder(purchaseOrder);
    grn.setGodown(godown);
    grn.setGrnDate(request.grnDate());
    grn.setReceivedDate(request.grnDate());
    grn.setNarration(request.narration());
    grn.setFirstWeight(request.firstWeight() != null ? request.firstWeight() : ticket != null ? ticket.getGrossWeight() : null);
    grn.setSecondWeight(request.secondWeight() != null ? request.secondWeight() : ticket != null ? ticket.getUnloadedWeight() : null);
    grn.setNetWeight(resolveNetWeight(request, ticket));
    grn.setStatus(DocumentStatus.DRAFT);
    Grn saved = grnRepository.save(grn);

    for (GrnDtos.GrnLineRequest lineRequest : request.lines()) {
      Item item = itemRepository.findById(lineRequest.itemId())
          .orElseThrow(() -> new IllegalArgumentException("Item not found"));
      Uom uom = uomRepository.findById(lineRequest.uomId())
          .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
      PurchaseOrderLine poLine = lineRequest.poLineId() != null
          ? purchaseOrderLineRepository.findById(lineRequest.poLineId())
              .orElseThrow(() -> new IllegalArgumentException("PO line not found"))
          : null;
      GrnLine line = new GrnLine();
      line.setGrn(saved);
      line.setPurchaseOrderLine(poLine);
      line.setItem(item);
      line.setUom(uom);
      line.setBagType("N/A");
      line.setBagCount(0);
      line.setQuantity(lineRequest.quantity());
      line.setExpectedQty(lineRequest.quantity());
      line.setReceivedQty(lineRequest.quantity());
      line.setAcceptedQty(lineRequest.quantity());
      line.setRejectedQty(BigDecimal.ZERO);
      line.setWeight(lineRequest.weight() != null ? lineRequest.weight() : lineRequest.quantity());
      line.setRate(lineRequest.rate());
      line.setAmount(lineRequest.amount() != null ? lineRequest.amount() : resolveAmount(lineRequest));
      GrnLine savedLine = grnLineRepository.save(line);
      saved.getLines().add(savedLine);
    }

    return saved;
  }

  @Transactional
  public Grn createDraftFromWeighbridge(WeighbridgeTicket ticket) {
    Optional<Grn> existing = grnRepository.findFirstByWeighbridgeTicketId(ticket.getId());
    if (existing.isPresent()) {
      return existing.get();
    }
    if (ticket.getStatus() != DocumentStatus.UNLOADED && ticket.getStatus() != DocumentStatus.POSTED) {
      throw new IllegalStateException("Weighbridge ticket must be completed before creating GRN");
    }
    PurchaseOrder po = ticket.getPurchaseOrder();
    if (po == null) {
      throw new IllegalStateException("Weighbridge ticket must reference a purchase order");
    }
    Grn grn = new Grn();
    grn.setGrnNo(resolveGrnNo(null));
    grn.setSupplier(po.getSupplier());
    grn.setPurchaseOrder(po);
    grn.setWeighbridgeTicket(ticket);
    grn.setGrnDate(java.time.LocalDate.now());
    grn.setReceivedDate(java.time.LocalDate.now());
    grn.setFirstWeight(ticket.getGrossWeight());
    grn.setSecondWeight(ticket.getUnloadedWeight());
    grn.setNetWeight(ticket.getNetWeight());
    grn.setStatus(DocumentStatus.DRAFT);
    Grn saved = grnRepository.save(grn);

    BigDecimal totalPoQty = po.getLines().stream()
        .map(line -> line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal net = ticket.getNetWeight() != null ? ticket.getNetWeight() : BigDecimal.ZERO;

    for (PurchaseOrderLine poLine : po.getLines()) {
      BigDecimal lineQty = poLine.getQuantity() != null ? poLine.getQuantity() : BigDecimal.ZERO;
      BigDecimal proportion = totalPoQty.compareTo(BigDecimal.ZERO) > 0
          ? lineQty.divide(totalPoQty, 6, java.math.RoundingMode.HALF_UP)
          : BigDecimal.ZERO;
      BigDecimal lineWeight = net.multiply(proportion);

      GrnLine line = new GrnLine();
      line.setGrn(saved);
      line.setPurchaseOrderLine(poLine);
      line.setItem(poLine.getItem());
      line.setUom(poLine.getUom());
      line.setBagType("N/A");
      line.setBagCount(0);
      line.setQuantity(lineQty);
      line.setExpectedQty(lineQty);
      line.setReceivedQty(lineQty);
      line.setAcceptedQty(lineQty);
      line.setRejectedQty(BigDecimal.ZERO);
      line.setWeight(lineWeight);
      line.setRate(poLine.getRate());
      line.setAmount(poLine.getRate() != null && lineQty != null ? poLine.getRate().multiply(lineQty) : BigDecimal.ZERO);
      grnLineRepository.save(line);
      saved.getLines().add(line);
    }
    return saved;
  }

  @Transactional
  public Grn updateDraft(Long grnId, GrnDtos.UpdateGrnRequest request) {
    Grn grn = grnRepository.findById(grnId)
        .orElseThrow(() -> new IllegalArgumentException("GRN not found"));
    if (grn.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalStateException("Only draft GRN can be updated");
    }
    if (request.godownId() != null) {
      Godown godown = godownRepository.findById(request.godownId())
          .orElseThrow(() -> new IllegalArgumentException("Godown not found"));
      grn.setGodown(godown);
    }
    grn.setNarration(request.narration());
    return grnRepository.save(grn);
  }

  @Transactional
  public Grn post(Long grnId) {
    Grn grn = grnRepository.findById(grnId)
        .orElseThrow(() -> new IllegalArgumentException("GRN not found"));
    if (grn.getStatus() == DocumentStatus.POSTED) {
      return grn;
    }
    if (grn.getGodown() == null) {
      throw new IllegalStateException("Godown is required before posting GRN");
    }
    if (grn.getFirstWeight() == null && grn.getWeighbridgeTicket() != null) {
      grn.setFirstWeight(grn.getWeighbridgeTicket().getGrossWeight());
    }
    if (grn.getSecondWeight() == null && grn.getWeighbridgeTicket() != null) {
      grn.setSecondWeight(grn.getWeighbridgeTicket().getUnloadedWeight());
    }
    if (grn.getNetWeight() == null && grn.getWeighbridgeTicket() != null) {
      grn.setNetWeight(grn.getWeighbridgeTicket().getNetWeight());
    }
    for (GrnLine line : grn.getLines()) {
      BigDecimal qty = resolveAcceptedQty(line);
      BigDecimal weight = line.getWeight() != null ? line.getWeight() : qty;
      stockLedgerService.postEntry("GRN", grn.getId(), line.getId(), LedgerTxnType.IN,
          line.getItem(), line.getUom(), null, null, null, grn.getGodown(),
          qty, weight, StockStatus.UNRESTRICTED, line.getRate(), line.getAmount());
    }
    grn.setStatus(DocumentStatus.POSTED);
    return grnRepository.save(grn);
  }

  private BigDecimal resolveAcceptedQty(GrnLine line) {
    if (line.getAcceptedQty() != null) {
      return line.getAcceptedQty();
    }
    if (line.getReceivedQty() != null) {
      return line.getReceivedQty();
    }
    if (line.getQuantity() != null) {
      return line.getQuantity();
    }
    if (line.getExpectedQty() != null) {
      return line.getExpectedQty();
    }
    return BigDecimal.ZERO;
  }

  private String resolveGrnNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "GRN-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        + "-" + System.nanoTime();
  }

  private BigDecimal resolveNetWeight(GrnDtos.CreateGrnRequest request, WeighbridgeTicket ticket) {
    if (request.netWeight() != null) {
      return request.netWeight();
    }
    BigDecimal first = request.firstWeight();
    BigDecimal second = request.secondWeight();
    if (first == null && ticket != null) {
      first = ticket.getGrossWeight();
    }
    if (second == null && ticket != null) {
      second = ticket.getUnloadedWeight();
    }
    if (first != null && second != null) {
      return second.subtract(first).abs();
    }
    return ticket != null ? ticket.getNetWeight() : BigDecimal.ZERO;
  }

  private BigDecimal resolveAmount(GrnDtos.GrnLineRequest request) {
    if (request.amount() != null) {
      return request.amount();
    }
    if (request.rate() != null && request.quantity() != null) {
      return request.rate().multiply(request.quantity());
    }
    return BigDecimal.ZERO;
  }
}
