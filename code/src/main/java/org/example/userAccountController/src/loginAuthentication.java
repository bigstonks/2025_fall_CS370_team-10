package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class loginAuthentication {

    @Autowired
    private loginAuthenticationDAO loginAuthenticationDAO;

    /**
     * Validates the login credentials using plain text comparison.
     * @param username The input username
     * @param password The input password (plain text)
     * @return true if valid, false otherwise
     */
    public boolean validateLogin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        // Retrieve the stored password from the database
        String storedPassword = loginAuthenticationDAO.getPasswordForUser(username);

        // Check if user exists
        if (storedPassword == null) {
            return false; 
        }

        // PLAIN TEXT COMPARISON (For development only)
        return storedPassword.equals(password);
    }

    public boolean resetPassword(String email) {

        if (email == null || email.isEmpty()) {
            return false;
        }
        String tempPassword = "Temp1234";
        return loginAuthenticationDAO.updatePasswordByEmail(email, tempPassword);
    }
}
