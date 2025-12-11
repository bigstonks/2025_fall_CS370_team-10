package org.example.deliveryRecorder.src;

public class vehicle {
    private String vehicleType;
    private String vehicleModel;
    private String currentVehicleDriven;
    private int currentVehicleMiles;
    private int startingMiles;
    private double purchasePrice;
    private double vehicleMpg; // Miles per gallon for gas cost calculations

    // Default purchase price if not specified
    public static final double DEFAULT_PURCHASE_PRICE = 15000.0;

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

    public int getStartingMiles() {
        return startingMiles;
    }

    public void setStartingMiles(int startingMiles) {
        this.startingMiles = startingMiles;
    }

    public double getPurchasePrice() {
        // Return default if not set
        return purchasePrice > 0 ? purchasePrice : DEFAULT_PURCHASE_PRICE;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    /**
     * Calculates the miles driven since purchase (current miles - starting miles).
     * @return Miles driven since purchase, or 0 if data is invalid
     */
    public int getMilesDrivenSincePurchase() {
        return Math.max(0, currentVehicleMiles - startingMiles);
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
