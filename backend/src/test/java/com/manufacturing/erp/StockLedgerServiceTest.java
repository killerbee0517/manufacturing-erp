package com.manufacturing.erp;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.LocationRepository;
import com.manufacturing.erp.repository.StockLedgerRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.service.StockLedgerService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class StockLedgerServiceTest {
  @Autowired
  private StockLedgerService stockLedgerService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private UomRepository uomRepository;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private StockLedgerRepository stockLedgerRepository;

  @Test
  void postsLedgerEntry() {
    var item = itemRepository.findAll().getFirst();
    var uom = uomRepository.findByCode("KG").orElseThrow();
    var location = locationRepository.findByCode("QC_HOLD").orElseThrow();

    var ledger = stockLedgerService.postEntry("TEST", 1L, 1L, LedgerTxnType.IN, item, uom, null, location,
        new BigDecimal("10"), new BigDecimal("10"), StockStatus.QC_HOLD);

    assertThat(stockLedgerRepository.findById(ledger.getId())).isPresent();
    assertThat(ledger.getQuantity()).isEqualByComparingTo("10");
  }
}
