package com.busgo;

import java.util.ArrayList;
import java.util.List;

/**
 * Bus entity representing a scheduled vehicle route and 2x2 cabin seat layout.
 * Demonstrates OOP Data Abstraction, Encapsulation, and Collections.
 */
public class Bus {
    private String busId;
    private String busName;
    private String source;
    private String destination;
    private String departure;
    private String arrival;
    private double fare;
    private int totalSeats;
    private List<Integer> bookedSeats;

    public Bus(String busId, String busName, String source, String destination,
               String departure, String arrival, double fare, int totalSeats) {
        this.busId = sanitize(busId).toUpperCase();
        this.busName = sanitize(busName);
        this.source = sanitize(source);
        this.destination = sanitize(destination);
        this.departure = sanitize(departure);
        this.arrival = sanitize(arrival);
        this.fare = Math.max(0.0, fare);
        this.totalSeats = (totalSeats >= 1 && totalSeats <= 60) ? totalSeats : 24;
        this.bookedSeats = new ArrayList<>();
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replace("|", " ").trim();
    }

    public synchronized int availableSeats() {
        return totalSeats - bookedSeats.size();
    }

    public synchronized boolean isBooked(int seatNo) {
        return bookedSeats.contains(seatNo);
    }

    public synchronized boolean bookSeat(int seatNo) {
        if (seatNo < 1 || seatNo > totalSeats) {
            return false;
        }
        if (bookedSeats.contains(seatNo)) {
            return false; // Concurrency check: double booking prevention
        }
        bookedSeats.add(seatNo);
        return true;
    }

    public synchronized boolean cancelSeat(int seatNo) {
        return bookedSeats.remove(Integer.valueOf(seatNo));
    }

    public String getBusId() { return busId; }
    public String getBusName() { return busName; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDeparture() { return departure; }
    public String getArrival() { return arrival; }
    public double getFare() { return fare; }
    public int getTotalSeats() { return totalSeats; }
    public List<Integer> getBookedSeats() { return new ArrayList<>(bookedSeats); }

    public void setBookedSeats(List<Integer> seats) {
        this.bookedSeats = (seats != null) ? new ArrayList<>(seats) : new ArrayList<>();
    }

    /**
     * Renders a 2x2 cabin seat diagram in ASCII with an aisle in the middle.
     */
    public void printSeatMap() {
        System.out.println("\n  +=========================================+");
        System.out.println("  |      FRONT (DRIVER'S CABIN [STEER])     |");
        System.out.println("  +=========================================+");
        
        int rows = (int) Math.ceil((double) totalSeats / 4);
        for (int r = 0; r < rows; r++) {
            int s1 = r * 4 + 1;
            int s2 = r * 4 + 2;
            int s3 = r * 4 + 3;
            int s4 = r * 4 + 4;

            String c1 = formatSeat(s1);
            String c2 = formatSeat(s2);
            String c3 = formatSeat(s3);
            String c4 = formatSeat(s4);

            System.out.printf("  |  %s  %s    [AISLE]    %s  %s  |\n", c1, c2, c3, c4);
        }
        System.out.println("  +=========================================+");
        System.out.println("  Legend: [ 07 ] = Available | [ XX ] = Booked\n");
    }

    private String formatSeat(int seatNo) {
        if (seatNo > totalSeats) {
            return "      ";
        }
        if (isBooked(seatNo)) {
            return "[ XX ]";
        }
        return String.format("[%02d  ]", seatNo);
    }

    /**
     * Serializes bus entry for file persistence (buses.txt)
     */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(busId).append("|")
          .append(busName).append("|")
          .append(source).append("|")
          .append(destination).append("|")
          .append(departure).append("|")
          .append(arrival).append("|")
          .append(fare).append("|")
          .append(totalSeats).append("|");

        for (int i = 0; i < bookedSeats.size(); i++) {
            sb.append(bookedSeats.get(i));
            if (i < bookedSeats.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Deserializes bus record from line in buses.txt
     */
    public static Bus fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length < 8) return null;

        String id = parts[0];
        String name = parts[1];
        String src = parts[2];
        String dest = parts[3];
        String dep = parts[4];
        String arr = parts[5];
        double fare = Double.parseDouble(parts[6]);
        int seats = Integer.parseInt(parts[7]);

        Bus bus = new Bus(id, name, src, dest, dep, arr, fare, seats);
        if (parts.length >= 9 && !parts[8].trim().isEmpty()) {
            String[] seatTokens = parts[8].split(",");
            List<Integer> booked = new ArrayList<>();
            for (String t : seatTokens) {
                try {
                    booked.add(Integer.parseInt(t.trim()));
                } catch (NumberFormatException ignored) {}
            }
            bus.setBookedSeats(booked);
        }
        return bus;
    }
}
