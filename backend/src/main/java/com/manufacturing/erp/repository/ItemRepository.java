package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
  List<Item> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);
}
