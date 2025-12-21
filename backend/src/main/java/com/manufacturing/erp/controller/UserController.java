package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Role;
import com.manufacturing.erp.domain.User;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.RoleRepository;
import com.manufacturing.erp.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public List<MasterDtos.UserResponse> list() {
    return userRepository.findAll().stream()
        .map(user -> new MasterDtos.UserResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRoles().stream().map(Role::getName).toList()))
        .toList();
  }

  @PostMapping
  public MasterDtos.UserResponse create(@Valid @RequestBody MasterDtos.UserRequest request) {
    Role role = roleRepository.findByName(request.roleName() != null ? request.roleName() : "ADMIN")
        .orElseThrow(() -> new IllegalArgumentException("Role not found"));

    User user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setFullName(request.fullName());
    user.getRoles().add(role);

    User saved = userRepository.save(user);
    return new MasterDtos.UserResponse(
        saved.getId(),
        saved.getUsername(),
        saved.getFullName(),
        saved.getRoles().stream().map(Role::getName).toList());
  }
}
