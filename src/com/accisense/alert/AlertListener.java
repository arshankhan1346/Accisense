package com.accisense.alert;

import com.accisense.model.AccidentRecord;

/**
 * Interface for alert events.
 * [RUBRIC: Interface implementation]
 *
 * @author AcciSense Team
 */
public interface AlertListener {
    /** Called when an alert is triggered. */
    void onAlertTriggered(AccidentRecord record);
}