package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginauthorizationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves the password for the given username to support loginAuthentication.
     * Matches schema from createAccountDAO.
     */
    public String getPasswordForUser(String username) {
        String sql = "SELECT password FROM userAccount WHERE userName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // User not found
        }
    }

    /**
     * Updates the password for a given email address.
     * Used by loginAuthentication.resetPassword.
     */
    public boolean updatePasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE userAccount SET password = ? WHERE emailAddress = ?";
        int rowsAffected = jdbcTemplate.update(sql, newPassword, email);
        return rowsAffected > 0;
    }
}