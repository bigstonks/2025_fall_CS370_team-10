package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

@Service
public class generalFinancialData {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // Valid transaction types
    public static final String TYPE_PURCHASE = "purchase";
    public static final String TYPE_WITHDRAWAL = "withdrawal";
    public static final String TYPE_DELIVERY_INCOME = "delivery income";
    public static final String TYPE_OTHER_INCOME = "other income";

    private static final List<String> VALID_TRANSACTION_TYPES = Arrays.asList(
            TYPE_PURCHASE, TYPE_WITHDRAWAL, TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME
    );

    private static final List<String> NEGATIVE_TYPES = Arrays.asList(TYPE_PURCHASE, TYPE_WITHDRAWAL);
    private static final List<String> POSITIVE_TYPES = Arrays.asList(TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME);

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
     * Calculates the total expenses (purchases + withdrawals) for a user from the transaction table.
     * @param userId The ID of the user.
     * @return The total expenses value (as a positive number).
     */
    public float getTotalExpense(int userId) {
        String sql = "SELECT SUM(ABS(amount)) FROM \"transaction\" WHERE userId = ? AND transactionType IN (?, ?)";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId, TYPE_PURCHASE, TYPE_WITHDRAWAL}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Calculates total income (delivery income + other income) for a user.
     * @param userId The ID of the user.
     * @return The total income value.
     */
    public float getTotalIncome(int userId) {
        String sql = "SELECT SUM(amount) FROM \"transaction\" WHERE userId = ? AND transactionType IN (?, ?)";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId, TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Calculates Net Worth (Assets - Liabilities/Expenses).
     * @param userId The ID of the user.
     * @return Net financial position.
     */
    public float getNetFinancialPosition(int userId) {
        float assets = getTotalAssets(userId);
        float expenses = getTotalExpense(userId);
        return assets - expenses;
    }

    /**
     * Validates and adds a transaction to the database.
     * @param userId The user ID.
     * @param amount The transaction amount.
     * @param transactionType The type: "purchase", "withdrawal", "delivery income", or "other income".
     * @throws IllegalArgumentException if the transaction type is invalid or amount sign doesn't match type.
     */
    public void addTransaction(int userId, float amount, String transactionType) {
        // Validate transaction type
        if (!VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType +
                    ". Must be one of: " + VALID_TRANSACTION_TYPES);
        }

        String normalizedType = transactionType.toLowerCase();

        // Validate amount sign based on transaction type
        if (NEGATIVE_TYPES.contains(normalizedType)) {
            // purchase and withdrawal must be negative (or we make them negative)
            if (amount > 0) {
                amount = -amount; // Auto-convert to negative for expense types
            }
        } else if (POSITIVE_TYPES.contains(normalizedType)) {
            // delivery income and other income must be positive
            if (amount < 0) {
                throw new IllegalArgumentException("Income transactions must have positive amounts. Got: " + amount);
            }
        }

        String sql = "INSERT INTO \"transaction\" (userId, amount, transactionType, transactionDate) VALUES (?, ?, ?, date('now'))";
        jdbcTemplate.update(sql, userId, amount, normalizedType);
    }

    /**
     * Gets all transactions for a user, organized by type.
     * @param userId The ID of the user.
     * @return A map with keys: "purchases", "withdrawals", "deliveryIncome", "otherIncome", "allTransactions"
     */
    public Map<String, List<Float>> getAllTransactions(int userId) {
        Map<String, List<Float>> financialData = new HashMap<>();

        // Get all transactions
        String allSql = "SELECT amount FROM \"transaction\" WHERE userId = ?";
        List<Float> allTransactions = jdbcTemplate.queryForList(allSql, new Object[]{userId}, Float.class);
        financialData.put("allTransactions", allTransactions);

        // Get purchases
        String purchasesSql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        List<Float> purchases = jdbcTemplate.queryForList(purchasesSql, new Object[]{userId, TYPE_PURCHASE}, Float.class);
        financialData.put("purchases", purchases);

        // Get withdrawals
        String withdrawalsSql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        List<Float> withdrawals = jdbcTemplate.queryForList(withdrawalsSql, new Object[]{userId, TYPE_WITHDRAWAL}, Float.class);
        financialData.put("withdrawals", withdrawals);

        // Get delivery income
        String deliveryIncomeSql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        List<Float> deliveryIncome = jdbcTemplate.queryForList(deliveryIncomeSql, new Object[]{userId, TYPE_DELIVERY_INCOME}, Float.class);
        financialData.put("deliveryIncome", deliveryIncome);

        // Get other income
        String otherIncomeSql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        List<Float> otherIncome = jdbcTemplate.queryForList(otherIncomeSql, new Object[]{userId, TYPE_OTHER_INCOME}, Float.class);
        financialData.put("otherIncome", otherIncome);

        return financialData;
    }

    /**
     * Gets transactions by specific type.
     * @param userId The user ID.
     * @param transactionType The transaction type to filter by.
     * @return List of amounts for that transaction type.
     */
    public List<Float> getTransactionsByType(int userId, String transactionType) {
        if (!VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        String sql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId, transactionType.toLowerCase()}, Float.class);
    }

    /**
     * Validates if a transaction type is valid.
     * @param transactionType The type to validate.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidTransactionType(String transactionType) {
        return VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase());
    }

    /**
     * Gets all valid transaction types.
     * @return List of valid transaction type strings.
     */
    public static List<String> getValidTransactionTypes() {
        return VALID_TRANSACTION_TYPES;
    }

    /**
     * Gets all detailed transaction records for a user across all bank accounts.
     * @param userId The user ID.
     * @return List of TransactionSummary objects with full details.
     */
    public List<TransactionSummary> getAllTransactionDetails(int userId) {
        String sql = "SELECT t.transactionId, t.userId, t.amount, t.transactionType, t.transactionDate, " +
                     "t.description, t.bankAccountId, b.accountType as bankAccountType " +
                     "FROM \"transaction\" t " +
                     "LEFT JOIN bankAccount b ON t.bankAccountId = b.idbankAccount " +
                     "WHERE t.userId = ? " +
                     "ORDER BY t.transactionDate DESC, t.transactionId DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> {
            TransactionSummary summary = new TransactionSummary();
            summary.setTransactionId(rs.getInt("transactionId"));
            summary.setUserId(rs.getInt("userId"));
            summary.setAmount(rs.getFloat("amount"));
            summary.setTransactionType(rs.getString("transactionType"));
            summary.setTransactionDate(rs.getString("transactionDate"));
            summary.setDescription(rs.getString("description"));
            summary.setBankAccountId(rs.getInt("bankAccountId"));
            summary.setBankAccountType(rs.getString("bankAccountType"));
            return summary;
        });
    }

    /**
     * Inner class to hold detailed transaction summary data.
     */
    public static class TransactionSummary {
        private int transactionId;
        private int userId;
        private float amount;
        private String transactionType;
        private String transactionDate;
        private String description;
        private int bankAccountId;
        private String bankAccountType;

        // Getters
        public int getTransactionId() { return transactionId; }
        public int getUserId() { return userId; }
        public float getAmount() { return amount; }
        public String getTransactionType() { return transactionType; }
        public String getTransactionDate() { return transactionDate; }
        public String getDescription() { return description; }
        public int getBankAccountId() { return bankAccountId; }
        public String getBankAccountType() { return bankAccountType; }

        // Setters
        public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
        public void setUserId(int userId) { this.userId = userId; }
        public void setAmount(float amount) { this.amount = amount; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
        public void setDescription(String description) { this.description = description; }
        public void setBankAccountId(int bankAccountId) { this.bankAccountId = bankAccountId; }
        public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }
    }
}
