package org.example.driverFinancialServiceDispatcher;
import java.util.Scanner;
import org.example.userAccountController.src.userAccountController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example") // Scans all sub-packages for your controllers and services
public class FinancialDriverTestApplication {
    public static void main(String[] args) {
        // 1. Start the Spring Context
        ApplicationContext context = SpringApplication.run(FinancialDriverTestApplication.class, args);

        // 2. Retrieve the Controller Bean
        userAccountController controller = context.getBean(userAccountController.class);

        // 3. Create a User Account
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username for new account:");
        String username = scanner.nextLine();
        System.out.println("Enter password for new account:");

        String password = scanner.nextLine();

        boolean success = controller.handleCreateAccount(username, password);

        if (success) {
            System.out.println("Main: Account successfully created!");
        } else {
            System.out.println("Main: Account creation failed (Duplicate user or invalid input).");
        }
    }
}