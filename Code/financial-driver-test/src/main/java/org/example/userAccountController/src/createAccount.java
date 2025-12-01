package org.example.userAccountController.src;


import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

// 1. The Model Class (Data Structure)
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

// 2. The Service Class

@Service
class createAccountService {

    @Autowired
    private createAccountDAO accountDAO;

    /**
     * Main method to be called by your Swing UI.
     * Validates input and creates the account.
     */
    public boolean attemptAccountCreation(String username, String password, String email) {
        // 1. Validation Logic
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return false;
        }
        if (password == null || password.length() < 8) {
            System.out.println("Error: Password must be at least 8 characters.");
            return false;
        }

        if (username == null || email.trim().isEmpty()) {
            System.out.println("Error: Username cannot be empty.");
            return false;
        }

        // 2. Check if user already exists (requires DAO method)
         if (accountDAO.findByUsername(username) != null) { return false; }

        // 3. Business Logic: Generate unique ID
        String uniqueID = UUID.randomUUID().toString();

        // 4. Create Model
        createAccount newAccount = new createAccount(username, password, email);

        // 5. Persist to DB
        try {
            accountDAO.create(newAccount);
            System.out.println("Success: Account created for " + username);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: Database saving failed.");
            return false;
        }
    }
}

