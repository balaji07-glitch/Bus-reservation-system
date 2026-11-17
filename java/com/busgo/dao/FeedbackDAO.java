package com.busgo.dao;

import com.busgo.db.DBConnection;
import com.busgo.model.PassengerFeedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for PassengerFeedback entity using MySQL.
 */
public class FeedbackDAO {

    public boolean addFeedback(PassengerFeedback fb) {
        String sql = "INSERT INTO passenger_feedback (bus_id, passenger_name, passenger_email, ticket_id, rating, punctuality, cleanliness, comfort, advice_for_passengers, comment, is_verified_passenger, helpful_votes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fb.getBusId());
            stmt.setString(2, fb.getPassengerName());
            stmt.setString(3, fb.getPassengerEmail());
            stmt.setString(4, fb.getTicketId());
            stmt.setInt(5, fb.getRating());
            stmt.setInt(6, fb.getPunctuality());
            stmt.setInt(7, fb.getCleanliness());
            stmt.setInt(8, fb.getComfort());
            stmt.setString(9, fb.getAdviceForPassengers());
            stmt.setString(10, fb.getComment());
            stmt.setBoolean(11, fb.isVerifiedPassenger());
            stmt.setInt(12, fb.getHelpfulVotes());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding passenger feedback: " + e.getMessage());
            return false;
        }
    }

    public List<PassengerFeedback> getFeedbackForBus(String busId) {
        List<PassengerFeedback> list = new ArrayList<>();
        String sql = "SELECT * FROM passenger_feedback WHERE bus_id = ? ORDER BY helpful_votes DESC, created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, busId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bus feedback: " + e.getMessage());
        }
        return list;
    }

    public List<PassengerFeedback> getAllFeedback() {
        List<PassengerFeedback> list = new ArrayList<>();
        String sql = "SELECT * FROM passenger_feedback ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all feedback: " + e.getMessage());
        }
        return list;
    }

    public boolean upvoteHelpful(int feedbackId) {
        String sql = "UPDATE passenger_feedback SET helpful_votes = helpful_votes + 1 WHERE feedback_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feedbackId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error upvoting feedback: " + e.getMessage());
            return false;
        }
    }

    private PassengerFeedback mapRow(ResultSet rs) throws SQLException {
        PassengerFeedback fb = new PassengerFeedback();
        fb.setFeedbackId(rs.getInt("feedback_id"));
        fb.setBusId(rs.getString("bus_id"));
        fb.setPassengerName(rs.getString("passenger_name"));
        fb.setPassengerEmail(rs.getString("passenger_email"));
        fb.setTicketId(rs.getString("ticket_id"));
        fb.setRating(rs.getInt("rating"));
        fb.setPunctuality(rs.getInt("punctuality"));
        fb.setCleanliness(rs.getInt("cleanliness"));
        fb.setComfort(rs.getInt("comfort"));
        fb.setAdviceForPassengers(rs.getString("advice_for_passengers"));
        fb.setComment(rs.getString("comment"));
        fb.setVerifiedPassenger(rs.getBoolean("is_verified_passenger"));
        fb.setHelpfulVotes(rs.getInt("helpful_votes"));
        fb.setCreatedAt(rs.getTimestamp("created_at"));
        return fb;
    }
}
