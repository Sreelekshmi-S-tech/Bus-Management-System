package service;

import model.Passenger;

/**
 * Service for calculating ticket fares and concession deductions.
 */
public class FareCalculator {

    // Configurable constant for disability concession percentage (100% free ticket)
    public static final int DISABILITY_CONCESSION_PERCENT = 100;

    /**
     * Determines whether the given passenger qualifies for the disability concession.
     */
    public static boolean isEligibleForDisabilityConcession(Passenger passenger) {
        if (passenger == null) return false;
        return passenger.isHasDisability() &&
               passenger.getDisabilityIdNo() != null &&
               !passenger.getDisabilityIdNo().trim().isEmpty();
    }

    /**
     * Returns the concession percentage applied for the passenger.
     */
    public static int getConcessionPercentage(Passenger passenger) {
        if (isEligibleForDisabilityConcession(passenger)) {
            return DISABILITY_CONCESSION_PERCENT;
        }
        return 0;
    }

    /**
     * Calculates the discounted fare for a single seat.
     */
    public static double calculateSeatFare(double baseFare, int concessionPercent) {
        if (concessionPercent >= 100) {
            return 0.0;
        }
        if (concessionPercent <= 0) {
            return baseFare;
        }
        double discount = baseFare * (concessionPercent / 100.0);
        return Math.max(0.0, baseFare - discount);
    }

    /**
     * Calculates the total fare for a group of seats.
     */
    public static double calculateTotalFare(double baseFare, int seatCount, int concessionPercent) {
        double perSeat = calculateSeatFare(baseFare, concessionPercent);
        return Math.round(perSeat * seatCount * 100.0) / 100.0;
    }
}
