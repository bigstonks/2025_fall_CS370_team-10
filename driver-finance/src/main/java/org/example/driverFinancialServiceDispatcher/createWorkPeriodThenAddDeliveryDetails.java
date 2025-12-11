package org.example.driverFinancialServiceDispatcher;

import org.example.deliveryRecorder.src.deliveryDataService;
import org.example.deliveryRecorder.src.workPeriodService;
import org.example.userAccountController.src.createAccountDAO;
import org.example.userAccountController.src.loginAuthentication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;

/**
 * Interactive helper: prompts user to login, then create a work period and a delivery linked to it.
 */
public class createWorkPeriodThenAddDeliveryDetails {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FinancialDriverTestApplication.class, args);

        loginAuthentication auth = context.getBean(loginAuthentication.class);
        createAccountDAO accountDAO = context.getBean(createAccountDAO.class);
        workPeriodService wpService = context.getBean(workPeriodService.class);
        deliveryDataService delivery = context.getBean(deliveryDataService.class);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please login to continue.");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean valid = auth.validateLogin(username, password);
        if (!valid) {
            System.out.println("Login failed. Exiting.");
            return;
        }

        Integer userId = accountDAO.getUserIdByUsername(username);
        if (userId == null) {
            System.out.println("Unable to determine user ID for username: " + username);
            return;
        }

        System.out.println("Login successful. User ID=" + userId);

        // Collect work period info
        System.out.println("Enter new work period details:");
        System.out.print("Vehicle (string): ");
        String vehicle = scanner.nextLine();
        System.out.print("Total vehicle miles (integer): ");
        int totalMiles = Integer.parseInt(scanner.nextLine());
        System.out.print("Vehicle MPG (integer): ");
        int mpg = Integer.parseInt(scanner.nextLine());
        System.out.print("Total hours worked (integer): ");
        int hours = Integer.parseInt(scanner.nextLine());

        long now = System.currentTimeMillis();
        wpService.setStartTime(now);
        wpService.setEndTime(now + 1000 * 60 * 60); // default +1 hour unless user wants to change
        wpService.setVehicle(vehicle);
        wpService.setTotalVehicleMiles(totalMiles);
        wpService.setVehicleMPG(mpg);
        wpService.setTotalHoursWorked(hours);

        long jobsId = wpService.createWorkPeriod(userId);
        if (jobsId == -1) {
            System.out.println("Failed to create work period. Exiting.");
            return;
        }

        System.out.println("Work period created with jobsId=" + jobsId);

        // Collect delivery info
        System.out.println("Enter delivery details for the created work period:");
        System.out.print("Miles driven (integer): ");
        int milesDriven = Integer.parseInt(scanner.nextLine());
        System.out.print("Base pay (float): ");
        float basePay = Float.parseFloat(scanner.nextLine());
        System.out.print("Expenses (float): ");
        float expenses = Float.parseFloat(scanner.nextLine());
        System.out.print("Platform (string): ");
        String platform = scanner.nextLine();
        System.out.print("Total time spent (minutes, integer): ");
        int totalTime = Integer.parseInt(scanner.nextLine());
        System.out.print("Time spent waiting at restaurant (minutes, integer): ");
        int waitTime = Integer.parseInt(scanner.nextLine());
        System.out.print("Restaurant (string): ");
        String restaurant = scanner.nextLine();

        // Populate delivery bean
        delivery.setDateTimeStart(System.currentTimeMillis());
        delivery.setMilesDriven(milesDriven);
        delivery.setBasePay(basePay);
        delivery.setExpenses(expenses);
        delivery.setPlatform(platform);
        delivery.setTotalTimeSpent(totalTime);
        delivery.setMinutesSpentWaitingAtResturant(waitTime);
        delivery.setRestaurant(restaurant);

        boolean added = wpService.addDelivery(delivery);
        if (added) {
            System.out.println("Delivery added successfully to jobsId=" + wpService.getJobsId());
        } else {
            System.out.println("Failed to add delivery to jobsId=" + wpService.getJobsId());
        }

        scanner.close();
        // Optional: SpringApplication.exit(context);
    }
}
