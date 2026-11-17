package com.busgo.ui;

import com.busgo.Bus;
import com.busgo.Passenger;
import com.busgo.Ticket;
import com.busgo.dao.BusDAO;
import com.busgo.dao.TicketDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * BusGo Java GUI Frontend built with Java Swing (AWT/Swing Framework).
 * Provides full visual desktop interface:
 * - Route searching & bus cards
 * - 2x2 Cabin seat layout with interactive graphical seat buttons
 * - Passenger details collection & booking confirmation
 * - Ticket lookup and 90% refund cancellation
 * - Fleet admin portal
 */
public class BusGoGUI extends JFrame {
    private final BusDAO busDAO = new BusDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    private JTabbedPane tabbedPane;
    private JTable busTable;
    private DefaultTableModel busTableModel;
    private JPanel seatMatrixPanel;
    private Bus currentSelectedBus = null;
    private int selectedSeatNumber = -1;

    // Booking form fields
    private JTextField nameField, ageField, mobileField, dateField;
    private JComboBox<String> genderBox;

    public BusGoGUI() {
        setTitle("BusGo – Java Bus Reservation System (Full-Stack Java Swing & MySQL)");
        setSize(1050, 720);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Theme customization
        UIManager.put("TabbedPane.selected", new Color(14, 116, 144));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("BusGo – Java Desktop Frontend");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(249, 115, 22));

        JLabel subLabel = new JLabel("Powered by Java Swing (GUI Frontend) + Java JDBC Backend + MySQL Database");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(new Color(148, 163, 184));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Tabbed Pane for Pages
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("1. Browse & Search Buses", createBusSearchTab());
        tabbedPane.addTab("2. 2x2 Cabin Seat Matrix & Booking", createBookingTab());
        tabbedPane.addTab("3. My Tickets & Cancellation", createTicketManageTab());
        tabbedPane.addTab("4. Admin Fleet Management", createAdminTab());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createBusSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JTextField fromField = new JTextField(12);
        JTextField toField = new JTextField(12);
        JButton searchBtn = new JButton("Search Routes");
        JButton showAllBtn = new JButton("Show All Buses");

        searchBtn.setBackground(new Color(234, 88, 12));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);

        searchBar.add(new JLabel("From:"));
        searchBar.add(fromField);
        searchBar.add(new JLabel("To:"));
        searchBar.add(toField);
        searchBar.add(searchBtn);
        searchBar.add(showAllBtn);

        // Buses Table
        String[] cols = {"Bus ID", "Bus Name", "Route", "Departure", "Arrival", "Fare (Rs)", "Available Seats", "Action"};
        busTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        busTable = new JTable(busTableModel);
        busTable.setRowHeight(32);
        busTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        busTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        busTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(busTable);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bookSelectedBtn = new JButton("Select Bus & Choose 2x2 Seats ->");
        bookSelectedBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        bookSelectedBtn.setBackground(new Color(13, 148, 136));
        bookSelectedBtn.setForeground(Color.WHITE);
        bookSelectedBtn.setFocusPainted(false);
        bottomBar.add(bookSelectedBtn);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);

        // Load Initial Data
        refreshBusTable(busDAO.getAllBuses());

        searchBtn.addActionListener(e -> {
            List<Bus> results = busDAO.searchBuses(fromField.getText(), toField.getText());
            refreshBusTable(results);
        });

        showAllBtn.addActionListener(e -> {
            fromField.setText("");
            toField.setText("");
            refreshBusTable(busDAO.getAllBuses());
        });

        bookSelectedBtn.addActionListener(e -> {
            int selectedRow = busTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a bus from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String busId = (String) busTableModel.getValueAt(selectedRow, 0);
            selectBusForBooking(busId);
        });

        return panel;
    }

    private void refreshBusTable(List<Bus> list) {
        busTableModel.setRowCount(0);
        for (Bus b : list) {
            busTableModel.addRow(new Object[]{
                    b.getBusId(),
                    b.getBusName(),
                    b.getSource() + " -> " + b.getDestination(),
                    b.getDeparture(),
                    b.getArrival(),
                    String.format("%.2f", b.getFare()),
                    b.availableSeats() + " / " + b.getTotalSeats(),
                    "Select"
            });
        }
    }

    private void selectBusForBooking(String busId) {
        currentSelectedBus = busDAO.getBusById(busId);
        if (currentSelectedBus == null) {
            JOptionPane.showMessageDialog(this, "Could not load details for bus " + busId);
            return;
        }
        tabbedPane.setSelectedIndex(1);
        renderSeatMatrix(currentSelectedBus);
    }

    private JPanel createBookingTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Side: 2x2 Seat Layout Grid
        JPanel leftContainer = new JPanel(new BorderLayout(5, 5));
        leftContainer.setBorder(BorderFactory.createTitledBorder("2x2 Cabin Seat Matrix (Click Seat to Reserve)"));
        leftContainer.setPreferredSize(new Dimension(460, 500));

        seatMatrixPanel = new JPanel();
        seatMatrixPanel.setLayout(new GridLayout(0, 5, 8, 8)); // 2 seats + aisle + 2 seats
        seatMatrixPanel.setBackground(Color.WHITE);

        JScrollPane seatScroll = new JScrollPane(seatMatrixPanel);
        leftContainer.add(seatScroll, BorderLayout.CENTER);

        // Right Side: Passenger Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Passenger & Booking Details"));

        nameField = new JTextField();
        ageField = new JTextField("25");
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        mobileField = new JTextField();
        dateField = new JTextField("2026-10-27");

        JButton confirmBtn = new JButton("Confirm Reservation in MySQL");
        confirmBtn.setBackground(new Color(234, 88, 12));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmBtn.setFocusPainted(false);

        addFormField(formPanel, "Full Legal Name *:", nameField);
        addFormField(formPanel, "Age (1-120):", ageField);
        addFormField(formPanel, "Gender:", genderBox);
        addFormField(formPanel, "Mobile (10 Digits) *:", mobileField);
        addFormField(formPanel, "Journey Date (YYYY-MM-DD):", dateField);

        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(confirmBtn);

        confirmBtn.addActionListener(this::handleBookingSubmit);

        panel.add(leftContainer, BorderLayout.WEST);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addFormField(JPanel parent, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        parent.add(label);
        parent.add(Box.createVerticalStrut(3));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private void renderSeatMatrix(Bus bus) {
        seatMatrixPanel.removeAll();
        selectedSeatNumber = -1;

        int total = bus.getTotalSeats();
        int rows = (int) Math.ceil((double) total / 4);

        for (int r = 0; r < rows; r++) {
            int s1 = r * 4 + 1;
            int s2 = r * 4 + 2;
            int s3 = r * 4 + 3;
            int s4 = r * 4 + 4;

            seatMatrixPanel.add(createSeatButton(bus, s1));
            seatMatrixPanel.add(createSeatButton(bus, s2));

            // Aisle spacer
            JLabel aisle = new JLabel("||", SwingConstants.CENTER);
            aisle.setForeground(new Color(203, 213, 225));
            seatMatrixPanel.add(aisle);

            seatMatrixPanel.add(createSeatButton(bus, s3));
            seatMatrixPanel.add(createSeatButton(bus, s4));
        }

        seatMatrixPanel.revalidate();
        seatMatrixPanel.repaint();
    }

    private JButton createSeatButton(Bus bus, int seatNo) {
        if (seatNo > bus.getTotalSeats()) {
            JButton blank = new JButton("");
            blank.setEnabled(false);
            blank.setVisible(false);
            return blank;
        }

        boolean booked = bus.isBooked(seatNo);
        JButton btn = new JButton(booked ? "XX" : String.valueOf(seatNo));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));

        if (booked) {
            btn.setBackground(new Color(226, 232, 240));
            btn.setForeground(new Color(148, 163, 184));
            btn.setEnabled(false);
        } else {
            btn.setBackground(new Color(220, 252, 231));
            btn.setForeground(new Color(22, 101, 52));
            btn.addActionListener(e -> {
                selectedSeatNumber = seatNo;
                JOptionPane.showMessageDialog(this, "Selected Seat #" + seatNo + " on " + bus.getBusName() + ". Fill passenger details to confirm.");
            });
        }
        return btn;
    }

    private void handleBookingSubmit(ActionEvent e) {
        if (currentSelectedBus == null) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the first tab first.");
            return;
        }
        if (selectedSeatNumber <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an available seat from the 2x2 layout.");
            return;
        }

        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String date = dateField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Passenger name is required.");
            return;
        }
        if (!mobile.matches("^[0-9]{10}$")) {
            JOptionPane.showMessageDialog(this, "Mobile number must be exactly 10 digits.");
            return;
        }

        int age = 25;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (Exception ignored) {}

        Passenger passenger = new Passenger(name, age, (String) genderBox.getSelectedItem(), mobile);

        try {
            Ticket ticket = ticketDAO.bookTicket(currentSelectedBus.getBusId(), selectedSeatNumber, passenger, currentSelectedBus.getFare(), date);
            JOptionPane.showMessageDialog(this,
                    "SUCCESS! Ticket " + ticket.getTicketId() + " generated!\n" +
                            "Bus: " + currentSelectedBus.getBusName() + " (Seat " + selectedSeatNumber + ")\n" +
                            "Fare: Rs. " + currentSelectedBus.getFare() + "\nSaved in MySQL Database table 'tickets'.",
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            // Reload and refresh
            selectBusForBooking(currentSelectedBus.getBusId());
            refreshBusTable(busDAO.getAllBuses());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTicketManageTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField idField = new JTextField(15);
        JButton searchTicketBtn = new JButton("Find Ticket");
        JButton cancelTicketBtn = new JButton("Cancel Ticket (90% Refund)");
        cancelTicketBtn.setBackground(new Color(220, 38, 38));
        cancelTicketBtn.setForeground(Color.WHITE);

        searchBox.add(new JLabel("Enter Ticket ID:"));
        searchBox.add(idField);
        searchBox.add(searchTicketBtn);
        searchBox.add(cancelTicketBtn);

        JTextArea ticketDisplayArea = new JTextArea();
        ticketDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ticketDisplayArea.setEditable(false);
        ticketDisplayArea.setBackground(new Color(248, 250, 252));
        ticketDisplayArea.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        panel.add(searchBox, BorderLayout.NORTH);
        panel.add(new JScrollPane(ticketDisplayArea), BorderLayout.CENTER);

        searchTicketBtn.addActionListener(e -> {
            String tId = idField.getText().trim();
            Ticket t = ticketDAO.getTicketById(tId);
            if (t == null) {
                ticketDisplayArea.setText("No ticket found with ID: " + tId);
                return;
            }
            Bus b = busDAO.getBusById(t.getBusId());
            ticketDisplayArea.setText(
                    "===========================================================\n" +
                    "                     BUSGO E-TICKET CARD                  \n" +
                    "===========================================================\n" +
                    " Ticket ID      : " + t.getTicketId() + "\n" +
                    " Passenger Name : " + t.getPassenger().getName() + " (" + t.getPassenger().getAge() + " yrs / " + t.getPassenger().getGender() + ")\n" +
                    " Mobile Number  : " + t.getPassenger().getMobile() + "\n" +
                    " Journey Date   : " + t.getDate() + "\n" +
                    " Bus Name / ID  : " + (b != null ? b.getBusName() + " (" + b.getBusId() + ")" : t.getBusId()) + "\n" +
                    " Route          : " + (b != null ? b.getSource() + " -> " + b.getDestination() : "N/A") + "\n" +
                    " Seat Number    : Seat #" + t.getSeatNo() + "\n" +
                    " Total Fare     : Rs. " + String.format("%.2f", t.getFare()) + "\n" +
                    " Status         : CONFIRMED in MySQL Database\n" +
                    " Cancellation   : 90% Refund (Rs. " + String.format("%.2f", t.calculateRefund()) + ")\n" +
                    "===========================================================\n"
            );
        });

        cancelTicketBtn.addActionListener(e -> {
            String tId = idField.getText().trim();
            Ticket t = ticketDAO.getTicketById(tId);
            if (t == null) {
                JOptionPane.showMessageDialog(this, "Ticket not found: " + tId);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel Ticket " + tId + "?\n" +
                            "Paid Fare: Rs. " + t.getFare() + "\n" +
                            "90% Refund Credit: Rs. " + t.calculateRefund(),
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    double refund = ticketDAO.cancelTicket(tId);
                    JOptionPane.showMessageDialog(this, "Ticket cancelled! Refund of Rs. " + refund + " processed. Seat is now available.");
                    refreshBusTable(busDAO.getAllBuses());
                    ticketDisplayArea.setText("Ticket " + tId + " has been CANCELLED. Refund of Rs. " + refund + " credited.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private JPanel createAdminTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridLayout(4, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Add New Bus Route to MySQL Database"));

        JTextField idF = new JTextField();
        JTextField nameF = new JTextField();
        JTextField srcF = new JTextField();
        JTextField dstF = new JTextField();
        JTextField depF = new JTextField("08:00");
        JTextField arrF = new JTextField("14:00");
        JTextField fareF = new JTextField("500.0");
        JTextField seatF = new JTextField("24");

        form.add(new JLabel("Bus ID (e.g. B104):")); form.add(idF);
        form.add(new JLabel("Bus Name:")); form.add(nameF);
        form.add(new JLabel("Source City:")); form.add(srcF);
        form.add(new JLabel("Destination City:")); form.add(dstF);
        form.add(new JLabel("Departure (HH:MM):")); form.add(depF);
        form.add(new JLabel("Arrival (HH:MM):")); form.add(arrF);
        form.add(new JLabel("Fare (Rs):")); form.add(fareF);
        form.add(new JLabel("Total Seats:")); form.add(seatF);

        JButton addBtn = new JButton("Save Bus to MySQL");
        addBtn.setBackground(new Color(13, 148, 136));
        addBtn.setForeground(Color.WHITE);

        panel.add(form, BorderLayout.NORTH);
        panel.add(addBtn, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                Bus b = new Bus(
                        idF.getText().trim(),
                        nameF.getText().trim(),
                        srcF.getText().trim(),
                        dstF.getText().trim(),
                        depF.getText().trim(),
                        arrF.getText().trim(),
                        Double.parseDouble(fareF.getText().trim()),
                        Integer.parseInt(seatF.getText().trim())
                );
                if (busDAO.addBus(b)) {
                    JOptionPane.showMessageDialog(this, "Bus " + b.getBusId() + " successfully added to MySQL database!");
                    refreshBusTable(busDAO.getAllBuses());
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add bus. Check if ID already exists.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Input Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BusGoGUI gui = new BusGoGUI();
            gui.setVisible(true);
        });
    }
}
