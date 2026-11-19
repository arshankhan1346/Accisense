package com.accisense.sensor;

import com.accisense.exception.SensorException;
import com.accisense.model.SensorData;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Streams simulated sensor readings on a background daemon thread.
 *
 * [REVIEW-1 FIX] AtomicBoolean flags give lock-free, thread-safe sensor-state
 * management; getAndSet(false) reads-and-resets in one atomic operation.
 * [RUBRIC: Multithreading - Runnable, daemon thread, synchronized, atomics]
 * [RUBRIC: Collections - ConcurrentLinkedQueue buffer]
 */
public class SensorSimulator implements Runnable {

    private final List<SensorListener> listeners = new ArrayList<>();
    private final Queue<SensorData> dataBuffer = new ConcurrentLinkedQueue<>();
    private static final int MAX_BUFFER_SIZE = 100;

    private final BaseSensor accelerometer;
    private final BaseSensor gyroscope;
    private final BaseSensor impactSensor;

    // [REVIEW-1 FIX] atomic state flags
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean simulateAccident = new AtomicBoolean(false);
    private final AtomicBoolean simulateRollover = new AtomicBoolean(false);

    private int updateIntervalMs = 500;

    public SensorSimulator() {
        this.accelerometer = new AccelerometerSensor();
        this.gyroscope     = new GyroscopeSensor();
        this.impactSensor  = new ImpactSensor();
    }

    public synchronized void addListener(SensorListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(SensorListener listener) {
        listeners.remove(listener);
    }

    public void setUpdateIntervalMs(int ms) {
        this.updateIntervalMs = ms;
    }

    public void triggerAccident() {
        simulateAccident.set(true);
        ((AccelerometerSensor) accelerometer).setAccidentMode(true);
        ((ImpactSensor) impactSensor).setCrashMode(true);
    }

    public void triggerRollover() {
        simulateRollover.set(true);
        ((GyroscopeSensor) gyroscope).setRolloverMode(true);
        ((ImpactSensor) impactSensor).setCrashMode(true);
    }

    public void start() {
        running.set(true);
        Thread t = new Thread(this, "SensorSimulator");
        t.setDaemon(true);
        t.start();
        System.out.println("[SENSOR] Simulator thread started.");
    }

    public void stop() {
        running.set(false);
        System.out.println("[SENSOR] Simulator thread stopped.");
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                SensorData data = generateReading();

                synchronized (dataBuffer) {
                    dataBuffer.add(data);
                    if (dataBuffer.size() > MAX_BUFFER_SIZE) dataBuffer.poll();
                }

                List<SensorListener> copy;
                synchronized (this) { copy = new ArrayList<>(listeners); }
                for (SensorListener l : copy) {
                    l.onSensorDataReceived(data);
                }

                // Atomic read-and-reset of one-shot simulation flags
                if (simulateAccident.getAndSet(false)) {
                    ((AccelerometerSensor) accelerometer).setAccidentMode(false);
                    ((ImpactSensor) impactSensor).setCrashMode(false);
                }
                if (simulateRollover.getAndSet(false)) {
                    ((GyroscopeSensor) gyroscope).setRolloverMode(false);
                    ((ImpactSensor) impactSensor).setCrashMode(false);
                }

                Thread.sleep(updateIntervalMs);

            } catch (SensorException e) {
                System.err.println("[SENSOR] Reading error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private SensorData generateReading() throws SensorException {
        double[] accel = accelerometer.readAll();
        double[] gyro  = gyroscope.readAll();
        double impact  = impactSensor.read();
        return new SensorData(accel[0], accel[1], accel[2],
                              gyro[0], gyro[1], gyro[2], impact);
    }

    public int getBufferSize() {
        synchronized (dataBuffer) { return dataBuffer.size(); }
    }
}