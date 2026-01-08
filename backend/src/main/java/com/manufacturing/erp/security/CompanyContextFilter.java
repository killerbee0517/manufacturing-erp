package com.manufacturing.erp.security;

import com.manufacturing.erp.repository.UserCompanyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CompanyContextFilter extends OncePerRequestFilter {
  private final CompanyContext companyContext;
  private final UserCompanyRepository userCompanyRepository;

  public CompanyContextFilter(CompanyContext companyContext, UserCompanyRepository userCompanyRepository) {
    this.companyContext = companyContext;
    this.userCompanyRepository = userCompanyRepository;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth")
        || path.startsWith("/actuator")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.equals("/api/companies/my");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated()) {
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        String companyHeader = request.getHeader("X-Company-Id");
        if (companyHeader == null || companyHeader.isBlank()) {
          writeError(response, HttpStatus.BAD_REQUEST, "Missing X-Company-Id header");
          return;
        }
        Long companyId;
        try {
          companyId = Long.parseLong(companyHeader);
        } catch (NumberFormatException ex) {
          writeError(response, HttpStatus.BAD_REQUEST, "Invalid company id");
          return;
        }
        String username = authentication.getName();
        if (!isAdmin) {
          boolean hasAccess = userCompanyRepository
              .findByUserUsernameAndCompanyId(username, companyId)
              .isPresent();
          if (!hasAccess) {
            writeError(response, HttpStatus.FORBIDDEN, "Company access denied");
            return;
          }
        }
        companyContext.setCompanyId(companyId);
      }
      filterChain.doFilter(request, response);
    } finally {
      companyContext.clear();
    }
  }

  private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
    response.setStatus(status.value());
    response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"message\":\"" + message + "\"}");
  }
}
