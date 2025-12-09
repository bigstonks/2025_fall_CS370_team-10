package org.example.reportGenerator.src;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.Instant;

@Repository
public class reportDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Retrieves basePay and tips from deliveryData where the timestamp
     * falls between the specified start and end times.
     *
     * @param startTime The start of the date range (inclusive)
     * @param endTime   The end of the date range (inclusive)
     * @return List of maps containing basePay and tips for each delivery in the range
     */
    public List<Map<String, Object>> getDeliveryPayByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        // Convert LocalDateTime to epoch milliseconds (matching the BIGINT storage format)
        long startEpoch = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String sql = "SELECT basePay, tips FROM deliveryData WHERE time BETWEEN ? AND ?";

        return jdbcTemplate.queryForList(sql, startEpoch, endEpoch);
    }

    /**
     * Retrieves basePay and tips from deliveryData between two dates,
     * also calculating the total for each delivery.
     *
     * @param startTime The start of the date range (inclusive)
     * @param endTime   The end of the date range (inclusive)
     * @return List of maps containing basePay, tips, and totalPay for each delivery
     */
    public List<Map<String, Object>> getDeliveryPayWithTotalByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        long startEpoch = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String sql = "SELECT basePay, tips, (basePay + tips) AS totalPay " +
                "FROM deliveryData WHERE time BETWEEN ? AND ?";

        return jdbcTemplate.queryForList(sql, startEpoch, endEpoch);
    }

    /**
     * Retrieves datestamps from a specified table and column.
     * Supports both BIGINT (epoch millis) and DATE column types.
     *
     * @param tableName  The name of the table to query
     * @param dateColumn The column containing datestamp values
     * @return List of LocalDate values from the specified column
     * @throws IllegalArgumentException if the column doesn't contain date/timestamp data
     */
    public List<LocalDate> getDatestampsFromTable(String tableName, String dateColumn) {
        // Validate that the column exists and contains date-compatible data
        if (!isDateColumn(tableName, dateColumn)) {
            throw new IllegalArgumentException(
                    "Column '" + dateColumn + "' in table '" + tableName + "' does not contain datestamp data");
        }

        String columnType = getColumnType(tableName, dateColumn);
        String sql;

        if ("BIGINT".equalsIgnoreCase(columnType)) {
            // Convert epoch milliseconds to date
            sql = "SELECT DISTINCT DATE(FROM_UNIXTIME(" + dateColumn + " / 1000)) AS dateValue " +
                    "FROM " + tableName + " WHERE " + dateColumn + " IS NOT NULL ORDER BY dateValue";
        } else {
            // Already a DATE type
            sql = "SELECT DISTINCT " + dateColumn + " AS dateValue " +
                    "FROM " + tableName + " WHERE " + dateColumn + " IS NOT NULL ORDER BY dateValue";
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            java.sql.Date sqlDate = rs.getDate("dateValue");
            return sqlDate != null ? sqlDate.toLocalDate() : null;
        });
    }

    /**
     * Checks if the specified column contains date/timestamp data.
     */
    private boolean isDateColumn(String tableName, String columnName) {
        String columnType = getColumnType(tableName, columnName);
        if (columnType == null) return false;

        return columnType.equalsIgnoreCase("DATE") ||
                columnType.equalsIgnoreCase("DATETIME") ||
                columnType.equalsIgnoreCase("TIMESTAMP") ||
                columnType.equalsIgnoreCase("BIGINT"); // Epoch timestamps stored as BIGINT
    }

    /**
     * Gets the SQL type of a column.
     */
    private String getColumnType(String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    return rs.getString("TYPE_NAME");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve column metadata", e);
        }
        return null;
    }

    /**
     * Retrieves basePay, tips, and timestamp from deliveryData where the timestamp
     * falls between the specified start and end times.
     *
     * @param startTime The start of the date range (inclusive), must not be null
     * @param endTime   The end of the date range (inclusive), must not be null
     * @return List of maps containing basePay, tips, and time for each delivery in the range
     * @throws IllegalArgumentException if startTime or endTime is null, or if startTime is after endTime
     */
    public List<Map<String, Object>> getDeliveryPayWithTimestampByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime must not be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must not be after endTime");
        }
        
        // Consider using a fixed timezone like ZoneOffset.UTC for consistency
        long startEpoch = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String sql = "SELECT basePay, tips, time FROM deliveryData WHERE time BETWEEN ? AND ?";

        return jdbcTemplate.queryForList(sql, startEpoch, endEpoch);
    }

    /**
     * Retrieves all jobs from JobsTable with their start and end times.
     *
     * @return List of maps containing jobsId, userId, startTime, endTime, vehicle, and totalEarnings
     */
    public List<Map<String, Object>> getAllJobs() {
        String sql = "SELECT jobsId, userId, startTime, endTime, vehicle, totalEarnings FROM JobsTable";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Retrieves jobs from JobsTable within a specified date range based on startTime.
     *
     * @param startTime The start of the date range (inclusive)
     * @param endTime   The end of the date range (inclusive)
     * @return List of maps containing job data
     */
    public List<Map<String, Object>> getJobsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime must not be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must not be after endTime");
        }

        long startEpoch = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String sql = "SELECT jobsId, userId, startTime, endTime, vehicle, totalEarnings FROM JobsTable WHERE startTime BETWEEN ? AND ?";
        return jdbcTemplate.queryForList(sql, startEpoch, endEpoch);
    }

    /**
     * Retrieves the start times of all jobs from JobsTable.
     *
     * @return List of start times as Long values (epoch milliseconds)
     */
    public List<Long> getJobStartTimes() {
        String sql = "SELECT startTime FROM JobsTable WHERE startTime IS NOT NULL ORDER BY startTime";
        return jdbcTemplate.queryForList(sql, Long.class);
    }

    /**
     * Retrieves the earliest and latest job start times from the JobsTable.
     *
     * @return Map containing "minStartTime" and "maxStartTime" as Long values (epoch milliseconds)
     */
    public Map<String, Object> getJobStartTimeRange() {
        String sql = "SELECT MIN(startTime) as minStartTime, MAX(startTime) as maxStartTime FROM JobsTable WHERE startTime IS NOT NULL";
        return jdbcTemplate.queryForMap(sql);
    }


}