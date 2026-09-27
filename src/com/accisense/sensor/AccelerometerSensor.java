package com.accisense.sensor;

import com.accisense.exception.SensorException;
import java.util.Random;

/**
 * Accelerometer sensor – measures vehicle acceleration.
 * [RUBRIC: Inheritance + Polymorphism]
 *
 * @author AcciSense Team
 */
public class AccelerometerSensor extends BaseSensor {

    private final Random random = new Random();
    private boolean accidentMode = false;

    public AccelerometerSensor() {
        super("Accelerometer");
    }

    /** Enables crash simulation mode (high spikes). */
    public void setAccidentMode(boolean mode) {
        this.accidentMode = mode;
    }

    @Override
    public double read() throws SensorException {
        if (!active) throw new SensorException(name, "Sensor is inactive");
        return accidentMode
                ? 25.0 + random.nextGaussian() * 5
                : random.nextGaussian() * 1.5;
    }

    @Override
    public double[] readAll() throws SensorException {
        if (!active) throw new SensorException(name, "Sensor is inactive");
        double[] v = new double[3];
        if (accidentMode) {
            v[0] = 25.0 + random.nextGaussian() * 5;
            v[1] = random.nextGaussian() * 3;
            v[2] = -15.0 + random.nextGaussian() * 4;
        } else {
            v[0] = random.nextGaussian() * 1.5;
            v[1] = random.nextGaussian() * 0.8;
            v[2] = 9.8 + random.nextGaussian() * 0.3;  // gravity
        }
        return v;
    }
}