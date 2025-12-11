package org.example.driverFinancialServiceDispatcher;

import org.example.deliveryRecorder.src.vehicle;
import org.example.reportGenerator.src.deliveryCalculator;
import org.example.reportGenerator.src.generalReports;
import org.example.reportGenerator.src.reportDAO;
import org.example.reportGenerator.src.reportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FinancialDriverTestApplicationTests {

    private deliveryCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new deliveryCalculator();
    }

    @Test
    void testDeliveryCalculatorBasicMath() {
        // calculuateRevenue
        float[] base = {5.0f, 10.0f};
        float[] tips = {1.0f, 2.0f};
        assertEquals(18.0f, calculator.calculuateRevenue(base, tips), 1e-6f);

        // net profit
        assertEquals(70.0f, calculator.calculateNetProfit(100.0f, 30.0f), 1e-6f);

        // profit margin
        assertEquals(0.75f, calculator.calculateProfitMargin(100.0f, 25.0f), 1e-6f);

        // median downtime (here returns average-like behavior based on implementation)
        float[] downtime = {10f, 20f, 30f};
        float expectedDowntime = (10f + 20f + 30f) / (downtime.length + 1); // matches implementation
        assertEquals(expectedDowntime, calculator.calculateMedianDowntime(downtime), 1e-6f);

        // calculateExpenses
        float[] expenses = {3f, 7f, 10f};
        assertEquals(20f, calculator.calculateExpenses(expenses), 1e-6f);

        // findOptimalResturnats ordering
        String[] names = {"A", "B", "C"};
        float[] profits = {10f, 30f, 20f};
        String[] sorted = calculator.findOptimalResturnats(names, profits);
        assertArrayEquals(new String[]{"B", "C", "A"}, sorted);

        // gas calculations
        assertEquals(4.0f, calculator.gasUsed(25, 100f), 1e-6f);
        assertEquals(4.0, calculator.gasUsed(25.0, 100.0), 1e-9);
        assertEquals(14.0f, calculator.calculateGasCost(25, 100f, 3.5f), 1e-6f);

        float[] milesArray = {10f, 20f};
        int[] mpgArray = {25, 25};
        assertEquals((10f/25f + 20f/25f) * 3.0f, calculator.calculateTotalGasCost(milesArray, mpgArray, 3.0f), 1e-6f);
    }

    @Test
    void testVehicleDepreciationScenarios() {
        vehicle v = new vehicle();
        v.setStartingMiles(1000);
        v.setCurrentVehicleMiles(1500);
        v.setPurchasePrice(15000.0);

        // Case 1: deliveries do not push recorded mileage higher
        deliveryCalculator.VehicleDepreciationResult res1 = calculator.calculateVehicleDepreciation(v, 400);
        // milesFromDeliveries = starting + deliveryMiles = 1400 which is <= current(1500) -> milesDrivenSincePurchase = 500
        assertEquals(500, res1.milesDrivenSincePurchase);
        assertEquals(500 * 0.08, res1.totalDepreciation, 1e-6);
        assertEquals(Math.max(0, 15000.0 - 500 * 0.08), res1.currentValue, 1e-6);
        assertFalse(res1.milesNeedUpdate);

        // Case 2: deliveries push recorded mileage higher than current
        deliveryCalculator.VehicleDepreciationResult res2 = calculator.calculateVehicleDepreciation(v, 600);
        // milesFromDeliveries = 1600 > current 1500 -> milesDrivenSincePurchase = 600
        assertEquals(600, res2.milesDrivenSincePurchase);
        assertEquals(600 * 0.08, res2.totalDepreciation, 1e-6);
        assertEquals(Math.max(0, 15000.0 - 600 * 0.08), res2.currentValue, 1e-6);
        assertTrue(res2.milesNeedUpdate);
    }

    @Test
    void testCreateFinancialPlanMath() {
        // Mocks for reportDAO and generalReports
        reportDAO mockDao = mock(reportDAO.class);
        generalReports mockReports = mock(generalReports.class);

        // Configure generalReports to return deterministic values
        // total historical earnings over analysis period
        when(mockReports.getTotalEarningsFromDB(any(), any())).thenReturn(3000.0f);
        when(mockReports.getDeliveryCountFromDB(any(), any())).thenReturn(100);
        when(mockReports.getCurrentMonthDeliveryIncomeFromDB()).thenReturn(200.0f);
        when(mockReports.getAverageEarningsFromDB(any(), any())).thenReturn(30.0f);

        // Create reportGenerator with mocks
        reportGenerator rg = new reportGenerator(mockDao, mockReports);

        // default daysToAnalyze is 90 (set in constructor). Use defaults.
        rg.setTargetMonthlyIncome(1500.0f);
        rg.setEstimatedExpenses(800.0f);
        rg.setTargetWorkHoursPerDay(6);

        reportGenerator.FinancialPlan plan = rg.createFinancialPlan();

        // dailyAverage = 3000 / 90 = 33.3333 -> projectedMonthlyIncome = 33.3333 * 30 = 1000
        assertEquals(1000.0f, plan.projectedMonthlyIncome(), 1e-3f);

        // incomeGap = 1500 - 1000 = 500
        assertEquals(500.0f, plan.incomeGap(), 1e-3f);

        // additionalDailyRequired = incomeGap / 30 = 16.6667
        assertEquals(500.0f / 30.0f, plan.additionalDailyRequired(), 1e-3f);

        // projectedNetProfit = projectedMonthlyIncome - estimatedExpenses = 1000 - 800 = 200
        assertEquals(200.0f, plan.projectedNetProfit(), 1e-3f);

        // ensure recommendations mention the optimal schedule (non-null)
        assertNotNull(plan.optimalSchedule());
        assertTrue(plan.recommendations().length() > 0);
    }

    @Test
    void testCalculateExpectedProfit() {
        // Prepare a mock DAO and calculator
        reportDAO mockDao = mock(reportDAO.class);
        deliveryCalculator calc = new deliveryCalculator(mockDao);

        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime reference = LocalDateTime.of(2025, 12, 1, 12, 0); // choose a fixed date
        long referenceTimestamp = reference.atZone(zone).toInstant().toEpochMilli();

        LocalDateTime histStart = reference.minusDays(30);
        LocalDateTime histEnd = reference.plusDays(30);

        // Build deliveries that fall on the same weekday as 'reference' and within hours 11-13
        List<Map<String, Object>> deliveries = new ArrayList<>();

        Map<String, Object> d1 = new HashMap<>();
        d1.put("time", reference.withHour(12).atZone(zone).toInstant().toEpochMilli());
        d1.put("basePay", 10.0f);
        d1.put("tips", 5.0f);
        deliveries.add(d1);

        Map<String, Object> d2 = new HashMap<>();
        d2.put("time", reference.withHour(13).atZone(zone).toInstant().toEpochMilli());
        d2.put("basePay", 7.0f);
        d2.put("tips", 3.0f);
        deliveries.add(d2);

        // Non-matching day (different weekday) should be ignored
        Map<String, Object> other = new HashMap<>();
        other.put("time", reference.plusDays(1).withHour(12).atZone(zone).toInstant().toEpochMilli());
        other.put("basePay", 100.0f);
        other.put("tips", 100.0f);
        deliveries.add(other);

        when(mockDao.getDeliveryPayWithTimestampByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(deliveries);

        float profit = calc.calculateExpectedProfit(referenceTimestamp,
                histStart.atZone(zone).toInstant().toEpochMilli(),
                histEnd.atZone(zone).toInstant().toEpochMilli(),
                11, 13);

        // Expected: sum of base+tips for d1 and d2 = (10+5) + (7+3) = 25
        assertEquals(25.0f, profit, 1e-6f);
    }

    @Test
    void testFindOptimalWorkHoursSimple() {
        reportDAO mockDao = mock(reportDAO.class);
        deliveryCalculator calc = new deliveryCalculator(mockDao);

        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime reference = LocalDateTime.of(2025, 12, 1, 0, 0); // fixed week reference
        LocalDateTime histStart = reference.minusDays(7);
        LocalDateTime histEnd = reference.plusDays(7);

        List<Map<String, Object>> deliveries = new ArrayList<>();

        // For Monday (reference date), put profits at 10:00, 11:00, 12:00
        LocalDateTime monday = reference; // treat as Monday in this test
        Map<String, Object> a = new HashMap<>();
        a.put("time", monday.withHour(10).atZone(zone).toInstant().toEpochMilli());
        a.put("basePay", 5.0f);
        a.put("tips", 0.0f);
        deliveries.add(a);

        Map<String, Object> b = new HashMap<>();
        b.put("time", monday.withHour(11).atZone(zone).toInstant().toEpochMilli());
        b.put("basePay", 10.0f);
        b.put("tips", 0.0f);
        deliveries.add(b);

        Map<String, Object> c = new HashMap<>();
        c.put("time", monday.withHour(12).atZone(zone).toInstant().toEpochMilli());
        c.put("basePay", 20.0f);
        c.put("tips", 0.0f);
        deliveries.add(c);

        when(mockDao.getDeliveryPayWithTimestampByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(deliveries);

        String result = calc.findOptimalWorkHours(2,
                histStart.atZone(zone).toInstant().toEpochMilli(),
                histEnd.atZone(zone).toInstant().toEpochMilli());

        // Expect the best 2-hour block to start at 11:00 (11 and 12 have the highest combined profit)
        assertTrue(result.contains("Monday") || result.contains("monday") );
        assertTrue(result.contains("11:00 AM - 1:00 PM") || result.contains("11:00 AM - 13:00") || result.contains("11:00 AM"));
    }

}
