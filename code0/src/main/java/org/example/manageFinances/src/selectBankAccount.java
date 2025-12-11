package org.example.manageFinances.src;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Represents a Bank Account and the logic to select/manage it.
 * Extends generalFinancialData to work with account-specific transactions.
 */
public class selectBankAccount extends generalFinancialData {
    private String accountID;
    private float balance;
    private float interestRate;
    private float accountFees;
    private float otherIncome;
    private String accountType;
    private String accountName;
    private int userId; // The user who owns this account

    // JdbcTemplate is inherited from generalFinancialData parent class
    // Account-specific database operations use the parent's jdbcTemplate

    // Constructors
    public selectBankAccount() {}

    public selectBankAccount(String accountID, float balance, String accountType, float otherIncome,
                             String accountName, float interestRate, float accountFees) {
        this.accountID = accountID;
        this.balance = balance;
        this.accountType = accountType;
        this.otherIncome = otherIncome;
        this.interestRate = interestRate;
        this.accountFees = accountFees;
        this.accountName = accountName;
    }

    public selectBankAccount(String accountID, float balance, String accountType, float otherIncome,
                             String accountName, float interestRate, float accountFees, int userId) {
        this(accountID, balance, accountType, otherIncome, accountName, interestRate, accountFees);
        this.userId = userId;
    }

    // Getters
    public String getAccountID() { return accountID; }
    public float getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public float getOtherIncome() { return otherIncome; }
    public float getInterestRate() { return interestRate; }
    public float getAccountFees() { return accountFees; }
    public String getAccountName() { return accountName; }
    public int getUserId() { return userId; }

    // Setters
    public void setAccountID(String accountID) { this.accountID = accountID; }
    public void setBalance(float balance) { this.balance = balance; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setInterestRate(float interestRate) { this.interestRate = interestRate; }
    public void setAccountFees(float accountFees) { this.accountFees = accountFees; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public void setUserId(int userId) { this.userId = userId; }

    // ==================== Account-Specific Transaction Methods ====================

    /**
     * Adds a transaction tied to THIS specific bank account.
     * @param amount The transaction amount.
     * @param transactionType Must be: "purchase", "withdrawal", "delivery income", or "other income".
     */
    public void addAccountTransaction(float amount, String transactionType) {
        if (accountID == null || userId == 0) {
            throw new IllegalStateException("Account ID and User ID must be set before adding transactions.");
        }
        addAccountTransaction(userId, amount, transactionType, Integer.parseInt(accountID));
    }

    /**
     * Adds a transaction tied to a specific bank account.
     * @param userId The user ID.
     * @param amount The transaction amount.
     * @param transactionType The transaction type.
     * @param bankAccountId The bank account ID to associate with this transaction.
     */
    public void addAccountTransaction(int userId, float amount, String transactionType, int bankAccountId) {
        // Validate transaction type
        if (!isValidTransactionType(transactionType)) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType +
                    ". Must be one of: " + getValidTransactionTypes());
        }

        String normalizedType = transactionType.toLowerCase();

        // Validate and adjust amount sign based on transaction type
        if (normalizedType.equals(TYPE_PURCHASE) || normalizedType.equals(TYPE_WITHDRAWAL)) {
            if (amount > 0) {
                amount = -amount; // Auto-convert to negative for expense types
            }
        } else if (normalizedType.equals(TYPE_DELIVERY_INCOME) || normalizedType.equals(TYPE_OTHER_INCOME)) {
            if (amount < 0) {
                throw new IllegalArgumentException("Income transactions must have positive amounts. Got: " + amount);
            }
        }

        String sql = "INSERT INTO \"transaction\" (userId, amount, transactionType, transactionDate, bankAccountId) " +
                     "VALUES (?, ?, ?, date('now'), ?)";
        jdbcTemplate.update(sql, userId, amount, normalizedType, bankAccountId);
    }

    /**
     * Gets all transactions for THIS specific bank account.
     * @return List of transaction amounts for this account.
     */
    public List<Float> getAccountTransactions() {
        if (accountID == null) {
            throw new IllegalStateException("Account ID must be set.");
        }
        return getTransactionsForAccount(Integer.parseInt(accountID));
    }

    /**
     * Gets all transactions for a specific bank account.
     * @param bankAccountId The bank account ID.
     * @return List of transaction amounts.
     */
    public List<Float> getTransactionsForAccount(int bankAccountId) {
        String sql = "SELECT amount FROM \"transaction\" WHERE bankAccountId = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{bankAccountId}, Float.class);
    }

    /**
     * Gets transactions for THIS account filtered by type.
     * @param transactionType The transaction type to filter by.
     * @return List of transaction amounts.
     */
    public List<Float> getAccountTransactionsByType(String transactionType) {
        if (accountID == null) {
            throw new IllegalStateException("Account ID must be set.");
        }
        return getTransactionsForAccountByType(Integer.parseInt(accountID), transactionType);
    }

    /**
     * Gets transactions for a specific bank account filtered by type.
     * @param bankAccountId The bank account ID.
     * @param transactionType The transaction type to filter by.
     * @return List of transaction amounts.
     */
    public List<Float> getTransactionsForAccountByType(int bankAccountId, String transactionType) {
        if (!isValidTransactionType(transactionType)) {
            throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
        String sql = "SELECT amount FROM \"transaction\" WHERE bankAccountId = ? AND transactionType = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{bankAccountId, transactionType.toLowerCase()}, Float.class);
    }

    /**
     * Gets total expenses (purchases + withdrawals) for THIS bank account.
     * @return Total expenses as a positive number.
     */
    public float getAccountTotalExpenses() {
        if (accountID == null) {
            throw new IllegalStateException("Account ID must be set.");
        }
        return getTotalExpensesForAccount(Integer.parseInt(accountID));
    }

    /**
     * Gets total expenses for a specific bank account.
     * @param bankAccountId The bank account ID.
     * @return Total expenses as a positive number.
     */
    public float getTotalExpensesForAccount(int bankAccountId) {
        String sql = "SELECT SUM(ABS(amount)) FROM \"transaction\" WHERE bankAccountId = ? AND transactionType IN (?, ?)";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{bankAccountId, TYPE_PURCHASE, TYPE_WITHDRAWAL}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Gets total income (delivery income + other income) for THIS bank account.
     * @return Total income.
     */
    public float getAccountTotalIncome() {
        if (accountID == null) {
            throw new IllegalStateException("Account ID must be set.");
        }
        return getTotalIncomeForAccount(Integer.parseInt(accountID));
    }

    /**
     * Gets total income for a specific bank account.
     * @param bankAccountId The bank account ID.
     * @return Total income.
     */
    public float getTotalIncomeForAccount(int bankAccountId) {
        String sql = "SELECT SUM(amount) FROM \"transaction\" WHERE bankAccountId = ? AND transactionType IN (?, ?)";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{bankAccountId, TYPE_DELIVERY_INCOME, TYPE_OTHER_INCOME}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Gets all transactions for THIS account organized by type.
     * @return Map with keys: "purchases", "withdrawals", "deliveryIncome", "otherIncome", "allTransactions"
     */
    public Map<String, List<Float>> getAllAccountTransactions() {
        if (accountID == null) {
            throw new IllegalStateException("Account ID must be set.");
        }
        return getAllTransactionsForAccount(Integer.parseInt(accountID));
    }

    /**
     * Gets all transactions for a specific bank account organized by type.
     * @param bankAccountId The bank account ID.
     * @return Map organized by transaction type.
     */
    public Map<String, List<Float>> getAllTransactionsForAccount(int bankAccountId) {
        Map<String, List<Float>> financialData = new HashMap<>();

        financialData.put("allTransactions", getTransactionsForAccount(bankAccountId));
        financialData.put("purchases", getTransactionsForAccountByType(bankAccountId, TYPE_PURCHASE));
        financialData.put("withdrawals", getTransactionsForAccountByType(bankAccountId, TYPE_WITHDRAWAL));
        financialData.put("deliveryIncome", getTransactionsForAccountByType(bankAccountId, TYPE_DELIVERY_INCOME));
        financialData.put("otherIncome", getTransactionsForAccountByType(bankAccountId, TYPE_OTHER_INCOME));

        return financialData;
    }

    /**
     * Gets net balance change for THIS account (income - expenses from transactions).
     * @return Net change from all transactions.
     */
    public float getAccountNetTransactionBalance() {
        return getAccountTotalIncome() - getAccountTotalExpenses();
    }

    // ==================== Convenience Methods for Account Transactions ====================

    /**
     * Records a purchase transaction for THIS account.
     */
    public void recordPurchase(float amount) {
        addAccountTransaction(amount, TYPE_PURCHASE);
    }

    /**
     * Records a withdrawal transaction for THIS account.
     */
    public void recordWithdrawal(float amount) {
        addAccountTransaction(amount, TYPE_WITHDRAWAL);
    }

    /**
     * Records delivery income for THIS account.
     */
    public void recordDeliveryIncome(float amount) {
        addAccountTransaction(amount, TYPE_DELIVERY_INCOME);
    }

    /**
     * Records other income for THIS account.
     */
    public void recordOtherIncome(float amount) {
        addAccountTransaction(amount, TYPE_OTHER_INCOME);
    }

    // ==================== Original Account Selection Logic ====================

    /**
     * Logic to allow user to select an account from a provided list.
     * @param accounts List of available accounts for the logged-in user
     * @return The selected BankAccount object, or null if invalid selection
     */
    public static selectBankAccount selectAccount(List<selectBankAccount> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return null;
        }

        System.out.println("Please select an account by ID:");
        for (selectBankAccount acc : accounts) {
            System.out.println("ID: " + acc.getAccountID() + " | Type: " + acc.getAccountType()
                    + " | Balance: $" + acc.getBalance() + " | Other Income: $" + acc.getOtherIncome());
        }

        Scanner input = new Scanner(System.in);
        System.out.print("Enter Account ID: ");
        String selectedID = input.nextLine();

        for (selectBankAccount acc : accounts) {
            if (acc.getAccountID().equals(selectedID)) {
                System.out.println("Account " + selectedID + " selected.");
                return acc;
            }
        }

        System.out.println("Invalid Account ID.");
        return null;
    }

    // ==================== Manage Finances Methods ====================

    public void deposit(float amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.println("Deposited: $" + amount + ". New Balance: $" + this.balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(float amount) {
        if (amount > 0 && (this.balance - amount) >= 0) {
            this.balance -= amount;
            System.out.println("Withdrew: $" + amount + ". New Balance: $" + this.balance);
        } else {
            System.out.println("Insufficient funds or invalid amount.");
        }
    }

    public void applyMonthlyFees() {
        this.balance -= accountFees;
        System.out.println("Fees applied: $" + accountFees + ". New Balance: $" + this.balance);
    }
}
