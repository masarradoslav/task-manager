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

// Wraps either an AdminAccount (system-level, no department) or a Worker (Manager/RegularEmployee)
// behind one Spring Security principal type, so controllers/services only ever deal with one shape.
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

    // Wraps the system admin account (no worker id, no department).
    public static AppUserPrincipal forAdmin(AdminAccount admin) {
        return new AppUserPrincipal(admin.getUsername(), admin.getPassword(), Role.ADMIN, null, admin.getId(), null, "Admin");
    }

    // Wraps a Worker, deriving the role from its Manager/RegularEmployee subtype.
    public static AppUserPrincipal forWorker(Worker worker) {
        Role role = worker instanceof Manager ? Role.MANAGER : Role.EMPLOYEE;
        return new AppUserPrincipal(worker.getUsername(), worker.getPassword(), role,
                worker.getId(), null, worker.getDepartment(), worker.getName());
    }

    // Returns the logged-in user's role (ADMIN/MANAGER/EMPLOYEE).
    public Role getRole() {
        return role;
    }

    // True when the principal is the system admin.
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    // True when the principal is a department manager.
    public boolean isManager() {
        return role == Role.MANAGER;
    }

    // True when the principal is a regular employee.
    public boolean isEmployee() {
        return role == Role.EMPLOYEE;
    }

    // Returns the backing Worker's id, or null for the admin.
    public Integer getWorkerId() {
        return workerId;
    }

    // Returns the backing AdminAccount's id, or null for workers.
    public Integer getAdminId() {
        return adminId;
    }

    // Returns the worker's department, or null for the admin.
    public Department getDepartment() {
        return department;
    }

    // Returns the human-readable name shown in the UI header.
    public String getDisplayName() {
        return displayName;
    }

    // Exposes the role as a Spring Security authority (ROLE_ADMIN etc.).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Returns the stored BCrypt hash for Spring Security's password check.
    @Override
    public String getPassword() {
        return password;
    }

    // Returns the login username.
    @Override
    public String getUsername() {
        return username;
    }

    // Account expiry/locking isn't modelled in this app - all accounts are always active.
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
