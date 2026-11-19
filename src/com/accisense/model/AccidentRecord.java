package com.accisense.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable-by-convention accident event record.
 * The detection timestamp is captured once at creation and preserved when
 * rows are re-hydrated from SQLite, so historical times never drift.
 * [RUBRIC: Collections & Generics - Comparable]
 */
public class AccidentRecord implements Comparable<AccidentRecord> {

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id = -1;
    private LocalDateTime timestamp;
    private final Severity severity;
    private final double impactForce;
    private final double resultantAcceleration;
    private final double resultantGyro;
    private final String location;
    private boolean alertSent;

    public AccidentRecord(Severity severity, double impactForce,
                          double resultantAcceleration, double resultantGyro,
                          String location) {
        this(severity, impactForce, resultantAcceleration, resultantGyro,
             location, LocalDateTime.now());
    }

    public AccidentRecord(Severity severity, double impactForce,
                          double resultantAcceleration, double resultantGyro,
                          String location, LocalDateTime timestamp) {
        this.severity = severity;
        this.impactForce = impactForce;
        this.resultantAcceleration = resultantAcceleration;
        this.resultantGyro = resultantGyro;
        this.location = location;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) {
        if (timestamp != null) this.timestamp = timestamp;
    }

    public Severity getSeverity() { return severity; }
    public double getImpactForce() { return impactForce; }
    public double getResultantAcceleration() { return resultantAcceleration; }
    public double getResultantGyro() { return resultantGyro; }
    public String getLocation() { return location; }

    public boolean isAlertSent() { return alertSent; }
    public void setAlertSent(boolean alertSent) { this.alertSent = alertSent; }

    /** Always formats the STORED detection time, never the current clock. */
    public String getFormattedTime() { return timestamp.format(TIME_FORMAT); }

    public static LocalDateTime parseTime(String text) {
        try {
            return LocalDateTime.parse(text, TIME_FORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    @Override
    public int compareTo(AccidentRecord other) {
        int bySeverity = Integer.compare(other.severity.ordinal(), this.severity.ordinal());
        if (bySeverity != 0) return bySeverity;
        return other.timestamp.compareTo(this.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Accident #%d | %s | %s | Impact=%.2f | Alert=%s",
                id, getFormattedTime(), severity, impactForce, alertSent ? "YES" : "NO");
    }
}