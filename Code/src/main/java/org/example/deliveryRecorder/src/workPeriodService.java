package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to group multiple deliveries together as a work period containing common information.
 * It manages work period data and can add delivery details to the database.
 */
@Service
public class workPeriodService {

    @Autowired
    private workPeriodServiceDAO workPeriodDAO;

    @Autowired
    private deliveryDataServiceDAO deliveryDataDAO;

    private long jobsId = -1; // Auto-generated ID from database
    private String vehicle;
    private int totalVehicleMiles;
    private long startTime; // changed to long to match DB
    private long endTime;   // changed to long to match DB

    private List<deliveryDataService> deliveries = new ArrayList<>();

    /**
     * Creates a new work period in the database for the specified user and stores the generated ID.
     * @param userId the user to associate the work period with
     * @return The generated jobsId, or -1 if failed
     */
    public long createWorkPeriod(int userId) {
        this.jobsId = workPeriodDAO.insertWorkPeriod(this, userId);
        if (jobsId != -1) {
            System.out.println("Work period created successfully with ID: " + jobsId);
        } else {
            System.out.println("Failed to create work period.");
        }
        return jobsId;
    }

    /**
     * Adds a new delivery to this work period.
     * @param delivery The delivery details to add
     * @return true if successful, false otherwise
     */
    public boolean addDelivery(deliveryDataService delivery) {
        if (jobsId == -1) {
            System.out.println("Error: Work period must be created first. Call createWorkPeriod().");
            return false;
        }

        String validationError = delivery.validateDelivery();
        if (validationError != null) {
            System.out.println("Validation Error: " + validationError);
            return false;
        }

        if (deliveryDataDAO.saveDelivery(delivery, jobsId)) {
            deliveries.add(delivery);
            System.out.println("Delivery added to work period ID: " + jobsId);
            return true;
        } else {
            System.out.println("Failed to add delivery to database.");
            return false;
        }
    }

    public void deleteWorkPeriod() {
        if (jobsId != -1) {
            workPeriodDAO.deleteWorkPeriod(jobsId);
        }
    }

    // --- Getters and Setters ---

    public long getJobsId() {
        return jobsId;
    }

    public String setVehicle(String vehicle) {

       return this.vehicle = vehicle;
    }

    public void setTotalVehicleMiles(int miles) {
        this.totalVehicleMiles = miles;
    }

    /**
     * Calculates and sets the total vehicle miles by summing all miles from deliveries
     * associated with this work period's jobId in the database.
     * @return the total miles calculated, or 0 if no deliveries or jobId is not set
     */
    public int calculateAndSetTotalVehicleMiles() {
        if (jobsId == -1) {
            System.out.println("Error: Work period ID not set. Cannot calculate miles.");
            return 0;
        }

        int totalMiles = workPeriodDAO.sumMilesByJobId(jobsId);
        this.totalVehicleMiles = totalMiles;
        System.out.println("Total miles calculated for job " + jobsId + ": " + totalMiles);
        return totalMiles;
    }

    /**
     * Calculates and sets the total vehicle miles by summing all miles from deliveries
     * associated with the specified jobId.
     * @param targetJobId the jobId to calculate miles for
     * @return the total miles calculated, or 0 if no deliveries found
     */
    public int calculateAndSetTotalVehicleMiles(long targetJobId) {
        int totalMiles = workPeriodDAO.sumMilesByJobId(targetJobId);
        this.totalVehicleMiles = totalMiles;
        System.out.println("Total miles calculated for job " + targetJobId + ": " + totalMiles);
        return totalMiles;
    }

    public void setEndTime(long time) {
        this.endTime = time;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getTotalVehicleMiles() {
        return totalVehicleMiles;
    }

    /**
     * Calculates the total hours worked based on startTime and endTime.
     * @return the total hours worked, or 0 if times are not set properly
     */
    public int getTotalHoursWorked() {
        if (startTime <= 0 || endTime <= 0 || endTime <= startTime) {
            return 0;
        }
        long durationMillis = endTime - startTime;
        return (int) (durationMillis / (1000 * 60 * 60)); // Convert milliseconds to hours
    }

    /**
     * Calculates the total minutes worked based on startTime and endTime.
     * @return the total minutes worked, or 0 if times are not set properly
     */
    public int getTotalMinutesWorked() {
        if (startTime <= 0 || endTime <= 0 || endTime <= startTime) {
            return 0;
        }
        long durationMillis = endTime - startTime;
        return (int) (durationMillis / (1000 * 60)); // Convert milliseconds to minutes
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public List<deliveryDataService> getDeliveries() {
        return deliveries;
    }
}