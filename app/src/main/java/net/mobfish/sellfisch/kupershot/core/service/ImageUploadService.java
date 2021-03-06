package net.mobfish.sellfisch.kupershot.core.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.squareup.otto.Bus;

import net.mobfish.sellfisch.kupershot.R;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadCompletedEvent;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadProgressEvent;

import org.joda.time.DateTime;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by Bajic Dusko (www.bajicdusko.com) on 21-Jul-16.
 */
public class ImageUploadService {

    @Inject
    Bus bus;
    @Inject
    Context context;

    private final int PROGRESS_TIMEOUT = 200;
    private long lastUpdateMoment = 0;

    @Inject
    public ImageUploadService() {

    }

    public void uploadImage(String imageFilePath) {
        File file = new File(imageFilePath);

        Ion.with(context)
                .load("POST", "https://live.mob.fish/incomingUpload.php")
                .uploadProgressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long uploaded, long fileLength) {
                        long nowMilis = DateTime.now().getMillis();
                        if (nowMilis > lastUpdateMoment + PROGRESS_TIMEOUT) {
                            lastUpdateMoment = nowMilis;
                            Log.i("ImageUploadService", "Progress called");
                            bus.post(new OnUploadProgressEvent(null, true, false, uploaded, fileLength));
                        }
                        if (uploaded >= fileLength) {
                            bus.post(new OnUploadProgressEvent(null, true, false, 100, 100));
                        }
                    }
                })
                .setTimeout(5 * 60 * 1000)
                .setHeader("Host", "live.mob.fish")
                .setHeader("Content-Length", String.valueOf(file.length()))
                .setHeader("Connection", "keep-alive")
                .setMultipartContentType("multipart/form-data;")
                .setMultipartFile("image\"; filename=\"" + file.getName() + "", "image/jpg", file)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (TextUtils.isEmpty(result) && e == null) {
                            Log.i("ImageUploadService", "Calling completed");
                            bus.post(new OnUploadCompletedEvent(null, false, false));
                        } else if (e != null) {
                            Log.i("ImageUploadService", "Exception " + e.getMessage());
                            bus.post(new OnUploadProgressEvent(new Exception(e.getMessage()), false, false, 100, 100));
                        } else if (!TextUtils.isEmpty(result)) {
                            Log.i("ImageUploadService", "Unknown Exception " + context.getString(R.string.error_occurred) + ", RESULT: " + result);
                            bus.post(new OnUploadCompletedEvent(null, false, false));
                            //.post(new OnUploadProgressEvent(new Exception(context.getString(R.string.error_occurred)), false, false, 100, 100));
                        }
                    }
                });
    }
}
