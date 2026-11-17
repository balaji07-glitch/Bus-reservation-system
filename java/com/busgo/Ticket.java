package com.busgo;

/**
 * Ticket entity representing a confirmed bus reservation.
 * Encapsulates passenger details, seat number, 90% cancellation refund policy,
 * and unique ticket ID format (e.g. TKT1001).
 */
public class Ticket {
    private String ticketId;
    private String busId;
    private int seatNo;
    private Passenger passenger;
    private double fare;
    private String date;

    public Ticket(String ticketId, String busId, int seatNo, Passenger passenger, double fare, String date) {
        this.ticketId = ticketId;
        this.busId = busId;
        this.seatNo = seatNo;
        this.passenger = passenger;
        this.fare = fare;
        this.date = (date != null && !date.trim().isEmpty()) ? date : "2026-10-26";
    }

    public String getTicketId() { return ticketId; }
    public String getBusId() { return busId; }
    public int getSeatNo() { return seatNo; }
    public Passenger getPassenger() { return passenger; }
    public double getFare() { return fare; }
    public String getDate() { return date; }

    /**
     * Business Rule: 90% Refund Policy on Cancellation
     */
    public double calculateRefund() {
        return Math.round((fare * 0.90) * 100.0) / 100.0;
    }

    /**
     * Serializes ticket data to tickets.txt
     */
    public String toFileString() {
        return ticketId + "|" + busId + "|" + seatNo + "|" + fare + "|" + date + "|" + passenger.toFileString();
    }

    /**
     * Parses ticket record from line in tickets.txt
     */
    public static Ticket fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) return null;

        String ticketId = parts[0];
        String busId = parts[1];
        int seatNo = Integer.parseInt(parts[2]);
        double fare = Double.parseDouble(parts[3]);
        String date = parts[4];

        String passName = parts[5];
        int passAge = Integer.parseInt(parts[6]);
        String passGender = parts[7];
        String passMobile = parts[8];
        String passEmail = (parts.length >= 10 && parts[9] != null && !parts[9].trim().isEmpty()) ? parts[9].trim() : (passMobile + "@busgo.com");

        Passenger passenger = new Passenger(passName, passAge, passGender, passMobile, passEmail);
        return new Ticket(ticketId, busId, seatNo, passenger, fare, date);
    }

    public void printTicketCard(Bus bus) {
        System.out.println("\n  +======================================================+");
        System.out.println("  |                  BUSGO E-TICKET                      |");
        System.out.println("  +======================================================+");
        System.out.printf("  | Ticket ID   : %-37s |\n", ticketId);
        System.out.printf("  | Journey Date: %-37s |\n", date);
        System.out.printf("  | Passenger   : %-37s |\n", passenger.getName());
        System.out.printf("  | Age / Gender: %-37s |\n", passenger.getAge() + " yrs / " + passenger.getGender());
        System.out.printf("  | Mobile      : %-37s |\n", passenger.getMobile());
        System.out.printf("  | Email       : %-37s |\n", passenger.getEmail());
        System.out.println("  +------------------------------------------------------+");
        System.out.printf("  | Bus ID/Name : %-37s |\n", (bus != null ? bus.getBusId() + " - " + bus.getBusName() : busId));
        System.out.printf("  | Route       : %-37s |\n", (bus != null ? bus.getSource() + " -> " + bus.getDestination() : "N/A"));
        System.out.printf("  | Timings     : %-37s |\n", (bus != null ? bus.getDeparture() + " - " + bus.getArrival() : "N/A"));
        System.out.printf("  | Seat Number : Seat %-32d |\n", seatNo);
        System.out.printf("  | Total Fare  : Rs. %-33.2f |\n", fare);
        System.out.println("  +------------------------------------------------------+");
        System.out.printf("  | QR Token    : [BUSGO-QR-%s-%s] |\n", ticketId, passenger.getMobile());
        System.out.println("  | Status      : CONFIRMED (Email Delivered Automatically)|");
        System.out.println("  | Cancellation: 90% Refund (Rs. " + String.format("%.2f", calculateRefund()) + ")       |");
        System.out.println("  +======================================================+\n");
    }
}
