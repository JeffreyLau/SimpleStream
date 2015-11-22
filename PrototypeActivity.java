package com.tjhelmuth.simplestream;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;

public class PrototypeActivity extends AppCompatActivity implements CameraTools.StateCallback {
    private CameraTools mCameraTools;
    private SurfaceView mSurfaceView;
    private boolean unlockedCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prototype);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mCameraTools = new CameraTools();
        SurfaceView sv = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceView = sv;

        if(sv != null) {
            mCameraTools.init(this, this, sv);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] ids = mCameraTools.getCameraIds();
                    mCameraTools.openCamera(ids[0]);
                }
            });
        }
    }

    public static void log(String msg){
        Log.i("ProtypeActivity", msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prototype, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraOpened(CameraDevice device) {
        log("CAMERA JUST GOT OPENEED!!!!!!!! IN THE ACTIVITY!!!@!@!@!");
    }

    @Override
    public void onCaptureSessionConfigured(CameraCaptureSession captureSession) {
        log("OMGGGGGG ON CAPTURE SESSION IS CONFIGURED OMG IN THE ACTIVITY!@!@!");
    }
}
