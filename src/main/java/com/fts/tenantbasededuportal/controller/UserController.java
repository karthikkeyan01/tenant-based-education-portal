package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.user.UserResponseDto;
import com.fts.tenantbasededuportal.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping
    public List<UserResponseDto> users(){

        return userService.fetchUsers();
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/{id}")
    public UserResponseDto fetchUserById(
            @PathVariable final String id) {
        return this.userService.fetchUserById(id);
    }
}
