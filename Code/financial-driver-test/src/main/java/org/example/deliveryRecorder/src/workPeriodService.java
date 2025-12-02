package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class workPeriodService {

    @Autowired
    private workPeriodServiceDAO formDefinitonServiceDAO;

    private String vehicle;
    private int totalVehicleMiles;
    private int vehicleMPG;
    private int totalHoursWorked;
    private int startTime;
    private int endTime;

    public void addJobLog(deliveryDataFormService form) {
        if (formDefinitonServiceDAO.insertJob(form)) {
            System.out.println("Job log added successfully.");
        } else {
            System.out.println("Failed to add job log.");
        }
    }

    public void deleteJobLog() {
        // TODO: Implement deletion logic
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public void setTotalVehicleMiles(int miles) {
        this.totalVehicleMiles = miles;
    }

    public String editJobLog() {
        // TODO: Implement edit logic and return status message
        return "Job log updated.";
    }

    public int timeWaiting() {
        return 0;
    }

    public void setPendingPayout() {
        // TODO: Implement pending payout logic
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

    public void createNewJob(Scanner scanner) {
        if (scanner == null) {
            System.out.println("Scanner not provided.");
            return;
        }

        System.out.println("\n--- Enter Job Details ---");
        deliveryDataFormService form = new deliveryDataFormService();

        try {
            System.out.print("Enter date and time (timestamp in milliseconds): ");
            String dt = scanner.nextLine().trim();
            if (!dt.isEmpty()) {
                form.setDateTime(Long.parseLong(dt));
            }

            System.out.print("Enter miles driven: ");
            String miles = scanner.nextLine().trim();
            if (!miles.isEmpty()) {
                form.setMilesDriven((int) Double.parseDouble(miles));
            }

            System.out.print("Enter base pay: ");
            String basePay = scanner.nextLine().trim();
            if (!basePay.isEmpty()) {
                form.setBasePay(Float.parseFloat(basePay));
            }

            System.out.print("Enter extra expenses: ");
            String expenses = scanner.nextLine().trim();
            if (!expenses.isEmpty()) {
                form.setExpenses(Float.parseFloat(expenses));
            }

            System.out.print("Enter platform (e.g., DoorDash, Uber Eats): ");
            String platform = scanner.nextLine().trim();
            if (!platform.isEmpty()) {
                form.setPlatform(platform);
            }

            System.out.print("Enter total time spent (minutes): ");
            String totalTime = scanner.nextLine().trim();
            if (!totalTime.isEmpty()) {
                form.setTotalTimeSpent(Integer.parseInt(totalTime));
            }

            System.out.print("Enter time spent waiting at restaurant (minutes): ");
            String waitTime = scanner.nextLine().trim();
            if (!waitTime.isEmpty()) {
                form.setTimeSpentWaitingAtRestaurant(Integer.parseInt(waitTime));
            }

            System.out.print("Enter restaurant name: ");
            String restaurant = scanner.nextLine().trim();
            if (!restaurant.isEmpty()) {
                form.setRestaurant(restaurant);
            }

            // New: prompt for start and end times (integers)
            System.out.print("Enter start time (int, e.g. minutes since midnight): ");
            String st = scanner.nextLine().trim();
            if (!st.isEmpty()) {
                setStartTime(Integer.parseInt(st));
            }

            System.out.print("Enter end time (int, e.g. minutes since midnight): ");
            String et = scanner.nextLine().trim();
            if (!et.isEmpty()) {
                setEndTime(Integer.parseInt(et));
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid numeric input. Aborting job creation.");
            return;
        }

        addJobLog(form);
    }

}