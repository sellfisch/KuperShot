package net.mobfish.sellfisch.kupershot.core.job;

import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.squareup.otto.Bus;

import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadProgressEvent;
import net.mobfish.sellfisch.kupershot.core.service.ImageUploadService;

import javax.inject.Inject;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */
public class ImageUploadJob extends Job {

    @Inject
    ImageUploadService mediaUploadService;
    @Inject
    Bus bus;
    @Inject
    JobManager jobManager;

    private final String imageFilePath;

    public ImageUploadJob(String imageFilePath) {
        super(new Params(0));
        this.imageFilePath = imageFilePath;
    }

    @Override
    public void onAdded() {
        bus.post(new OnUploadProgressEvent(null, true, false, 0, 1));
    }

    @Override
    public void onRun() throws Throwable {
        Log.i("ImageUploadJob", "running");
        mediaUploadService.uploadImage(imageFilePath);
    }

    @Override
    protected void onCancel() {
        bus.post(new OnUploadProgressEvent(null, false, true, 0, 1));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
