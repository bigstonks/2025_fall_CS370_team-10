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
        // Passing "N/A" for accountType as we removed it from database insertion logic
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username for new account:");
        String username = scanner.nextLine();
        System.out.println("Enter password for new account:");
        String password = scanner.nextLine();
        System.out.println("Enter email for new account:");
        String email = scanner.nextLine();




        boolean success = controller.handleCreateAccount(username, password, email);

        if (success) {
            System.out.println("Main: Account successfully created!");
        } else {
            System.out.println("Main: Account creation failed (Duplicate user or invalid input).");
        }


       /* ApplicationContext context = SpringApplication.run(FinancialDriverTestApplication.class, args);

        // 2. Retrieve the Controller Bean
        userAccountController controller = context.getBean(userAccountController.class);


        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username:");
        String username = scanner.nextLine();
        System.out.println("Enter password:");

        String password = scanner.nextLine();

        boolean success = controller.handleLoginRequest(username, password);

        if (success) {
            System.out.println("Main: logged in!");
        } else {
            System.out.println("Main: login failed Duplicate user or invalid input).");
        }*/


    }
}