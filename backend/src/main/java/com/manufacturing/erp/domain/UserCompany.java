package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_companies")
public class UserCompany extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "primary_company", nullable = false)
  private boolean primaryCompany = false;

  @Column(name = "role_scope")
  private String roleScope;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public boolean isPrimaryCompany() {
    return primaryCompany;
  }

  public void setPrimaryCompany(boolean primaryCompany) {
    this.primaryCompany = primaryCompany;
  }

  public String getRoleScope() {
    return roleScope;
  }

  public void setRoleScope(String roleScope) {
    this.roleScope = roleScope;
  }
}
