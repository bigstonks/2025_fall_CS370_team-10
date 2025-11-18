public class deliveryCalculator {

    public double net_profit(float revenue, float expenses) {


        return revenue - expenses;


    }

    public double calculate_profit_margin(double revenue, double expenses) {
        return (revenue - expenses) / expenses;
    }

    public double calculate_median_downtime(float[] downtime) {
        double[] downtime_array = new double[downtime.length];
        double sum_of_downtime = 0;
        for(int i = 0; i < downtime.length; i++) {
            sum_of_downtime  += downtime[i];
        }


        return sum_of_downtime / (downtime.length+1);
    }
    
    public double vehicle_Deprication(double starting_value, int miles_driven){
        return starting_value - miles_driven * 0.08;
    }

    public String[] find_optimal_resturnats(String resturants[], double profitList ){

    return;
    }


}