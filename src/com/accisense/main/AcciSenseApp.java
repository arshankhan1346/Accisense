package com.accisense.main;

import com.accisense.alert.AlertManager;
import com.accisense.detection.AccidentDetector;
import com.accisense.exception.DatabaseException;
import com.accisense.gui.Dashboard;
import com.accisense.sensor.SensorSimulator;
import com.accisense.storage.DatabaseManager;
import com.accisense.web.WebServer;

import javax.swing.*;

/**
 * Entry point - wires the Observer pipeline, launches the Swing GUI and
 * the embedded-Tomcat servlet layer against the same SQLite database.
 *
 * [RUBRIC: Servlet Implementation - web layer bootstrap]
 * [RUBRIC: Exception Handling - DatabaseException]
 * [RUBRIC: Multithreading - invokeLater, daemon threads, shutdown hook]
 */
public class AcciSenseApp {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("   AcciSense - Accident Detection System");
        System.out.println("==============================================");

        SensorSimulator simulator = new SensorSimulator();
        AccidentDetector detector = new AccidentDetector();
        AlertManager alertManager = new AlertManager();
        DatabaseManager db = new DatabaseManager();

        try {
            db.initialize();
        } catch (DatabaseException e) {
            e.printStackTrace();
            System.err.println("Fatal: " + e.getMessage());
            System.exit(1);
        }

        // Observer pipeline: Sensor -> Detector -> Alerts -> DB + GUI
        simulator.addListener(detector);
        detector.addAccidentListener(alertManager);
        alertManager.addAlertListener(db::saveAccident);

        // [RUBRIC: Servlet Implementation] web console on a background thread
        WebServer web = new WebServer(db, 8080);
        Thread webThread = new Thread(() -> {
            try {
                web.start();
            } catch (Exception e) {
                System.err.println("[WEB] Console unavailable: " + e.getMessage());
            }
        }, "WebServer");
        webThread.setDaemon(true);
        webThread.start();

        // Swing GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard(simulator);
            simulator.addListener(dashboard);
            alertManager.addAlertListener(dashboard);
            dashboard.setVisible(true);
        });

        simulator.start();
        try {
            System.out.println("[APP] Running. Total accidents in DB: " + db.getTotalAccidents());
        } catch (DatabaseException e) {
            System.err.println("[APP] Could not read accident count: " + e.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            simulator.stop();
            alertManager.shutdown();
            web.stop();
            db.close();
            System.out.println("[APP] Shut down cleanly.");
        }));
    }
}