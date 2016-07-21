package net.mobfish.sellfisch.kupershot.core.job.event;

import net.mobfish.sellfisch.kupershot.core.job.BaseEvent;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */
public class OnUploadCompletedEvent extends BaseEvent {

    public OnUploadCompletedEvent(Exception apiException, boolean isInProgress, boolean isCanceled) {
        super(apiException, isInProgress, isCanceled);
    }
}
