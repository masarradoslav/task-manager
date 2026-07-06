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
    private final Department department;
    private final String displayName;

    private AppUserPrincipal(String username, String password, Role role, Integer workerId,
                              Department department, String displayName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.workerId = workerId;
        this.department = department;
        this.displayName = displayName;
    }

    public static AppUserPrincipal forAdmin(AdminAccount admin) {
        return new AppUserPrincipal(admin.getUsername(), admin.getPassword(), Role.ADMIN, null, null, "Admin");
    }

    public static AppUserPrincipal forWorker(Worker worker) {
        Role role = worker instanceof Manager ? Role.MANAGER : Role.EMPLOYEE;
        return new AppUserPrincipal(worker.getUsername(), worker.getPassword(), role,
                worker.getId(), worker.getDepartment(), worker.getName());
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

    public Department getDepartment() {
        return department;
    }

    public String getDisplayName() {
        return displayName;
    }

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
