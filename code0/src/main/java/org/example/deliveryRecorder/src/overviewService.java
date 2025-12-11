
package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only service for retrieving all delivery and work period data for viewing.
 * Combines data from workPeriodService and deliveryDataService tables.
 */
@Service
public class overviewService {

    private final overviewServiceDAO overviewDAO;

    @Autowired
    public overviewService(overviewServiceDAO overviewDAO) {
        this.overviewDAO = overviewDAO;
    }

    // --- Work Period Retrieval ---

    public List<workPeriodService> getAllWorkPeriodsByUser(int userId) {
        return overviewDAO.findAllWorkPeriodsByUserId(userId);
    }

    public workPeriodService getWorkPeriodById(int workPeriodId) {
        return overviewDAO.findWorkPeriodById(workPeriodId);
    }

    // --- Delivery Data Retrieval ---

    public List<deliveryDataService> getAllDeliveriesByUser(int userId) {
        return overviewDAO.findAllDeliveriesByUserId(userId);
    }

    public List<deliveryDataService> getDeliveriesByWorkPeriod(int workPeriodId) {
        return overviewDAO.findDeliveriesByWorkPeriodId(workPeriodId);
    }

    public deliveryDataService getDeliveryById(int deliveryId) {
        return overviewDAO.findDeliveryById(deliveryId);
    }

    // --- Combined Overview Data ---

    public List<OverviewDTO> getFullOverviewByUser(int userId) {
        return overviewDAO.findFullOverviewByUserId(userId);
    }

    // --- Aggregate Queries ---

    public double getTotalEarningsByUser(int userId) {
        return overviewDAO.calculateTotalEarnings(userId);
    }

    public int getTotalDeliveriesByUser(int userId) {
        return overviewDAO.countDeliveriesByUserId(userId);
    }

    public int getTotalMilesByUser(int userId) {
        return overviewDAO.calculateTotalMiles(userId);
    }

    // --- DTO for combined view ---

    public static class OverviewDTO {
        private final int deliveryId;
        private final String fromAddress;
        private final String toAddress;
        private final String restaurant;
        private final String platform;
        private final float basePay;
        private final float tips;
        private final int milesDriven;
        private final long dateTime;
        private final String vehicle;
        private final int workPeriodId;

        public OverviewDTO(int deliveryId, String fromAddress, String toAddress, String restaurant,
                           String platform, float basePay, float tips, int milesDriven,
                           long dateTime, String vehicle, int workPeriodId) {
            this.deliveryId = deliveryId;
            this.fromAddress = fromAddress;
            this.toAddress = toAddress;
            this.restaurant = restaurant;
            this.platform = platform;
            this.basePay = basePay;
            this.tips = tips;
            this.milesDriven = milesDriven;
            this.dateTime = dateTime;
            this.vehicle = vehicle;
            this.workPeriodId = workPeriodId;
        }

        public int getDeliveryId() { return deliveryId; }
        public String getFromAddress() { return fromAddress; }
        public String getToAddress() { return toAddress; }
        public String getRestaurant() { return restaurant; }
        public String getPlatform() { return platform; }
        public float getBasePay() { return basePay; }
        public float getTips() { return tips; }
        public int getMilesDriven() { return milesDriven; }
        public long getDateTime() { return dateTime; }
        public String getVehicle() { return vehicle; }
        public int getWorkPeriodId() { return workPeriodId; }
    }
}