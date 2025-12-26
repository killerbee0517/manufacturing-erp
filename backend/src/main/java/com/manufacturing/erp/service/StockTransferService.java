package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.StockTransferHeader;
import com.manufacturing.erp.domain.StockTransferLine;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.StockTransferDtos;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.StockTransferHeaderRepository;
import com.manufacturing.erp.repository.StockTransferLineRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockTransferService {
  private final StockLedgerService stockLedgerService;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final GodownRepository godownRepository;
  private final StockTransferHeaderRepository stockTransferHeaderRepository;
  private final StockTransferLineRepository stockTransferLineRepository;

  public StockTransferService(StockLedgerService stockLedgerService,
                              ItemRepository itemRepository,
                              UomRepository uomRepository,
                              GodownRepository godownRepository,
                              StockTransferHeaderRepository stockTransferHeaderRepository,
                              StockTransferLineRepository stockTransferLineRepository) {
    this.stockLedgerService = stockLedgerService;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.godownRepository = godownRepository;
    this.stockTransferHeaderRepository = stockTransferHeaderRepository;
    this.stockTransferLineRepository = stockTransferLineRepository;
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<StockTransferDtos.StockTransferResponse> list(String status) {
    return stockTransferHeaderRepository.findAll().stream()
        .filter(header -> status == null || status.isBlank() || header.getStatus().name().equalsIgnoreCase(status))
        .map(this::toResponse)
        .toList();
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public StockTransferDtos.StockTransferResponse get(Long id) {
    StockTransferHeader header = stockTransferHeaderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Stock transfer not found"));
    return toResponse(header);
  }

  @org.springframework.transaction.annotation.Transactional
  public StockTransferDtos.StockTransferResponse save(StockTransferDtos.StockTransferRequest request) {
    StockTransferHeader header = request.id() != null
        ? stockTransferHeaderRepository.findById(request.id()).orElse(new StockTransferHeader())
        : new StockTransferHeader();
    header.setTransferNo(request.transferNo() != null ? request.transferNo() : generateTransferNo());
    header.setFromGodown(fetchGodown(request.fromGodownId()));
    header.setToGodown(fetchGodown(request.toGodownId()));
    header.setTransferDate(request.transferDate());
    header.setNarration(request.narration());
    header.setStatus(request.id() == null ? DocumentStatus.DRAFT : header.getStatus());

    header.getLines().clear();
    List<StockTransferLine> lines = new ArrayList<>();
    for (StockTransferDtos.StockTransferLineRequest lineRequest : request.lines()) {
      StockTransferLine line = new StockTransferLine();
      line.setHeader(header);
      line.setItem(fetchItem(lineRequest.itemId()));
      line.setUom(fetchUom(lineRequest.uomId()));
      line.setQuantity(lineRequest.qty());
      lines.add(line);
    }
    header.setLines(lines);
    StockTransferHeader saved = stockTransferHeaderRepository.save(header);
    stockTransferLineRepository.saveAll(lines);
    return toResponse(saved);
  }

  @org.springframework.transaction.annotation.Transactional
  public StockTransferDtos.StockTransferResponse post(Long id) {
    StockTransferHeader header = stockTransferHeaderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Stock transfer not found"));
    if (header.getStatus() == DocumentStatus.POSTED) {
      return toResponse(header);
    }
    for (StockTransferLine line : header.getLines()) {
      stockLedgerService.postEntry("STOCK_TRANSFER", header.getId(), line.getId(), LedgerTxnType.OUT,
          line.getItem(), line.getUom(), null, null, header.getFromGodown(), null,
          line.getQuantity(), line.getQuantity(), StockStatus.UNRESTRICTED);
      stockLedgerService.postEntry("STOCK_TRANSFER", header.getId(), line.getId(), LedgerTxnType.IN,
          line.getItem(), line.getUom(), null, null, null, header.getToGodown(),
          line.getQuantity(), line.getQuantity(), StockStatus.UNRESTRICTED);
    }
    header.setStatus(DocumentStatus.POSTED);
    StockTransferHeader saved = stockTransferHeaderRepository.save(header);
    return toResponse(saved);
  }

  private StockTransferDtos.StockTransferResponse toResponse(StockTransferHeader header) {
    List<StockTransferDtos.StockTransferLineResponse> lines = header.getLines().stream()
        .map(line -> new StockTransferDtos.StockTransferLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getQuantity()))
        .toList();
    return new StockTransferDtos.StockTransferResponse(
        header.getId(),
        header.getTransferNo(),
        header.getFromGodown() != null ? header.getFromGodown().getId() : null,
        header.getToGodown() != null ? header.getToGodown().getId() : null,
        header.getTransferDate(),
        header.getStatus().name(),
        header.getNarration(),
        lines);
  }

  private Item fetchItem(Long id) {
    return itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
  }

  private Uom fetchUom(Long id) {
    return uomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UOM not found"));
  }

  private Godown fetchGodown(Long id) {
    return godownRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Godown not found"));
  }

  private String generateTransferNo() {
    return "ST-" + System.currentTimeMillis();
  }
}
