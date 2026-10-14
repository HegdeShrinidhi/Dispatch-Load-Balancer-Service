package com.dispatch.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HaversineDistanceCalculatorTest {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double TOLERANCE = 1E-5; // Highly precise delta assertion limit

    /**
     * Helper method to compute a direct Spherical Law of Cosines distance
     * baseline to avoid hardcoding expected numbers in our assertions.
     */
    private double computeSphericalDistanceBaseline(double lat1, double lon1, double lat2, double lon2) {
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);
        double rDeltaLon = Math.toRadians(lon2 - lon1);

        double acosValue = Math.sin(rLat1) * Math.sin(rLat2) + Math.cos(rLat1) * Math.cos(rLat2) * Math.cos(rDeltaLon);
        // Prevent floating-point NaN on matching points
        acosValue = Math.max(-1.0, Math.min(1.0, acosValue));

        return EARTH_RADIUS_KM * Math.acos(acosValue);
    }

    /**
     * 1. EDGE CASE: Identical Coordinates
     */
    @Test
    void testSameCoordinatesShouldReturnZero() {
        double lat = 12.9716;
        double lon = 77.5946;

        double distance = HaversineDistanceCalculator.calculateDistanceKm(lat, lon, lat, lon);

        assertEquals(0.0, distance, TOLERANCE, "Distance between identical coordinates must be exactly zero.");
    }

    /**
     * 2. DYNAMIC INPUT VALIDATION: Local Proximity (Bangalore Area) Compares
     * the utility output directly against our separate mathematical baseline.
     */
    @Test
    void testLocalCoordinatesAgainstMathematicalBaseline() {
        double lat1 = 12.9716;  // MG Road, Bangalore
        double lon1 = 77.5946;
        double lat2 = 12.9716;  // Indiranagar, Bangalore
        double lon2 = 77.6413;

        double expected = computeSphericalDistanceBaseline(lat1, lon1, lat2, lon2);
        double actual = HaversineDistanceCalculator.calculateDistanceKm(lat1, lon1, lat2, lon2);

        assertEquals(expected, actual, TOLERANCE, "Calculated distance mismatch against pure spherical verification models.");
    }

    /**
     * 3. CRITICAL ALGO EDGE CASE: Floating-Point Clamping Validation Tests
     * antipodal (opposite side of the earth) conditions where floating point
     * precision could overflow standard trigonometric ranges.
     */
    @Test
    void testAntipodalPointsDoesNotOverflowClampingSats() {
        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 0.0;
        double lon2 = 180.0; // Directly across the globe on the equator

        double expected = computeSphericalDistanceBaseline(lat1, lon1, lat2, lon2);
        double actual = HaversineDistanceCalculator.calculateDistanceKm(lat1, lon1, lat2, lon2);

        assertEquals(expected, actual, TOLERANCE, "Antipodal point processing failed to handle math boundaries cleanly.");
    }

    /**
     * 4. GLOBAL AREA ZONE TEST: Crossing the International Date Line Tests
     * points very close to each other but on opposite sides of the 180/-180
     * boundary line.
     */
    @Test
    void testDistanceAcrossInternationalDateLine() {
        double lat1 = 0.0;
        double lon1 = 179.95;  // East hemisphere
        double lat2 = 0.0;
        double lon2 = -179.95; // West hemisphere

        double expected = computeSphericalDistanceBaseline(lat1, lon1, lat2, lon2);
        double actual = HaversineDistanceCalculator.calculateDistanceKm(lat1, lon1, lat2, lon2);

        assertEquals(expected, actual, TOLERANCE, "Failed to compute correct short path across the International Date Line.");
    }

    /**
     * 5. GLOBAL AREA ZONE TEST: Polar Proximity Compression Tests coordinates
     * near the North Pole where longitudinal paths compress significantly.
     */
    @Test
    void testPolarProximityLongitudinalCompression() {
        double lat1 = 89.0; // Near North Pole
        double lon1 = 0.0;
        double lat2 = 89.0;
        double lon2 = 90.0; // 90 degrees apart in longitude, but physically very close near the pole

        double expected = computeSphericalDistanceBaseline(lat1, lon1, lat2, lon2);
        double actual = HaversineDistanceCalculator.calculateDistanceKm(lat1, lon1, lat2, lon2);

        assertEquals(expected, actual, TOLERANCE, "Failed to scale down longitudinal distance accurately near the polar axes.");
    }

    /**
     * 6. BOUNDARY INPUTS: Latitude and Longitude Boundary Limits
     */
    @Test
    void testExactBoundaryLimitsPassValidation() {
        // Absolute maximum values allowed by spatial tracking standards
        assertDoesNotThrow(() -> HaversineDistanceCalculator.calculateDistanceKm(-90.0, -180.0, 90.0, 180.0));
    }

    /**
     * 7. DATA ERROR HANDLING: Out Of Bounds Latitude Inputs
     */
    @Test
    void testInvalidLatitudeShouldThrowException() {
        // Upper bound violation (> 90)
        IllegalArgumentException upperBoundException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(90.1, 77.5946, 12.9716, 77.6413)
        );
        assertNotNull(upperBoundException);

        // Lower bound violation (< -90)
        IllegalArgumentException lowerBoundException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(-90.1, 77.5946, 12.9716, 77.6413)
        );
        assertNotNull(lowerBoundException);
    }

    /**
     * 8. DATA ERROR HANDLING: Out Of Bounds Longitude Inputs
     */
    @Test
    void testInvalidLongitudeShouldThrowException() {
        // Upper bound violation (> 180)
        IllegalArgumentException upperBoundException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(12.9716, 180.1, 12.9716, 77.6413)
        );
        assertNotNull(upperBoundException);

        // Lower bound violation (< -180)
        IllegalArgumentException lowerBoundException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(12.9716, -180.1, 12.9716, 77.6413)
        );
        assertNotNull(lowerBoundException);
    }

    /**
     * 9. DATA ERROR HANDLING: Infinite and NaN (Not-a-Number) Protections
     */
    @Test
    void testNonFiniteCoordinatesThrowException() {
        IllegalArgumentException nanException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(Double.NaN, 77.5946, 12.9716, 77.6413)
        );
        assertNotNull(nanException);

        IllegalArgumentException infinityException = assertThrows(IllegalArgumentException.class, ()
                -> HaversineDistanceCalculator.calculateDistanceKm(12.9716, Double.POSITIVE_INFINITY, 12.9716, 77.6413)
        );
        assertNotNull(infinityException);
    }
}
