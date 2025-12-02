package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class workPeriodServiceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new job into the jobsTable.
     * @param form The deliveryFormValidation object containing job details
     * @return true if insertion was successful, false otherwise
     */
    public boolean insertJob(deliveryDataFormService form) {
        String sql = "INSERT INTO jobsTable (" +
                "time, miles, basePay, extraExpenses, platform, " +
                "totalTimeSpent, timeSpentWaiting, restaurant" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int rows = jdbcTemplate.update(sql,
                    form.getDateTime(),
                    form.getMilesDriven(),
                    form.getBasePay(),
                    form.getExpenses(),
                    form.getPlatform(),
                    form.getTotalTimeSpent(),
                    form.getTimeSpentWaitingAtRestaurant(),
                    form.getRestaurant()
            );
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing job record.
     * @param jobId The ID of the job to update
     * @param form The updated deliveryFormValidation object
     * @return true if update was successful, false otherwise
     */
    public boolean updateJob(long jobId, deliveryDataFormService form) {
        String sql = "UPDATE jobsTable SET time=?, miles=?, basePay=?, extraExpenses=?, " +
                "platform=?, totalTimeSpent=?, timeSpentWaiting=?, restaurant=? WHERE id=?";

        try {
            int rows = jdbcTemplate.update(sql,
                    form.getDateTime(),
                    form.getMilesDriven(),
                    form.getBasePay(),
                    form.getExpenses(),
                    form.getPlatform(),
                    form.getTotalTimeSpent(),
                    form.getTimeSpentWaitingAtRestaurant(),
                    form.getRestaurant(),
                    jobId
            );
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a job record by ID.
     * @param jobId The job ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteJob(long jobId) {
        String sql = "DELETE FROM jobsTable WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, jobId);
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}