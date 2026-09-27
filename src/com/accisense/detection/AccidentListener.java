package com.accisense.detection;

import com.accisense.model.AccidentRecord;

/**
 * Interface for accident detection events.
 * [RUBRIC: Interface implementation]
 *
 * @author AcciSense Team
 */
public interface AccidentListener {
    /** Called when an accident is detected. */
    void onAccidentDetected(AccidentRecord record);
}