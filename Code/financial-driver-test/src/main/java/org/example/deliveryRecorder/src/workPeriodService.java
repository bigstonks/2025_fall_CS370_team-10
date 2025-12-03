package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    private int vehicleMPG;
    private int totalHoursWorked;
    private int startTime;
    private int endTime;

    private List<deliveryDataFormService> deliveries = new ArrayList<>();

    /**
     * Creates a new work period in the database and stores the generated ID.
     * @return The generated jobsId, or -1 if failed
     */
    public long createWorkPeriod() {
        this.jobsId = workPeriodDAO.insertWorkPeriod(this);
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
    public boolean addDelivery(deliveryDataFormService delivery) {
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

    public void setEndTime(int time) {
        this.endTime = time;
    }

    public void setStartTime(int time) {
        this.startTime = time;
    }

    public void setTotalHoursWorked(int hours) {
        this.totalHoursWorked = hours;
    }

    public void setVehicleMPG(int mpg) {
        this.vehicleMPG = mpg;
    }

    public String getVehicle() {
        return vehicle;
    }

    public int getTotalVehicleMiles() {
        return totalVehicleMiles;
    }

    public int getVehicleMPG() {
        return vehicleMPG;
    }

    public int getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public List<deliveryDataFormService> getDeliveries() {
        return deliveries;
    }
}