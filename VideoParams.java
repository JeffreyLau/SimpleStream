package com.tjhelmuth.simplestream;

import android.media.MediaFormat;

/**
 * Created by Tyler on 11/20/2015.
 */
public class VideoParams {
    public static final String DEFAULT_MIMETYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static final int DEFAULT_HEIGHT = 480;
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_FPS = 24;
    public static final VideoParams DEFAULT_PARAMS = new VideoParams(DEFAULT_MIMETYPE, DEFAULT_WIDTH, DEFAULT_HEIGHT);

    private int mWidth;
    private int mHeight;
    private int mFps;
    private MediaFormat mVideoFormat;

    public VideoParams(){
        this(DEFAULT_MIMETYPE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public VideoParams(String mimeType, int mWidth, int mHeight){
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        mVideoFormat = MediaFormat.createVideoFormat(mimeType, mWidth, mHeight);
        mVideoFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT, DEFAULT_HEIGHT);
        mVideoFormat.setInteger(MediaFormat.KEY_MAX_WIDTH, DEFAULT_WIDTH);
        mVideoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        mVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 600000);
        mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, 2135033992);
        mVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, DEFAULT_FPS);
        mVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

    }

    public MediaFormat getVideoFormat(){
        return mVideoFormat;
    }

    public int getmWidth(){
        return mWidth;
    }

    public int getmHeight(){
        return mHeight;
    }

    public void setmWidth(int mWidth){
        this.mWidth = mWidth;
        mVideoFormat.setInteger(MediaFormat.KEY_WIDTH, mWidth);
    }

    public void setmHeight(int mHeight){
        this.mHeight = mHeight;
        mVideoFormat.setInteger(MediaFormat.KEY_HEIGHT, mHeight);
    }

    public String getMimeType(){
        return mVideoFormat.getString(MediaFormat.KEY_MIME);
    }
}
