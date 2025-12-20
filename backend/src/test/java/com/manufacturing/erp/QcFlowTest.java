package com.manufacturing.erp;

import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.GrnLineRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.StockLedgerRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.service.GrnService;
import com.manufacturing.erp.service.QcService;
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
class QcFlowTest {
  @Autowired
  private WeighbridgeService weighbridgeService;

  @Autowired
  private GrnService grnService;

  @Autowired
  private QcService qcService;

  @Autowired
  private SupplierRepository supplierRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private GrnLineRepository grnLineRepository;

  @Autowired
  private StockLedgerRepository stockLedgerRepository;

  @Test
  void qcAcceptanceMovesStockToUnrestricted() {
    Long supplierId = supplierRepository.findAll().getFirst().getId();
    Long itemId = itemRepository.findAll().getFirst().getId();

    var ticket = weighbridgeService.createTicket(new WeighbridgeDtos.CreateTicketRequest(
        "WB-2001",
        "TN10CD5678",
        supplierId,
        itemId,
        LocalDate.now(),
        LocalTime.now(),
        List.of(
            new WeighbridgeDtos.ReadingRequest("GROSS", new BigDecimal("1500"), Instant.now()),
            new WeighbridgeDtos.ReadingRequest("TARE", new BigDecimal("300"), Instant.now())
        )
    ));

    grnService.createGrn(new GrnDtos.CreateGrnRequest(
        "GRN-1001",
        supplierId,
        ticket.getId(),
        LocalDate.now(),
        List.of(new GrnDtos.GrnLineRequest(itemId, "JUTE", 10, new BigDecimal("1200"), new BigDecimal("1200")))
    ));

    var grnLineId = grnLineRepository.findAll().getFirst().getId();

    qcService.updateStatus(new QcDtos.QcUpdateRequest(grnLineId, "ACCEPTED", LocalDate.now()));

    var ledgerEntries = stockLedgerRepository.findAll();
    assertThat(ledgerEntries).hasSizeGreaterThanOrEqualTo(2);
    assertThat(ledgerEntries.stream().anyMatch(entry -> entry.getStatus().name().equals("UNRESTRICTED"))).isTrue();
  }
}
