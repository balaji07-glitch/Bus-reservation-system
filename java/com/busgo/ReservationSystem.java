package com.busgo;

import java.io.*;
import java.util.*;

/**
 * Controller service managing bus inventories, ticket reservations,
 * persistent file I/O (buses.txt and tickets.txt), and cancellation logic.
 */
public class ReservationSystem {
    private static final String BUSES_FILE = "buses.txt";
    private static final String TICKETS_FILE = "tickets.txt";

    private List<Bus> buses;
    private List<Ticket> tickets;

    public ReservationSystem() {
        this.buses = new ArrayList<>();
        this.tickets = new ArrayList<>();
        loadData();
    }

    public List<Bus> getBuses() {
        return buses;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public Bus findBusById(String busId) {
        if (busId == null) return null;
        for (Bus b : buses) {
            if (b.getBusId().equalsIgnoreCase(busId.trim())) {
                return b;
            }
        }
        return null;
    }

    public Ticket findTicketById(String ticketId) {
        if (ticketId == null) return null;
        for (Ticket t : tickets) {
            if (t.getTicketId().equalsIgnoreCase(ticketId.trim())) {
                return t;
            }
        }
        return null;
    }

    public List<Bus> searchBuses(String from, String to) {
        List<Bus> results = new ArrayList<>();
        String srcQuery = (from != null) ? from.trim().toLowerCase() : "";
        String dstQuery = (to != null) ? to.trim().toLowerCase() : "";

        for (Bus b : buses) {
            boolean matchSrc = srcQuery.isEmpty() || b.getSource().toLowerCase().contains(srcQuery);
            boolean matchDst = dstQuery.isEmpty() || b.getDestination().toLowerCase().contains(dstQuery);
            if (matchSrc && matchDst) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Thread-safe Ticket Booking with Re-Check Availability
     */
    public synchronized Ticket bookTicket(String busId, int seatNo, Passenger passenger, String date) throws Exception {
        Bus bus = findBusById(busId);
        if (bus == null) {
            throw new IllegalArgumentException("Bus not found with ID: " + busId);
        }

        // Re-check availability right at the moment of booking
        if (bus.isBooked(seatNo)) {
            throw new IllegalStateException("Seat " + seatNo + " has already been booked. Please choose another seat.");
        }

        boolean success = bus.bookSeat(seatNo);
        if (!success) {
            throw new IllegalStateException("Could not allocate Seat " + seatNo);
        }

        String nextId = generateTicketId();
        Ticket ticket = new Ticket(nextId, bus.getBusId(), seatNo, passenger, bus.getFare(), date);
        tickets.add(ticket);

        saveData();
        return ticket;
    }

    /**
     * Cancels ticket, computes 90% refund, frees seat, and updates file storage.
     */
    public synchronized double cancelTicket(String ticketId) throws Exception {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket ID not found: " + ticketId);
        }

        Bus bus = findBusById(ticket.getBusId());
        if (bus != null) {
            bus.cancelSeat(ticket.getSeatNo());
        }

        double refundAmount = ticket.calculateRefund();
        tickets.remove(ticket);

        saveData();
        return refundAmount;
    }

    /**
     * Adds a new bus with unique ID verification.
     */
    public synchronized boolean addBus(Bus bus) {
        if (findBusById(bus.getBusId()) != null) {
            return false;
        }
        buses.add(bus);
        saveData();
        return true;
    }

    /**
     * Deletes bus if no active tickets exist.
     */
    public synchronized boolean deleteBus(String busId) {
        Bus bus = findBusById(busId);
        if (bus == null) return false;

        // Verify active bookings
        for (Ticket t : tickets) {
            if (t.getBusId().equalsIgnoreCase(busId)) {
                return false; // Active bookings exist
            }
        }

        buses.remove(bus);
        saveData();
        return true;
    }

    private String generateTicketId() {
        int maxId = 1000;
        for (Ticket t : tickets) {
            try {
                String numStr = t.getTicketId().replaceAll("[^0-9]", "");
                int val = Integer.parseInt(numStr);
                if (val > maxId) {
                    maxId = val;
                }
            } catch (Exception ignored) {}
        }
        return "TKT" + (maxId + 1);
    }

    /**
     * Loads persistent data from buses.txt and tickets.txt
     */
    public void loadData() {
        buses.clear();
        tickets.clear();

        File bFile = new File(BUSES_FILE);
        if (bFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(bFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Bus b = Bus.fromFileString(line);
                    if (b != null) buses.add(b);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read " + BUSES_FILE + ": " + e.getMessage());
            }
        }

        // If file was empty or not found, seed initial buses
        if (buses.isEmpty()) {
            seedDefaultBuses();
        }

        File tFile = new File(TICKETS_FILE);
        if (tFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(tFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Ticket t = Ticket.fromFileString(line);
                    if (t != null) {
                        tickets.add(t);
                        // Ensure bus booked list reflects loaded tickets
                        Bus bus = findBusById(t.getBusId());
                        if (bus != null && !bus.isBooked(t.getSeatNo())) {
                            bus.bookSeat(t.getSeatNo());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read " + TICKETS_FILE + ": " + e.getMessage());
            }
        }
    }

    /**
     * Saves persistent data to buses.txt and tickets.txt
     */
    public synchronized void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BUSES_FILE))) {
            for (Bus b : buses) {
                pw.println(b.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving buses: " + e.getMessage());
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(TICKETS_FILE))) {
            for (Ticket t : tickets) {
                pw.println(t.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving tickets: " + e.getMessage());
        }
    }

    public void seedDefaultBuses() {
        buses.clear();
        buses.add(new Bus("B101", "SRM Express", "Trichy", "Chennai", "06:00", "11:00", 450.0, 24));
        buses.add(new Bus("B102", "Cauvery Travels", "Trichy", "Madurai", "09:30", "12:30", 250.0, 20));
        buses.add(new Bus("B103", "Marina Deluxe", "Chennai", "Coimbatore", "21:00", "05:00", 700.0, 28));
        saveData();
    }
}
