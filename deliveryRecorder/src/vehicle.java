package org.example.deliveryRecorder.src;

public class vehicle {
    private String vehicleType;
    private String vehicleModel;
    private String currentVehicleDriven;
    private int currentVehicleMiles;
    private int startingVehicleMiles;
    private double vehicleMpg; // Miles per gallon for gas cost calculations

    public String getCurrentVehicleDriven() {
        return currentVehicleDriven;
    }
    public void setCurrentVehicleDriven(String currentVehicleDriven) {
        this.currentVehicleDriven = currentVehicleDriven;
    }
    public int getCurrentVehicleMiles() {
        return currentVehicleMiles;
    }
    public int setCurrentVehicleMiles(int currentVehicleMiles) {
       return this.currentVehicleMiles = currentVehicleMiles;
    }
    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    public String getVehicleModel() {
        return vehicleModel;
    }
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public double getVehicleMpg() {
        return vehicleMpg;
    }

    public void setVehicleMpg(double vehicleMpg) {
        this.vehicleMpg = vehicleMpg;
    }

    /**
     * Returns the current vehicle identifier (model).
     * This replaces the previous vehicleMPG field usage per request.
     */
    public String getVehicle() {
        return this.vehicleModel;
    }

    public void addNewVehicle(String vehicleType, String vehicleModel) {
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
    }

}
