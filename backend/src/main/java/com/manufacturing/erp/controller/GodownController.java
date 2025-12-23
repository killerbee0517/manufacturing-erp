package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.GodownRepository;
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
@RequestMapping("/api/godowns")
public class GodownController {
  private final GodownRepository godownRepository;

  public GodownController(GodownRepository godownRepository) {
    this.godownRepository = godownRepository;
  }

  @GetMapping
  public List<MasterDtos.GodownResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Godown> godowns = (q == null || q.isBlank())
        ? godownRepository.findAll()
        : godownRepository.findByNameContainingIgnoreCase(q);
    return applyLimit(godowns, limit).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.GodownResponse get(@PathVariable Long id) {
    Godown godown = godownRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Godown not found"));
    return toResponse(godown);
  }

  @PostMapping
  @Transactional
  public MasterDtos.GodownResponse create(@Valid @RequestBody MasterDtos.GodownRequest request) {
    Godown godown = new Godown();
    applyRequest(godown, request);
    Godown saved = godownRepository.save(godown);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.GodownResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.GodownRequest request) {
    Godown godown = godownRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Godown not found"));
    applyRequest(godown, request);
    Godown saved = godownRepository.save(godown);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!godownRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Godown not found");
    }
    godownRepository.deleteById(id);
  }

  private void applyRequest(Godown godown, MasterDtos.GodownRequest request) {
    godown.setName(request.name());
    godown.setLocation(request.location());
  }

  private MasterDtos.GodownResponse toResponse(Godown godown) {
    return new MasterDtos.GodownResponse(godown.getId(), godown.getName(), godown.getLocation());
  }

  private List<Godown> applyLimit(List<Godown> godowns, Integer limit) {
    if (limit == null) {
      return godowns;
    }
    return godowns.stream().limit(limit).toList();
  }
}
