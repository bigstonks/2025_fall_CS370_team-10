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

    public boolean saveDelivery(deliveryDataService form, long jobsId) {
        String sql = "INSERT INTO deliveryData(" +
                "startTime, endTime, miles, basePay, extraExpenses, platform, " +
                "totalTimeSpent, timeSpentWaiting, resturant, jobsTableId, tips, " +
                "fromLocation, toLocation" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    form.getDateTimeStart(),
                    form.getDateTimeEnd(),
                    form.getMilesDriven(),
                    form.getBasePay(),
                    form.getExpenses(),
                    form.getPlatform(),
                    form.getTotalTimeSpent(), // Now calculated from start/end times
                    form.getMinutesSpentWaitingAtResturant(),
                    form.getRestaurant(),
                    jobsId,
                    form.getTips(),
                    form.getFromAddressfromAdress(),
                    form.getToAddressfromAdress()
            );
            return true;
        } catch (Exception e) {
            System.out.println("Error saving delivery: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


}

    /**
     * Deletes a delivery record by ID (if you had an ID field).
     * This is just a placeholder to show typical DAO structure.
     */

