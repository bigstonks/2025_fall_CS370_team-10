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
                return v;
            });
    }

    public void create(vehicle v) {
        String sql = "INSERT INTO vehicle (vehicleType, vehicleModel, currentVehicleDriven, currentVehicleMiles) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            v.getVehicleType(),
            v.getVehicleModel(),
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles()
        );
    }

    public void update(vehicle v) {
        String sql = "UPDATE vehicle SET vehicleType = ?, currentVehicleDriven = ?, currentVehicleMiles = ? WHERE vehicleModel = ?";
        jdbcTemplate.update(sql,
            v.getVehicleType(),
            v.getCurrentVehicleDriven(),
            v.getCurrentVehicleMiles(),
            v.getVehicleModel()
        );
    }

    public void delete(String vehicleModel) {
        String sql = "DELETE FROM vehicle WHERE vehicleModel = ?";
        jdbcTemplate.update(sql, vehicleModel);
    }
}
