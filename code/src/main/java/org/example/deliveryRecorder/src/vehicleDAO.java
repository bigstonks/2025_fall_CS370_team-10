package org.example.deliveryRecorder.src;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class vehicleDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public vehicle findByModel(String vehicleModel) {
        String sql = "SELECT * FROM vehicle WHERE vehicleName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{vehicleModel},
                (ResultSet rs, int rowNum) -> {
                    vehicle v = new vehicle();
                    v.setVehicleType(rs.getString("vehicleType"));
                    v.setVehicleModel(rs.getString("vehicleName"));  // Use vehicleName from DB
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    // Get MPG from database
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
                    // Get totalVehicleMiles as startingMiles
                    try {
                        v.setStartingMiles(rs.getInt("totalVehicleMiles"));
                    } catch (Exception e) {
                        v.setStartingMiles(0);
                    }
                    // purchasePrice not in this schema, use default
                    v.setPurchasePrice(vehicle.DEFAULT_PURCHASE_PRICE);
                    return v;
                });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<vehicle> findAll() {
        String sql = "SELECT * FROM vehicle";
        return jdbcTemplate.query(sql,
            (ResultSet rs, int rowNum) -> {
                vehicle v = new vehicle();
                v.setVehicleType(rs.getString("vehicleType"));
                v.setVehicleModel(rs.getString("vehicleName"));  // Use vehicleName from DB
                v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                // Get MPG from database
                try {
                    v.setVehicleMpg(rs.getDouble("mpg"));
                } catch (Exception e) {
                    v.setVehicleMpg(0.0);
                }
                // Get totalVehicleMiles as startingMiles
                try {
                    v.setStartingMiles(rs.getInt("totalVehicleMiles"));
                } catch (Exception e) {
                    v.setStartingMiles(0);
                }
                // purchasePrice not in this schema, use default
                v.setPurchasePrice(vehicle.DEFAULT_PURCHASE_PRICE);
                return v;
            });
    }

    public void create(vehicle v) {
        // Use column names that match the actual database schema
        String sql = "INSERT INTO vehicle (vehicleName, vehicleType, vehicleModel, currentVehicleDriven, currentVehicleMiles, mpg, totalVehicleMiles) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            v.getVehicleModel(),  // vehicleName in DB = vehicleModel in code (the display name)
            v.getVehicleType(),
            v.getVehicleModel(),  // vehicleModel
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles(),
            v.getVehicleMpg(),
            v.getStartingMiles()  // Use startingMiles as totalVehicleMiles
        );
    }

    public void update(vehicle v) {
        // Use column names that match the actual database schema
        String sql = "UPDATE vehicle SET vehicleType = ?, currentVehicleDriven = ?, currentVehicleMiles = ?, mpg = ?, totalVehicleMiles = ? WHERE vehicleName = ?";
        jdbcTemplate.update(sql,
            v.getVehicleType(),
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles(),
            v.getVehicleMpg(),
            v.getStartingMiles(),
            v.getVehicleModel()  // vehicleName in DB = vehicleModel in code
        );
    }

    public void delete(String vehicleModel) {
        String sql = "DELETE FROM vehicle WHERE vehicleName = ?";
        jdbcTemplate.update(sql, vehicleModel);
    }
    public String getCurrentVehicleDriven(String vehicleModel) {
        String sql = "SELECT currentVehicleDriven FROM vehicle WHERE vehicleName = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{vehicleModel}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // Vehicle not found
        }
    }

    /**
     * Finds the vehicle that is marked as the current vehicle being driven.
     * If no vehicle is marked as current, returns the first vehicle in the database.
     * @return The current vehicle, or null if no vehicles exist
     */
    public vehicle findCurrentVehicle() {
        // First, try to find a vehicle marked as current (currentVehicleDriven = 'true' or 'yes' or '1')
        String sql = "SELECT * FROM vehicle WHERE currentVehicleDriven = 'true' OR currentVehicleDriven = 'yes' OR currentVehicleDriven = '1' LIMIT 1";
        try {
            List<vehicle> currentVehicles = jdbcTemplate.query(sql,
                (ResultSet rs, int rowNum) -> {
                    vehicle v = new vehicle();
                    v.setVehicleType(rs.getString("vehicleType"));
                    v.setVehicleModel(rs.getString("vehicleName"));  // Use vehicleName from DB
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
                    try {
                        v.setStartingMiles(rs.getInt("totalVehicleMiles"));
                    } catch (Exception e) {
                        v.setStartingMiles(0);
                    }
                    v.setPurchasePrice(vehicle.DEFAULT_PURCHASE_PRICE);
                    return v;
                });

            if (!currentVehicles.isEmpty()) {
                return currentVehicles.get(0);
            }
        } catch (Exception e) {
            System.out.println("Error finding current vehicle: " + e.getMessage());
        }

        // If no vehicle is marked as current, get the first vehicle and mark it as current
        String firstSql = "SELECT * FROM vehicle LIMIT 1";
        try {
            List<vehicle> vehicles = jdbcTemplate.query(firstSql,
                (ResultSet rs, int rowNum) -> {
                    vehicle v = new vehicle();
                    v.setVehicleType(rs.getString("vehicleType"));
                    v.setVehicleModel(rs.getString("vehicleName"));  // Use vehicleName from DB
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
                    try {
                        v.setStartingMiles(rs.getInt("totalVehicleMiles"));
                    } catch (Exception e) {
                        v.setStartingMiles(0);
                    }
                    v.setPurchasePrice(vehicle.DEFAULT_PURCHASE_PRICE);
                    return v;
                });

            if (!vehicles.isEmpty()) {
                vehicle firstVehicle = vehicles.get(0);
                // Mark this vehicle as current
                setAsCurrentVehicle(firstVehicle.getVehicleModel());
                firstVehicle.setCurrentVehicleDriven("true");
                return firstVehicle;
            }
        } catch (Exception e) {
            System.out.println("Error getting first vehicle: " + e.getMessage());
        }

        return null; // No vehicles in database
    }

    /**
     * Sets the specified vehicle as the current vehicle and unsets all others.
     * @param vehicleModel The vehicle model to set as current
     */
    public void setAsCurrentVehicle(String vehicleModel) {
        // First, unset all vehicles as current
        String unsetSql = "UPDATE vehicle SET currentVehicleDriven = 'false'";
        try {
            jdbcTemplate.update(unsetSql);
        } catch (Exception e) {
            System.out.println("Error unsetting current vehicles: " + e.getMessage());
        }

        // Then set the specified vehicle as current (use vehicleName column)
        String setSql = "UPDATE vehicle SET currentVehicleDriven = 'true' WHERE vehicleName = ?";
        try {
            jdbcTemplate.update(setSql, vehicleModel);
            System.out.println("Vehicle '" + vehicleModel + "' set as current.");
        } catch (Exception e) {
            System.out.println("Error setting current vehicle: " + e.getMessage());
        }
    }

    /**
     * Updates the miles (odometer reading) for a specific vehicle.
     * @param vehicleModel The vehicle model to update
     * @param miles The new miles value
     */
    public void updateVehicleMiles(String vehicleModel, int miles) {
        String sql = "UPDATE vehicle SET currentVehicleMiles = ? WHERE vehicleName = ?";
        try {
            int rowsUpdated = jdbcTemplate.update(sql, miles, vehicleModel);
            if (rowsUpdated > 0) {
                System.out.println("Vehicle '" + vehicleModel + "' miles updated to " + miles);
            } else {
                System.out.println("No vehicle found with model: " + vehicleModel);
            }
        } catch (Exception e) {
            System.out.println("Error updating vehicle miles: " + e.getMessage());
            throw new RuntimeException("Failed to update vehicle miles", e);
        }
    }
}
