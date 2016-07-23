package net.mobfish.sellfisch.kupershot.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.mobfish.sellfisch.kupershot.KupershotApplication;
import net.mobfish.sellfisch.kupershot.R;
import net.mobfish.sellfisch.kupershot.core.job.ImageUploadJob;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadCompletedEvent;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadProgressEvent;
import net.mobfish.sellfisch.kupershot.util.ImageUtility;

import java.io.File;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private final int REQUEST_IMAGE_CAPTURE = 1212;
    private final String TAG = "MAIN";

    @Inject
    Bus bus;
    @Inject
    JobManager jobManager;

    private String imagePath;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((KupershotApplication) getApplication()).getComponent().inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermissionsDispatcher.captureImageWithCheck(MainActivity.this);
            }
        });

        WebView webView=(WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://live.mob.fish?view=mobil");
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void captureImage() {
        try {
            File frazzleDirectory = new File(Environment.getExternalStorageDirectory() + ImageUtility.IMAGE_DIRECTORY);
            if (frazzleDirectory.exists()) {
                frazzleDirectory.mkdirs();
            }

            File originalImageCopy = new File(Environment.getExternalStorageDirectory() + ImageUtility.IMAGE_DIRECTORY + ImageUtility.IMAGE_NAME);
            if (originalImageCopy.exists()) {
                originalImageCopy.delete();
            }
            originalImageCopy.createNewFile();

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory() + ImageUtility.IMAGE_DIRECTORY + ImageUtility.IMAGE_NAME)));
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    loadImage();
                    break;
            }
        }
    }

    private void loadImage() {
        try {
            Toast.makeText(this, "Preparing...", Toast.LENGTH_LONG).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imagePath = ImageUtility.compressImage();
                            jobManager.addJob(new ImageUploadJob(imagePath));
                        }
                    });
                }
            }).start();

        } catch (Exception ex) {
            Log.i(TAG, "loadImage exception" + ex.getMessage());
        }
    }

    @Subscribe
    public void onImageUploadProgressEvent(OnUploadProgressEvent event) {
        Log.i(TAG, "Progress call");
        if (event.isInProgress()) {
            updateProgress(event.getProgressPercentage(), null);
        } else if (event.hasError()) {
            updateProgress(100, event.getErrorMessage());
        }
    }

    @Subscribe
    public void onImageUploadCompleted(OnUploadCompletedEvent event) {
        Log.i(TAG, "Completed call");
        updateProgress(100, null);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Toast.makeText(this, R.string.image_uploaded, Toast.LENGTH_SHORT).show();
    }

    private void updateProgress(int percentage, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.uploading);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.setProgress(percentage);
            progressDialog.setMessage(TextUtils.isEmpty(message) ? (percentage + "% " + getString(R.string.uploaded)) : message);
            progressDialog.show();
        }

        progressDialog.setProgress(percentage);
        progressDialog.setMessage(TextUtils.isEmpty(message) ? (percentage + "% " + getString(R.string.uploaded)) : message);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.CAMERA})
    public void onCameraShowRationale(final PermissionRequest permissionRequest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_explanation);
        builder.setMessage(R.string.capture_image_permission_explanation);
        builder.setPositiveButton(R.string.ok_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                permissionRequest.proceed();
            }
        });
        builder.setNegativeButton(R.string.deny_for_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                permissionRequest.cancel();
            }
        });
        builder.create().show();
    }

    @OnShowRationale({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void onStorageShowRationale(final PermissionRequest permissionRequest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_explanation);
        builder.setMessage(R.string.storage_permission_explanation);
        builder.setPositiveButton(R.string.ok_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                permissionRequest.proceed();
            }
        });
        builder.setNegativeButton(R.string.deny_for_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                permissionRequest.cancel();
            }
        });
        builder.create().show();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void onPermissionDenied() {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
    }
}
