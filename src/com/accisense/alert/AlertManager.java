package com.accisense.alert;

import com.accisense.detection.AccidentListener;
import com.accisense.model.AccidentRecord;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlertManager implements AccidentListener {

    private final List<AlertListener> listeners = new ArrayList<>();
    // [FIX 1] Dedicated background executor for blocking alert operations
    private final ExecutorService alertExecutor = Executors.newSingleThreadExecutor();

    public synchronized void addAlertListener(AlertListener l) {
        listeners.add(l);
    }

    @Override
    public void onAccidentDetected(AccidentRecord record) {
        record.setAlertSent(true);

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       ⚠  ACCIDENT DETECTED – ALERT SENT  ⚠       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println(record);

        // [FIX 1] Offload blocking operations so sensor thread is never delayed
        alertExecutor.submit(() -> {
            playBeep();
            simulateSMS(record);

            List<AlertListener> copy;
            synchronized (this) { copy = new ArrayList<>(listeners); }
            for (AlertListener l : copy) l.onAlertTriggered(record);
        });
    }

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

    // [FIX 4] Explicitly label as simulation
    private void simulateSMS(AccidentRecord record) {
        System.out.println("📱 [SIMULATION ONLY] Mock SMS Gateway triggered...");
        System.out.printf("   \"[MOCK SMS] ACCIDENT ALERT! Severity: %s | Impact: %.1f kN | Time: %s | Loc: %s\"%n",
                record.getSeverity(), record.getImpactForce(),
                record.getFormattedTime(), record.getLocation());
        System.out.println("   ✅ Mock SMS dispatched successfully.");
    }
    
    public void shutdown() {
        alertExecutor.shutdown();
    }
}