package com.accisense.storage;

import com.accisense.exception.DatabaseException;
import com.accisense.model.AccidentRecord;
import com.accisense.model.AccidentRecord.Severity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database manager – all CRUD operations via JDBC.
 *
 * [RUBRIC: JDBC – DriverManager, PreparedStatement, ResultSet]
 * [RUBRIC: DB Operations Class – Create / Read / Delete]
 * [RUBRIC: Exception Handling – custom DatabaseException]
 * [RUBRIC: Collections – returns List<AccidentRecord>]
 *
 * @author AcciSense Team
 */
public class DatabaseManager {

    // SQLite JDBC connection URL
    private static final String DB_URL = "jdbc:sqlite:accisense.db";

    // JDBC connection object
    private Connection connection;

    // ══════════════════════════════════════════════
    // INITIALISE – load driver, connect, create table
    // ══════════════════════════════════════════════

    /**
     * Loads the SQLite driver explicitly, opens the connection
     * and creates the accidents table if it does not exist.
     *
     * @throws DatabaseException if driver missing or SQL fails
     */
    public void initialize() throws DatabaseException {
        try {
            // Explicitly load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Open connection
            connection = DriverManager.getConnection(DB_URL);

            // Create table (plain concatenation – safest for copy/paste)
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
            System.out.println("✅ Database connected: accisense.db");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite driver NOT on classpath!");
            throw new DatabaseException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            e.printStackTrace();   // shows the REAL reason
            throw new DatabaseException("Failed: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════
    // CREATE – insert a new accident record
    // ══════════════════════════════════════════════

    /**
     * Saves an accident record using PreparedStatement.
     * The auto-generated ID is written back into the record.
     *
     * @param record the accident to save
     */
    public void saveAccident(AccidentRecord record) {
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

            // Read back the AUTOINCREMENT id
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
            System.out.println("💾 Saved accident #" + record.getId() + " to DB.");

        } catch (SQLException e) {
            System.err.println("❌ Insert failed: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════
    // READ – all accidents
    // ══════════════════════════════════════════════

    /**
     * Returns every accident record, newest first.
     *
     * @return list of all records
     */
    public List<AccidentRecord> getAllAccidents() {
        List<AccidentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM accidents ORDER BY timestamp DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AccidentRecord r = new AccidentRecord(
                        Severity.valueOf(rs.getString("severity")),
                        rs.getDouble("impact_force"),
                        rs.getDouble("accel_resultant"),
                        rs.getDouble("gyro_resultant"),
                        rs.getString("location"));
                r.setId(rs.getInt("id"));
                r.setAlertSent(rs.getInt("alert_sent") == 1);
                records.add(r);
            }

        } catch (SQLException e) {
            System.err.println("❌ Query failed: " + e.getMessage());
        }
        return records;
    }

    // ══════════════════════════════════════════════
    // READ – filter by severity
    // ══════════════════════════════════════════════

    /**
     * Returns accidents matching a severity level.
     *
     * @param severity LOW / MEDIUM / HIGH / CRITICAL
     * @return matching records
     */
    public List<AccidentRecord> getBySeverity(String severity) {
        List<AccidentRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM accidents WHERE severity = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, severity.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AccidentRecord r = new AccidentRecord(
                            Severity.valueOf(rs.getString("severity")),
                            rs.getDouble("impact_force"),
                            rs.getDouble("accel_resultant"),
                            rs.getDouble("gyro_resultant"),
                            rs.getString("location"));
                    r.setId(rs.getInt("id"));
                    records.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Query failed: " + e.getMessage());
        }
        return records;
    }

    // ══════════════════════════════════════════════
    // READ – total count
    // ══════════════════════════════════════════════

    /**
     * Returns the total number of stored accidents.
     *
     * @return count
     */
    public int getTotalAccidents() {
        String sql = "SELECT COUNT(*) AS total FROM accidents";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.err.println("❌ Count failed: " + e.getMessage());
        }
        return 0;
    }

    // ══════════════════════════════════════════════
    // DELETE – clear all records
    // ══════════════════════════════════════════════

    /** Deletes every accident record. */
    public void clearAll() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM accidents");
            System.out.println("🗑 All accident records deleted.");
        } catch (SQLException e) {
            System.err.println("❌ Delete failed: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════
    // CLOSE – release connection
    // ══════════════════════════════════════════════

    /** Closes the JDBC connection safely. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Close failed: " + e.getMessage());
        }
    }
}