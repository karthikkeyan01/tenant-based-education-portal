package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    private final List<RolePermission> rolePermissions;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        final List<GrantedAuthority> authorities = new ArrayList<>();

        for(final RolePermission rolePermission : this.rolePermissions) {

            authorities.add(new SimpleGrantedAuthority(
                    rolePermission.getPermission().getName()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !this.user.getDeleted();
    }

    public Boolean getMfaEnabled(){
        return this.user.getMfaEnabled();
    }

    public String getRoleName(){
        return this.user.getRole().getName();
    }
}
