package com.tjhelmuth.simplestream;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tyler on 11/19/2015.
 */
public class CameraTools {
    public enum State {
        UNINITIALIZED,
        INITIALIZED,
        CAMERA_OPEN,
        CAPTURE_SESSION_OPEN,
    }
    public static String LOG_TAG = "CameraTools";
    private CameraManager mCameraManager;
    private CameraDevice mCamera;
    private StateCallback mClientCallback;
    private Activity mActivity;
    private SurfaceView mSurfaceView;
    private Surface mCurrSurface;
    private CameraRequestCallback mCameraCallback;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession.StateCallback mCaptureCallback;
    private ImageReader.OnImageAvailableListener mImageReaderCallback;
    private MediaCodec mMediaCodec;
    private VideoParams mVideoParams;

    public void init(Activity activity, StateCallback callback, SurfaceView surfaceView) {
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mClientCallback = callback;
        mCameraCallback = new CameraRequestCallback(this);
        mCaptureCallback = new CaptureSessionCallback(this);
        mActivity = activity;
        mSurfaceView = surfaceView;

        mVideoParams = VideoParams.DEFAULT_PARAMS;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(mVideoParams.getMimeType());
            if(mMediaCodec == null){
                throw new Exception();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not find a suitable media codec for format: " + mVideoParams.toString());
            return;
        }

        Log.i(LOG_TAG, "Found media codec: " + mMediaCodec.toString());


        mMediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                Log.i(LOG_TAG, "in onInputBufferAvailable");
                ByteBuffer iBuffer = codec.getInputBuffer(index);
                codec.queueInputBuffer(index, 0, iBuffer.capacity(), 60, 0);
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                Log.i(LOG_TAG, "in onOutputBufferAvailable");
                codec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(MediaCodec codec, MediaCodec.CodecException e) {
                Log.i(LOG_TAG, "in onError");
            }

            @Override
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                Log.i(LOG_TAG, "in onOutputFormatChanged");
            }
        });
    }


    public void openCamera(String id) {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "Camera permission not granted.");
            return;
        }

        try {
            mCameraManager.openCamera(id, mCameraCallback, new Handler());
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "Camera access exception opening camera: " + id);
        }
    }

    public CameraCharacteristics getCameraCharacteristics(){
        CameraCharacteristics camParams = null;
        if(mCamera != null && mCameraManager != null){
            try {
                camParams = mCameraManager.getCameraCharacteristics(mCamera.getId());
            } catch (CameraAccessException e){
                Log.e(LOG_TAG, "Problem accessing camera to get characteristics.");
            }
        }
        return camParams;
    }

    public String[] getCameraIds(){
        String[] ids = null;
        if(mCameraManager != null){
            try {
                ids = mCameraManager.getCameraIdList();
            } catch (CameraAccessException e){
                Log.e(LOG_TAG, "Error accessing the camera while getting the id list.");
            }
        }
        return ids;
    }

    public void createCaptureSession(){
        List<Surface> surfaces = new ArrayList<>();
        mCurrSurface = mSurfaceView.getHolder().getSurface();
        mVideoParams.getVideoFormat().setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaCodec.configure(mVideoParams.getVideoFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mCurrSurface = mMediaCodec.createInputSurface();
        assert mCurrSurface == null;
        surfaces.add(mCurrSurface);
        //surfaces.add(blabla);

        for(String s : mMediaCodec.getCodecInfo().getSupportedTypes()){
            System.out.println("SUPPORTED TYPE: " + s);
            MediaCodecInfo.CodecCapabilities capabilities = mMediaCodec.getCodecInfo().getCapabilitiesForType(s);
            Range<Integer> heights = capabilities.getVideoCapabilities().getSupportedHeights();
            Range<Integer> widths = capabilities.getVideoCapabilities().getSupportedWidths();

            System.out.println("UPPER HEIGHT: " + heights.getUpper() + ", LOWER HEIGHT: " + heights.getLower());
            System.out.println("UPPER Width: " + widths.getUpper() + ", LOWER widtyh: " + widths.getLower());
            for(int i : capabilities.colorFormats){
                System.out.println(String.valueOf(i));
            }
        }

        try {
            mCamera.createCaptureSession(surfaces, mCaptureCallback, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }



    }
    /*********************** vvvv CALLBACK EVENTS vvvv *******************************************/
    public boolean onCameraAdded(CameraDevice camera){
        if(mCamera != null){
            Log.i(LOG_TAG, "Attempted add camera but already have a camera.");
            return false;
        }
        Log.i(LOG_TAG, "Setting Camera: " + camera.getId());
        mCamera = camera;
        if(mClientCallback != null){
            mClientCallback.onCameraOpened(mCamera);
        }
        return true;
    }
    public boolean onCaptureSessionConfigured(CameraCaptureSession captureSession){
        mCaptureSession = captureSession;

        try {
            CaptureRequest.Builder requestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            requestBuilder.addTarget(mCurrSurface);

            mMediaCodec.start();

            captureSession.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {


                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.i(LOG_TAG, "CAPTURE: STARTED");
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                    super.onCaptureProgressed(session, request, partialResult);
                    Log.i(LOG_TAG, "CAPTURE: PROGRESS");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i(LOG_TAG, "CAPTURE: COMPLETE");
                    //mMediaCodec.stop();
                    //mMediaCodec.release();
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.i(LOG_TAG, "CAPTURE: FAILED");

                }
            }, new Handler());

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if(mClientCallback != null){
            mClientCallback.onCaptureSessionConfigured(captureSession);
        }
        return mCaptureSession == null;
    }
    /********************* ^^^^ CALLBACK EVENTS ^^^^ *********************************************/


    public interface StateCallback{
        void onCameraOpened(CameraDevice device);
        void onCaptureSessionConfigured(CameraCaptureSession captureSession);
    }
}
