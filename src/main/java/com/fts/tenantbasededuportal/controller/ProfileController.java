package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.profile.ChangePasswordRequestDto;
import com.fts.tenantbasededuportal.dtos.profile.ProfileResponseDto;
import com.fts.tenantbasededuportal.dtos.profile.UpdateProfileRequestDto;
import com.fts.tenantbasededuportal.service.ProfileService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponseDto fetchProfile() {

        return this.profileService.fetchProfile();
    }

    @PutMapping
    public ProfileResponseDto updateProfile
            (@RequestBody final UpdateProfileRequestDto profile) {

        return this.profileService.updateProfile(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword
            (@RequestBody final ChangePasswordRequestDto request) {

        this.profileService.changeProfilePassword(request);

        return ResponseEntity.noContent().build();
    }
}
