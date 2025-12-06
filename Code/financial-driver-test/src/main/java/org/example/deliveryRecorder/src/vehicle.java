package org.example.deliveryRecorder.src;

public class vehicle {
    private String vehicleType;
    private String vehicleModel;
    private int vehicleMPG;
    private String currentVehicleDriven;
    private int currentVehicleMiles;
    private int startingVehicleMiles;

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
    public int getVehicleMPG() {
        return vehicleMPG;
    }
    public void setVehicleMPG(int vehicleMPG) {
        this.vehicleMPG = vehicleMPG;
    }

    public void addNewVehicle(String vehicleType, String vehicleModel, int vehicleMPG) {
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
        this.vehicleMPG = vehicleMPG;
    }

}
