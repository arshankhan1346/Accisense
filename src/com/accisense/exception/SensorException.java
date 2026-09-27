package com.accisense.exception;

/**
 * Custom exception for sensor errors.
 * [RUBRIC: Exception Handling + Inheritance]
 *
 * @author AcciSense Team
 */
public class SensorException extends Exception {

    private final String sensorName;

    public SensorException(String sensorName, String message) {
        super("[" + sensorName + "] " + message);
        this.sensorName = sensorName;
    }

    public SensorException(String sensorName, String message, Throwable cause) {
        super("[" + sensorName + "] " + message, cause);
        this.sensorName = sensorName;
    }

    public String getSensorName() { return sensorName; }
}