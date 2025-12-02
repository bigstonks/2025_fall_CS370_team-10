package org.example.manageFinances.src;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a Bank Account and the logic to select/manage it.
 */
public class selectBankAccount {
    private String accountID;
    private float balance;
    private float interestRate;
    private float accountFees;
    private float otherIncome; // New Field
    private String accountType;
    private String accountName;

    // Constructors
    public selectBankAccount() {}

    public selectBankAccount(String accountID, float balance, String accountType, float otherIncome, String accountName, float interestRate, float accountFees ) {
        this.accountID = accountID;
        this.balance = balance;
        this.accountType = accountType;
        this.otherIncome = otherIncome;
        this.interestRate = interestRate; // Default 2%
        this.accountFees = accountFees;
        this.accountName = accountName;// Default fee
    }

    // Getters
    public String getAccountID() { return accountID; }
    public float getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public float getOtherIncome() { return otherIncome; }

    public void setOtherIncome(float otherIncome) {
        this.otherIncome = otherIncome;
    }

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

    // --- Manage Finances Methods ---

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
