package net.mobfish.sellfisch.kupershot.core.job.event;

import net.mobfish.sellfisch.kupershot.core.job.BaseEvent;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */
public class OnUploadProgressEvent extends BaseEvent {

    private final long uploaded;
    private final long fileSize;

    public OnUploadProgressEvent(Exception apiException, boolean isInProgress, boolean isCanceled, long uploaded, long fileSize) {
        super(apiException, isInProgress, isCanceled);
        this.uploaded = uploaded;
        this.fileSize = fileSize;
    }

    public int getProgressPercentage() {
        return (int) (((double) uploaded / (double) fileSize) * 100);
    }
}
