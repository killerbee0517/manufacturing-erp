package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Uom;
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
@RequestMapping("/api/uoms")
public class UomController {
  private final UomRepository uomRepository;

  public UomController(UomRepository uomRepository) {
    this.uomRepository = uomRepository;
  }

  @GetMapping
  public List<UomResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Uom> uoms = (q == null || q.isBlank())
        ? uomRepository.findAll()
        : uomRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q);
    return applyLimit(uoms, limit).stream()
        .map(uom -> new UomResponse(uom.getId(), uom.getCode(), uom.getDescription()))
        .toList();
  }

  @GetMapping("/{id}")
  public UomResponse get(@PathVariable Long id) {
    Uom uom = uomRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UOM not found"));
    return new UomResponse(uom.getId(), uom.getCode(), uom.getDescription());
  }

  @PostMapping
  @Transactional
  public UomResponse create(@Valid @RequestBody UomRequest request) {
    Uom uom = new Uom();
    uom.setCode(request.code());
    uom.setDescription(request.description());
    Uom saved = uomRepository.save(uom);
    return new UomResponse(saved.getId(), saved.getCode(), saved.getDescription());
  }

  @PutMapping("/{id}")
  @Transactional
  public UomResponse update(@PathVariable Long id, @Valid @RequestBody UomRequest request) {
    Uom uom = uomRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UOM not found"));
    uom.setCode(request.code());
    uom.setDescription(request.description());
    Uom saved = uomRepository.save(uom);
    return new UomResponse(saved.getId(), saved.getCode(), saved.getDescription());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!uomRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UOM not found");
    }
    uomRepository.deleteById(id);
  }

  public record UomResponse(Long id, String code, String description) {}
  public record UomRequest(String code, String description) {}

  private List<Uom> applyLimit(List<Uom> uoms, Integer limit) {
    if (limit == null) {
      return uoms;
    }
    return uoms.stream().limit(limit).toList();
  }
}
