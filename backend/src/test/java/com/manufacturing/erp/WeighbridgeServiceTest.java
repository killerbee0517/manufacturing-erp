package com.manufacturing.erp;

import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.service.WeighbridgeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WeighbridgeServiceTest {
  @Autowired
  private WeighbridgeService weighbridgeService;

  @Autowired
  private SupplierRepository supplierRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Test
  void calculatesNetWeightFromReadings() {
    Long supplierId = supplierRepository.findAll().getFirst().getId();
    Long itemId = itemRepository.findAll().getFirst().getId();

    WeighbridgeDtos.CreateTicketRequest request = new WeighbridgeDtos.CreateTicketRequest(
        "WB-1001",
        "TN09AB1234",
        supplierId,
        itemId,
        LocalDate.now(),
        LocalTime.now(),
        List.of(
            new WeighbridgeDtos.ReadingRequest("GROSS", new BigDecimal("1200"), Instant.now()),
            new WeighbridgeDtos.ReadingRequest("TARE", new BigDecimal("200"), Instant.now())
        )
    );

    var ticket = weighbridgeService.createTicket(request);
    assertThat(ticket.getNetWeight()).isEqualByComparingTo("1000");
  }
}
