package org.example.reportGenerator.src;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class deliveryCalculator {

    private reportDAO reportDAO;

    public deliveryCalculator(reportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public deliveryCalculator() {
        // Default constructor for cases where DAO is not needed
    }


    public float  calculateNetProfit(float revenue, float expenses) {


        return revenue - expenses;


    }

    public float calculuateRevenue(float[] basePay, float[] tips){
        float totalBasePay = 0;
        float totalTips = 0;
    for(int i = 0; i < basePay.length; i++) {
        totalBasePay += basePay[i];
    }
    for(int i = 0; i < tips.length; i++) {
        totalTips += tips[i];

    }
    return totalBasePay + totalTips;

    }

    public float calculateProfitMargin(float revenue, float expenses) {

        return (revenue - expenses) / revenue;
    }

    public float calculateMedianDowntime(float[] downtime) {
        float[] downtime_array = new float[downtime.length];
        float sum_of_downtime = 0;

        for(int i = 0; i < downtime.length; i++) {
            sum_of_downtime  += downtime[i];
        }


        return sum_of_downtime / (downtime.length+1);
    }

    /**
     * @deprecated Use calculateVehicleDepreciation(vehicle, int) instead.
     * This method is kept for backward compatibility.
     */
    @Deprecated
    public float calcualteVehicleDeprication(float starting_value, int miles_driven){
        return (float) (starting_value - miles_driven * 0.08);
    }

    /**
     * Calculates the estimated depreciation of a vehicle based on:
     * - Miles driven during deliveries (from delivery data)
     * - Current vehicle miles (from vehicle record)
     * - Starting miles when vehicle was acquired
     * - Purchase price for depreciation calculations
     * Uses $0.08 per mile depreciation rate.
     *
     * If delivery miles exceed current vehicle miles, the calculation uses delivery miles
     * and flags that the vehicle record should be updated.
     *
     * @param currentVehicle The current vehicle with purchase price and miles data
     * @param deliveryMiles Total miles driven during deliveries
     * @return A VehicleDepreciationResult containing all depreciation details
     */
    public VehicleDepreciationResult calculateVehicleDepreciation(org.example.deliveryRecorder.src.vehicle currentVehicle, int deliveryMiles) {
        if (currentVehicle == null) {
            return new VehicleDepreciationResult(0, 0, 0, 0, 0, 0, 0, false);
        }

        double purchasePrice = currentVehicle.getPurchasePrice();
        int startingMiles = currentVehicle.getStartingMiles();
        int currentMiles = currentVehicle.getCurrentVehicleMiles();

        // Check if delivery miles exceed current miles
        // If so, use delivery miles + starting miles as the effective current miles
        boolean milesNeedUpdate = false;
        int effectiveDeliveryMiles = deliveryMiles;
        int effectiveCurrentMiles = currentMiles;

        // Calculate miles from deliveries (starting miles + delivery miles)
        int milesFromDeliveries = startingMiles + deliveryMiles;

        if (milesFromDeliveries > currentMiles) {
            // Delivery miles exceed what's recorded - use delivery miles
            effectiveCurrentMiles = milesFromDeliveries;
            milesNeedUpdate = true;
        }

        // Miles driven since purchase (use the higher of: current miles or delivery-based miles)
        int milesDrivenSincePurchase = Math.max(0, effectiveCurrentMiles - startingMiles);

        // Depreciation rate: $0.08 per mile
        double depreciationRate = 0.08;

        // Total depreciation from all miles driven since purchase
        double totalDepreciation = milesDrivenSincePurchase * depreciationRate;

        // Current estimated value
        double currentValue = Math.max(0, purchasePrice - totalDepreciation);

        // Depreciation percentage
        double depreciationPercentage = purchasePrice > 0 ? (totalDepreciation / purchasePrice) * 100 : 0;

        return new VehicleDepreciationResult(
            purchasePrice,
            currentValue,
            totalDepreciation,
            milesDrivenSincePurchase,
            startingMiles,
            depreciationPercentage,
            effectiveDeliveryMiles,
            milesNeedUpdate
        );
    }

    /**
     * Overload for backward compatibility - calculates without delivery miles.
     * @param currentVehicle The current vehicle
     * @return VehicleDepreciationResult
     */
    public VehicleDepreciationResult calculateVehicleDepreciation(org.example.deliveryRecorder.src.vehicle currentVehicle) {
        return calculateVehicleDepreciation(currentVehicle, 0);
    }

    /**
     * Data class to hold vehicle depreciation calculation results.
     */
    public static class VehicleDepreciationResult {
        public final double purchasePrice;
        public final double currentValue;
        public final double totalDepreciation;
        public final int milesDrivenSincePurchase;
        public final int startingMiles;
        public final double depreciationPercentage;
        public final int deliveryMiles;
        public final boolean milesNeedUpdate;

        public VehicleDepreciationResult(double purchasePrice, double currentValue,
                                          double totalDepreciation, int milesDrivenSincePurchase,
                                          int startingMiles, double depreciationPercentage,
                                          int deliveryMiles, boolean milesNeedUpdate) {
            this.purchasePrice = purchasePrice;
            this.currentValue = currentValue;
            this.totalDepreciation = totalDepreciation;
            this.milesDrivenSincePurchase = milesDrivenSincePurchase;
            this.startingMiles = startingMiles;
            this.depreciationPercentage = depreciationPercentage;
            this.deliveryMiles = deliveryMiles;
            this.milesNeedUpdate = milesNeedUpdate;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "Purchase Price: $%.2f\n" +
                "Starting Miles: %,d\n" +
                "Miles from Deliveries: %,d\n" +
                "Total Miles Driven Since Purchase: %,d\n" +
                "Total Depreciation: $%.2f\n" +
                "Current Estimated Value: $%.2f\n" +
                "Depreciation: %.1f%%",
                purchasePrice, startingMiles, deliveryMiles, milesDrivenSincePurchase,
                totalDepreciation, currentValue, depreciationPercentage
            ));

            if (milesNeedUpdate) {
                sb.append("\n\n⚠ Note: Delivery miles exceed recorded vehicle miles.");
                sb.append("\n  Vehicle odometer should be updated to: ").append(String.format("%,d", startingMiles + deliveryMiles));
            }

            return sb.toString();
        }
    }

    public String[] findOptimalResturnats(String resturants[], float[] profitList) {
    if (resturants == null || profitList == null || resturants.length != profitList.length) {
        return new String[0];
    }
    
    int n = resturants.length;
    Integer[] indices = new Integer[n];
    for (int i = 0; i < n; i++) {
        indices[i] = i;
    }
    
    // Sort using comparator (descending by profit)
    java.util.Arrays.sort(indices, (a, b) -> Float.compare(profitList[b], profitList[a]));
    
    String[] sortedRestaurants = new String[n];
    for (int i = 0; i < n; i++) {
        sortedRestaurants[i] = resturants[indices[i]];
    }
    
    return sortedRestaurants;
}

    public float calculateExpenses(float expenses[]){
        float totalexpenses = 0;

    for(int i = 0; i < expenses.length; i++) {
        totalexpenses += expenses[i];
    }
    return totalexpenses;
    }

    /**
     * Calculates expected profit based on historical data for a specific day of week and hour range.
     *
     * @param timestamp         The reference timestamp (epoch milliseconds) to determine the day of week
     * @param historicalStartTime Start of the historical date range to analyze (epoch milliseconds)
     * @param historicalEndTime   End of the historical date range to analyze (epoch milliseconds)
     * @param startHour         Start hour of the day (0-23, inclusive)
     * @param endHour           End hour of the day (0-23, inclusive)
     * @return The sum of all profits (basePay + tips) for matching days and hours
     * @throws IllegalStateException    if reportDAO is not initialized
     * @throws IllegalArgumentException if parameters are invalid
     */
    public float calculateExpectedProfit(long timestamp, long historicalStartTime, long historicalEndTime, 
                                          int startHour, int endHour) {
        if (reportDAO == null) {
            throw new IllegalStateException("reportDAO is not initialized");
        }
    
        if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
            throw new IllegalArgumentException("Hours must be between 0 and 23");
        }
        
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must be non-negative");
        }
        if (historicalStartTime < 0 || historicalEndTime < 0) {
            throw new IllegalArgumentException("Historical time values must be non-negative");
        }
        if (historicalStartTime > historicalEndTime) {
            throw new IllegalArgumentException("historicalStartTime must not be after historicalEndTime");
        }

        // Convert timestamp to LocalDateTime to get the day of week
        LocalDateTime referenceDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DayOfWeek targetDayOfWeek = referenceDateTime.getDayOfWeek();

        // Convert historical range to LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(historicalStartTime), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(historicalEndTime), ZoneId.systemDefault());

        // Get all delivery data with timestamps within the historical range
        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayWithTimestampByDateRange(startDateTime, endDateTime);

        float totalProfit = 0;

        for (Map<String, Object> delivery : deliveries) {
            Number timeValue = (Number) delivery.get("time");
            if (timeValue == null) {
                continue;
            }

            // Convert delivery timestamp to LocalDateTime
            LocalDateTime deliveryDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timeValue.longValue()), ZoneId.systemDefault());

            // Check if delivery is on the target day of week
            if (deliveryDateTime.getDayOfWeek() != targetDayOfWeek) {
                continue;
            }

            // Check if delivery is within the hour range
            int deliveryHour = deliveryDateTime.getHour();
            boolean inHourRange;
            if (startHour <= endHour) {
                // Normal range (e.g., 9 AM to 5 PM)
                inHourRange = deliveryHour >= startHour && deliveryHour <= endHour;
            } else {
                // Overnight range (e.g., 10 PM to 2 AM)
                inHourRange = deliveryHour >= startHour || deliveryHour <= endHour;
            }

            if (!inHourRange) {
                continue;
            }

            // Add basePay and tips to total profit
            Number basePay = (Number) delivery.get("basePay");
            Number tips = (Number) delivery.get("tips");

            if (basePay != null) {
                totalProfit += basePay.floatValue();
            }
            if (tips != null) {
                totalProfit += tips.floatValue();
            }
        }

        return totalProfit;
    }

    /**
     * Finds the optimal work hours for each day of the week based on historical profit data.
     * Analyzes the data to find the best consecutive hour blocks for working.
     *
     * @param hours              The number of consecutive hours to work per day
     * @param historicalStartTime Start of the historical date range to analyze (epoch milliseconds)
     * @param historicalEndTime   End of the historical date range to analyze (epoch milliseconds)
     * @return A formatted string showing the optimal work hours for each day of the week
     */
    public String findOptimalWorkHours(int hours, long historicalStartTime, long historicalEndTime) {
        if (reportDAO == null) {
            throw new IllegalStateException("reportDAO is not initialized");
        }

        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException("Hours must be between 1 and 24");
        }

        // Convert historical range to LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(historicalStartTime), ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(historicalEndTime), ZoneId.systemDefault());

        // Get all delivery data with timestamps
        List<Map<String, Object>> deliveries = reportDAO.getDeliveryPayWithTimestampByDateRange(startDateTime, endDateTime);

        // Create a profit matrix: [dayOfWeek (0-6)][hour (0-23)]
        float[][] profitByDayAndHour = new float[7][24];

        // Populate the profit matrix
        for (Map<String, Object> delivery : deliveries) {
            Number timeValue = (Number) delivery.get("time");
            if (timeValue == null) {
                continue;
            }

            LocalDateTime deliveryDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timeValue.longValue()), ZoneId.systemDefault());

            int dayIndex = deliveryDateTime.getDayOfWeek().getValue() - 1; // Monday = 0, Sunday = 6
            int hour = deliveryDateTime.getHour();

            Number basePay = (Number) delivery.get("basePay");
            Number tips = (Number) delivery.get("tips");

            float profit = 0;
            if (basePay != null) {
                profit += basePay.floatValue();
            }
            if (tips != null) {
                profit += tips.floatValue();
            }

            profitByDayAndHour[dayIndex][hour] += profit;
        }

        // Find optimal hours for each day
        StringBuilder result = new StringBuilder("Optimal Work Hours by Day:\n");
        result.append("===========================\n");

        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int day = 0; day < 7; day++) {
            int bestStartHour = 0;
            float bestProfit = 0;

            // Find the best consecutive block of 'hours' length
            for (int startHour = 0; startHour <= 24 - hours; startHour++) {
                float blockProfit = 0;
                for (int h = 0; h < hours; h++) {
                    blockProfit += profitByDayAndHour[day][startHour + h];
                }

                if (blockProfit > bestProfit) {
                    bestProfit = blockProfit;
                    bestStartHour = startHour;
                }
            }

            int endHour = bestStartHour + hours;
            String startFormatted = formatHour(bestStartHour);
            String endFormatted = formatHour(endHour);

            result.append(String.format("%-9s: %s - %s (Avg Profit: $%.2f)\n",
                    dayNames[day], startFormatted, endFormatted, bestProfit));
        }

        return result.toString();
    }

    /**
     * Formats an hour (0-24) into a readable time string (e.g., "9:00 AM", "5:00 PM").
     */
    private String formatHour(int hour) {
        if (hour == 0 || hour == 24) {
            return "12:00 AM";
        } else if (hour == 12) {
            return "12:00 PM";
        } else if (hour < 12) {
            return hour + ":00 AM";
        } else {
            return (hour - 12) + ":00 PM";
        }
    }

    public String compareProfitBetweenPlatforms(String[] platform, float[] profitList) {

        // Create array of platform-profit pairs
        class PlatformProfit {
            String platform;
            float profit;

            PlatformProfit(String p, float pr) {
                platform = p;
                profit = pr;
            }
        }

        PlatformProfit[] pairs = new PlatformProfit[platform.length];
        for (int i = 0; i < platform.length; i++) {
            pairs[i] = new PlatformProfit(platform[i], profitList[i]);
        }

        // Sort by profit in descending order
        java.util.Arrays.sort(pairs, (a, b) -> Float.compare(b.profit, a.profit));

        // Build result string
        StringBuilder result = new StringBuilder("Platforms ranked by profit:\n");
        for (PlatformProfit pair : pairs) {
            result.append(pair.platform).append(": $").append(String.format("%.2f", pair.profit)).append("\n");
        }

        return result.toString();
    }

    /**
     * Calculates the gallons of gas used based on miles driven and vehicle MPG.
     *
     * @param mpg The miles per gallon of the vehicle
     * @param milesDriven The total miles driven
     * @return The gallons of gas used
     */
    public float gasUsed(int mpg, float milesDriven) {
        if (mpg <= 0) {
            return 0;
        }
        return milesDriven / mpg;
    }

    /**
     * Calculates the gallons of gas used based on miles driven and vehicle MPG (double version).
     *
     * @param mpg The miles per gallon of the vehicle
     * @param milesDriven The total miles driven
     * @return The gallons of gas used
     */
    public double gasUsed(double mpg, double milesDriven) {
        if (mpg <= 0) {
            return 0;
        }
        return milesDriven / mpg;
    }

    /**
     * Calculates the estimated gas cost based on miles driven, vehicle MPG, and gas price per gallon.
     *
     * @param mpg The miles per gallon of the vehicle
     * @param milesDriven The total miles driven
     * @param gasPricePerGallon The current price of gas per gallon
     * @return The estimated gas cost in dollars
     */
    public float calculateGasCost(int mpg, float milesDriven, float gasPricePerGallon) {
        float gallonsUsed = gasUsed(mpg, milesDriven);
        return gallonsUsed * gasPricePerGallon;
    }

    /**
     * Calculates the estimated gas cost based on miles driven, vehicle MPG, and gas price per gallon (double version).
     *
     * @param mpg The miles per gallon of the vehicle
     * @param milesDriven The total miles driven
     * @param gasPricePerGallon The current price of gas per gallon
     * @return The estimated gas cost in dollars
     */
    public double calculateGasCost(double mpg, double milesDriven, double gasPricePerGallon) {
        double gallonsUsed = gasUsed(mpg, milesDriven);
        return gallonsUsed * gasPricePerGallon;
    }

    /**
     * Calculates the total gas cost for multiple deliveries.
     *
     * @param milesArray Array of miles driven for each delivery
     * @param mpgArray Array of MPG values for the vehicle used in each delivery
     * @param gasPricePerGallon The current price of gas per gallon
     * @return The total estimated gas cost for all deliveries
     */
    public float calculateTotalGasCost(float[] milesArray, int[] mpgArray, float gasPricePerGallon) {
        if (milesArray == null || mpgArray == null || milesArray.length != mpgArray.length) {
            return 0;
        }

        float totalCost = 0;
        for (int i = 0; i < milesArray.length; i++) {
            totalCost += calculateGasCost(mpgArray[i], milesArray[i], gasPricePerGallon);
        }
        return totalCost;
    }

    /**
     * Calculates the total gas cost using a single MPG value for all deliveries.
     *
     * @param totalMiles The total miles driven across all deliveries
     * @param mpg The miles per gallon of the vehicle
     * @param gasPricePerGallon The current price of gas per gallon
     * @return The total estimated gas cost
     */
    public double calculateTotalGasCost(double totalMiles, double mpg, double gasPricePerGallon) {
        return calculateGasCost(mpg, totalMiles, gasPricePerGallon);
    }



}