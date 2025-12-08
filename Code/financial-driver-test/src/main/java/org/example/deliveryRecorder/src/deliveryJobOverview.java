package org.example.deliveryRecorder.src;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages the overview of a delivery shift/workday.
 * Handles vehicle metadata and the collection of individual delivery jobs.
 */
public class deliveryJobOverview {

    // Metadata for the workday/shift
    private String vehicleDriven;
    private long shiftStartTime;
    private long shiftEndTime;
    
    // List of completed delivery jobs for this session
    private List<deliveryDataService> completedJobs;
    
    // Reference to the service that defines/saves the data
    private workPeriodService formService;

    public deliveryJobOverview() {
        this.completedJobs = new ArrayList<>();
        this.formService = new workPeriodService();
        this.shiftStartTime = System.currentTimeMillis();
    }

    // --- Metadata Management ---

    public void setVehicleDetails(String vehicle) {
        this.vehicleDriven = vehicle;
        // Sync with the service if needed
        formService.setVehicle(vehicleDriven);
    }

    public void endShift() {
        this.shiftEndTime = System.currentTimeMillis();
        long duration = shiftEndTime - shiftStartTime;
        System.out.println("Shift ended. Duration: " + (duration / 1000 / 60) + " minutes.");
        // formService.setWorkPeriodTime(...);
    }

    // --- Job Management Logic ---

    /**
     * Creates a new delivery job logic.
     * This mimics a "Form" where user inputs data.
     */
    /*public void createNewJob() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- New Delivery Job ---");

        deliveryDataForm newJob = new deliveryDataForm();

        // 1. Input Data (Simulating Form)
        System.out.print("Enter Platform (e.g. UberEats): ");
        newJob.setPlatform(scanner.nextLine());

        System.out.print("Enter Restaurant Name: ");
        newJob.setRestaurant(scanner.nextLine());

        System.out.print("Enter Miles Driven: ");
        newJob.setMilesDriven(Integer.parseInt(scanner.nextLine()));

        System.out.print("Enter Base Pay: ");
        newJob.setBasePay(Float.parseFloat(scanner.nextLine()));

        // 2. Validate
        String error = newJob.validateDelivery();
        if (error != null) {
            System.out.println("Error creating job: " + error);
            return;
        }

        // 3. Add to local list and Service
        completedJobs.add(newJob);
        formService.addJobLog(newJob); // Notify service
        System.out.println("Job added successfully.");
    }*/


    /**
     * Allows user to select and view a specific job from the list.
     */
    public void selectJobToView() {
        if (completedJobs.isEmpty()) {
            System.out.println("No jobs recorded yet.");
            return;
        }

        System.out.println("Select a job ID (0 to " + (completedJobs.size() - 1) + "): ");
        for (int i = 0; i < completedJobs.size(); i++) {
            deliveryDataService job = completedJobs.get(i);
            System.out.println(i + ": " + job.getRestaurant() + " (" + job.getPlatform() + ") - $" + job.getBasePay());
        }

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        if (choice >= 0 && choice < completedJobs.size()) {
            deliveryDataService selected = completedJobs.get(choice);
            System.out.println("Selected Job: " + selected.getRestaurant());
            System.out.println("Pay: $" + selected.getBasePay());
            // Add more details as needed
        } else {
            System.out.println("Invalid selection.");
        }
    }
}
