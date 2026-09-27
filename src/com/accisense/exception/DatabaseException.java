package com.accisense.exception;

/**
 * Custom exception for database errors.
 * [RUBRIC: Exception Handling + Inheritance]
 *
 * @author AcciSense Team
 */
public class DatabaseException extends Exception {

    private final int errorCode;

    public DatabaseException(String message, int errorCode) {
        super("DB Error #" + errorCode + ": " + message);
        this.errorCode = errorCode;
    }

    public DatabaseException(String message, Throwable cause) {
        super("DB Error: " + message, cause);
        this.errorCode = -1;
    }

    public int getErrorCode() { return errorCode; }
}