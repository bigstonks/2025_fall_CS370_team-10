
package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class loginAuthenticationDAO{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves the stored password for a given username.
     * Returns null if user not found.
     */
    public String getPasswordForUser(String username) {
        String sql = "SELECT password FROM userAccount WHERE userName = ?";
        try {
            String password = jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class);
            System.out.println("loginAuthenticationDAO: Found user '" + username + "' in database");
            return password;
        } catch (EmptyResultDataAccessException e) {
            System.out.println("loginAuthenticationDAO: User '" + username + "' not found in database");
            return null; // User not found
        } catch (Exception e) {
            System.err.println("loginAuthenticationDAO: Error querying database - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the password for a given email (used for reset logic).
     * Matches schema: Table `userAccount`, Column `emailAddress`, Column `password`
     */
    public boolean updatePasswordByEmail(String email, String newPassword) {
        String sql = "UPDATE userAccount SET password = ? WHERE emailAddress = ?";
        int rowsAffected = jdbcTemplate.update(sql, newPassword, email);
        return rowsAffected > 0;
    }
}