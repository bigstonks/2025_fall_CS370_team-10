package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class selectBankAccountDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public selectBankAccount findById(String accountID) {
        String sql = "SELECT * FROM bankAccount WHERE idbankAccount = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{accountID},
                (ResultSet rs, int rowNum) -> new selectBankAccount(
                    String.valueOf(rs.getInt("idbankAccount")),
                    rs.getFloat("balance"),
                    rs.getString("accountType"),
                    rs.getFloat("otherIncome"),
                    rs.getString("accountName"),
                    rs.getFloat("interestRate"),
                    rs.getFloat("accountFees")
                ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<selectBankAccount> getAccountsForUser(int userId) {
        String sql = "SELECT * FROM bankAccount WHERE userId = ?";
        return jdbcTemplate.query(sql, new Object[]{userId},
            (ResultSet rs, int rowNum) -> new selectBankAccount(
                String.valueOf(rs.getInt("idbankAccount")),
                rs.getFloat("balance"),
                rs.getString("accountType"),
                rs.getFloat("otherIncome"),
                rs.getString("accountName"),
                rs.getFloat("interestRate"),
                rs.getFloat("accountFees")
            ));
    }

    public List<selectBankAccount> findAll() {
        String sql = "SELECT * FROM bankAccount";
        return jdbcTemplate.query(sql,
            (ResultSet rs, int rowNum) -> new selectBankAccount(
                String.valueOf(rs.getInt("idbankAccount")),
                rs.getFloat("balance"),
                rs.getString("accountType"),
                rs.getFloat("otherIncome"),
                rs.getString("accountName"),
                rs.getFloat("interestRate"),
                rs.getFloat("accountFees")
            ));
    }

    public void updateBalance(String accountID, float newBalance) {
        String sql = "UPDATE bankAccount SET balance = ? WHERE idbankAccount = ?";
        jdbcTemplate.update(sql, newBalance, accountID);
    }

    public void updateInterestRate(String accountID, float interestRate) {
        String sql = "UPDATE bankAccount SET interestRate = ? WHERE idbankAccount = ?";
        jdbcTemplate.update(sql, interestRate, accountID);
    }

    public void updateAccountFees(String accountID, float accountFees) {
        String sql = "UPDATE bankAccount SET accountFees = ? WHERE idbankAccount = ?";
        jdbcTemplate.update(sql, accountFees, accountID);
    }

    public void updateOtherIncome(String accountID, float otherIncome) {
        String sql = "UPDATE bankAccount SET otherIncome = ? WHERE idbankAccount = ?";
        jdbcTemplate.update(sql, otherIncome, accountID);
    }

    public void create(addBankAccount account) {
        String sql = "INSERT INTO bankAccount (balance, accountType, otherIncome, accountName, interestRate, accountFees) VALUES ( ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,

            account.getBalance(),
            account.getAccountType(),
            account.getOtherIncome(),
            account.getAccountName(),
            account.getInterestRate(),
            account.getAccountFees()
        );
    }

    public void delete(String accountID) {
        String sql = "DELETE FROM bankAccount WHERE accountID = ?";
        jdbcTemplate.update(sql, accountID);
    }
}
