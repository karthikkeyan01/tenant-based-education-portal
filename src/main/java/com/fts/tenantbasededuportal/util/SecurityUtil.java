package com.fts.tenantbasededuportal.util;

import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.security.CustomUserDetailsService;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.util.constants.RoleConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final CustomUserDetailsService customUserDetailsService;

    public void setAuthentication(final User user) {

        final UserPrincipal principal =
                this.customUserDetailsService.loadUserByUsername(user.getEmail());

        final Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,null,principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void clearAuthentication() {

        SecurityContextHolder.clearContext();
    }


    private UserPrincipal getPrincipal() {

        final Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null) {

            throw new UnauthorizedException("User is not authenticated.");
        }

        final Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {

            throw new UnauthorizedException("User is not authenticated.");
        }

        return userPrincipal;
    }

    public String getCurrentUserId() {

        final UserPrincipal principal = this.getPrincipal();

        return principal.getId();
    }

    public String getCurrentEmail() {

        final UserPrincipal principal = this.getPrincipal();

        return principal.getEmail();
    }

    public String getCurrentRole() {

        final UserPrincipal principal = this.getPrincipal();

        return principal.getRole();
    }

    public String getCurrentOrganizationId() {

        final UserPrincipal principal = this.getPrincipal();

        return principal.getOrganizationId();
    }

    public User getCurrentUser() {

        return this.userRepository.findByIdAndActiveTrue(
                        this.getCurrentUserId()).orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user not found."));
    }

    public Organization getCurrentOrganization() {

        final String organizationId =
                this.getCurrentOrganizationId();

        if (organizationId == null) {

            return null;
        }

        return this.organizationRepository
                .findByIdAndActiveTrue(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                                "Organization not found."));
    }

    public boolean isSuperAdmin() {

        return RoleConstants.SUPER_ADMIN
                .equals(this.getCurrentRole());
    }

    public boolean isOrgAdmin() {

        return RoleConstants.ORG_ADMIN
                .equals(this.getCurrentRole());
    }

    public boolean isUser(){

        return RoleConstants.USER
                .equals(this.getCurrentRole());
    }

    public boolean isCurrentUser(final String userId) {

        return userId != null && this.getCurrentUserId().equals(userId);
    }

    public boolean isSameOrganization(final String organizationId) {

        return organizationId != null
                && organizationId.equals(this.getCurrentOrganizationId());
    }
}