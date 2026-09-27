package com.accisense.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single reading from the vehicle's sensors.
 * Immutable data carrier class.
 *
 * @author AcciSense Team
 */
public class SensorData {

    private final double accelerationX;   // m/s²
    private final double accelerationY;   // m/s²
    private final double accelerationZ;   // m/s²
    private final double gyroX;           // °/s
    private final double gyroY;           // °/s
    private final double gyroZ;           // °/s
    private final double impactForce;     // kN
    private final LocalDateTime timestamp;

    /**
     * Constructor.
     */
    public SensorData(double ax, double ay, double az,
                      double gx, double gy, double gz,
                      double impactForce) {
        this.accelerationX = ax;
        this.accelerationY = ay;
        this.accelerationZ = az;
        this.gyroX = gx;
        this.gyroY = gy;
        this.gyroZ = gz;
        this.impactForce = impactForce;
        this.timestamp = LocalDateTime.now();
    }

    // ---------- Getters ----------
    public double getAccelerationX() { return accelerationX; }
    public double getAccelerationY() { return accelerationY; }
    public double getAccelerationZ() { return accelerationZ; }
    public double getGyroX() { return gyroX; }
    public double getGyroY() { return gyroY; }
    public double getGyroZ() { return gyroZ; }
    public double getImpactForce() { return impactForce; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Resultant acceleration = sqrt(x² + y² + z²) */
    public double getResultantAcceleration() {
        return Math.sqrt(accelerationX * accelerationX +
                         accelerationY * accelerationY +
                         accelerationZ * accelerationZ);
    }

    /** Resultant angular velocity = sqrt(x² + y² + z²) */
    public double getResultantGyro() {
        return Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
    }

    /** Formatted timestamp string. */
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] Accel=%.2f | Gyro=%.2f | Impact=%.2f",
                getFormattedTime(), getResultantAcceleration(),
                getResultantGyro(), impactForce);
    }
}