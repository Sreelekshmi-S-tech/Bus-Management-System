-- ====================================================================
-- Bus Management & Reservation System - Full Database Schema & Seed Data
-- ====================================================================

CREATE DATABASE IF NOT EXISTS bus_reservation;
USE bus_reservation;

-- Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS booking_group;
DROP TABLE IF EXISTS rate_limit_log;
DROP TABLE IF EXISTS passenger;
DROP TABLE IF EXISTS bus;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Table: users
CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- 'ADMIN' or 'USER'
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    failed_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Table: passenger
CREATE TABLE passenger (
    passenger_id INT NOT NULL,
    user_id INT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL, -- 'MALE', 'FEMALE', 'OTHERS'
    phone VARCHAR(20) NOT NULL,
    has_disability BOOLEAN DEFAULT FALSE,
    disability_id_no VARCHAR(50) NULL,
    PRIMARY KEY (passenger_id),
    CONSTRAINT fk_passenger_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Table: bus
CREATE TABLE bus (
    bus_id INT NOT NULL,
    bus_name VARCHAR(100) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    fare DOUBLE NOT NULL DEFAULT 0.0,
    departure_time DATETIME NOT NULL,
    bus_type VARCHAR(50) NOT NULL DEFAULT 'SEATER 2+2',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'DEPARTED', 'CANCELLED'
    PRIMARY KEY (bus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Table: booking_group
CREATE TABLE booking_group (
    group_id INT NOT NULL AUTO_INCREMENT,
    passenger_id INT NOT NULL,
    bus_id INT NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_fare DOUBLE NOT NULL,
    PRIMARY KEY (group_id),
    CONSTRAINT fk_group_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_bus FOREIGN KEY (bus_id) REFERENCES bus (bus_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Table: booking
CREATE TABLE booking (
    booking_id INT NOT NULL AUTO_INCREMENT,
    group_id INT NOT NULL,
    passenger_id INT NOT NULL,
    bus_id INT NOT NULL,
    seat_number INT NOT NULL,
    booking_date VARCHAR(50) NOT NULL,
    base_fare DOUBLE NOT NULL,
    concession_percent INT DEFAULT 0,
    fare DOUBLE NOT NULL,
    seat_passenger_gender VARCHAR(10) NOT NULL DEFAULT 'MALE', -- 'MALE', 'FEMALE', 'OTHERS'
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED', -- 'BOOKED', 'CANCELLED'
    PRIMARY KEY (booking_id),
    KEY idx_bus_seat_status (bus_id, seat_number, status),
    CONSTRAINT fk_booking_group FOREIGN KEY (group_id) REFERENCES booking_group (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (passenger_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_bus FOREIGN KEY (bus_id) REFERENCES bus (bus_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Table: rate_limit_log
CREATE TABLE rate_limit_log (
    id INT NOT NULL AUTO_INCREMENT,
    identifier VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'LOGIN_SUCCESS', 'LOGIN_FAILED', 'SIGNUP', 'BOOKING'
    attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_rate_lookup (identifier, action, attempted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ====================================================================
-- Sample Seed Data
-- ====================================================================

-- 1. Seed Users (Pre-hashed with PBKDF2WithHmacSHA256, 65536 iterations)
-- Default Admin: admin / 1234
-- Regular User:  john_doe / User@1234
INSERT INTO users (user_id, username, password_hash, salt, role, email) VALUES
(1, 'admin', '25853f146cee3b1abf4d8363554650e26ccb8c9ea81b78c7a568904489070e89', '9a777fb68d0d31c64a96977043c2a293', 'ADMIN', 'admin@busreservation.com'),
(2, 'john_doe', '911c486237e5062b5047cf1f685fc481c310e24ef1de9d98a9f9fac39a255893', '1b25a534538c5a3cd9b669adae847a7f', 'USER', 'john.doe@example.com');

-- 2. Seed Passengers
INSERT INTO passenger (passenger_id, user_id, name, age, gender, phone, has_disability, disability_id_no) VALUES
(101, 2, 'John Doe', 28, 'MALE', '9876543210', FALSE, NULL),
(102, NULL, 'Sarah Smith', 24, 'FEMALE', '9123456780', FALSE, NULL),
(103, NULL, 'Robert Davis', 62, 'MALE', '9988776655', TRUE, 'DIS-9842'),
(104, NULL, 'Emily Watson', 29, 'FEMALE', '9871234560', FALSE, NULL);

-- 3. Seed Buses (with scheduled future dates and past dates to test DEPARTED logic)
INSERT INTO bus (bus_id, bus_name, source, destination, total_seats, fare, departure_time, bus_type, status) VALUES
(1, 'Express Volvo AC', 'New York', 'Boston', 40, 45.00, DATE_ADD(NOW(), INTERVAL 1 DAY), 'SEATER 2+2', 'SCHEDULED'),
(2, 'City Deluxe Sleeper', 'New York', 'Washington DC', 36, 55.00, DATE_ADD(NOW(), INTERVAL 2 DAY), 'SLEEPER 2+1', 'SCHEDULED'),
(3, 'Metro Superfast', 'Boston', 'Philadelphia', 44, 40.00, DATE_ADD(NOW(), INTERVAL 3 DAY), 'SEATER 2+2', 'SCHEDULED'),
(4, 'Highway Star Express', 'Chicago', 'Detroit', 40, 50.00, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'SEATER 2+2', 'DEPARTED');

-- 4. Seed Booking Groups & Bookings (Demonstrates neighbour-aware seat map with Male/Female/Disability seats)
-- Group 1001: Sarah Smith (FEMALE) booked Seat 3 on Bus 1
INSERT INTO booking_group (group_id, passenger_id, bus_id, booking_time, total_fare) VALUES
(1001, 102, 1, NOW(), 45.00);

INSERT INTO booking (booking_id, group_id, passenger_id, bus_id, seat_number, booking_date, base_fare, concession_percent, fare, seat_passenger_gender, status) VALUES
(1, 1001, 102, 1, 3, CURDATE(), 45.00, 0, 45.00, 'FEMALE', 'BOOKED');

-- Group 1002: John Doe (MALE) booked Seat 7 on Bus 1
INSERT INTO booking_group (group_id, passenger_id, bus_id, booking_time, total_fare) VALUES
(1002, 101, 1, NOW(), 45.00);

INSERT INTO booking (booking_id, group_id, passenger_id, bus_id, seat_number, booking_date, base_fare, concession_percent, fare, seat_passenger_gender, status) VALUES
(2, 1002, 101, 1, 7, CURDATE(), 45.00, 0, 45.00, 'MALE', 'BOOKED');

-- Group 1003: Robert Davis (MALE with 100% disability concession) booked Seat 12 on Bus 1
INSERT INTO booking_group (group_id, passenger_id, bus_id, booking_time, total_fare) VALUES
(1003, 103, 1, NOW(), 0.00);

INSERT INTO booking (booking_id, group_id, passenger_id, bus_id, seat_number, booking_date, base_fare, concession_percent, fare, seat_passenger_gender, status) VALUES
(3, 1003, 103, 1, 12, CURDATE(), 45.00, 100, 0.00, 'MALE', 'BOOKED');
