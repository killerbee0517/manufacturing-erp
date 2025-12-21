package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.UomRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;

  public ItemController(ItemRepository itemRepository, UomRepository uomRepository) {
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
  }

  @GetMapping
  public List<MasterDtos.ItemResponse> list() {
    return itemRepository.findAll().stream()
        .map(item -> new MasterDtos.ItemResponse(
            item.getId(), item.getName(), item.getSku(), item.getUom() != null ? item.getUom().getId() : null))
        .toList();
  }

  @PostMapping
  public MasterDtos.ItemResponse create(@Valid @RequestBody MasterDtos.ItemRequest request) {
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
    Item item = new Item();
    item.setName(request.name());
    item.setSku(request.sku());
    item.setUom(uom);
    Item saved = itemRepository.save(item);
    return new MasterDtos.ItemResponse(saved.getId(), saved.getName(), saved.getSku(), saved.getUom().getId());
  }
}
