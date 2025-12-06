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
        String sql = "INSERT INTO bankAccount (accountName, accountType, balance, interestRate, accountFees, otherIncome) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    account.getAccountName(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getInterestRate(),
                    account.getAccountFees(),
                    account.getOtherIncome()
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
