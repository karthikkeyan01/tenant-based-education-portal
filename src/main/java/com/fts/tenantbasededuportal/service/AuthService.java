package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.auth.LoginRequestDto;
import com.fts.tenantbasededuportal.dtos.auth.LoginResponseDto;
import com.fts.tenantbasededuportal.dtos.auth.RegisterRequestDto;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.security.JwtService;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final AuditService auditService;

    private final SecurityUtil  securityUtil;

    public void register(final RegisterRequestDto request){

        if(this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        final Role userRole = this.roleRepository.findByName("USER")
                .orElseThrow(() -> new BadRequestException(
                        "Default User role not found"));

        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .deleted(false)
                .mfaEnabled(false)
                .build();

        this.userRepository.save(user);

        this.auditService.log(
                user,
                "REGISTER",
                "USER",
                user.getId(),
                "User registered with email " + user.getEmail());
    }

    public LoginResponseDto login(final LoginRequestDto request){

        final Authentication authentication = this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        final UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal == null) {
            throw new UnauthorizedException(
                    "Authenticated user not found"
            );
        }

        final String token = this.jwtService.generateToken(userPrincipal);

        this.auditService.log(
                userPrincipal.getUser(),
                "LOGIN",
                "USER",
                userPrincipal.getUser().getId(),
                "User logged in");

        return LoginResponseDto.builder()
                .accessToken(token)
                .email(userPrincipal.getUsername())
                .role(userPrincipal.getRoleName())
                .mfaRequired(userPrincipal.getMfaEnabled())
                .build();
    }

    public void logout(){

        final User currentUser = this.securityUtil.getCurrentUser();

        this.auditService.log(
                currentUser,
                "LOGOUT",
                "USER",
                currentUser.getId(),
                "User logged out");
    }
}
