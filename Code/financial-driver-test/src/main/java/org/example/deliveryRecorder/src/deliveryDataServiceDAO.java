 package org.example.deliveryRecorder.src;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class deliveryDataServiceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveDelivery(deliveryDataFormService form) {
        // Assuming we have a current job ID context. 
        // For this snippet, I'll insert a placeholder job ID or pass it in.

        int currentJobId = 1; // Placeholder

        String sql = "INSERT INTO jobsTable(" +
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
    }

    /**
     * Deletes a delivery record by ID (if you had an ID field).
     * This is just a placeholder to show typical DAO structure.
     */
    public void deleteDelivery(long deliveryId) {
        String sql = "DELETE FROM jobsTable WHERE id = ?";
        jdbcTemplate.update(sql, deliveryId);
    }
}
