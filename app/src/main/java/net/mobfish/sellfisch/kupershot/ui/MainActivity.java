package net.mobfish.sellfisch.kupershot.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.mobfish.sellfisch.kupershot.KupershotApplication;
import net.mobfish.sellfisch.kupershot.R;
import net.mobfish.sellfisch.kupershot.core.job.ImageUploadJob;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadCompletedEvent;
import net.mobfish.sellfisch.kupershot.core.job.event.OnUploadProgressEvent;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private final int REQUEST_IMAGE_CAPTURE = 1212;

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
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
                MainActivityPermissionsDispatcher.captureImageWithCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void captureImage() {
        Toast.makeText(MainActivity.this, "Capture", Toast.LENGTH_SHORT).show();
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_IMAGE_CAPTURE);
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
                    if (data.getExtras().get("data") != null) {
                        loadImage(data.getExtras().get("data"));
                    }
                    break;
            }
        }
    }

    private void loadImage(Object cameraImage) {

        try {
            imagePath = compressImage((Bitmap) cameraImage);
            jobManager.addJob(new ImageUploadJob(imagePath));
        } catch (Exception ex) {

        }
    }

    public String compressImage(Bitmap bitmap) {
        try {
            File frazzleDirectory = new File(Environment.getExternalStorageDirectory() + "/mobfish/kupershot/");
            if (frazzleDirectory.exists()) {
                frazzleDirectory.mkdirs();
            }
            File compressedImage = new File(Environment.getExternalStorageDirectory() + "/mobfish/kupershot/kupershot_image.jpg");
            if (compressedImage.exists()) {
                compressedImage.delete();
            }
            compressedImage.createNewFile();
            FileOutputStream compressedOutputStream = new FileOutputStream(compressedImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, compressedOutputStream);
            compressedOutputStream.flush();
            compressedOutputStream.close();
            return compressedImage.getAbsolutePath();
        } catch (Exception ex) {
            return null;
        }
    }

    @Subscribe
    public void onImageUploadProgressEvent(OnUploadProgressEvent event) {
        if (event.isInProgress()) {
            updateProgress(event.getProgressPercentage(), null);
        } else if (event.hasError()) {
            updateProgress(100, event.getErrorMessage());
        }
    }

    @Subscribe
    public void onImageUploadCompleted(OnUploadCompletedEvent event) {
        updateProgress(100, null);
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

    @OnShowRationale(Manifest.permission.CAMERA)
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

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.CAMERA})
    public void onPermissionDenied() {
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
    }
}
