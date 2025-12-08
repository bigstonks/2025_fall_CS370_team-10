package org.example.driverFinancialServiceDispatcher;

import org.example.reportGenerator.src.deliveryCalculator;
import org.example.reportGenerator.src.reportDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@SpringBootApplication
@ComponentScan(basePackages = {"org.example"})
public class reportTester implements CommandLineRunner {

    @Autowired
    private reportDAO reportDAO;

    private deliveryCalculator calculator;

    /**
     * Main method to run the reportTester as a standalone application.
     * Bootstraps the Spring application context and runs the tests.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(reportTester.class, args);
        // The run() method will be called automatically by Spring Boot
        // After completion, close the context
        context.close();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           DELIVERY REPORT TESTER - STARTING               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Validate that reportDAO was injected
        if (reportDAO == null) {
            System.err.println("ERROR: reportDAO was not injected. Check Spring configuration.");
            return;
        }

        // Initialize calculator with injected DAO
        calculator = new deliveryCalculator(reportDAO);

        // Run all tests
        testDataAvailability();
        testFindOptimalWorkHours();
        testCalculateExpectedProfit();

        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           ALL TESTS COMPLETED                             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    /**
     * Tests that data is available in the database before running other tests.
     * Fetches actual job start times from the database.
     */
    private void testDataAvailability() {
        printTestHeader("DATA AVAILABILITY CHECK");

        try {
            // Get the job start time range from the database
            Map<String, Object> timeRange = reportDAO.getJobStartTimeRange();
            Long minStartTime = (Long) timeRange.get("minStartTime");
            Long maxStartTime = (Long) timeRange.get("maxStartTime");

            if (minStartTime == null || maxStartTime == null) {
                System.out.println("⚠ WARNING: No job data found in the JobsTable.");
                System.out.println("  Using default date range (last 90 days).");

                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusDays(90);
                checkDeliveryData(startDate, endDate);
            } else {
                LocalDateTime startDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(minStartTime), ZoneId.systemDefault());
                LocalDateTime endDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(maxStartTime), ZoneId.systemDefault());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                System.out.println("✓ Found jobs in database.");
                System.out.println("  Job time range: " + startDate.format(formatter) + " to " + endDate.format(formatter));

                // Also fetch all job start times
                List<Long> jobStartTimes = reportDAO.getJobStartTimes();
                System.out.println("  Total jobs found: " + jobStartTimes.size());

                checkDeliveryData(startDate, endDate);
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR checking data availability: " + e.getMessage());
            e.printStackTrace();
        }

        printTestFooter();
    }

    /**
     * Helper method to check delivery data within a date range.
     */
    private void checkDeliveryData(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayWithTimestampByDateRange(startDate, endDate);

            if (deliveries == null || deliveries.isEmpty()) {
                System.out.println("⚠ WARNING: No delivery data found in the specified date range.");
                System.out.println("  Other tests may not produce meaningful results.");
                System.out.println("  Consider adding test data to the deliveryData table.");
            } else {
                System.out.println("✓ Found " + deliveries.size() + " delivery records.");

                // Show sample data
                if (!deliveries.isEmpty()) {
                    System.out.println("\n  Sample record:");
                    Map<String, Object> sample = deliveries.get(0);
                    System.out.println("    - basePay: " + sample.get("basePay"));
                    System.out.println("    - tips: " + sample.get("tips"));
                    System.out.println("    - time: " + sample.get("time"));
                }
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR checking delivery data: " + e.getMessage());
        }
    }

    /**
     * Tests the findOptimalWorkHours method with various shift lengths.
     * Uses actual job start times from the database.
     */
    private void testFindOptimalWorkHours() {
        printTestHeader("FIND OPTIMAL WORK HOURS");

        long historicalStartTime;
        long historicalEndTime;

        try {
            // Get the job start time range from the database
            Map<String, Object> timeRange = reportDAO.getJobStartTimeRange();
            Long minStartTime = (Long) timeRange.get("minStartTime");
            Long maxStartTime = (Long) timeRange.get("maxStartTime");

            if (minStartTime == null || maxStartTime == null) {
                System.out.println("⚠ WARNING: No job data found in JobsTable. Using default 90-day range.");
                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusDays(90);
                historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                historicalStartTime = minStartTime;
                historicalEndTime = maxStartTime;

                LocalDateTime startDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(minStartTime), ZoneId.systemDefault());
                LocalDateTime endDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(maxStartTime), ZoneId.systemDefault());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                System.out.println("Analysis Period (from database): " + startDate.format(formatter) + " to " + endDate.format(formatter));

                // Show job start times from the database
                List<Long> jobStartTimes = reportDAO.getJobStartTimes();
                System.out.println("Number of jobs found: " + jobStartTimes.size());
                if (!jobStartTimes.isEmpty() && jobStartTimes.size() <= 10) {
                    System.out.println("Job start times:");
                    for (Long startTime : jobStartTimes) {
                        LocalDateTime jobStart = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
                        System.out.println("  - " + jobStart.format(formatter));
                    }
                } else if (!jobStartTimes.isEmpty()) {
                    System.out.println("(Showing first 5 job start times)");
                    for (int i = 0; i < Math.min(5, jobStartTimes.size()); i++) {
                        LocalDateTime jobStart = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(jobStartTimes.get(i)), ZoneId.systemDefault());
                        System.out.println("  - " + jobStart.format(formatter));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR fetching job times: " + e.getMessage());
            System.out.println("Falling back to default 90-day range.");
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(90);
            historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        System.out.println();

        int[] shiftLengths = {4, 6, 8};

        for (int hours : shiftLengths) {
            System.out.println("┌─────────────────────────────────────────────┐");
            System.out.println("│ Testing " + hours + "-hour shift optimization           │");
            System.out.println("└─────────────────────────────────────────────┘");

            try {
                String result = calculator.findOptimalWorkHours(hours, historicalStartTime, historicalEndTime);
                System.out.println(result);
                System.out.println("✓ " + hours + "-hour shift test PASSED");
            } catch (IllegalStateException e) {
                System.err.println("✗ Configuration Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("✗ Invalid Argument: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("✗ Unexpected Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }

        // Test edge cases
        System.out.println("Testing edge cases...");
        
        // Test with invalid hours (should throw exception)
        try {
            calculator.findOptimalWorkHours(0, historicalStartTime, historicalEndTime);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for hours=0");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected hours=0");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        try {
            calculator.findOptimalWorkHours(25, historicalStartTime, historicalEndTime);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for hours=25");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected hours=25");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        printTestFooter();
    }

    /**
     * Tests the calculateExpectedProfit method.
     * Uses actual job start times from the database.
     */
    private void testCalculateExpectedProfit() {
        printTestHeader("CALCULATE EXPECTED PROFIT");

        long historicalStartTime;
        long historicalEndTime;
        long currentTimestamp = System.currentTimeMillis();

        try {
            // Get the job start time range from the database
            Map<String, Object> timeRange = reportDAO.getJobStartTimeRange();
            Long minStartTime = (Long) timeRange.get("minStartTime");
            Long maxStartTime = (Long) timeRange.get("maxStartTime");

            if (minStartTime == null || maxStartTime == null) {
                System.out.println("⚠ WARNING: No job data found in JobsTable. Using default 90-day range.");
                LocalDateTime endDate = LocalDateTime.now();
                LocalDateTime startDate = endDate.minusDays(90);
                historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                historicalStartTime = minStartTime;
                historicalEndTime = maxStartTime;

                LocalDateTime startDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(minStartTime), ZoneId.systemDefault());
                LocalDateTime endDate = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(maxStartTime), ZoneId.systemDefault());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                System.out.println("Using job data from database: " + startDate.format(formatter) + " to " + endDate.format(formatter));
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR fetching job times: " + e.getMessage());
            System.out.println("Falling back to default 90-day range.");
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(90);
            historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        System.out.println("Testing expected profit for " + currentDay + " (based on current timestamp)");
        System.out.println();

        // Test various hour ranges
        int[][] hourRanges = {
            {9, 12},   // Morning shift
            {12, 17},  // Afternoon shift
            {17, 21},  // Evening shift (typically busy)
            {21, 2},   // Late night shift (overnight range)
            {0, 23}    // Full day
        };

        String[] rangeNames = {
            "Morning (9 AM - 12 PM)",
            "Afternoon (12 PM - 5 PM)",
            "Evening (5 PM - 9 PM)",
            "Late Night (9 PM - 2 AM)",
            "Full Day (12 AM - 11 PM)"
        };

        for (int i = 0; i < hourRanges.length; i++) {
            int startHour = hourRanges[i][0];
            int endHour = hourRanges[i][1];

            try {
                float profit = calculator.calculateExpectedProfit(
                    currentTimestamp, historicalStartTime, historicalEndTime, startHour, endHour);
                
                System.out.printf("  %-30s: $%.2f%n", rangeNames[i], profit);
            } catch (Exception e) {
                System.err.printf("  %-30s: ERROR - %s%n", rangeNames[i], e.getMessage());
            }
        }

        System.out.println();

        // Test edge cases
        System.out.println("Testing edge cases...");

        // Invalid hour values
        try {
            calculator.calculateExpectedProfit(currentTimestamp, historicalStartTime, historicalEndTime, -1, 12);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for startHour=-1");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected startHour=-1");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        try {
            calculator.calculateExpectedProfit(currentTimestamp, historicalStartTime, historicalEndTime, 9, 24);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for endHour=24");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected endHour=24");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        // Invalid time range (start after end)
        try {
            calculator.calculateExpectedProfit(currentTimestamp, historicalEndTime, historicalStartTime, 9, 17);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for inverted time range");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected inverted time range");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        // Negative timestamp
        try {
            calculator.calculateExpectedProfit(-1, historicalStartTime, historicalEndTime, 9, 17);
            System.err.println("✗ Edge case FAILED: Should have thrown exception for negative timestamp");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Edge case PASSED: Correctly rejected negative timestamp");
        } catch (Exception e) {
            System.err.println("✗ Edge case FAILED with unexpected exception: " + e.getMessage());
        }

        printTestFooter();
    }

    private void printTestHeader(String testName) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  TEST: " + testName);
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println();
    }

    private void printTestFooter() {
        System.out.println();
        System.out.println("───────────────────────────────────────────────────────────");
    }
}
