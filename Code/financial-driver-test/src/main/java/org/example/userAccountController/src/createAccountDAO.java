package org.example.userAccountController.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;


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
                    rs.getString("password")
                ));
        } catch (EmptyResultDataAccessException e) {
            return null; 
        }
    }

    public void create(createAccount account) {
        // Removed accountID and accountType from INSERT
        String sql = "INSERT INTO userAccount (userName, password, emailAddress) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, 
            account.getUsername(), 
            account.getPassword(), 
            "user@example.com" // Placeholder for email
        );
    }
}

