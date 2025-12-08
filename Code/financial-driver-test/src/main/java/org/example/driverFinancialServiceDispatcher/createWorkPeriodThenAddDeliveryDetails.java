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

        // Obtain necessary services from Spring context
        workPeriodService wpService = context.getBean(workPeriodService.class);
        deliveryDataService delivery = context.getBean(deliveryDataService.class);

        // Use a fixed user id of 1 as requested
        int userId = 1;
        System.out.println("Starting automated flow for userId=1");

        // Populate and create a new work period
        wpService.setVehicle("Auto-Gen-Vehicle");
        wpService.setTotalVehicleMiles(50);
        wpService.setTotalHoursWorked(3);
        long now = System.currentTimeMillis();
        wpService.setStartTime(now);
        wpService.setEndTime(now + 1000 * 60 * 60); // +1 hour

        long jobsId = wpService.createWorkPeriod(userId);
        if (jobsId == -1) {
            System.out.println("Failed to create work period for userId=1");
            return;
        }

        System.out.println("Work period created with jobsId=" + jobsId);

        // Create a delivery linked to the created work period
        delivery.setDateTimeStart(System.currentTimeMillis());
        delivery.setMilesDriven(12);
        delivery.setBasePay(18.50f);
        delivery.setExpenses(3.25f);
        delivery.setPlatform("ExamplePlatform");
        delivery.setTotalTimeSpent(25);
        delivery.setMinutesSpentWaitingAtResturant(4);
        delivery.setRestaurant("Example Restaurant");

        boolean added = wpService.addDelivery(delivery);
        if (added) {
            System.out.println("Delivery added successfully to jobsId=" + wpService.getJobsId());
        } else {
            System.out.println("Failed to add delivery to jobsId=" + wpService.getJobsId());
        }

        // Optionally shut down Spring context to end the app
        // SpringApplication.exit(context);
    }
}
