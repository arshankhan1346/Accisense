package com.accisense.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stores details of a detected accident.
 * [RUBRIC: Collections – implements Comparable for sorting]
 *
 * @author AcciSense Team
 */
public class AccidentRecord implements Comparable<AccidentRecord> {

    /** Severity levels. */
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private int id;
    private final LocalDateTime timestamp;
    private final Severity severity;
    private final double impactForce;
    private final double resultantAcceleration;
    private final double resultantGyro;
    private final String location;
    private boolean alertSent;

    /** Constructor. ID is assigned later by database AUTOINCREMENT. */
    public AccidentRecord(Severity severity, double impactForce,
                          double resultantAcceleration, double resultantGyro,
                          String location) {
        this.id = -1;
        this.timestamp = LocalDateTime.now();
        this.severity = severity;
        this.impactForce = impactForce;
        this.resultantAcceleration = resultantAcceleration;
        this.resultantGyro = resultantGyro;
        this.location = location;
        this.alertSent = false;
    }

    // ---------- Getters & Setters ----------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Severity getSeverity() { return severity; }
    public double getImpactForce() { return impactForce; }
    public double getResultantAcceleration() { return resultantAcceleration; }
    public double getResultantGyro() { return resultantGyro; }
    public String getLocation() { return location; }
    public boolean isAlertSent() { return alertSent; }
    public void setAlertSent(boolean alertSent) { this.alertSent = alertSent; }

    /** Formatted timestamp. */
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** [RUBRIC: Collections] Sort by severity (CRITICAL first), then time. */
    @Override
    public int compareTo(AccidentRecord other) {
        int sev = other.severity.compareTo(this.severity);
        if (sev != 0) return sev;
        return other.timestamp.compareTo(this.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Accident #%d | %s | %s | Impact=%.2f | Alert=%s",
                id, getFormattedTime(), severity, impactForce,
                alertSent ? "YES" : "NO");
    }

    /** CSV row for export. */
    public String toCSV() {
        return String.format("%d,%s,%s,%.2f,%.2f,%.2f,%s,%s",
                id, getFormattedTime(), severity, impactForce,
                resultantAcceleration, resultantGyro, location, alertSent);
    }

    /** CSV header. */
    public static String csvHeader() {
        return "ID,Timestamp,Severity,ImpactForce,ResultantAccel,ResultantGyro,Location,AlertSent";
    }
}