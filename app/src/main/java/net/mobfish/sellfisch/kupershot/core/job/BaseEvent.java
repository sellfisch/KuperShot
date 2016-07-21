package net.mobfish.sellfisch.kupershot.core.job;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */

public class BaseEvent {

    private final String EMPTY_STRING = "";
    protected final Exception exception;
    protected final boolean isInProgress;
    protected final boolean isCanceled;

    public BaseEvent(Exception apiException, boolean isInProgress, boolean isCanceled) {
        this.exception = apiException;
        this.isInProgress = isInProgress;
        this.isCanceled = isCanceled;
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean hasError() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }

    public String getErrorMessage() {
        return exception != null ? exception.getMessage() : EMPTY_STRING;
    }
}