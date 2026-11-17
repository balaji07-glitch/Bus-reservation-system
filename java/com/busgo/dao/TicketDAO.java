package com.busgo.dao;

import com.busgo.db.DBConnection;
import com.busgo.Passenger;
import com.busgo.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Ticket operations backed by MySQL.
 */
public class TicketDAO {

    public synchronized Ticket bookTicket(String busId, int seatNo, Passenger passenger, double fare, String journeyDate) throws Exception {
        // Concurrency Check directly in MySQL transaction
        String checkSql = "SELECT COUNT(*) FROM tickets WHERE bus_id = ? AND seat_no = ? AND journey_date = ? AND status = 'CONFIRMED' FOR UPDATE";
        String insertSql = "INSERT INTO tickets (ticket_id, bus_id, seat_no, journey_date, passenger_name, passenger_age, passenger_gender, passenger_mobile, passenger_email, fare_paid, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, busId);
                checkStmt.setInt(2, seatNo);
                checkStmt.setString(3, journeyDate);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        throw new IllegalStateException("Seat " + seatNo + " is already confirmed for another traveler.");
                    }
                }
            }

            String ticketId = generateNextTicketId(conn);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, ticketId);
                insertStmt.setString(2, busId);
                insertStmt.setInt(3, seatNo);
                insertStmt.setString(4, journeyDate);
                insertStmt.setString(5, passenger.getName());
                insertStmt.setInt(6, passenger.getAge());
                insertStmt.setString(7, passenger.getGender());
                insertStmt.setString(8, passenger.getMobile());
                insertStmt.setString(9, passenger.getEmail());
                insertStmt.setDouble(10, fare);

                insertStmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return new Ticket(ticketId, busId, seatNo, passenger, fare, journeyDate);
        }
    }

    public synchronized double cancelTicket(String ticketId) throws Exception {
        String selectSql = "SELECT fare_paid, status FROM tickets WHERE ticket_id = ? FOR UPDATE";
        String updateSql = "UPDATE tickets SET status = 'CANCELLED', refund_amount = ? WHERE ticket_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            double farePaid = 0.0;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, ticketId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new IllegalArgumentException("Ticket not found: " + ticketId);
                    }
                    if ("CANCELLED".equalsIgnoreCase(rs.getString("status"))) {
                        conn.rollback();
                        throw new IllegalStateException("Ticket " + ticketId + " has already been cancelled.");
                    }
                    farePaid = rs.getDouble("fare_paid");
                }
            }

            double refund = Math.round((farePaid * 0.90) * 100.0) / 100.0;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDouble(1, refund);
                updateStmt.setString(2, ticketId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return refund;
        }
    }

    public Ticket getTicketById(String ticketId) {
        String sql = "SELECT * FROM tickets WHERE ticket_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTicket(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ticket: " + e.getMessage());
        }
        return null;
    }

    public List<Ticket> getAllActiveTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE status = 'CONFIRMED' ORDER BY booked_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active tickets: " + e.getMessage());
        }
        return list;
    }

    private String generateNextTicketId(Connection conn) throws SQLException {
        String sql = "SELECT ticket_id FROM tickets ORDER BY ticket_id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String latest = rs.getString("ticket_id");
                String numStr = latest.replaceAll("[^0-9]", "");
                int num = Integer.parseInt(numStr);
                return "TKT" + (num + 1);
            }
        }
        return "TKT1001";
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        String email = null;
        try {
            email = rs.getString("passenger_email");
        } catch (Exception ignored) {}
        
        Passenger passenger = new Passenger(
                rs.getString("passenger_name"),
                rs.getInt("passenger_age"),
                rs.getString("passenger_gender"),
                rs.getString("passenger_mobile"),
                email
        );
        return new Ticket(
                rs.getString("ticket_id"),
                rs.getString("bus_id"),
                rs.getInt("seat_no"),
                passenger,
                rs.getDouble("fare_paid"),
                rs.getString("journey_date")
        );
    }
}
