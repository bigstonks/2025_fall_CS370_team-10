package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class deliveryJobOveviewDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Represents a summary of a shift to be saved/loaded.
     * This inner class serves as a DTO for the DAO.
     */
    public static class ShiftSummary {
        private long id;
        private long startTime;
        private long endTime;
        private String vehicle;
        private double totalEarnings;
        // Getters/Setters omitted for brevity but implied
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public String getVehicle() { return vehicle; }
        public void setVehicle(String vehicle) { this.vehicle = vehicle; }
        public double getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
    }

    private static final RowMapper<ShiftSummary> shiftRowMapper = new RowMapper<ShiftSummary>() {
        @Override
        public ShiftSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShiftSummary summary = new ShiftSummary();
            summary.setId(rs.getInt("jobsId"));
            summary.setStartTime(rs.getLong("startTime"));
            summary.setEndTime(rs.getLong("endTime"));
            summary.setVehicle(rs.getString("vehicle"));
            summary.setTotalEarnings(rs.getDouble("totalEarnings"));
            return summary;
        }
    };

    public void saveShift(ShiftSummary shift, int userId) {
        String sql = "INSERT INTO JobsTable (userId, startTime, endTime, vehicle, totalEarnings) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, shift.getStartTime(), shift.getEndTime(), shift.getVehicle(), shift.getTotalEarnings());
    }

    public List<ShiftSummary> getAllShifts(int userId) {
        String sql = "SELECT * FROM JobsTable WHERE userId = ? ORDER BY startTime DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, shiftRowMapper);
    }
}