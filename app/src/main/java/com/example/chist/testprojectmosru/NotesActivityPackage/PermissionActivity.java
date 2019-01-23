package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.chist.testprojectmosru.application.LaunchApplication;
import com.example.chist.testprojectmosru.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilya on 18.08.18.
 */

public class PermissionActivity extends AppCompatActivity {

    public static final int CONFIG_REQUEST_CODE = 1562;
    private static final int PERMISSION_REQUEST_CODE = 1563;
    LTPermissionRequest mRequestInProgress;
    private List<LTPermissionRequest> mRequests = new ArrayList<>();

    public void requestPermission(@NonNull String permission, PermissionHandler resultHandler) {
        if (resultHandler == null) {
            return;
        }
        mRequests.add(new LTPermissionRequest(permission, resultHandler));
        checkPermissions();
    }

    private void checkPermissions() {
        for (int i = mRequests.size() - 1; i >= 0; i--) {
            LTPermissionRequest request = mRequests.get(i);
            if (check(request.permission)) {
                if (request.permissionHandler != null)
                    request.permissionHandler.onPermissionGranted();
                mRequests.remove(request);
            } else {
                if (mRequestInProgress != null)
                    return;
                mRequestInProgress = mRequests.remove(i);
                ActivityCompat.requestPermissions(PermissionActivity.this,
                        new String[]{mRequestInProgress.permission}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && permissions.length > 0 && mRequestInProgress != null) {
            if (permissions[0].equalsIgnoreCase(mRequestInProgress.permission)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mRequestInProgress.permissionHandler != null)
                        mRequestInProgress.permissionHandler.onPermissionGranted();
                    mRequestInProgress = null;
                    checkPermissions();
                }
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, mRequestInProgress.permission)) {
                        if (mRequestInProgress.permissionHandler != null)
                            mRequestInProgress.permissionHandler.onPermissionDenied();
                        mRequestInProgress = null;
                        checkPermissions();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PermissionActivity.this);
                        alertDialogBuilder.setMessage(getMessageResId(mRequestInProgress.permission))
                                .setCancelable(false)
                                .setPositiveButton(R.string.permission_dialog_yes, (DialogInterface.OnClickListener) (dialog, which) -> {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", PermissionActivity.this.getPackageName(), null);
                                    intent.setData(uri);
                                    PermissionActivity.this.startActivityForResult(intent, CONFIG_REQUEST_CODE);
                                }).setNegativeButton(R.string.permission_dialog_no, (dialog, which) -> {
                            if (mRequestInProgress.permissionHandler != null)
                                mRequestInProgress.permissionHandler.onPermissionDenied();
                            mRequestInProgress = null;
                            checkPermissions();
                        }).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private int getMessageResId(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.turn_on_permission_storage;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return R.string.turn_on_permission_storage;
            case Manifest.permission.GET_ACCOUNTS:
                return R.string.turn_on_permission_contacts;
            case Manifest.permission.CAMERA:
                return R.string.turn_on_permission_camera;
            default:
                return R.string.turn_on_permission;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIG_REQUEST_CODE && mRequestInProgress != null) {
            requestPermission(mRequestInProgress.permission, mRequestInProgress.permissionHandler);
            mRequestInProgress = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface PermissionHandler {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    private class LTPermissionRequest {
        final String permission;
        final PermissionHandler permissionHandler;

        private LTPermissionRequest(String permission, PermissionHandler permissionHandler) {
            this.permission = permission;
            this.permissionHandler = permissionHandler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LTPermissionRequest that = (LTPermissionRequest) o;

            if (permission != null ? !permission.equals(that.permission) : that.permission != null)
                return false;
            return permissionHandler != null ? permissionHandler.equals(that.permissionHandler) : that.permissionHandler == null;

        }

        @Override
        public int hashCode() {
            int result = permission != null ? permission.hashCode() : 0;
            result = 31 * result + (permissionHandler != null ? permissionHandler.hashCode() : 0);
            return result;
        }
    }

    public static boolean check(String permission) {
        return (ContextCompat.checkSelfPermission(LaunchApplication.getInstance(), permission) == PackageManager.PERMISSION_GRANTED);
    }
}

