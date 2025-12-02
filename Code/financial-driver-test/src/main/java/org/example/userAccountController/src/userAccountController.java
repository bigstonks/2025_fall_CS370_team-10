package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class userAccountController {

    @Autowired
    private loginAuthentication loginService;

    @Autowired
    private createAccountService accountService;

    /**
     * Handles the login request from the UI.
     *
     * @param username Input username
     * @param password Input password
     * @return true if login successful, false otherwise
     */
    public boolean handleLoginRequest(String username, String password) {
        System.out.println("Attempting login for user: " + username);
        return loginService.validateLogin(username, password);
    }

    /**
     * Handles the password reset request.
     * @param email Input email
     * @return true if reset initiated successfully
     */
    public boolean handlePasswordReset(String email) {
        return loginService.resetPassword(email);
    }

    /**
     * Handles a new account creation request from the UI.
     * @param username Desired username
     * @param password Desired password
     * @param email Desired email
     * @return true if account created successfully
     */
    public boolean handleCreateAccount(String username, String password, String email){
        System.out.println("Attempting to create account for: " + username);
        return accountService.attemptAccountCreation(username, password, email);
    }
}
