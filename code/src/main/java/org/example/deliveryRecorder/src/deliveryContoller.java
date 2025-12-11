package org.example.deliveryRecorder.src;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class deliveryContoller {

    // The DAO handles database interactions
    @Autowired
    private deliveryDataServiceDAO deliveryDAO;
    private long jobsId;

    // The Overview handles the current session/workday state (injected via Spring)
    @Autowired
    private overviewService jobOverview;

    // Default constructor for Spring
    public deliveryContoller() {
    }

    // Constructor for dependency injection
    @Autowired
    public deliveryContoller(overviewService overviewService) {
        this.jobOverview = overviewService;
    }


    /**
     * Starts the logic to create a new delivery job.
     * In a Swing app, this might open the Form window.
     */
   /* public void initNewDelivery() {
        // Delegates to the session manager to handle input logic
        jobOverview.createNewJob();
    }*/

    /**
     * Saves a fully formed delivery object to the database.
     * @param form The populated form data
     * @return true if successful
     */
    public boolean saveDeliveryRecord(deliveryDataService form) {
        // 1. Validate again to be safe
        String error = form.validateDelivery();
        if (error != null) {
            System.out.println("Save failed: " + error);
            return false;
        }

        // 2. Use DAO to persist
        try {
            deliveryDAO.saveDelivery(form, jobsId);
            System.out.println("Delivery saved to Database.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }
    /*public boolean setVehicleForWorkPeriod(String vehicleModel) {
        return workPeriodService.setVehicle(vehicleModel);
    }*/



    // --- Overview retrieval methods ---

    /**
     * Gets all past deliveries for a user with combined work period data.
     * @param userId The user ID
     * @return List of OverviewDTO containing delivery and work period information
     */
    public List<overviewService.OverviewDTO> getPastDeliveries(int userId) {
        return jobOverview.getFullOverviewByUser(userId);
    }

    /**
     * Gets total earnings for a user across all deliveries.
     * @param userId The user ID
     * @return Total earnings (base pay + tips)
     */
    public double getTotalEarnings(int userId) {
        return jobOverview.getTotalEarningsByUser(userId);
    }

    /**
     * Gets the total number of deliveries for a user.
     * @param userId The user ID
     * @return Total delivery count
     */
    public int getTotalDeliveryCount(int userId) {
        return jobOverview.getTotalDeliveriesByUser(userId);
    }

    /**
     * Gets total miles driven for a user across all deliveries.
     * @param userId The user ID
     * @return Total miles driven
     */
    public int getTotalMiles(int userId) {
        return jobOverview.getTotalMilesByUser(userId);
    }

    /**
     * Gets all work periods for a user.
     * @param userId The user ID
     * @return List of work periods
     */
    public List<workPeriodService> getAllWorkPeriods(int userId) {
        return jobOverview.getAllWorkPeriodsByUser(userId);
    }

    /**
     * Gets all deliveries for a user.
     * @param userId The user ID
     * @return List of deliveries
     */
    public List<deliveryDataService> getAllDeliveries(int userId) {
        return jobOverview.getAllDeliveriesByUser(userId);
    }

    /**
     * Gets deliveries for a specific work period.
     * @param workPeriodId The work period ID
     * @return List of deliveries in the work period
     */
    public List<deliveryDataService> getDeliveriesForWorkPeriod(int workPeriodId) {
        return jobOverview.getDeliveriesByWorkPeriod(workPeriodId);
    }
}
