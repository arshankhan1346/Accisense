package com.accisense.detection;

import com.accisense.model.AccidentRecord;
import com.accisense.model.AccidentRecord.Severity;
import com.accisense.model.SensorData;
import com.accisense.sensor.SensorListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyses sensor data and confirms accidents using 2-of-3 threshold logic.
 * [FIX 3] Explicitly tags simulated data so it cannot be confused with real emergency services.
 *
 * @author AcciSense Team
 */
public class AccidentDetector implements SensorListener {

    private final Map<String, Double> thresholds = new HashMap<>();
    private final List<AccidentListener> listeners = new ArrayList<>();
    private static final long COOLDOWN_MS = 5000;
    private long lastDetectionTime = 0;

    public AccidentDetector() {
        thresholds.put("IMPACT", 40.0);
        thresholds.put("ACCEL", 20.0);
        thresholds.put("GYRO", 120.0);
    }

    public synchronized void addAccidentListener(AccidentListener l) {
        listeners.add(l);
    }

    @Override
    public void onSensorDataReceived(SensorData data) {
        long now = System.currentTimeMillis();
        if (now - lastDetectionTime < COOLDOWN_MS) return;

        double impact = data.getImpactForce();
        double accel  = data.getResultantAcceleration();
        double gyro   = data.getResultantGyro();

        boolean t1 = impact > thresholds.get("IMPACT");
        boolean t2 = accel  > thresholds.get("ACCEL");
        boolean t3 = gyro   > thresholds.get("GYRO");

        int triggers = (t1 ? 1 : 0) + (t2 ? 1 : 0) + (t3 ? 1 : 0);

        if (triggers >= 2) {
            Severity severity = determineSeverity(impact, accel, gyro);
            
            // [FIX 3] Explicitly tag simulated data
            String mockLocation = "[SIMULATED GPS - NO HARDWARE ATTACHED]";
            
            AccidentRecord record = new AccidentRecord(
                    severity, impact, accel, gyro, mockLocation);
            lastDetectionTime = now;

            List<AccidentListener> copy;
            synchronized (this) { copy = new ArrayList<>(listeners); }
            for (AccidentListener l : copy) l.onAccidentDetected(record);
        }
    }

    private Severity determineSeverity(double impact, double accel, double gyro) {
        double score = impact * 0.5 + accel * 1.5 + gyro * 0.3;
        if (score > 200) return Severity.CRITICAL;
        if (score > 120) return Severity.HIGH;
        if (score > 70)  return Severity.MEDIUM;
        return Severity.LOW;
    }
}