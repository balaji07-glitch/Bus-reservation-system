package com.busgo.dao;

import com.busgo.db.DBConnection;
import com.busgo.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Bus operations backed by MySQL.
 */
public class BusDAO {

    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses ORDER BY bus_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Bus bus = mapResultSetToBus(rs);
                loadBookedSeats(conn, bus);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching buses: " + e.getMessage());
        }
        return buses;
    }

    public List<Bus> searchBuses(String from, String to) {
        List<Bus> buses = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM buses WHERE 1=1 ");
        if (from != null && !from.trim().isEmpty()) {
            sql.append("AND LOWER(source_city) LIKE ? ");
        }
        if (to != null && !to.trim().isEmpty()) {
            sql.append("AND LOWER(destination_city) LIKE ? ");
        }
        sql.append("ORDER BY departure_time ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (from != null && !from.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + from.trim().toLowerCase() + "%");
            }
            if (to != null && !to.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + to.trim().toLowerCase() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Bus bus = mapResultSetToBus(rs);
                    loadBookedSeats(conn, bus);
                    buses.add(bus);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching buses: " + e.getMessage());
        }
        return buses;
    }

    public Bus getBusById(String busId) {
        String sql = "SELECT * FROM buses WHERE bus_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, busId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Bus bus = mapResultSetToBus(rs);
                    loadBookedSeats(conn, bus);
                    return bus;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding bus by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean addBus(Bus bus) {
        String sql = "INSERT INTO buses (bus_id, bus_name, source_city, destination_city, departure_time, arrival_time, fare, total_seats) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bus.getBusId());
            stmt.setString(2, bus.getBusName());
            stmt.setString(3, bus.getSource());
            stmt.setString(4, bus.getDestination());
            stmt.setString(5, bus.getDeparture());
            stmt.setString(6, bus.getArrival());
            stmt.setDouble(7, bus.getFare());
            stmt.setInt(8, bus.getTotalSeats());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding bus: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBus(String busId) {
        String sql = "DELETE FROM buses WHERE bus_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, busId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting bus: " + e.getMessage());
            return false;
        }
    }

    private void loadBookedSeats(Connection conn, Bus bus) {
        String sql = "SELECT seat_no FROM tickets WHERE bus_id = ? AND status = 'CONFIRMED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bus.getBusId());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Integer> booked = new ArrayList<>();
                while (rs.next()) {
                    booked.add(rs.getInt("seat_no"));
                }
                bus.setBookedSeats(booked);
            }
        } catch (SQLException e) {
            System.err.println("Error loading booked seats for bus " + bus.getBusId() + ": " + e.getMessage());
        }
    }

    private Bus mapResultSetToBus(ResultSet rs) throws SQLException {
        return new Bus(
                rs.getString("bus_id"),
                rs.getString("bus_name"),
                rs.getString("source_city"),
                rs.getString("destination_city"),
                rs.getString("departure_time"),
                rs.getString("arrival_time"),
                rs.getDouble("fare"),
                rs.getInt("total_seats")
        );
    }
}
