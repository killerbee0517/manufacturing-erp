package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {
  Optional<Company> findByCode(String code);

  @Query("select uc.company from UserCompany uc where uc.user.username = :username")
  List<Company> findCompaniesForUser(@Param("username") String username);
}
