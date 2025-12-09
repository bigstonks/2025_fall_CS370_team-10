package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class addBankAccount {

    @Autowired
    private addBankAccountDAO addBankAccountDAO;

    // Fields aligned with 'bankAccount' table in the database

    private String accountName; // Present in delivery.db schema
    private String accountType;
    private double balance;
    private double interestRate;
    private double accountFees;
    private double otherIncome;

    // Helper field to identify the user before getting userId
    private String ownerUsername;

    public addBankAccount() {
        super();
    }

    /**
     * Method to validate and save the new bank account information.
     * Aligned with database columns: accountName, accountType, balance, interestRate, accountFees, otherIncome.
     */
    public boolean createNewBankAccount(String ownerUsername, String accountName, String accountType,
                                        double initialBalance, double interestRate, double accountFees, double otherIncome) {
        // 1. Set the local fields

        this.accountName = accountName;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.interestRate = interestRate;
        this.accountFees = accountFees;
        this.otherIncome = otherIncome;

        // 2. Basic Validation
        if (!validateInput()) {
            System.out.println("Error: Invalid bank account details.");
            return false;
        }

        // 3. Persist using the DAO
        try {
            // The DAO should handle looking up userId from ownerUsername if needed, or we set it before saving
            return addBankAccountDAO.saveNewAccount(this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateInput() {
        if (ownerUsername == null || ownerUsername.trim().isEmpty()) return false;
        if (accountName == null || accountName.trim().isEmpty()) return false;
        // accountType is optional in DB but good to have
        return true;
    }

    // Getters and Setters matching Database Columns

   // public int getUserId() { return userId; }
   // public void setUserId(int userId) { this.userId = userId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public double getAccountFees() { return accountFees; }
    public void setAccountFees(double accountFees) { this.accountFees = accountFees; }

    public double getOtherIncome() { return otherIncome; }
    public void setOtherIncome(double otherIncome) { this.otherIncome = otherIncome; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
