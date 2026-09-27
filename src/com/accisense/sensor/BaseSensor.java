package com.accisense.sensor;

import com.accisense.exception.SensorException;

/**
 * Abstract base class for all vehicle sensors.
 * [RUBRIC: Inheritance – parent class]
 * [RUBRIC: Polymorphism – abstract methods overridden by subclasses]
 *
 * @author AcciSense Team
 */
public abstract class BaseSensor {

    protected String name;
    protected boolean active;

    public BaseSensor(String name) {
        this.name = name;
        this.active = true;
    }

    /** Reads a single value. Overridden by each sensor type. */
    public abstract double read() throws SensorException;

    /** Reads 3-axis values [x, y, z]. Overridden by each sensor type. */
    public abstract double[] readAll() throws SensorException;

    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + (active ? " [ACTIVE]" : " [INACTIVE]");
    }
}