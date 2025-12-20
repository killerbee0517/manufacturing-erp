package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.StockTransferDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.UomRepository;
import org.springframework.stereotype.Service;

@Service
public class StockTransferService {
  private final StockLedgerService stockLedgerService;
  private final ItemRepository itemRepository;
  private final LocationRepository locationRepository;
  private final UomRepository uomRepository;

  public StockTransferService(StockLedgerService stockLedgerService,
                              ItemRepository itemRepository,
                              LocationRepository locationRepository,
                              UomRepository uomRepository) {
    this.stockLedgerService = stockLedgerService;
    this.itemRepository = itemRepository;
    this.locationRepository = locationRepository;
    this.uomRepository = uomRepository;
  }

  public void transfer(StockTransferDtos.TransferRequest request) {
    Item item = itemRepository.findById(request.itemId())
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    Location fromLocation = locationRepository.findById(request.fromLocationId())
        .orElseThrow(() -> new IllegalArgumentException("From location not found"));
    Location toLocation = locationRepository.findById(request.toLocationId())
        .orElseThrow(() -> new IllegalArgumentException("To location not found"));
    Uom uom = uomRepository.findByCode("KG")
        .orElseThrow(() -> new IllegalArgumentException("UOM KG missing"));

    stockLedgerService.postEntry("STOCK_TRANSFER", 0L, null, LedgerTxnType.MOVE, item, uom,
        fromLocation, toLocation, request.quantity(), request.weight(), StockStatus.UNRESTRICTED);
  }
}
