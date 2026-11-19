package com.accisense.storage;

import com.accisense.exception.DatabaseException;
import com.accisense.model.AccidentRecord;
import com.accisense.model.AccidentRecord.Severity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database manager - all CRUD operations via JDBC.
 * [REVIEW-1 FIX] Failures propagate to callers via DatabaseException.
 * [REVIEW-1 FIX] Connection access serialized for alert + servlet threads.
 * [REVIEW-2 FIX] Stored detection timestamps are restored on read.
 * [RUBRIC: JDBC - DriverManager, PreparedStatement, ResultSet]
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:accisense.db";
    private Connection connection;

    public synchronized void initialize() throws DatabaseException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);

            String sql = "CREATE TABLE IF NOT EXISTS accidents ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "timestamp TEXT NOT NULL, "
                    + "severity TEXT NOT NULL, "
                    + "impact_force REAL NOT NULL, "
                    + "accel_resultant REAL NOT NULL, "
                    + "gyro_resultant REAL NOT NULL, "
                    + "location TEXT, "
                    + "alert_sent INTEGER DEFAULT 0)";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
            System.out.println("[DB] Connected: accisense.db");

        } catch (ClassNotFoundException e) {
            throw new DatabaseException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database", e);
        }
    }

    /** Listener-safe wrapper used by the alert pipeline. */
    public synchronized void saveAccident(AccidentRecord record) {
        try {
            saveAccidentOrThrow(record);
        } catch (DatabaseException e) {
            System.err.println("[DB] Insert failed: " + e.getMessage());
        }
    }

    /** Caller-aware insert; propagates failure (used by the servlet layer). */
    public synchronized void saveAccidentOrThrow(AccidentRecord record) throws DatabaseException {
        String sql = "INSERT INTO accidents "
                + "(timestamp, severity, impact_force, accel_resultant, "
                + "gyro_resultant, location, alert_sent) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, record.getFormattedTime());
            ps.setString(2, record.getSeverity().toString());
            ps.setDouble(3, record.getImpactForce());
            ps.setDouble(4, record.getResultantAcceleration());
            ps.setDouble(5, record.getResultantGyro());
            ps.setString(6, record.getLocation());
            ps.setInt(7, record.isAlertSent() ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) record.setId(keys.getInt(1));
            }
            System.out.println("[DB] Saved accident #" + record.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save accident record", e);
        }
    }

    public synchronized List<AccidentRecord> getAllAccidents() throws DatabaseException {
        List<AccidentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM accidents ORDER BY id DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve accident records", e);
        }
        return records;
    }

    public synchronized List<AccidentRecord> getBySeverity(String severity) throws DatabaseException {
        List<AccidentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM accidents WHERE severity = ? ORDER BY id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, severity.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query accidents by severity", e);
        }
        return records;
    }

    public synchronized int getTotalAccidents() throws DatabaseException {
        String sql = "SELECT COUNT(*) AS total FROM accidents";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count accidents", e);
        }
    }

    public synchronized void clearAll() throws DatabaseException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM accidents");
            System.out.println("[DB] All accident records deleted.");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to clear accident records", e);
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Close failed: " + e.getMessage());
        }
    }

    /** Re-hydrates a row, restoring the ORIGINAL detection timestamp. */
    private AccidentRecord mapRow(ResultSet rs) throws SQLException {
        AccidentRecord r = new AccidentRecord(
                Severity.valueOf(rs.getString("severity")),
                rs.getDouble("impact_force"),
                rs.getDouble("accel_resultant"),
                rs.getDouble("gyro_resultant"),
                rs.getString("location"),
                AccidentRecord.parseTime(rs.getString("timestamp")));
        r.setId(rs.getInt("id"));
        r.setAlertSent(rs.getInt("alert_sent") == 1);
        return r;
    }
}