package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class generalFinancialDataDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Valid transaction types - must match generalFinancialData constants
    public static final String TYPE_PURCHASE = "purchase";
    public static final String TYPE_WITHDRAWAL = "withdrawal";
    public static final String TYPE_DELIVERY_INCOME = "delivery income";
    public static final String TYPE_OTHER_INCOME = "other income";

    private static final List<String> VALID_TRANSACTION_TYPES = Arrays.asList(
            TYPE_PURCHASE, TYPE_WITHDRAWAL, TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME
    );

    private static final List<String> NEGATIVE_TYPES = Arrays.asList(TYPE_PURCHASE, TYPE_WITHDRAWAL);
    private static final List<String> POSITIVE_TYPES = Arrays.asList(TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME);

    public Float getTotalAssets(int userId) {
        String sql = "SELECT SUM(balance) FROM bankAccount WHERE userId = ?";
        try {
            Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
            return (total != null) ? total : 0.0f;
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    /**
     * Gets total expenses (purchases + withdrawals) from the transaction table.
     */
    public Float getTotalExpense(int userId) {
        String sql = "SELECT SUM(ABS(amount)) FROM \"transaction\" WHERE userId = ? AND transactionType IN (?, ?)";
        try {
            Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId, TYPE_PURCHASE, TYPE_WITHDRAWAL}, Float.class);
            return (total != null) ? total : 0.0f;
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    /**
     * Gets total income (delivery income + other income) from the transaction table.
     */
    public Float getTotalIncome(int userId) {
        String sql = "SELECT SUM(amount) FROM \"transaction\" WHERE userId = ? AND transactionType IN (?, ?)";
        try {
            Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId, TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME}, Float.class);
            return (total != null) ? total : 0.0f;
        } catch (EmptyResultDataAccessException e) {
            return 0.0f;
        }
    }

    /**
     * Gets all transactions of a specific type for a user.
     */
    public List<Float> getTransactionsByType(int userId, String transactionType) {
        if (!VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        String sql = "SELECT amount FROM \"transaction\" WHERE userId = ? AND transactionType = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId, transactionType.toLowerCase()}, Float.class);
    }

    /**
     * Gets all transactions for a user.
     */
    public List<Float> getAllTransactions(int userId) {
        String sql = "SELECT amount FROM \"transaction\" WHERE userId = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }

    /**
     * Gets delivery income transactions.
     */
    public List<Float> getDeliveryIncome(int userId) {
        return getTransactionsByType(userId, TYPE_DELIVERY_INCOME);
    }

    /**
     * Gets other income transactions.
     */
    public List<Float> getOtherIncome(int userId) {
        return getTransactionsByType(userId, TYPE_OTHER_INCOME);
    }

    /**
     * Gets purchase transactions (expenses).
     */
    public List<Float> getPurchases(int userId) {
        return getTransactionsByType(userId, TYPE_PURCHASE);
    }

    /**
     * Gets withdrawal transactions.
     */
    public List<Float> getWithdrawals(int userId) {
        return getTransactionsByType(userId, TYPE_WITHDRAWAL);
    }

    /**
     * Adds a transaction with validation.
     * @param userId The user ID.
     * @param amount The amount (will be auto-converted to negative for purchase/withdrawal).
     * @param transactionType Must be: "purchase", "withdrawal", "delivery income", or "other income".
     * @throws IllegalArgumentException if type is invalid or income amount is negative.
     */
    public void addTransaction(int userId, float amount, String transactionType) {
        // Validate transaction type
        String normalizedType = transactionType.toLowerCase();
        if (!VALID_TRANSACTION_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType +
                    ". Must be one of: " + VALID_TRANSACTION_TYPES);
        }

        // Validate and adjust amount sign based on transaction type
        if (NEGATIVE_TYPES.contains(normalizedType)) {
            // purchase and withdrawal must be negative
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
     * Convenience method to add a purchase (expense).
     */
    public void addPurchase(int userId, float amount) {
        addTransaction(userId, amount, TYPE_PURCHASE);
    }

    /**
     * Convenience method to add a withdrawal.
     */
    public void addWithdrawal(int userId, float amount) {
        addTransaction(userId, amount, TYPE_WITHDRAWAL);
    }

    /**
     * Convenience method to add delivery income.
     */
    public void addDeliveryIncome(int userId, float amount) {
        addTransaction(userId, amount, TYPE_DELIVERY_INCOME);
    }

    /**
     * Convenience method to add other income.
     */
    public void addOtherIncome(int userId, float amount) {
        addTransaction(userId, amount, TYPE_OTHER_INCOME);
    }

    /**
     * Validates if a transaction type is valid.
     */
    public static boolean isValidTransactionType(String transactionType) {
        return VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase());
    }

    // =========================================================
    //   BANK ACCOUNT-SPECIFIC TRANSACTION METHODS
    // =========================================================

    /**
     * Gets all detailed transaction records for a specific bank account.
     * @param bankAccountId The bank account ID.
     * @return List of TransactionRecord objects.
     */
    public List<TransactionRecord> getTransactionsForBankAccount(int bankAccountId) {
        String sql = "SELECT transactionId, userId, amount, transactionType, transactionDate, description, bankAccountId " +
                     "FROM \"transaction\" WHERE bankAccountId = ? ORDER BY transactionDate DESC, transactionId DESC";
        return jdbcTemplate.query(sql, new Object[]{bankAccountId}, (rs, rowNum) -> {
            TransactionRecord record = new TransactionRecord();
            record.setTransactionId(rs.getInt("transactionId"));
            record.setUserId(rs.getInt("userId"));
            record.setAmount(rs.getFloat("amount"));
            record.setTransactionType(rs.getString("transactionType"));
            record.setTransactionDate(rs.getString("transactionDate"));
            record.setDescription(rs.getString("description"));
            record.setBankAccountId(rs.getInt("bankAccountId"));
            return record;
        });
    }

    /**
     * Gets detailed transaction records for a bank account filtered by type.
     * @param bankAccountId The bank account ID.
     * @param transactionType The transaction type to filter by.
     * @return List of TransactionRecord objects.
     */
    public List<TransactionRecord> getTransactionsForBankAccountByType(int bankAccountId, String transactionType) {
        if (!VALID_TRANSACTION_TYPES.contains(transactionType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        String sql = "SELECT transactionId, userId, amount, transactionType, transactionDate, description, bankAccountId " +
                     "FROM \"transaction\" WHERE bankAccountId = ? AND transactionType = ? ORDER BY transactionDate DESC, transactionId DESC";
        return jdbcTemplate.query(sql, new Object[]{bankAccountId, transactionType.toLowerCase()}, (rs, rowNum) -> {
            TransactionRecord record = new TransactionRecord();
            record.setTransactionId(rs.getInt("transactionId"));
            record.setUserId(rs.getInt("userId"));
            record.setAmount(rs.getFloat("amount"));
            record.setTransactionType(rs.getString("transactionType"));
            record.setTransactionDate(rs.getString("transactionDate"));
            record.setDescription(rs.getString("description"));
            record.setBankAccountId(rs.getInt("bankAccountId"));
            return record;
        });
    }

    /**
     * Adds a transaction tied to a specific bank account.
     * @param userId The user ID.
     * @param amount The amount.
     * @param transactionType The transaction type.
     * @param bankAccountId The bank account ID.
     * @param description Optional description.
     */
    public void addTransactionForBankAccount(int userId, float amount, String transactionType, int bankAccountId, String description) {
        String normalizedType = transactionType.toLowerCase();
        if (!VALID_TRANSACTION_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }

        // Validate and adjust amount sign
        if (NEGATIVE_TYPES.contains(normalizedType)) {
            if (amount > 0) {
                amount = -amount;
            }
        } else if (POSITIVE_TYPES.contains(normalizedType)) {
            if (amount < 0) {
                throw new IllegalArgumentException("Income transactions must have positive amounts. Got: " + amount);
            }
        }

        String sql = "INSERT INTO \"transaction\" (userId, amount, transactionType, transactionDate, description, bankAccountId) " +
                     "VALUES (?, ?, ?, date('now'), ?, ?)";
        jdbcTemplate.update(sql, userId, amount, normalizedType, description, bankAccountId);
    }

    /**
     * Inner class to hold detailed transaction record data.
     */
    public static class TransactionRecord {
        private int transactionId;
        private int userId;
        private float amount;
        private String transactionType;
        private String transactionDate;
        private String description;
        private int bankAccountId;

        // Getters
        public int getTransactionId() { return transactionId; }
        public int getUserId() { return userId; }
        public float getAmount() { return amount; }
        public String getTransactionType() { return transactionType; }
        public String getTransactionDate() { return transactionDate; }
        public String getDescription() { return description; }
        public int getBankAccountId() { return bankAccountId; }

        // Setters
        public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
        public void setUserId(int userId) { this.userId = userId; }
        public void setAmount(float amount) { this.amount = amount; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
        public void setDescription(String description) { this.description = description; }
        public void setBankAccountId(int bankAccountId) { this.bankAccountId = bankAccountId; }

        @Override
        public String toString() {
            return String.format("Transaction[id=%d, type=%s, amount=%.2f, date=%s, desc=%s]",
                    transactionId, transactionType, amount, transactionDate, description);
        }
    }
}
