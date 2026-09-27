package com.accisense.sensor;

import com.accisense.exception.SensorException;
import java.util.Random;

/**
 * Gyroscope sensor – measures angular velocity.
 * [RUBRIC: Inheritance + Polymorphism]
 *
 * @author AcciSense Team
 */
public class GyroscopeSensor extends BaseSensor {

    private final Random random = new Random();
    private boolean rolloverMode = false;

    public GyroscopeSensor() {
        super("Gyroscope");
    }

    /** Enables rollover simulation mode (high spin). */
    public void setRolloverMode(boolean mode) {
        this.rolloverMode = mode;
    }

    @Override
    public double read() throws SensorException {
        if (!active) throw new SensorException(name, "Sensor is inactive");
        return rolloverMode
                ? 200.0 + random.nextGaussian() * 30
                : random.nextGaussian() * 5;
    }

    @Override
    public double[] readAll() throws SensorException {
        if (!active) throw new SensorException(name, "Sensor is inactive");
        double[] v = new double[3];
        if (rolloverMode) {
            v[0] = 200.0 + random.nextGaussian() * 30;
            v[1] = random.nextGaussian() * 20;
            v[2] = 180.0 + random.nextGaussian() * 30;
        } else {
            v[0] = random.nextGaussian() * 5;
            v[1] = random.nextGaussian() * 5;
            v[2] = random.nextGaussian() * 5;
        }
        return v;
    }
}