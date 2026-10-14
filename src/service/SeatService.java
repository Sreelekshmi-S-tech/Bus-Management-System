package service;

import model.Seat;

import java.util.HashMap;
import java.util.Map;

/**
 * Service managing seat layout geometry, neighbour relationships, and seating preference filters.
 */
public class SeatService {

    /**
     * Determines the adjacent neighbour seat in a 2+2 bus seating grid.
     * In 2+2 layout:
     * Seats 1 & 2 are side-by-side (left of aisle).
     * Seats 3 & 4 are side-by-side (right of aisle).
     */
    public static int getAdjacentSeatNumber(int seatNumber, int totalSeats) {
        int rem = seatNumber % 4;
        int adjacent;
        if (rem == 1) {
            adjacent = seatNumber + 1;
        } else if (rem == 2) {
            adjacent = seatNumber - 1;
        } else if (rem == 3) {
            adjacent = seatNumber + 1;
        } else { // rem == 0 (4th seat)
            adjacent = seatNumber - 1;
        }

        if (adjacent >= 1 && adjacent <= totalSeats) {
            return adjacent;
        }
        return -1;
    }

    /**
     * Populates neighbour information across all seats for a bus.
     */
    public static void populateNeighbourInfo(Map<Integer, Seat> seatMap, int totalSeats) {
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = seatMap.computeIfAbsent(i, Seat::new);
            int adjNum = getAdjacentSeatNumber(i, totalSeats);
            seat.setAdjacentSeatNumber(adjNum);

            if (adjNum != -1 && seatMap.containsKey(adjNum)) {
                Seat adjSeat = seatMap.get(adjNum);
                if (adjSeat.isBooked()) {
                    seat.setAdjacentGender(adjSeat.getPassengerGender());
                } else {
                    seat.setAdjacentGender(null);
                }
            } else {
                seat.setAdjacentGender(null);
            }
        }
    }

    /**
     * Applies seating preference filtering (e.g., "Next to Same Gender").
     * Preference matching rule:
     * - "No Preference": All available seats are normal.
     * - "Next to Same Gender" for passenger with gender G:
     *   Seat matches if adjacent is empty OR adjacent is booked by someone of gender G.
     *   Seat does NOT match (flagged) if adjacent is booked by another gender.
     */
    public static void applyPreferenceFilter(Map<Integer, Seat> seatMap, int totalSeats,
                                             String preference, String passengerGender) {
        boolean filterActive = "Next to Same Gender".equalsIgnoreCase(preference) &&
                               passengerGender != null && !passengerGender.trim().isEmpty();

        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = seatMap.get(i);
            if (seat == null || !seat.isAvailable()) {
                if (seat != null) seat.setPreferenceMatch(false);
                continue;
            }

            if (!filterActive) {
                seat.setPreferenceMatch(false);
                continue;
            }

            String adjGender = seat.getAdjacentGender();
            if (adjGender == null) {
                // Adjacent seat is completely free -> safe & good match
                seat.setPreferenceMatch(true);
            } else if (adjGender.equalsIgnoreCase(passengerGender)) {
                // Adjacent seat is booked by the same gender -> strong match!
                seat.setPreferenceMatch(true);
            } else {
                // Adjacent seat is occupied by a different gender -> not preferred
                seat.setPreferenceMatch(false);
            }
        }
    }

    /**
     * Generates a privacy-preserving tooltip for a seat.
     * Strictly reveals gender and availability, NEVER personal details (name, phone).
     */
    public static String getSeatTooltip(Seat seat) {
        if (seat == null) return "";
        StringBuilder sb = new StringBuilder("<html><body style='padding:4px;'>");
        sb.append("<b>Seat #").append(seat.getSeatNumber()).append("</b><br>");

        if (seat.isBooked()) {
            sb.append("<span style='color:#C0392B;'>Status: Booked</span><br>");
            String g = seat.getPassengerGender() != null ? seat.getPassengerGender() : "Not Specified";
            sb.append("Passenger Gender: <b>").append(g).append("</b>");
        } else if (seat.isSelected()) {
            sb.append("<span style='color:#2980B9;'>Status: Selected by you</span>");
        } else {
            sb.append("<span style='color:#27AE60;'>Status: Available</span><br>");
            if (seat.getAdjacentSeatNumber() != -1) {
                sb.append("Adjacent Seat #").append(seat.getAdjacentSeatNumber()).append(": ");
                if (seat.getAdjacentGender() != null) {
                    sb.append("Booked by <b>").append(seat.getAdjacentGender()).append("</b>");
                } else {
                    sb.append("<i>Available / Empty</i>");
                }
            }
            if (seat.isPreferenceMatch()) {
                sb.append("<br><span style='color:#D35400;'>★ Matches Seating Preference</span>");
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }
}
