# 🚌 Bus Management & Reservation System

An enterprise-grade Java Swing desktop application integrated with MySQL JDBC for complete fleet logistics, passenger records, multi-seat interactive reservations, disability concessions, and boarding pass generation following the **MVC / DAO** architectural pattern.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java (JDK 21+)
- **GUI Framework:** Java Swing (`JFrame`, `JTable`, `JPanel`, `CardLayout`/`BorderLayout`, custom Java2D graphics)
- **Database:** MySQL Server 8.0+
- **Database Driver:** `mysql-connector-j-26.7.0.jar`
- **Security:** Salted PBKDF2 with HMAC-SHA256 password hashing (OWASP standard, 65,536 iterations)
- **Concurrency:** Asynchronous DB operations via `SwingWorker` off the Event Dispatch Thread (EDT)
- **Transactions:** Atomic transactions with `SELECT ... FOR UPDATE` row-level locking

### Package Architecture:
- **`main`**: Application entry point (`Main.java`)
- **`database`**: JDBC connection management with auto-reconnect (`DBConnection.java`)
- **`model`**: Domain entities (`User.java`, `Passenger.java`, `Bus.java`, `Seat.java`, `BookingGroup.java`, `Booking.java`)
- **`dao`**: Data Access Objects (`UserDAO.java`, `PassengerDAO.java`, `BusDAO.java`, `BookingDAO.java`)
- **`service`**: Business logic & rules (`AuthService.java`, `RateLimiter.java`, `FareCalculator.java`, `SeatService.java`)
- **`util`**: Security and input sanitization (`PasswordUtil.java`, `Validator.java`)
- **`gui`**: Graphical user interface windows:
  - `LoginFrame.java` (Authentication & rate limiting)
  - `SignupFrame.java` (Account creation & validation)
  - `Dashboard.java` (Real-time KPI metrics & role-based navigation)
  - `PassengerManagement.java` (CRUD with disability support)
  - `BusManagement.java` (Fleet management & automatic departure updates)
  - `BookingManagement.java` (Interactive multi-seat reservation & neighbour filters)
  - `SeatMapPanel.java` (Custom Java2D bus interior graphic & 2+2 seat grid)
  - `TicketGeneration.java` (Boarding pass preview, Java Print API & TXT export)

---

## 🔑 Default Credentials

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `1234` | Full CRUD access across all subsystems |
| **Standard User** | `john_doe` | `User@1234` | Booking, ticket printing, and profile management |

---

## 🗄️ Database Setup

The database schema and sample data are located at [`schema.sql`](schema.sql) and [`lib/database.sql`](lib/database.sql).

### Import into MySQL:
```powershell
# Using MySQL Command Line Client:
mysql -u root -p < schema.sql

# Or inside MySQL shell:
SOURCE c:/Users/ASUS/OneDrive/Desktop/bus management system/schema.sql;
```

### Relational Schema Summary:
1. `users`: Credentials, PBKDF2 hash, salt, roles (`ADMIN`/`USER`), failed attempt tracking, and lockout timestamps.
2. `passenger`: Profile info, phone, gender (`MALE`/`FEMALE`/`OTHERS`), and disability fields (`has_disability`, `disability_id_no`).
3. `bus`: Route (`source` -> `destination`), capacity, fare, `departure_time`, `bus_type` (`SEATER 2+2`, `SLEEPER 2+1`), and `status` (`SCHEDULED`, `DEPARTED`, `CANCELLED`).
4. `booking_group`: Multi-seat reservation grouping parent (`group_id`, `passenger_id`, `bus_id`, `total_fare`).
5. `booking`: Individual reserved seats (`seat_number`, `base_fare`, `concession_percent`, `fare`, `seat_passenger_gender`, `status`).
6. `rate_limit_log`: Security audit log for brute-force prevention.

---

## 🚀 How to Run

### 1. Compile All Files:
```powershell
$files = (Get-ChildItem -Recurse src -Filter *.java).FullName
javac -cp "lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" -d bin $files
```

### 2. Run the Application:
```powershell
java -cp "bin;lib/mysql-connector-j-26.7.0/mysql-connector-j-26.7.0.jar" main.Main
```

---

## ✨ Implemented Core Features

1. **Authentication & Rate Limiting (`AuthService`, `RateLimiter`):**
   - PBKDF2 password hashing with cryptographically secure random salt.
   - Max 5 failed login attempts in 10 minutes locks account for 15 minutes with real-time countdown feedback.
   - Machine signup limit (max 3 accounts per 10 mins).
   - User booking rate limiter (max 5 requests per minute).

2. **Custom Java2D Bus Interior Seat Map (`SeatMapPanel`):**
   - Hand-crafted bus interior rendering: windshield, driver cabin, steering wheel, entrance door, and central aisle with walking arrows.
   - 2+2 seating layout with rounded seats, headrests, and armrests.
   - Color code legend: Available (Green), Selected (Blue), Booked-Male (Light Blue), Booked-Female (Pink), Booked-Others (Purple), Preference Match (Amber glow).
   - Privacy-preserving hover tooltips displaying seat status and adjacent neighbour gender (strictly concealing personal names/phones).

3. **Multi-Seat Transactional Booking (`BookingDAO`):**
   - Book up to 6 seats in a single atomic transaction.
   - Database row-locking (`SELECT ... FOR UPDATE`) preventing double-booking race conditions.
   - Server-side and client-side departure validation blocking bookings on buses that have already started.

4. **Neighbour-Aware Seating Filter (`SeatService`):**
   - Calculate side-by-side adjacent seats in 2+2 geometry.
   - Optional seating preference dropdown ("Next to Same Gender") dynamically highlights seats matching passenger preference.

5. **Disability Concession Module (`FareCalculator`):**
   - Passengers with verified disability IDs receive a **100% concession** (free ticket).
   - Automatically computes base fare, applied concession %, and net total fare.

6. **Official Boarding Pass & Ticket Generation (`TicketGeneration`):**
   - Clean boarding pass preview with trip details, passenger info, seat numbers, concession status, and barcode strip.
   - Direct physical / PDF printing via the Java Print API (`Printable`).
   - Save ticket receipt to a `.txt` file via standard file chooser.