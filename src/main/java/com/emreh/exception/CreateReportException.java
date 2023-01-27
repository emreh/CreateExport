package com.emreh.exception;

public class CreateReportException extends RuntimeException{

    private static final long serialVersionUID = 4430659850934452322L;

    public CreateReportException() {
    }

    public CreateReportException(String message) {
        super(message);
    }

    public CreateReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateReportException(Throwable cause) {
        super(cause);
    }

    public CreateReportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
