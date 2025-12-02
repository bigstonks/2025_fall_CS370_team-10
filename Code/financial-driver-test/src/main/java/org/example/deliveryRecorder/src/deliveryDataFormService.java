package org.example.deliveryRecorder.src;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
public class deliveryDataFormService {
    @Autowired
    private deliveryDataServiceDAO deliveryDataDAO;


    // Fields (Data Model)
    private long dateTime; // Changed to long for timestamp compatibility
    private int milesDriven;
    private float basePay;
    private float expenses;
    private String platform;
    private int totalTimeSpent; // In minutes
    private String[] fromTo; // [StartLocation, EndLocation]
    private int timeSpentWaitingAtRestaurant; // In minutes
    private String restaurant;

    // --- Getters and Setters (Fixed Logic) ---

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Exclusively handles miles driven input.
     * Enforces non-negative values immediately.
     */
    public void setMilesDriven(int miles) {
        if (miles < 0) {
            System.out.println("Error: Miles driven cannot be negative. Setting to 0.");
            this.milesDriven = 0;
        } else {
            this.milesDriven = miles;
        }
    }

    /**
     * Exclusively handles base pay input.
     * Enforces non-negative values immediately.
     */
    public void setBasePay(float basePay) {
        if (basePay < 0) {
            System.out.println("Error: Base pay cannot be negative. Setting to 0.0.");
            this.basePay = 0.0f;
        } else {
            this.basePay = basePay;
        }
    }

    public void setExpenses(float expenses) {
        this.expenses = expenses;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setTotalTimeSpent(int totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public void setFromTo(String[] fromTo) {
        this.fromTo = fromTo;
    }

    public void setTimeSpentWaitingAtRestaurant(int waitingAtRestaurant) {
        this.timeSpentWaitingAtRestaurant = waitingAtRestaurant;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    // --- Getters (Added for DAO Access) ---

    public long getDateTime() { return dateTime; }
    public int getMilesDriven() { return milesDriven; }
    public float getBasePay() { return basePay; }
    public float getExpenses() { return expenses; }
    public String getPlatform() { return platform; }
    public int getTotalTimeSpent() { return totalTimeSpent; }
    public String[] getFromTo() { return fromTo; }
    public int getTimeSpentWaitingAtRestaurant() { return timeSpentWaitingAtRestaurant; }
    public String getRestaurant() { return restaurant; }

    /**
     * Validates the delivery data.
     * @return A string containing the error message, or null if valid.
     */
    public String validateDelivery() {
        if (milesDriven < 0) {
            return "Miles driven cannot be negative.";
        }
        if (basePay < 0) {
            return "Base pay cannot be negative.";
        }
        if (platform == null || platform.trim().isEmpty()) {
            return "Platform (e.g., UberEats, DoorDash) is required.";
        }
        if (restaurant == null || restaurant.trim().isEmpty()) {
            return "Restaurant name is required.";
        }
        if (totalTimeSpent <= 0) {
            return "Total time spent must be greater than zero.";
        }
        return null; // No errors
    }

    // --- Service Logic (Nested for this file structure) ---

    @Service
    public static class DeliveryInputService {

        public boolean processDeliveryInput(deliveryDataFormService form) {
            String validationError = form.validateDelivery();
            if (validationError != null) {
                System.out.println("Validation Error: " + validationError);
                return false;
            }

            float netProfit = form.basePay - form.expenses;
            System.out.println("Processing delivery for " + form.platform);
            System.out.println("Net Profit: $" + netProfit);

            return true;
        }
    }


        private void saveToDatabase(deliveryDataFormService form) {
            // Call to DAO would go here
            System.out.println("Data saved successfully for restaurant: " + form.restaurant);
        }
    public boolean createDeliveryDetails(long jobId) {
        this.jobId = jobId;

        String validationError = validateDelivery();
        if (validationError != null) {
            System.out.println("Validation Error: " + validationError);
            return false;
        }

        if (deliveryDataDAO.insertDeliveryData(
                jobId,
                dateTime,
                milesDriven,
                basePay,
                expenses,
                platform,
                totalTimeSpent,
                fromTo,
                timeSpentWaitingAtRestaurant,
                restaurant)) {
            System.out.println("Delivery details inserted into database for job ID: " + jobId);
            return true;
        } else {
            System.out.println("Failed to insert delivery details into database.");
            return false;
        }
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }
    }

