package com.company.task_manager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

// Samostatná entita mimo Worker hierarchie - admin nepatrí do žiadneho oddelenia,
// je to čisto systémový účet (vytvorený/seeded pri štarte, pozri AdminAccountSeeder).
@Entity
public class AdminAccount {

    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String username;

    // BCrypt hash
    private String password;

    public AdminAccount() {
    }

    public AdminAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
