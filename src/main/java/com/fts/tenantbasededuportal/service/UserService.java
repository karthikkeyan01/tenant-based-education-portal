package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.user.UserResponseDto;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.fts.tenantbasededuportal.util.RoleConstants;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    public List<UserResponseDto> fetchUsers(){

        final User currentUser = this.securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        final List<User> users;

        if(RoleConstants.SUPER_ADMIN.equals(roleName)){

            users = this.userRepository.findAll();
        }
        else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            users = this.userRepository.findByOrganization(
                    currentUser.getOrganization());
        }
        else {
            throw new UnauthorizedException(
                    "you don't have permission to view users");
        }

        final List<UserResponseDto> response = new ArrayList<>();

        for(final User user : users){

            String organizationName = null;

            if (user.getOrganization() != null){

                organizationName = user.getOrganization().getName();
            }

            response.add(
                    UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .roleName(user.getRole().getName())
                            .mfaEnabled(user.getMfaEnabled())
                            .deleted(user.getDeleted())
                            .organizationName(organizationName)
                            .createdAt(user.getCreatedAt())
                            .build()
            );
        }

        return response;
    }
}
