package org.example.manageFinances.src;

import java.util.Optional;

public interface BankAccountDAO {
    Optional<BankAccount> findById(long id);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    void updateBalance(long id, double newBalance);
    void create(BankAccount account);
    void delete(long id);
}
