package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {}
