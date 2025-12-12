package org.example.userAccountController.src;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service class for handling account creation logic.
 */
@Service
public class createAccountService {

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

        if (email == null || email.trim().isEmpty()) {
            System.out.println("Error: Email cannot be empty.");
            return false;
        }

        // 2. Check if user already exists (requires DAO method)
        if (accountDAO.findByUsername(username) != null) {
            System.out.println("Error: Username already exists.");
            return false;
        }

        // 3. Create Model
        createAccount newAccount = new createAccount(username, password, email);

        // 4. Persist to DB
        try {
            accountDAO.create(newAccount);
            System.out.println("Success: Account created for " + username);
            return true;
        } catch (Exception e) {
            System.err.println("Error: Database saving failed - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

