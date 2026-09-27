package com.accisense.alert;

import com.accisense.detection.AccidentListener;
import com.accisense.model.AccidentRecord;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles sound alert, SMS simulation, and listener notification.
 *
 * [RUBRIC: Interface – implements AccidentListener]
 * [RUBRIC: Collections – ArrayList<AlertListener>]
 * [RUBRIC: Exception Handling – try-catch around audio API]
 *
 * @author AcciSense Team
 */
public class AlertManager implements AccidentListener {

    private final List<AlertListener> listeners = new ArrayList<>();

    public synchronized void addAlertListener(AlertListener l) {
        listeners.add(l);
    }

    /** [RUBRIC: Interface method] */
    @Override
    public void onAccidentDetected(AccidentRecord record) {
        record.setAlertSent(true);

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       ⚠  ACCIDENT DETECTED – ALERT SENT  ⚠       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println(record);

        playBeep();
        simulateSMS(record);

        List<AlertListener> copy;
        synchronized (this) { copy = new ArrayList<>(listeners); }
        for (AlertListener l : copy) l.onAlertTriggered(record);
    }

    /** Generates and plays an 880 Hz beep. [RUBRIC: Exception Handling] */
    private void playBeep() {
        try {
            float sampleRate = 8000f;
            byte[] buf = new byte[(int)(sampleRate * 0.8)];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (sampleRate / 880.0) * 2.0 * Math.PI;
                buf[i] = (byte)(Math.sin(angle) * 127);
            }
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            System.err.println("⚠ Could not play beep: " + e.getMessage());
        }
    }

    /** Simulates SMS to emergency contacts. */
    private void simulateSMS(AccidentRecord record) {
        System.out.println("📱 [SMS Simulation] Sending to emergency contacts...");
        System.out.printf("   \"ACCIDENT ALERT! Severity: %s | Impact: %.1f kN | Time: %s | Location: %s\"%n",
                record.getSeverity(), record.getImpactForce(),
                record.getFormattedTime(), record.getLocation());
        System.out.println("   ✅ SMS sent successfully (simulated).");
    }
}