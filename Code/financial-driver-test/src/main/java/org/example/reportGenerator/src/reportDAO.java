package src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class reportDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Calculates total earnings from all jobs.
     */
    public float getTotalEarnings(int userId) {
        String sql = "SELECT SUM(totalEarnings) FROM JobsTable WHERE userId = ";
        Float total = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Calculates average earnings per job.
     */
    public float getAverageEarnings(int userId) {
        String sql = "SELECT AVG(totalEarnings) FROM JobsTable WHERE userId = ?";
        Float avg = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Float.class);
        return (avg != null) ? avg : 0.0f;
    }

    /**
     * Gets total income specifically from deliveries (Base Pay + Tips).
     */
    public float getTotalDeliveryIncome() {
        String sql = "SELECT SUM(basePay + tips) FROM deliveryData";
        Float total = jdbcTemplate.queryForObject(sql, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Gets delivery income for the current month.
     */
    public float getCurrentMonthDeliveryIncome() {
        // Using MySQL specific date logic (start of current month)
        String sql = "SELECT SUM(basePay + tips) FROM deliveryData WHERE time >= " +
                "UNIX_TIMESTAMP(DATE_FORMAT(NOW() ,'%Y-%m-01')) * 1000";
        Float total = jdbcTemplate.queryForObject(sql, Float.class);
        return (total != null) ? total : 0.0f;
    }

    /**
     * Retrieves a raw list of all income values (for array processing if needed).
     */
    public List<Float> getAllIncomeValues(int userId) {
        String sql = "SELECT totalEarnings FROM JobsTable WHERE userId = ?";
        return jdbcTemplate.queryForList(sql, new Object[]{userId}, Float.class);
    }
}