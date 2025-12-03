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
    public long insertWorkPeriod(workPeriodService workPeriod) {
        String sql = "INSERT INTO JobsTable (" +
                "startTime, endTime, vehicle, totalVehicleMiles, vehicleMPG, totalHoursWorked" +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, workPeriod.getStartTime());
                ps.setInt(2, workPeriod.getEndTime());
                ps.setString(3, workPeriod.getVehicle());
                ps.setInt(4, workPeriod.getTotalVehicleMiles());
                ps.setInt(5, workPeriod.getVehicleMPG());
                ps.setInt(6, workPeriod.getTotalHoursWorked());
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
                "totalVehicleMiles=?, vehicleMPG=?, totalHoursWorked=? WHERE id=?";

        try {
            int rows = jdbcTemplate.update(sql,
                    workPeriod.getStartTime(),
                    workPeriod.getEndTime(),
                    workPeriod.getVehicle(),
                    workPeriod.getTotalVehicleMiles(),
                    workPeriod.getVehicleMPG(),
                    workPeriod.getTotalHoursWorked(),
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

    public boolean saveDelivery(deliveryDataFormService form, long jobsId) {
        String sql = "INSERT INTO deliveryData(" +
                "time, miles, basePay, extraExpenses, platform, " +
                "totalTimeSpent, timeSpentWaiting, resturant, jobsTableId" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                    jobsId
            );
            return rows == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}