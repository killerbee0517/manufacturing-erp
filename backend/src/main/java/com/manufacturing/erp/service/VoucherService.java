package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.Voucher;
import com.manufacturing.erp.domain.VoucherLine;
import com.manufacturing.erp.repository.VoucherLineRepository;
import com.manufacturing.erp.repository.VoucherRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherService {
  private final VoucherRepository voucherRepository;
  private final VoucherLineRepository voucherLineRepository;

  public VoucherService(VoucherRepository voucherRepository, VoucherLineRepository voucherLineRepository) {
    this.voucherRepository = voucherRepository;
    this.voucherLineRepository = voucherLineRepository;
  }

  @Transactional
  public Voucher createVoucher(String referenceType, Long referenceId, LocalDate voucherDate, String narration,
                               List<VoucherLineRequest> lines) {
    BigDecimal drTotal = lines.stream().map(VoucherLineRequest::drAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal crTotal = lines.stream().map(VoucherLineRequest::crAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (drTotal.compareTo(crTotal) != 0) {
      throw new IllegalArgumentException("Voucher is not balanced");
    }

    Voucher voucher = new Voucher();
    voucher.setVoucherNo("VCH-" + System.currentTimeMillis());
    voucher.setVoucherDate(voucherDate != null ? voucherDate : LocalDate.now());
    voucher.setNarration(narration);
    voucher.setReferenceType(referenceType);
    voucher.setReferenceId(referenceId);
    Voucher saved = voucherRepository.save(voucher);

    List<VoucherLine> entities = new ArrayList<>();
    for (VoucherLineRequest line : lines) {
      VoucherLine entity = new VoucherLine();
      entity.setVoucher(saved);
      entity.setLedger(line.ledger());
      entity.setDrAmount(line.drAmount());
      entity.setCrAmount(line.crAmount());
      entities.add(entity);
    }
    voucherLineRepository.saveAll(entities);
    return saved;
  }

  public record VoucherLineRequest(Ledger ledger, BigDecimal drAmount, BigDecimal crAmount) {}
}
