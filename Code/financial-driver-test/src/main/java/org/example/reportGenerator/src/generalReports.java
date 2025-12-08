package org.example.reportGenerator.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class generalReports {

    @Autowired
    private reportDAO reportDAO;

    // ==================== Array-based calculations ====================

    /**
     * Calculates total income from an array of income values.
     *
     * @param income Array of income values
     * @return Sum of all income values, or 0 if array is null
     */
    public float totalIncome(float[] income) {
        if (income == null) return 0;
        float sum = 0;
        for (float v : income) {
            sum += v;
        }
        return sum;
    }

    /**
     * Calculates average income from an array of income values.
     *
     * @param income Array of income values
     * @return Average income, or 0 if array is null or empty
     */
    public float averageIncome(float[] income) {
        if (income == null || income.length == 0) return 0;
        return totalIncome(income) / income.length;
    }

    // ==================== Database-based calculations ====================

    /**
     * Gets total earnings (basePay + tips) for all deliveries within a date range.
     *
     * @param startTime Start of the date range (inclusive)
     * @param endTime   End of the date range (inclusive)
     * @return Total earnings from all deliveries in the range
     */
    public float getTotalEarningsFromDB(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayByDateRange(startTime, endTime);
        
        float total = 0;
        for (Map<String, Object> delivery : deliveries) {
            Number basePay = (Number) delivery.get("basePay");
            Number tips = (Number) delivery.get("tips");
            
            if (basePay != null) {
                total += basePay.floatValue();
            }
            if (tips != null) {
                total += tips.floatValue();
            }
        }
        return total;
    }

    /**
     * Gets average earnings per delivery within a date range.
     *
     * @param startTime Start of the date range (inclusive)
     * @param endTime   End of the date range (inclusive)
     * @return Average earnings per delivery, or 0 if no deliveries found
     */
    public float getAverageEarningsFromDB(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayByDateRange(startTime, endTime);
        
        if (deliveries == null || deliveries.isEmpty()) {
            return 0;
        }

        float total = 0;
        for (Map<String, Object> delivery : deliveries) {
            Number basePay = (Number) delivery.get("basePay");
            Number tips = (Number) delivery.get("tips");
            
            if (basePay != null) {
                total += basePay.floatValue();
            }
            if (tips != null) {
                total += tips.floatValue();
            }
        }
        return total / deliveries.size();
    }

    /**
     * Gets total delivery income for all time.
     *
     * @return Total earnings from all deliveries
     */
    public float getTotalDeliveryIncomeFromDB() {
        // Use a very old start date to capture all historical data
        LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.now();
        
        return getTotalEarningsFromDB(startTime, endTime);
    }

    /**
     * Gets total delivery income for the current month.
     *
     * @return Total earnings from deliveries in the current month
     */
    public float getCurrentMonthDeliveryIncomeFromDB() {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startTime = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        return getTotalEarningsFromDB(startTime, endTime);
    }

    /**
     * Gets the count of deliveries within a date range.
     *
     * @param startTime Start of the date range (inclusive)
     * @param endTime   End of the date range (inclusive)
     * @return Number of deliveries in the range
     */
    public int getDeliveryCountFromDB(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayByDateRange(startTime, endTime);
        return deliveries != null ? deliveries.size() : 0;
    }

    /**
     * Gets a summary of earnings by platform within a date range.
     * Useful for the "Earnings by platform" chart in the UI.
     *
     * @param startTime Start of the date range (inclusive)
     * @param endTime   End of the date range (inclusive)
     * @return Map of platform names to total earnings
     */
    public Map<String, Float> getEarningsByPlatformFromDB(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }

        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayWithTimestampByDateRange(startTime, endTime);
        
        Map<String, Float> platformEarnings = new java.util.HashMap<>();
        
        for (Map<String, Object> delivery : deliveries) {
            String platform = (String) delivery.get("platform");
            if (platform == null || platform.isEmpty()) {
                platform = "Unknown";
            }
            
            float earnings = 0;
            Number basePay = (Number) delivery.get("basePay");
            Number tips = (Number) delivery.get("tips");
            
            if (basePay != null) {
                earnings += basePay.floatValue();
            }
            if (tips != null) {
                earnings += tips.floatValue();
            }
            
            platformEarnings.merge(platform, earnings, Float::sum);
        }
        
        return platformEarnings;
    }
}