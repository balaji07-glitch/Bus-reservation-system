package com.busgo.model;

import java.sql.Timestamp;

/**
 * PassengerFeedback entity representing passenger-to-passenger reviews,
 * ratings, travel advice, and cleanliness/punctuality scores.
 */
public class PassengerFeedback {
    private int feedbackId;
    private String busId;
    private String passengerName;
    private String passengerEmail;
    private String ticketId;
    private int rating;
    private int punctuality;
    private int cleanliness;
    private int comfort;
    private String adviceForPassengers;
    private String comment;
    private boolean isVerifiedPassenger;
    private int helpfulVotes;
    private Timestamp createdAt;

    public PassengerFeedback() {
        this.rating = 5;
        this.punctuality = 5;
        this.cleanliness = 5;
        this.comfort = 5;
        this.isVerifiedPassenger = true;
        this.helpfulVotes = 0;
    }

    public PassengerFeedback(String busId, String passengerName, String passengerEmail, String ticketId,
                             int rating, int punctuality, int cleanliness, int comfort,
                             String adviceForPassengers, String comment) {
        this.busId = busId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.ticketId = ticketId;
        this.rating = Math.max(1, Math.min(5, rating));
        this.punctuality = Math.max(1, Math.min(5, punctuality));
        this.cleanliness = Math.max(1, Math.min(5, cleanliness));
        this.comfort = Math.max(1, Math.min(5, comfort));
        this.adviceForPassengers = adviceForPassengers;
        this.comment = comment;
        this.isVerifiedPassenger = true;
        this.helpfulVotes = 0;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getPassengerEmail() { return passengerEmail; }
    public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getPunctuality() { return punctuality; }
    public void setPunctuality(int punctuality) { this.punctuality = punctuality; }

    public int getCleanliness() { return cleanliness; }
    public void setCleanliness(int cleanliness) { this.cleanliness = cleanliness; }

    public int getComfort() { return comfort; }
    public void setComfort(int comfort) { this.comfort = comfort; }

    public String getAdviceForPassengers() { return adviceForPassengers; }
    public void setAdviceForPassengers(String adviceForPassengers) { this.adviceForPassengers = adviceForPassengers; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isVerifiedPassenger() { return isVerifiedPassenger; }
    public void setVerifiedPassenger(boolean verifiedPassenger) { isVerifiedPassenger = verifiedPassenger; }

    public int getHelpfulVotes() { return helpfulVotes; }
    public void setHelpfulVotes(int helpfulVotes) { this.helpfulVotes = helpfulVotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
