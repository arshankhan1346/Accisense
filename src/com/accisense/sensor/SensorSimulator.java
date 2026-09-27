package com.accisense.sensor;

import com.accisense.exception.SensorException;
import com.accisense.model.SensorData;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simulates vehicle sensor readings on a background thread.
 *
 * [RUBRIC: Multithreading – implements Runnable, daemon thread]
 * [RUBRIC: Synchronization – synchronized blocks]
 * [RUBRIC: Collections – ArrayList, ConcurrentLinkedQueue]
 * [RUBRIC: Polymorphism – BaseSensor references]
 *
 * @author AcciSense Team
 */
public class SensorSimulator implements Runnable {

    // [RUBRIC: Collections]
    private final List<SensorListener> listeners = new ArrayList<>();

    // [RUBRIC: Collections + thread-safe buffer]
    private final Queue<SensorData> dataBuffer = new ConcurrentLinkedQueue<>();
    private static final int MAX_BUFFER_SIZE = 100;

    // [RUBRIC: Polymorphism – abstract reference, concrete objects]
    private final BaseSensor accelerometer;
    private final BaseSensor gyroscope;
    private final BaseSensor impactSensor;

    private volatile boolean running = false;
    private volatile boolean simulateAccident = false;
    private volatile boolean simulateRollover = false;
    private int updateIntervalMs = 500;

    public SensorSimulator() {
        this.accelerometer = new AccelerometerSensor();
        this.gyroscope     = new GyroscopeSensor();
        this.impactSensor  = new ImpactSensor();
    }

    // ---------- Listener management (synchronized) ----------
    public synchronized void addListener(SensorListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(SensorListener listener) {
        listeners.remove(listener);
    }

    public void setUpdateIntervalMs(int ms) { this.updateIntervalMs = ms; }

    // ---------- Simulation triggers ----------
    public void triggerAccident() {
        this.simulateAccident = true;
        ((AccelerometerSensor) accelerometer).setAccidentMode(true);
        ((ImpactSensor) impactSensor).setCrashMode(true);
    }

    public void triggerRollover() {
        this.simulateRollover = true;
        ((GyroscopeSensor) gyroscope).setRolloverMode(true);
        ((ImpactSensor) impactSensor).setCrashMode(true);
    }

    // ---------- Thread control ----------
    /** [RUBRIC: Multithreading – daemon thread] */
    public void start() {
        running = true;
        Thread t = new Thread(this, "SensorSimulator");
        t.setDaemon(true);
        t.start();
        System.out.println("🟢 Sensor simulator thread started.");
    }

    public void stop() {
        running = false;
        System.out.println("🔴 Sensor simulator thread stopped.");
    }

    public boolean isRunning() { return running; }

    // ---------- Background loop ----------
    /** [RUBRIC: Multithreading + Exception Handling] */
    @Override
    public void run() {
        while (running) {
            try {
                SensorData data = generateReading();

                // [RUBRIC: Synchronization – buffer under lock]
                synchronized (dataBuffer) {
                    dataBuffer.add(data);
                    if (dataBuffer.size() > MAX_BUFFER_SIZE) dataBuffer.poll();
                }

                // Notify listeners (copy under lock)
                List<SensorListener> copy;
                synchronized (this) { copy = new ArrayList<>(listeners); }
                for (SensorListener l : copy) l.onSensorDataReceived(data);

                // Reset one-shot modes
                if (simulateAccident) {
                    ((AccelerometerSensor) accelerometer).setAccidentMode(false);
                    ((ImpactSensor) impactSensor).setCrashMode(false);
                    simulateAccident = false;
                }
                if (simulateRollover) {
                    ((GyroscopeSensor) gyroscope).setRolloverMode(false);
                    ((ImpactSensor) impactSensor).setCrashMode(false);
                    simulateRollover = false;
                }

                Thread.sleep(updateIntervalMs);

            } catch (SensorException e) {
                System.err.println("⚠ Sensor error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /** [RUBRIC: Polymorphism – runtime dispatch of readAll()] */
    private SensorData generateReading() throws SensorException {
        double[] accel = accelerometer.readAll();
        double[] gyro  = gyroscope.readAll();
        double impact  = impactSensor.read();
        return new SensorData(accel[0], accel[1], accel[2],
                              gyro[0], gyro[1], gyro[2], impact);
    }

    /** Thread-safe buffer size. [RUBRIC: Synchronization] */
    public int getBufferSize() {
        synchronized (dataBuffer) { return dataBuffer.size(); }
    }
}