package org.example.userAccountController.src;

/**
 * Model class representing a user account.
 */
public class createAccount {
    private String username;
    private String password; //  TODO: store passwords in hashes later
    private String email;

    // Constructors
    public createAccount(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}


