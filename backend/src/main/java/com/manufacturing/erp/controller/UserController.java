package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Role;
import com.manufacturing.erp.domain.User;
import com.manufacturing.erp.domain.UserCompany;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.RoleRepository;
import com.manufacturing.erp.repository.UserCompanyRepository;
import com.manufacturing.erp.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
  private final CompanyRepository companyRepository;
  private final UserCompanyRepository userCompanyRepository;

  public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                        CompanyRepository companyRepository, UserCompanyRepository userCompanyRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.companyRepository = companyRepository;
    this.userCompanyRepository = userCompanyRepository;
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
  @Transactional
  public MasterDtos.UserResponse create(@Valid @RequestBody MasterDtos.UserRequest request) {
    Role role = roleRepository.findByName(request.roleName() != null ? request.roleName() : "ADMIN")
        .orElseThrow(() -> new IllegalArgumentException("Role not found"));

    User user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setFullName(request.fullName());
    user.getRoles().add(role);

    User saved = userRepository.save(user);
    List<Long> companyIds = request.companyIds();
    if (companyIds != null && !companyIds.isEmpty()) {
      List<Company> companies = companyRepository.findAllById(companyIds);
      for (int index = 0; index < companies.size(); index += 1) {
        Company company = companies.get(index);
        UserCompany userCompany = new UserCompany();
        userCompany.setUser(saved);
        userCompany.setCompany(company);
        userCompany.setPrimaryCompany(index == 0);
        userCompanyRepository.save(userCompany);
      }
    } else if (!"ADMIN".equalsIgnoreCase(role.getName())) {
      throw new IllegalArgumentException("At least one company is required for the user");
    }
    return new MasterDtos.UserResponse(
        saved.getId(),
        saved.getUsername(),
        saved.getFullName(),
        saved.getRoles().stream().map(Role::getName).toList());
  }
}
