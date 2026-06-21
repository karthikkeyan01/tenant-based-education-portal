package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.auth.LoginRequestDto;
import com.fts.tenantbasededuportal.dtos.auth.LoginResponseDto;
import com.fts.tenantbasededuportal.dtos.auth.RegisterRequestDto;
import com.fts.tenantbasededuportal.dtos.auth.VerifyMfaRequestDto;
import com.fts.tenantbasededuportal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody final RegisterRequestDto request){

        this.authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody final LoginRequestDto request){

        return ResponseEntity.ok(this.authService.login(request));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponseDto> verifyMfa(
            @RequestBody final VerifyMfaRequestDto request){

        return ResponseEntity.ok(this.authService.verifyMfa(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){

        this.authService.logout();

        return ResponseEntity.ok("Successfully logged out");
    }

}
