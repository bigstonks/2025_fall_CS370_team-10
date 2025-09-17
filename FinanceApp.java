import java.util.Scanner;

public class FinanceApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TransactionService service = new TransactionService();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Finance Tracker ===");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View Totals");
            System.out.println("4. View All Transactions");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> addTransaction("income");
                case 2 -> addTransaction("expense");
                case 3 -> viewTotals();
                case 4 -> service.listTransactions();
                case 5 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static void addTransaction(String type) {
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        service.addTransaction(new Transaction(type, amount, description));
        System.out.println(type.substring(0,1).toUpperCase() + type.substring(1) + " added!");
    }

    private static void viewTotals() {
        double income = service.getTotalIncome();
        double expenses = service.getTotalExpenses();
        double profit = service.getNetProfit();

        System.out.println("Total Income: $" + income);
        System.out.println("Total Expenses: $" + expenses);
        System.out.println("Net Profit: $" + profit);
    }
}
