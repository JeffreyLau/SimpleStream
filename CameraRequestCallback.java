package com.tjhelmuth.simplestream;

import android.hardware.camera2.CameraDevice;
import static com.tjhelmuth.simplestream.PrototypeActivity.log;

/**
 * Created by Tyler on 11/19/2015.
 */
public class CameraRequestCallback extends CameraDevice.StateCallback{
    private CameraTools mTools;

    public CameraRequestCallback(CameraTools cameraTools){
        mTools = cameraTools;
    }

    @Override
    public void onOpened(CameraDevice camera) {
        log("CAMERA: " +camera.getId() + "OPENED");
        mTools.onCameraAdded(camera);
        mTools.createCaptureSession();
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        log("CAMERA: " + camera.getId() + "DISCONNECTED");
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        log("CAMERA: " + camera.getId() + " ERROR NUMBER: " + error);
    }
}
