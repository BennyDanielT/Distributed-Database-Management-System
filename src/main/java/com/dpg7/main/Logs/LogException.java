package com.dpg7.main.Logs;

public final class LogException extends Exception
{

    private final String errorMessage;

    public LogException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "Log Exception{" +
                "Exception:" + errorMessage + '\'' +
                '}';
    }
}