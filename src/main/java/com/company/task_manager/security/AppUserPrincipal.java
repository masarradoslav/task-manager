package com.company.task_manager.security;

import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.Worker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUserPrincipal implements UserDetails {

    public enum Role { ADMIN, MANAGER, EMPLOYEE }

    private final String username;
    private final String password;
    private final Role role;
    private final Integer workerId;
    private final Integer adminId;
    private final Department department;
    private final String displayName;

    private AppUserPrincipal(String username, String password, Role role, Integer workerId, Integer adminId,
                              Department department, String displayName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.workerId = workerId;
        this.adminId = adminId;
        this.department = department;
        this.displayName = displayName;
    }

    // wraps the system admin account (no worker id, no department)
    public static AppUserPrincipal forAdmin(AdminAccount admin) {
        return new AppUserPrincipal(admin.getUsername(), admin.getPassword(), Role.ADMIN, null, admin.getId(), null, "Admin");
    }

    // wraps a Worker, deriving the role from its Manager/RegularEmployee subtype
    public static AppUserPrincipal forWorker(Worker worker) {
        Role role = worker instanceof Manager ? Role.MANAGER : Role.EMPLOYEE;
        return new AppUserPrincipal(worker.getUsername(), worker.getPassword(), role,
                worker.getId(), null, worker.getDepartment(), worker.getName());
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    public boolean isEmployee() {
        return role == Role.EMPLOYEE;
    }

    public Integer getWorkerId() {
        return workerId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public Department getDepartment() {
        return department;
    }

    public String getDisplayName() {
        return displayName;
    }

    // returns the role as a Spring Security authority (ROLE_ADMIN...).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return true;
    }
}
