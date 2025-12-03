 package org.example.deliveryRecorder.src;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class deliveryDataServiceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private deliveryDataServiceDAO deliveryDataDAO;


    // Fields (Data Model)
    private long jobsId;
    private long dateTime; // Changed to long for timestamp compatibility
    private int milesDriven;

    public long getJobId() {
        return jobsId;
    }

    public void setJobId(long jobsId) {
        this.jobsId = jobsId;
    }

    public boolean saveDelivery(deliveryDataFormService form, long jobsId) {
        // Assuming we have a current job ID context.
        // For this snippet, I'll insert a placeholder job ID or pass it in.

        int currentJobId = 1; // Placeholder


        String sql = "INSERT INTO deliveryData(" +
                "time, miles, basePay, extraExpenses, platform, " +
                "totalTimeSpent, timeSpentWaiting, resturant, jobsTableId" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                form.getDateTime(),
                form.getMilesDriven(),
                form.getBasePay(),
                form.getExpenses(),
                form.getPlatform(),
                form.getTotalTimeSpent(),
                form.getTimeSpentWaitingAtRestaurant(),
                form.getRestaurant(),
                currentJobId
        );
        return true;
    }


}

    /**
     * Deletes a delivery record by ID (if you had an ID field).
     * This is just a placeholder to show typical DAO structure.
     */

