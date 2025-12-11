package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO for read-only access to combined delivery and work period data.
 */
@Repository
public class overviewServiceDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public overviewServiceDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- Work Period Queries ---

    public List<workPeriodService> findAllWorkPeriodsByUserId(int userId) {
        String sql = "SELECT * FROM JobsTable WHERE userId = ? ORDER BY startTime DESC";
        return jdbcTemplate.query(sql, new WorkPeriodRowMapper(), userId);
    }

    public workPeriodService findWorkPeriodById(int workPeriodId) {
        String sql = "SELECT * FROM JobsTable WHERE jobsId = ?";
        return jdbcTemplate.queryForObject(sql, new WorkPeriodRowMapper(), workPeriodId);
    }

    // --- Delivery Data Queries ---

    public List<deliveryDataService> findAllDeliveriesByUserId(int userId) {
        // Use the actual column name 'startTime' and select all deliveryData columns
        String sql = "SELECT d.* FROM deliveryData d " +
            "INNER JOIN JobsTable j ON d.jobsTableId = j.jobsId " +
            "WHERE j.userId = ? " +
            "ORDER BY d.startTime DESC";
        return jdbcTemplate.query(sql, new DeliveryDataRowMapper(), userId);
    }

    public List<deliveryDataService> findDeliveriesByWorkPeriodId(int workPeriodId) {
        String sql = "SELECT * FROM deliveryData WHERE jobsTableId = ? ORDER BY startTime DESC";
        return jdbcTemplate.query(sql, new DeliveryDataRowMapper(), workPeriodId);
    }

    public deliveryDataService findDeliveryById(int deliveryId) {
        // SQLite uses deliveryDataID as the primary key column
        String sql = "SELECT * FROM deliveryData WHERE deliveryDataID = ?";
        return jdbcTemplate.queryForObject(sql, new DeliveryDataRowMapper(), deliveryId);
    }

    // --- Combined Overview Query ---

    public List<overviewService.OverviewDTO> findFullOverviewByUserId(int userId) {
        String sql = "SELECT d.deliveryDataID, d.fromLocation, d.toLocation, d.resturant, d.platform, " +
            "d.basePay, d.tips, d.miles, d.startTime, j.vehicle, d.jobsTableId " +
            "FROM deliveryData d " +
            "LEFT JOIN JobsTable j ON d.jobsTableId = j.jobsId " +
            "WHERE j.userId = ? " +
            "ORDER BY d.startTime DESC";
        return jdbcTemplate.query(sql, new OverviewDTORowMapper(), userId);
    }

    // --- Aggregate Queries ---

    public double calculateTotalEarnings(int userId) {
        String sql = "SELECT COALESCE(SUM(d.basePay + d.tips), 0) FROM deliveryData d " +
            "INNER JOIN JobsTable j ON d.jobsTableId = j.jobsId " +
            "WHERE j.userId = ?";
        Double result = jdbcTemplate.queryForObject(sql, Double.class, userId);
        return result != null ? result : 0.0;
    }

    public int countDeliveriesByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM deliveryData d " +
            "INNER JOIN JobsTable j ON d.jobsTableId = j.jobsId " +
            "WHERE j.userId = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    public int calculateTotalMiles(int userId) {
        String sql = "SELECT COALESCE(SUM(d.miles), 0) FROM deliveryData d " +
            "INNER JOIN JobsTable j ON d.jobsTableId = j.jobsId " +
            "WHERE j.userId = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    // --- Row Mappers ---

    private static class WorkPeriodRowMapper implements RowMapper<workPeriodService> {
        @Override
        public workPeriodService mapRow(ResultSet rs, int rowNum) throws SQLException {
            workPeriodService wp = new workPeriodService();
            wp.setVehicle(rs.getString("vehicle"));
            wp.setStartTime(rs.getLong("startTime"));
            wp.setEndTime(rs.getLong("endTime"));
            // Note: JobsTable doesn't have totalVehicleMiles column, set to 0
            wp.setTotalVehicleMiles(0);
            return wp;
        }
    }

    private static class DeliveryDataRowMapper implements RowMapper<deliveryDataService> {
        @Override
        public deliveryDataService mapRow(ResultSet rs, int rowNum) throws SQLException {
            deliveryDataService dd = new deliveryDataService();
            dd.setMilesDriven(rs.getInt("miles"));
            dd.setBasePay(rs.getFloat("basePay"));
            dd.setTips(rs.getFloat("tips"));
            dd.setPlatform(rs.getString("platform"));
            dd.setRestaurant(rs.getString("resturant"));
            dd.setFromAddress(rs.getString("fromLocation"));
            dd.setToAddress(rs.getString("toLocation"));
            // actual column name is startTime in SQLite dump
            dd.setDateTimeStart(rs.getLong("startTime"));
            dd.setTotalTimeSpent(rs.getInt("totalTimeSpent"));
            dd.setMinutesSpentWaitingAtResturant(rs.getInt("timeSpentWaiting"));
            dd.setExpenses(rs.getFloat("extraExpenses"));
            return dd;
        }
    }

    private static class OverviewDTORowMapper implements RowMapper<overviewService.OverviewDTO> {
        @Override
        public overviewService.OverviewDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new overviewService.OverviewDTO(
                    rs.getInt("deliveryDataID"),
                    rs.getString("fromLocation"),
                    rs.getString("toLocation"),
                    rs.getString("resturant"),
                    rs.getString("platform"),
                    rs.getFloat("basePay"),
                    rs.getFloat("tips"),
                    rs.getInt("miles"),
                    rs.getLong("startTime"),
                    rs.getString("vehicle"),
                    rs.getInt("jobsTableId")
            );
        }
    }
}
