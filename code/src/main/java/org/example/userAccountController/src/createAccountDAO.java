package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;


@Repository
public class createAccountDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public createAccount findByUsername(String username) {
        String sql = "SELECT * FROM userAccount WHERE userName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{username},
                (ResultSet rs, int rowNum) -> new createAccount(

                    rs.getString("userName"),
                    rs.getString("password"),
                        rs.getString("emailAddress")
                ));
        } catch (EmptyResultDataAccessException e) {
            return null; 
        }
    }

    public Integer getUserIdByUsername(String username) {
        String sql = "SELECT userID FROM userAccount WHERE userName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // not found
        }
    }

    /**
     * Gets the authorization level for a user by username.
     * @param username The username to look up
     * @return The authorization level ("admin" or "user"), defaults to "user" if not found
     */
    public String getAuthorizationByUsername(String username) {
        String sql = "SELECT authorization FROM userAccount WHERE userName = ?";
        try {
            String auth = jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class);
            return (auth != null && !auth.isEmpty()) ? auth : "user";
        } catch (EmptyResultDataAccessException e) {
            return "user"; // default to user if not found
        }
    }

    /**
     * Updates the authorization level for a user.
     * @param username The username to update
     * @param authorization The new authorization level
     * @return true if updated successfully
     */
    public boolean updateAuthorization(String username, String authorization) {
        String sql = "UPDATE userAccount SET authorization = ? WHERE userName = ?";
        try {
            int rows = jdbcTemplate.update(sql, authorization, username);
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error updating authorization: " + e.getMessage());
            return false;
        }
    }

    public void create(createAccount account) {
        String sql = "INSERT INTO userAccount (userName, password, emailAddress, authorization) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                account.getUsername(),
                account.getPassword(),
                account.getEmail(),
                "user"
            );
            System.out.println("createAccountDAO: Account created successfully for " + account.getUsername());
        } catch (Exception e) {
            System.err.println("createAccountDAO: Failed to create account - " + e.getMessage());
            throw e; // Re-throw to let caller handle it
        }
    }
}
