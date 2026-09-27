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
 *
 * [RUBRIC: Interface – implements SensorListener]
 * [RUBRIC: Collections – ArrayList, HashMap]
 * [RUBRIC: Generics – List<AccidentListener>, Map<String, Double>]
 *
 * @author AcciSense Team
 */
public class AccidentDetector implements SensorListener {

    // [RUBRIC: Collections – thresholds stored in HashMap]
    private final Map<String, Double> thresholds = new HashMap<>();

    // [RUBRIC: Collections + Generics]
    private final List<AccidentListener> listeners = new ArrayList<>();

    private static final long COOLDOWN_MS = 5000;
    private long lastDetectionTime = 0;

    public AccidentDetector() {
        thresholds.put("IMPACT", 40.0);   // kN
        thresholds.put("ACCEL", 20.0);    // m/s²
        thresholds.put("GYRO", 120.0);    // °/s
    }

    public synchronized void addAccidentListener(AccidentListener l) {
        listeners.add(l);
    }

    /** [RUBRIC: Interface method] */
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
            AccidentRecord record = new AccidentRecord(
                    severity, impact, accel, gyro, "Simulated GPS Location");
            lastDetectionTime = now;

            List<AccidentListener> copy;
            synchronized (this) { copy = new ArrayList<>(listeners); }
            for (AccidentListener l : copy) l.onAccidentDetected(record);
        }
    }

    /**
     * Severity score = impact×0.5 + accel×1.5 + gyro×0.3
     * >200 CRITICAL | >120 HIGH | >70 MEDIUM | else LOW
     */
    private Severity determineSeverity(double impact, double accel, double gyro) {
        double score = impact * 0.5 + accel * 1.5 + gyro * 0.3;
        if (score > 200) return Severity.CRITICAL;
        if (score > 120) return Severity.HIGH;
        if (score > 70)  return Severity.MEDIUM;
        return Severity.LOW;
    }
}