package com.busgo;

import com.busgo.dao.BusDAO;
import com.busgo.dao.TicketDAO;
import com.busgo.db.DBConnection;
import com.busgo.ui.BusGoGUI;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

/**
 * Main application launcher for BusGo.
 * Supports:
 * 1. Full Java Swing Graphical Desktop Frontend (BusGoGUI)
 * 2. Java Command-Line Console Interface
 * 3. MySQL Database Integration (busgo_db)
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BusDAO busDAO = new BusDAO();
    private static final TicketDAO ticketDAO = new TicketDAO();

    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("      BUSGO - FULL-STACK JAVA & MYSQL RESERVATION SYSTEM     ");
        System.out.println("=============================================================");
        System.out.println("Choose how you would like to run the application:");
        System.out.println("  1. Launch Java Swing Desktop Frontend (GUI Window)");
        System.out.println("  2. Run Java Console Command-Line Interface");
        System.out.println("  3. Test MySQL Database Connectivity (busgo_db)");
        System.out.print("Enter choice (1, 2, or 3) [Default: 1]: ");

        String choice = scanner.nextLine().trim();
        if (choice.equals("2")) {
            runConsoleApp();
        } else if (choice.equals("3")) {
            testMySQLConnection();
            runConsoleApp();
        } else {
            System.out.println("Starting Java Swing Frontend GUI...");
            try {
                SwingUtilities.invokeLater(() -> {
                    BusGoGUI gui = new BusGoGUI();
                    gui.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("Note: Headless environment detected or GUI failed to start. Falling back to Console.");
                runConsoleApp();
            }
        }
    }

    private static void testMySQLConnection() {
        System.out.print("\nConnecting to MySQL Database 'busgo_db' via JDBC... ");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("[SUCCESS] Connected to MySQL successfully!");
            }
        } catch (Exception e) {
            System.out.println("\n[NOTICE] MySQL server connection status: " + e.getMessage());
            System.out.println("Make sure MySQL is running on localhost:3306 with database 'busgo_db' created via schema.sql.");
        }
    }

    private static void runConsoleApp() {
        boolean running = true;
        while (running) {
            System.out.println("\n=======================================================");
            System.out.println("           BUSGO JAVA & MYSQL CONSOLE MENU             ");
            System.out.println("=======================================================");
            System.out.println("  1. Search & View Available Buses (from MySQL)");
            System.out.println("  2. Book Bus Seats (Interactive 2x2 Seat Map)");
            System.out.println("  3. View / Print Ticket by ID");
            System.out.println("  4. Cancel Ticket (90% Instant Refund in MySQL)");
            System.out.println("  5. Launch Java Swing Desktop GUI Window");
            System.out.println("  6. Exit");
            System.out.print("Select an option (1-6): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    handleSearchBuses();
                    break;
                case "2":
                    handleBooking();
                    break;
                case "3":
                    handleViewTicket();
                    break;
                case "4":
                    handleCancellation();
                    break;
                case "5":
                    SwingUtilities.invokeLater(() -> new BusGoGUI().setVisible(true));
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting BusGo. Thank you!");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose 1-6.");
            }
        }
    }

    private static void handleSearchBuses() {
        System.out.print("Enter Origin City (or ENTER for all): ");
        String from = scanner.nextLine().trim();
        System.out.print("Enter Destination City (or ENTER for all): ");
        String to = scanner.nextLine().trim();

        List<Bus> results = busDAO.searchBuses(from, to);
        if (results.isEmpty()) {
            System.out.println("No buses found in MySQL matching: " + from + " -> " + to);
            return;
        }

        System.out.println("\nFound " + results.size() + " bus(es) in MySQL database:");
        System.out.printf("%-8s | %-16s | %-18s | %-12s | %-10s | %s\n",
                "Bus ID", "Bus Name", "Route", "Timings", "Fare", "Available Seats");
        System.out.println("-----------------------------------------------------------------------------------");
        for (Bus b : results) {
            System.out.printf("%-8s | %-16s | %-18s | %-12s | Rs. %-6.2f | %d / %d seats left\n",
                    b.getBusId(),
                    b.getBusName(),
                    b.getSource() + " -> " + b.getDestination(),
                    b.getDeparture() + "-" + b.getArrival(),
                    b.getFare(),
                    b.availableSeats(),
                    b.getTotalSeats());
        }
    }

    private static void handleBooking() {
        System.out.print("Enter Bus ID (e.g. B101): ");
        String busId = scanner.nextLine().trim();

        Bus bus = busDAO.getBusById(busId);
        if (bus == null) {
            System.out.println("Bus ID not found in MySQL.");
            return;
        }

        bus.printSeatMap();

        System.out.print("Enter Seat Number to book (1-" + bus.getTotalSeats() + "): ");
        int seatNo = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Passenger Full Legal Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Passenger Age (1-120): ");
        int age = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Gender: ");
        String gender = scanner.nextLine().trim();
        System.out.print("Mobile Number (10 digits): ");
        String mobile = scanner.nextLine().trim();

        Passenger p = new Passenger(name, age, gender, mobile);
        try {
            Ticket t = ticketDAO.bookTicket(bus.getBusId(), seatNo, p, bus.getFare(), "2026-10-27");
            System.out.println("\n>> SUCCESS: Reserved in MySQL Database!");
            t.printTicketCard(bus);
        } catch (Exception e) {
            System.out.println(">> Booking failed: " + e.getMessage());
        }
    }

    private static void handleViewTicket() {
        System.out.print("Enter Ticket ID (e.g. TKT1001): ");
        String tId = scanner.nextLine().trim();
        Ticket t = ticketDAO.getTicketById(tId);
        if (t == null) {
            System.out.println("No ticket found in MySQL with ID: " + tId);
            return;
        }
        Bus b = busDAO.getBusById(t.getBusId());
        t.printTicketCard(b);
    }

    private static void handleCancellation() {
        System.out.print("Enter Ticket ID to Cancel: ");
        String tId = scanner.nextLine().trim();
        try {
            double refund = ticketDAO.cancelTicket(tId);
            System.out.printf(">> SUCCESS: Ticket %s cancelled in MySQL. 90%% Refund amount: Rs. %.2f\n", tId, refund);
        } catch (Exception e) {
            System.out.println(">> Cancellation failed: " + e.getMessage());
        }
    }
}
