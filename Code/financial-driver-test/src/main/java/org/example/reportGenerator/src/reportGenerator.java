package org.example.reportGenerator.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Report Generator service that interfaces with deliveryCalculator
 * to set date ranges, generate reports, and create financial plans.
 */
@Service
public class reportGenerator {

    private final deliveryCalculator calculator;
    private final reportDAO reportDAO;
    private final generalReports generalReports;

    // Current report date range settings
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int daysToAnalyze = 90; // Default analysis period

    // Financial plan settings
    private float targetMonthlyIncome;
    private float estimatedExpenses;
    private int targetWorkHoursPerDay = 6;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public reportGenerator(reportDAO reportDAO, generalReports generalReports) {
        this.reportDAO = reportDAO;
        this.generalReports = generalReports;
        this.calculator = new deliveryCalculator(reportDAO);
        
        // Initialize default date range to last 90 days
        this.endDate = LocalDateTime.now();
        this.startDate = endDate.minusDays(daysToAnalyze);
    }

    // ==================== Date Range Configuration ====================

    /**
     * Sets the date range for report generation.
     *
     * @param startDate Start of the analysis period
     * @param endDate   End of the analysis period
     * @throws IllegalArgumentException if dates are invalid
     */
    public void setDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Sets the date range using number of days back from today.
     *
     * @param daysBack Number of days to analyze from today
     * @throws IllegalArgumentException if daysBack is not positive
     */
    public void setDateRangeByDaysBack(int daysBack) {
        if (daysBack <= 0) {
            throw new IllegalArgumentException("Days back must be a positive number");
        }
        this.daysToAnalyze = daysBack;
        this.endDate = LocalDateTime.now();
        this.startDate = endDate.minusDays(daysBack);
    }

    /**
     * Gets the currently configured start date.
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * Gets the currently configured end date.
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    // ==================== Report Data Methods ====================

    /**
     * Gets delivery report data for the configured date range.
     *
     * @return DeliveryReportData containing summary statistics
     */
    public DeliveryReportData getDeliveryReportsData() {
        float totalEarnings = generalReports.getTotalEarningsFromDB(startDate, endDate);
        float avgEarnings = generalReports.getAverageEarningsFromDB(startDate, endDate);
        int deliveryCount = generalReports.getDeliveryCountFromDB(startDate, endDate);
        
        String optimalHours = calculator.findOptimalWorkHours(
                targetWorkHoursPerDay, 
                toEpochMilli(startDate), 
                toEpochMilli(endDate));

        return new DeliveryReportData(
                startDate, endDate, 
                totalEarnings, avgEarnings, deliveryCount, 
                optimalHours);
    }

    /**
     * Gets general financial report data for the configured date range.
     *
     * @return GeneralReportData containing overall financial summary
     */
    public GeneralReportData getGeneralReportsData() {
        float totalIncome = generalReports.getTotalEarningsFromDB(startDate, endDate);
        float currentMonthIncome = generalReports.getCurrentMonthDeliveryIncomeFromDB();
        float avgPerDelivery = generalReports.getAverageEarningsFromDB(startDate, endDate);
        int totalDeliveries = generalReports.getDeliveryCountFromDB(startDate, endDate);
        
        // Calculate projected monthly income based on current pace
        float dailyAverage = totalIncome / Math.max(1, daysToAnalyze);
        float projectedMonthlyIncome = dailyAverage * 30;

        return new GeneralReportData(
                totalIncome, currentMonthIncome, avgPerDelivery, 
                totalDeliveries, projectedMonthlyIncome);
    }

    /**
     * Queries expected profit for a specific day and hour range.
     *
     * @param dayTimestamp Timestamp to determine the day of week
     * @param startHour    Start hour (0-23)
     * @param endHour      End hour (0-23)
     * @return Expected profit based on historical data
     */
    public float querySingularReport(long dayTimestamp, int startHour, int endHour) {
        return calculator.calculateExpectedProfit(
                dayTimestamp,
                toEpochMilli(startDate),
                toEpochMilli(endDate),
                startHour, endHour);
    }

    // ==================== Financial Plan Methods ====================

    /**
     * Sets the target monthly income goal for financial planning.
     *
     * @param targetIncome Target monthly income in dollars
     */
    public void setTargetMonthlyIncome(float targetIncome) {
        if (targetIncome < 0) {
            throw new IllegalArgumentException("Target income must be non-negative");
        }
        this.targetMonthlyIncome = targetIncome;
    }

    /**
     * Sets the estimated monthly expenses for financial planning.
     *
     * @param expenses Estimated monthly expenses in dollars
     */
    public void setEstimatedExpenses(float expenses) {
        if (expenses < 0) {
            throw new IllegalArgumentException("Expenses must be non-negative");
        }
        this.estimatedExpenses = expenses;
    }

    /**
     * Sets the target work hours per day for optimization.
     *
     * @param hours Number of hours to work per day (1-24)
     */
    public void setTargetWorkHoursPerDay(int hours) {
        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException("Hours must be between 1 and 24");
        }
        this.targetWorkHoursPerDay = hours;
    }

    /**
     * Creates a financial plan based on historical data and user goals.
     *
     * @return FinancialPlan containing recommendations and projections
     */
    public FinancialPlan createFinancialPlan() {
        // Get historical data
        float totalHistoricalEarnings = generalReports.getTotalEarningsFromDB(startDate, endDate);
        int totalDeliveries = generalReports.getDeliveryCountFromDB(startDate, endDate);
        
        // Calculate metrics
        float dailyAverage = totalHistoricalEarnings / Math.max(1, daysToAnalyze);
        float projectedMonthlyIncome = dailyAverage * 30;
        float incomeGap = targetMonthlyIncome - projectedMonthlyIncome;
        
        // Calculate required additional daily earnings
        float additionalDailyRequired = incomeGap > 0 ? incomeGap / 30 : 0;
        
        // Get optimal work schedule
        String optimalSchedule = calculator.findOptimalWorkHours(
                targetWorkHoursPerDay,
                toEpochMilli(startDate),
                toEpochMilli(endDate));
        
        // Calculate net profit projection
        float projectedNetProfit = projectedMonthlyIncome - estimatedExpenses;
        
        // Build recommendations
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("=== Financial Plan Recommendations ===\n\n");
        
        if (incomeGap > 0) {
            recommendations.append(String.format("⚠ You need an additional $%.2f/month to meet your goal.\n", incomeGap));
            recommendations.append(String.format("   That's approximately $%.2f more per day.\n\n", additionalDailyRequired));
        } else {
            recommendations.append("✓ You're on track to meet or exceed your monthly income goal!\n\n");
        }
        
        recommendations.append("Optimal Work Schedule:\n");
        recommendations.append(optimalSchedule).append("\n");
        
        if (projectedNetProfit < 0) {
            recommendations.append(String.format("⚠ Warning: Projected expenses ($%.2f) exceed projected income ($%.2f).\n",
                    estimatedExpenses, projectedMonthlyIncome));
            recommendations.append("   Consider reducing expenses or increasing work hours.\n");
        } else {
            recommendations.append(String.format("✓ Projected monthly net profit: $%.2f\n", projectedNetProfit));
        }

        return new FinancialPlan(
                targetMonthlyIncome, estimatedExpenses,
                projectedMonthlyIncome, projectedNetProfit,
                incomeGap, additionalDailyRequired,
                optimalSchedule, recommendations.toString());
    }

    /**
     * Retrieves a previously created financial plan (current state).
     *
     * @return Current financial plan based on configured settings
     */
    public FinancialPlan retrieveFinancialPlan() {
        return createFinancialPlan();
    }

    // ==================== Export Methods ====================

    /**
     * Exports report data as a formatted string.
     *
     * @return Formatted report string
     */
    public String exportData() {
        StringBuilder export = new StringBuilder();
        export.append("=== Delivery Financial Report ===\n");
        export.append("Generated: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        export.append("Analysis Period: ").append(startDate.format(DATE_FORMATTER))
              .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n\n");

        DeliveryReportData deliveryData = getDeliveryReportsData();
        export.append("--- Delivery Summary ---\n");
        export.append(String.format("Total Deliveries: %d\n", deliveryData.deliveryCount()));
        export.append(String.format("Total Earnings: $%.2f\n", deliveryData.totalEarnings()));
        export.append(String.format("Average per Delivery: $%.2f\n", deliveryData.avgEarnings()));
        export.append("\n");

        GeneralReportData generalData = getGeneralReportsData();
        export.append("--- Financial Summary ---\n");
        export.append(String.format("Current Month Income: $%.2f\n", generalData.currentMonthIncome()));
        export.append(String.format("Projected Monthly Income: $%.2f\n", generalData.projectedMonthlyIncome()));
        export.append("\n");

        export.append("--- Optimal Schedule ---\n");
        export.append(deliveryData.optimalHours());

        return export.toString();
    }

    // ==================== Helper Methods ====================

    private long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // ==================== Data Transfer Objects ====================

    /**
     * Container for delivery report data.
     */
    public record DeliveryReportData(
            LocalDateTime startDate,
            LocalDateTime endDate,
            float totalEarnings,
            float avgEarnings,
            int deliveryCount,
            String optimalHours
    ) {}

    /**
     * Container for general financial report data.
     */
    public record GeneralReportData(
            float totalIncome,
            float currentMonthIncome,
            float avgPerDelivery,
            int totalDeliveries,
            float projectedMonthlyIncome
    ) {}

    /**
     * Container for financial plan data.
     */
    public record FinancialPlan(
            float targetMonthlyIncome,
            float estimatedExpenses,
            float projectedMonthlyIncome,
            float projectedNetProfit,
            float incomeGap,
            float additionalDailyRequired,
            String optimalSchedule,
            String recommendations
    ) {}
}

