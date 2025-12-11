package org.example.deliveryRecorder.src;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
public class deliveryDataService {
    @Autowired
    private deliveryDataServiceDAO deliveryDataDAO;


    // Fields (Data Model)
    private long dateTimeStart; // Changed to long for timestamp compatibility
    private long dateTimeEnd;
    private int milesDriven;
    private float basePay;
    private float expenses;
    private String platform;
    private int totalTimeSpent; // In minutes
    private String fromAddress;
    private String toAddress;
    private int minutesSpentWaitingAtResturant; // In minutes
    private String restaurant;
    private float tips;


    // --- Getters and Setters (Fixed Logic) ---

    public void setDateTimeStart(long dateTimeStart) {
        this.dateTimeStart = dateTimeStart;
    }
    public void setDateTimeEnd(long dateTimeEnd) {
        this.dateTimeEnd = dateTimeEnd;
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

    public void setFromAddress(String fromAddress) {

        this.fromAddress = fromAddress;
    }
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public void setMinutesSpentWaitingAtResturant(int waitingAtRestaurant) {
        this.minutesSpentWaitingAtResturant = waitingAtRestaurant;
    }
    public void setTips(float tips) {
        this.tips = tips;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    // --- Getters (Added for DAO Access) ---

    public long getDateTimeStart() { return dateTimeStart; }
    public long getDateTimeEnd() { return dateTimeEnd; }
    public int getMilesDriven() { return milesDriven; }
    public float getBasePay() { return basePay; }
    public float getExpenses() { return expenses; }
    public String getPlatform() { return platform; }

    /**
     * Calculates and returns the total time spent in minutes based on start and end timestamps.
     * @return Total time spent in minutes, or 0 if timestamps are invalid
     */
    public int getTotalTimeSpent() {
        if (dateTimeStart <= 0 || dateTimeEnd <= 0 || dateTimeEnd <= dateTimeStart) {
            return totalTimeSpent; // Fall back to manually set value if timestamps invalid
        }
        // Calculate time difference in minutes (timestamps are in milliseconds)
        long diffMillis = dateTimeEnd - dateTimeStart;
        return (int) (diffMillis / (1000 * 60));
    }

    public String getFromAddressfromAdress() { return fromAddress; }
    public String getToAddressfromAdress() { return toAddress; }
    public int getMinutesSpentWaitingAtResturant() { return minutesSpentWaitingAtResturant; }
    public String getRestaurant() { return restaurant; }
    public float getTips() { return tips; }

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
       /*if (totalTimeSpent <= 0) {
            return "Total time spent must be greater than zero.";
        }*/
        return null; // No errors
    }

    // --- Service Logic (Nested for this file structure) ---

    @Service
    public static class DeliveryInputService {

        public boolean processDeliveryInput(deliveryDataService form) {
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


        private void saveToDatabase(deliveryDataService form) {
            // Call to DAO would go here
            System.out.println("Data saved successfully for restaurant: " + form.restaurant);
        }
    public boolean createDeliveryDetails(long jobsId) {
        String validationError = validateDelivery();
        if (validationError != null) {
            System.out.println("Validation Error: " + validationError);
            return false;
        }

        try {
            deliveryDataDAO.saveDelivery(this, jobsId);
            System.out.println("Delivery details inserted into database for job ID: " + jobsId);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to insert delivery details into database.");
            e.printStackTrace();
            return false;
        }
    }
}

