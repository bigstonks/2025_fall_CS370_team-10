package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class workPeriodServiceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new work period into the jobsTable.
     * @param workPeriod The workPeriodService object containing work period details
     * @return The auto-generated job ID, or -1 if insertion failed
     */
    public long insertWorkPeriod(workPeriodService workPeriod, int userId) {
        String sql = "INSERT INTO JobsTable (userId, startTime, endTime, vehicle, totalVehicleMiles) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setLong(2, workPeriod.getStartTime());
                ps.setLong(3, workPeriod.getEndTime());
                ps.setString(4, workPeriod.getVehicle());
                ps.setInt(5, workPeriod.getTotalVehicleMiles());
                return ps;
            }, keyHolder);

            Number generatedId = keyHolder.getKey();
            return generatedId != null ? generatedId.longValue() : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Updates an existing work period record.
     * @param jobId The ID of the work period to update
     * @param workPeriod The updated workPeriodService object
     * @return true if update was successful, false otherwise
     */
    public boolean updateWorkPeriod(long jobId, workPeriodService workPeriod) {
        String sql = "UPDATE JobsTable SET startTime=?, endTime=?, vehicle=?, " +
                "totalVehicleMiles=? WHERE jobsId=?";

        try {
            int rows = jdbcTemplate.update(sql,
                    workPeriod.getStartTime(),
                    workPeriod.getEndTime(),
                    workPeriod.getVehicle(),
                    workPeriod.getTotalVehicleMiles(),
                    jobId
            );
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a work period record by ID.
     * @param jobId The work period ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteWorkPeriod(long jobId) {
        String sql = "DELETE FROM JobsTable WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, jobId);
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveDelivery(deliveryDataService form, long jobsId) {
        String sql = "INSERT INTO deliveryData(" +
                "time, miles, basePay, extraExpenses, platform, " +
                "totalTimeSpent, timeSpentWaiting, resturant, jobsTableId" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            int rows = jdbcTemplate.update(sql,
                    form.getDateTimeStart(),
                    form.getMilesDriven(),
                    form.getBasePay(),
                    form.getExpenses(),
                    form.getPlatform(),
                    form.getTotalTimeSpent(),
                    form.getMinutesSpentWaitingAtResturant(),
                    form.getRestaurant(),
                    jobsId
            );
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sums all miles from deliveries associated with a specific jobId.
     * @param jobId The work period/job ID to sum miles for
     * @return The total miles from all deliveries in that work period, or 0 if none found
     */
    public int sumMilesByJobId(long jobId) {
        String sql = "SELECT COALESCE(SUM(miles), 0) FROM deliveryData WHERE jobsTableId = ?";
        try {
            Integer totalMiles = jdbcTemplate.queryForObject(sql, Integer.class, jobId);
            return totalMiles != null ? totalMiles : 0;
        } catch (Exception e) {
            System.out.println("Error summing miles for jobId " + jobId + ": " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}