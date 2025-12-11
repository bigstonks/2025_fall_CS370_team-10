package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class addBankAccountDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Saves a new bank account to the database.
     * @param account the addBankAccount object containing account details
     * @return true if the account was saved successfully, false otherwise
     */
    public boolean saveNewAccount(addBankAccount account) {
        // Look up the userId from the username
        Integer userId = getUserIdByUsername(account.getOwnerUsername());
        if (userId == null) {
            System.out.println("Error: User not found for username: " + account.getOwnerUsername());
            return false;
        }

        String sql = "INSERT INTO bankAccount (userId, accountName, accountType, balance, interestRate, accountFees, otherIncome) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    userId,
                    account.getAccountName(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getInterestRate(),
                    account.getAccountFees(),
                    account.getOtherIncome()
            );
            if (rowsAffected > 0) {
                System.out.println("Bank account '" + account.getAccountName() + "' created successfully for user ID: " + userId);
            }
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Error saving bank account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Looks up the userId from the username.
     * @param username the username to look up
     * @return the userId, or null if not found
     */
    private Integer getUserIdByUsername(String username) {
        String sql = "SELECT userID FROM userAccount WHERE userName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, username);
        } catch (Exception e) {
            System.out.println("Error looking up user: " + e.getMessage());
            return null;
        }
    }
}
