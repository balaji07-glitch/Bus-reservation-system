-- ====================================================================
-- BusGo Online Bus Reservation System - MySQL Database Schema
-- Database: busgo_db
-- Compatibility: MySQL 5.7+ / MySQL 8.0+ / MariaDB 10.3+
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `busgo_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `busgo_db`;

-- --------------------------------------------------------------------
-- 1. Table: users (Passenger & Staff Accounts)
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `mobile` VARCHAR(15) NOT NULL UNIQUE,
    `email` VARCHAR(100) DEFAULT NULL,
    `age` INT DEFAULT 25,
    `gender` ENUM('Male', 'Female', 'Other') DEFAULT 'Male',
    `password_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('PASSENGER', 'ADMIN') DEFAULT 'PASSENGER',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 2. Table: buses (Bus Fleet, Routes, Timings & Capacity)
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `buses`;
CREATE TABLE `buses` (
    `bus_id` VARCHAR(20) PRIMARY KEY,
    `bus_name` VARCHAR(100) NOT NULL,
    `source_city` VARCHAR(50) NOT NULL,
    `destination_city` VARCHAR(50) NOT NULL,
    `departure_time` VARCHAR(10) NOT NULL,
    `arrival_time` VARCHAR(10) NOT NULL,
    `fare` DECIMAL(10, 2) NOT NULL,
    `total_seats` INT NOT NULL DEFAULT 24,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 3. Table: tickets (Confirmed Bookings with 90% Refund Tracking)
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets` (
    `ticket_id` VARCHAR(20) PRIMARY KEY,
    `bus_id` VARCHAR(20) NOT NULL,
    `seat_no` INT NOT NULL,
    `journey_date` DATE NOT NULL,
    `passenger_name` VARCHAR(100) NOT NULL,
    `passenger_age` INT NOT NULL,
    `passenger_gender` VARCHAR(10) NOT NULL,
    `passenger_mobile` VARCHAR(15) NOT NULL,
    `passenger_email` VARCHAR(100) DEFAULT 'balaji@example.com',
    `fare_paid` DECIMAL(10, 2) NOT NULL,
    `status` ENUM('CONFIRMED', 'CANCELLED') DEFAULT 'CONFIRMED',
    `refund_amount` DECIMAL(10, 2) DEFAULT 0.00,
    `booked_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ticket_bus` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`bus_id`) ON DELETE CASCADE,
    CONSTRAINT `unique_bus_seat_date` UNIQUE (`bus_id`, `seat_no`, `journey_date`, `status`)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 4. Table: passenger_feedback (Passenger-to-Passenger Reviews & Tips)
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `passenger_feedback`;
CREATE TABLE `passenger_feedback` (
    `feedback_id` INT AUTO_INCREMENT PRIMARY KEY,
    `bus_id` VARCHAR(20) NOT NULL,
    `passenger_name` VARCHAR(100) NOT NULL,
    `passenger_email` VARCHAR(100) DEFAULT NULL,
    `ticket_id` VARCHAR(20) DEFAULT NULL,
    `rating` INT NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
    `punctuality` INT DEFAULT 5,
    `cleanliness` INT DEFAULT 5,
    `comfort` INT DEFAULT 5,
    `advice_for_passengers` TEXT DEFAULT NULL,
    `comment` TEXT NOT NULL,
    `is_verified_passenger` BOOLEAN DEFAULT TRUE,
    `helpful_votes` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_feedback_bus` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`bus_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- 5. Initial Seed Data
-- --------------------------------------------------------------------

-- Seed Default Admin & Sample Passenger (Password: admin123 & user123)
INSERT INTO `users` (`full_name`, `mobile`, `email`, `age`, `gender`, `password_hash`, `role`) VALUES
('System Administrator', '9999999999', 'admin@busgo.com', 35, 'Male', 'admin123', 'ADMIN'),
('Balaji S', '9876543210', 'balaji07052757@gmail.com', 22, 'Male', 'user123', 'PASSENGER');

-- Seed Sample Buses (From college mini-project spec)
INSERT INTO `buses` (`bus_id`, `bus_name`, `source_city`, `destination_city`, `departure_time`, `arrival_time`, `fare`, `total_seats`) VALUES
('B101', 'SRM Express', 'Trichy', 'Chennai', '06:00', '11:00', 450.00, 24),
('B102', 'Cauvery Travels', 'Trichy', 'Madurai', '09:30', '12:30', 250.00, 20),
('B103', 'Marina Deluxe', 'Chennai', 'Coimbatore', '21:00', '05:00', 700.00, 28);

-- Seed Sample Confirmed Ticket
INSERT INTO `tickets` (`ticket_id`, `bus_id`, `seat_no`, `journey_date`, `passenger_name`, `passenger_age`, `passenger_gender`, `passenger_mobile`, `passenger_email`, `fare_paid`, `status`) VALUES
('TKT1001', 'B101', 7, '2026-10-27', 'Balaji S', 22, 'Male', '9876543210', 'balaji07052757@gmail.com', 450.00, 'CONFIRMED');

-- Seed Passenger to Passenger Feedback
INSERT INTO `passenger_feedback` (`bus_id`, `passenger_name`, `passenger_email`, `ticket_id`, `rating`, `punctuality`, `cleanliness`, `comfort`, `advice_for_passengers`, `comment`, `is_verified_passenger`, `helpful_votes`) VALUES
('B101', 'Balaji S', 'balaji***@gmail.com', 'TKT1001', 5, 5, 5, 5, 'Choose Seats 7 or 8 for extra legroom near middle window; charging socket works great.', 'Very smooth ride from Trichy Central bus stand to Chennai Koyambedu. Bus departed exactly at 06:00 AM.', TRUE, 12),
('B102', 'Priya K', 'priya***@gmail.com', 'TKT1004', 4, 4, 5, 4, 'Carry a light shawl as the AC gets quite chill near the front rows.', 'Cauvery Travels is punctual and driver was very courteous. Dropped us right at Madurai Periyar stand.', TRUE, 8),
('B103', 'Arun Kumar', 'arun***@gmail.com', 'TKT1009', 5, 5, 4, 5, 'Great overnight sleeper option; clean bedsheets and peaceful night.', 'Reached Gandhipuram Coimbatore 15 minutes ahead of schedule! Will book again.', TRUE, 15);
