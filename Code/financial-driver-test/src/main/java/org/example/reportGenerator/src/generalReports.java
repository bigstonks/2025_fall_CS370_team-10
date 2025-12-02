package src.generalReports;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import src.reportDAO;
import java.util.List;

@Service
public class generalReports {

    @Autowired
    private reportDAO reportDAO;

    // 1. Total Income Logic
    // If you want to process an existing array:
    public float totalIncome(float[] income) {
        float sum = 0;
        if (income == null) return 0;
        for (float v : income) {
            sum += v;
        }
        return sum;
    }

    // If you want to fetch from DB for a specific user:
    public float getTotalIncomeFromDB(int userId) {
        return reportDAO.getTotalEarnings(userId);
    }

    // 2. Average Income Logic
    public float averageIncome(float[] income) {
        if (income == null || income.length == 0) return 0;
        return totalIncome(income) / income.length;
    }

    public float getAverageIncomeFromDB(int userId) {
        return reportDAO.getAverageEarnings(userId);
    }

    // 3. Delivery Income Logic
    public float getTotalDeliveryIncomeFromDB() {
        return reportDAO.getTotalDeliveryIncome();
    }

    public float getCurrentMonthDeliveryIncomeFromDB() {
        return reportDAO.getCurrentMonthDeliveryIncome();
    }

    public car car
        {

    }
}