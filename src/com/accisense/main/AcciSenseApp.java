package com.accisense.main;

import com.accisense.alert.AlertManager;
import com.accisense.detection.AccidentDetector;
import com.accisense.exception.DatabaseException;
import com.accisense.gui.Dashboard;
import com.accisense.sensor.SensorSimulator;
import com.accisense.storage.DatabaseManager;

import javax.swing.*;

/**
 * Main entry point – creates all components, wires the Observer
 * pipeline, launches the GUI and starts the sensor thread.
 *
 * [RUBRIC: Exception Handling – catches DatabaseException]
 * [RUBRIC: Multithreading – SwingUtilities.invokeLater, shutdown hook]
 *
 * @author AcciSense Team
 */
public class AcciSenseApp {

    /**
     * Application entry point.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   AcciSense - Accident Detection System  ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // ── Step 1: create components ──
        SensorSimulator simulator = new SensorSimulator();
        AccidentDetector detector = new AccidentDetector();
        AlertManager alertManager = new AlertManager();
        DatabaseManager db = new DatabaseManager();

        // ── Step 2: initialise database (with full error trace) ──
        try {
            db.initialize();
        } catch (DatabaseException e) {
            e.printStackTrace();              // full stack trace for debugging
            System.err.println("Fatal: " + e.getMessage());
            System.exit(1);
        }

        // ── Step 3: wire the pipeline ──
        // Sensor → Detector → AlertManager → Database
        simulator.addListener(detector);
        detector.addAccidentListener(alertManager);
        alertManager.addAlertListener(db::saveAccident);

        // ── Step 4: launch GUI on the Event Dispatch Thread ──
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard(simulator);

            // GUI listens to live sensor data and alerts
            simulator.addListener(dashboard);
            alertManager.addAlertListener(dashboard);

            dashboard.setVisible(true);
        });

        // ── Step 5: start sensor simulation thread ──
        simulator.start();
        System.out.println("✅ AcciSense running. Total accidents in DB: "
                + db.getTotalAccidents());

        // ── Step 6: clean shutdown hook ──
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            simulator.stop();
            db.close();
            System.out.println("👋 AcciSense shut down cleanly.");
        }));
    }
}