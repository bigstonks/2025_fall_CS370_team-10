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
        String sql = "SELECT * FROM vehicle WHERE vehicleModel = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{vehicleModel},
                (ResultSet rs, int rowNum) -> {
                    vehicle v = new vehicle();
                    v.setVehicleType(rs.getString("vehicleType"));
                    v.setVehicleModel(rs.getString("vehicleModel"));
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    // Get MPG from database
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
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
                v.setVehicleModel(rs.getString("vehicleModel"));
                v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                // Get MPG from database
                try {
                    v.setVehicleMpg(rs.getDouble("mpg"));
                } catch (Exception e) {
                    v.setVehicleMpg(0.0);
                }
                return v;
            });
    }

    public void create(vehicle v) {
        String sql = "INSERT INTO vehicle (vehicleType, vehicleModel, currentVehicleDriven, currentVehicleMiles, mpg) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            v.getVehicleType(),
            v.getVehicleModel(),
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles(),
            v.getVehicleMpg()
        );
    }

    public void update(vehicle v) {
        String sql = "UPDATE vehicle SET vehicleType = ?, currentVehicleDriven = ?, currentVehicleMiles = ?, mpg = ? WHERE vehicleModel = ?";
        jdbcTemplate.update(sql,
            v.getVehicleType(),
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles(),
            v.getVehicleMpg(),
            v.getVehicleModel()
        );
    }

    public void delete(String vehicleModel) {
        String sql = "DELETE FROM vehicle WHERE vehicleModel = ?";
        jdbcTemplate.update(sql, vehicleModel);
    }
    public String getCurrentVehicleDriven(String vehicleModel) {
        String sql = "SELECT currentVehicleDriven FROM vehicle WHERE vehicleModel = ?";
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
                    v.setVehicleModel(rs.getString("vehicleModel"));
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    // Get MPG from database
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
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
                    v.setVehicleModel(rs.getString("vehicleModel"));
                    v.setCurrentVehicleDriven(rs.getString("currentVehicleDriven"));
                    v.setCurrentVehicleMiles(rs.getInt("currentVehicleMiles"));
                    // Get MPG from database
                    try {
                        v.setVehicleMpg(rs.getDouble("mpg"));
                    } catch (Exception e) {
                        v.setVehicleMpg(0.0);
                    }
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

        // Then set the specified vehicle as current
        String setSql = "UPDATE vehicle SET currentVehicleDriven = 'true' WHERE vehicleModel = ?";
        try {
            jdbcTemplate.update(setSql, vehicleModel);
            System.out.println("Vehicle '" + vehicleModel + "' set as current.");
        } catch (Exception e) {
            System.out.println("Error setting current vehicle: " + e.getMessage());
        }
    }
}
