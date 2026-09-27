package com.accisense.sensor;

import com.accisense.model.SensorData;

/**
 * Interface for receiving sensor data updates.
 * [RUBRIC: Interface implementation]
 *
 * @author AcciSense Team
 */
public interface SensorListener {
    /** Called when new sensor data is available. */
    void onSensorDataReceived(SensorData data);
}