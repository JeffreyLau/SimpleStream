package com.tjhelmuth.simplestream;

import android.hardware.camera2.CameraCaptureSession;
import android.util.Log;

/**
 * Created by Tyler on 11/19/2015.
 */
public class CaptureSessionCallback extends CameraCaptureSession.StateCallback {
    private CameraTools mTools;

    public CaptureSessionCallback(CameraTools tools){
        mTools = tools;
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        Log.i("CameraTools", "CONFIGURED");
        mTools.onCaptureSessionConfigured(session);
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        Log.e("CameraTools", "Capture Session Configuration Error");
    }
}
