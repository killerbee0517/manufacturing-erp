package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.UserCompany;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {
  List<UserCompany> findByUserUsername(String username);

  Optional<UserCompany> findByUserUsernameAndCompanyId(String username, Long companyId);
}
