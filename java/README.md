# BusGo - Online Bus Reservation System (Java OOP Project)

## Project Overview
BusGo is a full Object-Oriented Programming (OOP) reservation system designed for college mini-projects and lab evaluations. It manages bus inventories, 2x2 cabin seat maps, traveler bookings, ticket generation, cancellations with a 90% refund policy, and administrative fleet operations.

## OOP Concepts Demonstrated
1. **Encapsulation**: Private member variables with validated getters and setters across `Passenger`, `Bus`, and `Ticket`.
2. **Data Abstraction**: Complex seat matrix and availability calculation abstracted behind clear interfaces (`availableSeats()`, `bookSeat()`, `cancelSeat()`).
3. **Collections Framework**: Dynamic in-memory management using `java.util.List` and `java.util.ArrayList`.
4. **Exception Handling**: Robust input validation and custom error handling using `try-catch` blocks for seat collisions and invalid formats.
5. **File Handling (Persistence)**: File I/O (`BufferedReader`, `PrintWriter`, `FileReader`, `FileWriter`) reading and writing records to `buses.txt` and `tickets.txt` separated by `|`.
6. **Business Logic & Concurrency**: Atomic seat booking check prevents double bookings; guaranteed 90% refund calculation on ticket cancellation.

---

## File Structure
```
java/
├── com/
│   └── busgo/
│       ├── Bus.java                # Bus entity with 2x2 cabin layout & booking logic
│       ├── Passenger.java          # Traveler entity with mobile & age validation
│       ├── Ticket.java             # Ticket entity with 90% refund formula & ASCII card
│       ├── ReservationSystem.java  # Persistent controller managing file I/O
│       └── Main.java               # Interactive console application menu
├── buses.txt                       # Bus route persistence file
├── tickets.txt                     # Confirmed booking records file
└── README.md                       # Setup and execution guide
```

---

## How to Compile and Run

### 1. Open Terminal or Command Prompt
Navigate to the `java` folder:
```bash
cd java
```

### 2. Compile the Java Source Files
```bash
javac com/busgo/*.java
```

### 3. Run the Application
```bash
java com.busgo.Main
```

---

## Admin Credentials
- **Menu Option**: `5. Fleet Administrator Portal`
- **Demo Password**: `admin123`
