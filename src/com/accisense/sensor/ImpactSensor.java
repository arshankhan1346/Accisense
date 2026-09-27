package com.accisense.sensor;

import com.accisense.exception.SensorException;
import java.util.Random;

/**
 * Impact sensor – measures collision force.
 * [RUBRIC: Inheritance + Polymorphism]
 *
 * @author AcciSense Team
 */
public class ImpactSensor extends BaseSensor {

    private final Random random = new Random();
    private boolean crashMode = false;

    public ImpactSensor() {
        super("ImpactSensor");
    }

    /** Enables crash simulation mode (80–120 kN). */
    public void setCrashMode(boolean mode) {
        this.crashMode = mode;
    }

    @Override
    public double read() throws SensorException {
        if (!active) throw new SensorException(name, "Sensor is inactive");
        return crashMode
                ? 80.0 + random.nextDouble() * 40
                : random.nextDouble() * 5;
    }

    @Override
    public double[] readAll() throws SensorException {
        double val = read();
        return new double[]{val, val, val};  // single-axis sensor
    }
}