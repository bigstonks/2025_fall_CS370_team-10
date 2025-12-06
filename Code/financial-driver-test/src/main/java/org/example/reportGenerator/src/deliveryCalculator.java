package org.example.reportGenerator.src;
public class deliveryCalculator {

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


    }

    public float calculateProfitMargin(float revenue, float expenses) {
        return (revenue - expenses) / expenses;
    }

    public float calculateMedianDowntime(float[] downtime) {
        float[] downtime_array = new float[downtime.length];
        float sum_of_downtime = 0;

        for(int i = 0; i < downtime.length; i++) {
            sum_of_downtime  += downtime[i];
        }


        return sum_of_downtime / (downtime.length+1);
    }
    
    public float calcualteVehicleDeprication(float starting_value, int miles_driven){
        return (float) (starting_value - miles_driven * 0.08);
    }

    public String[] findOptimalResturnats(String resturants[], float profitList ){




    }

    public float[] calculateExpenses(float expenses[]){

    }
    public float calculateExpectedProfit(float expected ){

    }
    public String findOptimalWorkHours(int hours ){

    }
    public String compareProfitBetweenPlatforms(float[] revenue){

    }
    public float gasUsed(int mpg, float milesDriven){
       float gallonsOfGasUsed = milesDriven / mpg;

        return gallonsOfGasUsed;
    }

}