import java.util.ArrayList;

public class TransactionService {
    private final ArrayList<Transaction> transactions = new ArrayList<>();

    // Add a transaction
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    // Calculate totals
    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("income"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNetProfit() {
        return getTotalIncome() - getTotalExpenses();
    }

    // List all transactions
    public void listTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }
}
