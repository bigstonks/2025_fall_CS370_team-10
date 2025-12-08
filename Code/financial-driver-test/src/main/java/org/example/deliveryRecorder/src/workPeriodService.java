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
    private int totalHoursWorked;
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

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public void setTotalVehicleMiles(int miles) {
        this.totalVehicleMiles = miles;
    }

    public void setEndTime(long time) {
        this.endTime = time;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public void setTotalHoursWorked(int hours) {
        this.totalHoursWorked = hours;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getTotalVehicleMiles() {
        return totalVehicleMiles;
    }


    public int getTotalHoursWorked() {
        return totalHoursWorked;
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