package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class generalFinancialDataDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Float getTotalAssets(int userId) {
        String sql = "SELECT SUM(balance) FROM bankAccount WHERE userId = ?";
        try {
            Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
            return (total != null) ? total : 0.0f;
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    public Float getTotalExpense(int userId) {
        String sql = "SELECT SUM(amount) FROM expenses WHERE userId = ?";
        try {
            Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
            return (total != null) ? total : 0.0f;
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    public List<Float> getTransactions(int userId) {
        String sql = "SELECT amount FROM transactions WHERE userId = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }

    public List<Float> getDeliveryIncome(int userId) {
        String sql = "SELECT amount FROM income WHERE userId = ? AND type = 'DELIVERY'";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }

    public List<Float> getOtherIncome(int userId) {
        String sql = "SELECT amount FROM income WHERE userId = ? AND type = 'OTHER'";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }

    public List<Float> getExpenses(int userId) {
        String sql = "SELECT amount FROM expenses WHERE userId = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }

    public void addExpense(int userId, float amount, String description) {
        String sql = "INSERT INTO expenses (userId, amount, description) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, amount, description);
    }

    public void addIncome(int userId, float amount, String type) {
        String sql = "INSERT INTO income (userId, amount, type) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, amount, type);
    }

    public void addTransaction(int userId, float amount) {
        String sql = "INSERT INTO transactions (userId, amount) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, amount);
    }
}
