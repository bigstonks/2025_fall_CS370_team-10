package org.example.reportGenerator.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/reports")
public class reportController {

    @Autowired
    private reportDAO reportDAO;

    private final deliveryCalculator calculator;

    @Autowired
    public reportController(reportDAO reportDAO) {
        this.reportDAO = reportDAO;
        this.calculator = new deliveryCalculator(reportDAO);
    }

    /**
     * GET /api/reports/optimal-hours
     * Finds the optimal work hours for each day of the week.
     *
     * @param hours             Number of consecutive hours to work (default: 6)
     * @param daysBack          Number of days of historical data to analyze (default: 90)
     * @return Formatted string with optimal work hours for each day
     */
    @GetMapping("/optimal-hours")
    public ResponseEntity<String> getOptimalWorkHours(
            @RequestParam(defaultValue = "6") int hours,
            @RequestParam(defaultValue = "90") int daysBack) {

        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(daysBack);

            long historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            String result = calculator.findOptimalWorkHours(hours, historicalStartTime, historicalEndTime);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid parameters: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error calculating optimal hours: " + e.getMessage());
        }
    }

    /**
     * GET /api/reports/expected-profit
     * Calculates expected profit for a specific day of week and hour range.
     *
     * @param dayTimestamp      Timestamp to determine the target day of week (epoch millis)
     * @param startHour         Start hour of the work period (0-23)
     * @param endHour           End hour of the work period (0-23)
     * @param daysBack          Number of days of historical data to analyze (default: 90)
     * @return Expected profit value
     */
    @GetMapping("/expected-profit")
    public ResponseEntity<?> getExpectedProfit(
            @RequestParam long dayTimestamp,
            @RequestParam int startHour,
            @RequestParam int endHour,
            @RequestParam(defaultValue = "90") int daysBack) {

        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(daysBack);

            long historicalStartTime = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long historicalEndTime = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            float profit = calculator.calculateExpectedProfit(
                    dayTimestamp, historicalStartTime, historicalEndTime, startHour, endHour);

            return ResponseEntity.ok(new ProfitResponse(profit));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid parameters: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error calculating expected profit: " + e.getMessage());
        }
    }

    /**
     * Simple response wrapper for profit data.
     */
    private static class ProfitResponse {
        private final float expectedProfit;

        public ProfitResponse(float expectedProfit) {
            this.expectedProfit = expectedProfit;
        }

        public float getExpectedProfit() {
            return expectedProfit;
        }
    }
}
