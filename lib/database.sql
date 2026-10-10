-- Bus Reservation System Database Schema
CREATE DATABASE IF NOT EXISTS bus_reservation;
USE bus_reservation;

-- Table: passenger
CREATE TABLE IF NOT EXISTS passenger (
    passenger_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    PRIMARY KEY (passenger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: bus
CREATE TABLE IF NOT EXISTS bus (
    bus_id INT NOT NULL,
    bus_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    fare DOUBLE NOT NULL DEFAULT 0.0,
    PRIMARY KEY (bus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: booking
CREATE TABLE IF NOT EXISTS booking (
    booking_id INT NOT NULL AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    bus_id INT NOT NULL,
    seat_number INT NOT NULL,
    booking_date VARCHAR(50) NOT NULL,
    fare DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    PRIMARY KEY (booking_id),
    KEY fk_passenger (passenger_id),
    KEY fk_bus (bus_id),
    CONSTRAINT fk_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE,
    CONSTRAINT fk_bus FOREIGN KEY (bus_id) REFERENCES bus (bus_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Seed Data
INSERT IGNORE INTO passenger (passenger_id, name, age, gender, phone) VALUES
(101, 'John Doe', 28, 'Male', '9876543210'),
(102, 'Sarah Smith', 24, 'Female', '9123456780'),
(103, 'Michael Brown', 35, 'Male', '9988776655');

INSERT IGNORE INTO bus (bus_id, bus_name, source, destination, total_seats, fare) VALUES
(1, 'Express Volvo AC', 'New York', 'Boston', 40, 45.00),
(2, 'City Deluxe Sleeper', 'New York', 'Washington DC', 36, 55.00),
(3, 'Metro Superfast', 'Boston', 'Philadelphia', 44, 40.00),
(4, 'Highway Star Express', 'Chicago', 'Detroit', 40, 50.00);

INSERT IGNORE INTO booking (booking_id, passenger_id, bus_id, seat_number, booking_date, fare, status) VALUES
(1001, 101, 1, 12, '2026-10-15', 45.00, 'CONFIRMED'),
(1002, 102, 2, 7, '2026-10-16', 55.00, 'CONFIRMED');
