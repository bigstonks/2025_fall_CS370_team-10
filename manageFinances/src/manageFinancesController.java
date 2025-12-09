package org.example.manageFinances.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finances")
public class manageFinancesController {

    @Autowired
    private addBankAccount addBankAccountService;

    @Autowired
    private selectBankAccountDAO selectBankAccountDAO;

    @Autowired
    private generalFinancialData generalFinancialDataService;

    // ==================== Bank Account Endpoints ====================

    @PostMapping("/accounts")
    public ResponseEntity<String> createBankAccount(
            @RequestParam String ownerUsername,
            @RequestParam String accountName,
            @RequestParam String accountType,
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam double accountFees,
            @RequestParam double otherIncome) {

        boolean success = addBankAccountService.createNewBankAccount(
                ownerUsername, accountName, accountType, initialBalance, interestRate, accountFees, otherIncome);

        if (success) {
            return ResponseEntity.ok("Bank account created successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to create bank account");
        }
    }

    @GetMapping("/accounts/{userId}")
    public ResponseEntity<List<selectBankAccount>> getUserAccounts(@PathVariable int userId) {
        List<selectBankAccount> accounts = selectBankAccountDAO.getAccountsForUser(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/details/{accountId}")
    public ResponseEntity<selectBankAccount> getAccountById(@PathVariable String accountId) {
        selectBankAccount account = selectBankAccountDAO.findById(accountId);
        if (account != null) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/accounts/{accountId}/balance")
    public ResponseEntity<String> updateAccountBalance(
            @PathVariable String accountId,
            @RequestParam float newBalance) {

        selectBankAccountDAO.updateBalance(accountId, newBalance);
        return ResponseEntity.ok("Balance updated successfully");
    }

    // ==================== Financial Overview Endpoints ====================

    @GetMapping("/overview/{userId}/assets")
    public ResponseEntity<Float> getTotalAssets(@PathVariable int userId) {
        float totalAssets = generalFinancialDataService.getTotalAssets(userId);
        return ResponseEntity.ok(totalAssets);
    }

    @GetMapping("/overview/{userId}/expenses")
    public ResponseEntity<Float> getTotalExpenses(@PathVariable int userId) {
        float totalExpenses = generalFinancialDataService.getTotalExpense(userId);
        return ResponseEntity.ok(totalExpenses);
    }

    @GetMapping("/overview/{userId}/networth")
    public ResponseEntity<Float> getNetWorth(@PathVariable int userId) {
        float netWorth = generalFinancialDataService.getNetFinancialPosition(userId);
        return ResponseEntity.ok(netWorth);
    }

    @GetMapping("/overview/{userId}/transactions")
    public ResponseEntity<Map<String, List<Float>>> getAllTransactions(@PathVariable int userId) {
        Map<String, List<Float>> transactions = generalFinancialDataService.getAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    // ==================== Account Operations Endpoints ====================

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable String accountId,
            @RequestParam float amount) {

        selectBankAccount account = selectBankAccountDAO.findById(accountId);
        if (account != null) {
            account.deposit(amount);
            selectBankAccountDAO.updateBalance(accountId, account.getBalance());
            return ResponseEntity.ok("Deposit successful. New balance: " + account.getBalance());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable String accountId,
            @RequestParam float amount) {

        selectBankAccount account = selectBankAccountDAO.findById(accountId);
        if (account != null) {
            account.withdraw(amount);
            selectBankAccountDAO.updateBalance(accountId, account.getBalance());
            return ResponseEntity.ok("Withdrawal successful. New balance: " + account.getBalance());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/accounts/{accountId}/apply-fees")
    public ResponseEntity<String> applyMonthlyFees(@PathVariable String accountId) {
        selectBankAccount account = selectBankAccountDAO.findById(accountId);
        if (account != null) {
            account.applyMonthlyFees();
            selectBankAccountDAO.updateBalance(accountId, account.getBalance());
            return ResponseEntity.ok("Monthly fees applied. New balance: " + account.getBalance());
        }
        return ResponseEntity.notFound().build();
    }
}