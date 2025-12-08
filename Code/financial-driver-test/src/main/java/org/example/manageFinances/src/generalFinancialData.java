package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Service
public class generalFinancialData {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Calculates the total assets (sum of balances) across all bank accounts for a user.
     * @param userId The ID of the user.
     * @return The total assets value.
     */
    public float getTotalAssets(int userId) {
        String sql = "SELECT SUM(balance) FROM bankAccount WHERE userId = ?";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Calculates the total expenses across all accounts for a user.
     * @param userId The ID of the user.
     * @return The total expenses value.
     */
    public float getTotalExpense(int userId) {
        // Assuming expenses table has a userId column (as per our schema fix)
        // Or we join via bankAccount if userId isn't directly in expenses.
        // Based on schema fix, we added userId to expenses, so this is efficient.
        String sql = "SELECT SUM(amount) FROM expenses WHERE userId = ?";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Calculates Net Worth (Assets - Liabilities/Expenses).
     * Note: Typically expenses are flow, liabilities are debt. 
     * If 'expenses' tracks total spending, Net Worth might be Assets - Debt.
     * For this specific request, we'll return Assets - Total Expenses as a metric.
     */
    public float getNetFinancialPosition(int userId) {
        float assets = getTotalAssets(userId);
        float expenses = getTotalExpense(userId);
        return assets - expenses;
    }

    // --- Placeholder/Legacy Methods (Refactored or Deprecated) ---

    public Map<String, List<Float>> getAllTransactions(int userId) {
        Map<String, List<Float>> financialData = new HashMap<>();

        // Get all transactions
        String transactionsSql = "SELECT amount FROM transactions WHERE userId = ?";
        List<Float> transactions = jdbcTemplate.queryForList(transactionsSql,
                new Object[]{userId}, Float.class);
        financialData.put("transactions", transactions);

        // Get delivery income
        String deliveryIncomeSql = "SELECT amount FROM income WHERE userId = ? AND type = 'DELIVERY'";
        List<Float> deliveryIncome = jdbcTemplate.queryForList(deliveryIncomeSql,
                new Object[]{userId}, Float.class);
        financialData.put("deliveryIncome", deliveryIncome);

        // Get other income
        String otherIncomeSql = "SELECT amount FROM income WHERE userId = ? AND type = 'OTHER'";
        List<Float> otherIncome = jdbcTemplate.queryForList(otherIncomeSql,
                new Object[]{userId}, Float.class);
        financialData.put("otherIncome", otherIncome);

        // Get expenses
        String expensesSql = "SELECT amount FROM expenses WHERE userId = ?";
        List<Float> expenses = jdbcTemplate.queryForList(expensesSql,
                new Object[]{userId}, Float.class);
        financialData.put("expenses", expenses);

        return financialData;
    }
}
