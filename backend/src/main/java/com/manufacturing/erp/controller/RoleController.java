package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Role;
import com.manufacturing.erp.repository.RoleRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
  private final RoleRepository roleRepository;

  public RoleController(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @GetMapping
  public List<RoleResponse> list() {
    return roleRepository.findAll().stream()
        .map(role -> new RoleResponse(role.getId(), role.getName()))
        .toList();
  }

  public record RoleResponse(Long id, String name) {}
}
