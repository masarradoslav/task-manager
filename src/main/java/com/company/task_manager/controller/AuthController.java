package com.company.task_manager.controller;

import com.company.task_manager.dto.CurrentUserResponse;
import com.company.task_manager.security.AppUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// The actual login/logout POST endpoints are handled by Spring Security's formLogin/logout
// filters (see SecurityConfig) - this controller only exposes "who am I" for the frontend
// to bootstrap its role-based UI after a successful login.
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return CurrentUserResponse.from(principal);
    }
}
