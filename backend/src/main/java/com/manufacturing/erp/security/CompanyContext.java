package com.manufacturing.erp.security;

import org.springframework.stereotype.Component;

@Component
public class CompanyContext {
  private static final ThreadLocal<Long> COMPANY_HOLDER = new ThreadLocal<>();

  public void setCompanyId(Long companyId) {
    COMPANY_HOLDER.set(companyId);
  }

  public Long getCompanyId() {
    return COMPANY_HOLDER.get();
  }

  public void clear() {
    COMPANY_HOLDER.remove();
  }
}
