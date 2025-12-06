 package org.example.deliveryRecorder.src;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class deliveryContoller {

    // The DAO handles database interactions
    @Autowired
    private deliveryDataServiceDAO deliveryDAO;
    private long jobsId;

    // The Overview handles the current session/workday state
    private final deliveryJobOverview jobOverview;

    public deliveryContoller() {
        // Initialize a new session when controller starts (or handle via Dependency Injection)
        this.jobOverview = new deliveryJobOverview();
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

    /**
     * Ends the current shift and prints/returns summary.
     */
    public void finishShift() {
        jobOverview.endShift();
        // Logic to save shift summary to another table could go here
    }

    /**
     * Updates vehicle metadata for the current session.
     */
    public void setShiftVehicle(String vehicleName, int mpg) {
        jobOverview.setVehicleDetails(vehicleName, mpg);
    }
}
