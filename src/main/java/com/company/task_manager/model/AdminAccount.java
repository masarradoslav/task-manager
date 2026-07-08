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

    // No-arg constructor required by JPA.
    public AdminAccount() {
    }

    // Creates an admin account with the given username and (already-hashed) password.
    public AdminAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Returns the account's id.
    public int getId() {
        return id;
    }

    // Returns the login username.
    public String getUsername() {
        return username;
    }

    // Sets the login username.
    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the BCrypt password hash.
    public String getPassword() {
        return password;
    }

    // Sets the BCrypt password hash.
    public void setPassword(String password) {
        this.password = password;
    }
}
