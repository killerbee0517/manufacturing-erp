package com.manufacturing.erp;

import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.service.TdsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TdsServiceTest {
  @Autowired
  private TdsService tdsService;

  @Autowired
  private SupplierRepository supplierRepository;

  @Test
  void calculatesTdsWhenThresholdExceeded() {
    Long supplierId = supplierRepository.findAll().getFirst().getId();
    BigDecimal tds = tdsService.calculateTds(supplierId, LocalDate.now(), new BigDecimal("6000000"));

    assertThat(tds).isGreaterThan(BigDecimal.ZERO);
  }
}
