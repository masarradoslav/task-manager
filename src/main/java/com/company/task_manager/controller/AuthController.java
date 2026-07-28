package com.company.task_manager.controller;

import com.company.task_manager.dto.AdminAccountRequest;
import com.company.task_manager.dto.AdminAccountResponse;
import com.company.task_manager.dto.CurrentUserResponse;
import com.company.task_manager.security.AppUserPrincipal;
import com.company.task_manager.service.AdminAccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// The actual login/logout POST endpoints are handled by Spring Security's formLogin/logout
// filters (SecurityConfig), this controller only exposes "who am I" for the frontend
// to bootstrap its role-based UI after a successful login, plus Admin's own self-edit (Manager/
// Employee self-edit goes through WorkerController instead, since they're Worker rows).
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AdminAccountService adminAccountService;

    public AuthController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    // GET /auth/me - returns currently logged in user
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return CurrentUserResponse.from(principal);
    }

    // PUT /auth/me - the Admin self edit
    @PutMapping("/me")
    public AdminAccountResponse updateMe(@Valid @RequestBody AdminAccountRequest request,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        return AdminAccountResponse.from(adminAccountService.updateSelf(request.username(), request.password(), principal));
    }
}
