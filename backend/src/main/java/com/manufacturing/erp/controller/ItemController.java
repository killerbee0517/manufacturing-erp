package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.UomRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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
  public List<MasterDtos.ItemResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Item> items = (q == null || q.isBlank())
        ? itemRepository.findAll()
        : itemRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(q, q);
    return applyLimit(items, limit).stream()
        .map(item -> new MasterDtos.ItemResponse(
            item.getId(), item.getName(), item.getSku(), item.getUom() != null ? item.getUom().getId() : null))
        .toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.ItemResponse get(@PathVariable Long id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    return new MasterDtos.ItemResponse(
        item.getId(), item.getName(), item.getSku(), item.getUom() != null ? item.getUom().getId() : null);
  }

  @PostMapping
  @Transactional
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

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.ItemResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.ItemRequest request) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    Uom uom = uomRepository.findById(request.uomId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "UOM not found"));
    item.setName(request.name());
    item.setSku(request.sku());
    item.setUom(uom);
    Item saved = itemRepository.save(item);
    return new MasterDtos.ItemResponse(saved.getId(), saved.getName(), saved.getSku(), saved.getUom().getId());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!itemRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
    }
    itemRepository.deleteById(id);
  }

  private List<Item> applyLimit(List<Item> items, Integer limit) {
    if (limit == null) {
      return items;
    }
    return items.stream().limit(limit).toList();
  }
}
