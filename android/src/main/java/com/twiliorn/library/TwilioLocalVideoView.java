/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.ThemedReactContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import tvi.webrtc.Camera2Enumerator;


public class TwilioLocalVideoView extends RNVideoViewGroup {
    private final ReactContext context;
    public String trackId = "";
    public boolean enabled = false;
    private final SnapshotVideoSink snapshotSink = new SnapshotVideoSink();
    private static final String TAG = "TwilioLocalVideoView";

    public TwilioLocalVideoView(ThemedReactContext context) {
        super(context);
        this.context = context;
        logCameras(getAvaliableCameras(context));
        logTracks(getAvaliableLocalVideoTracks());
    }

    public void setTrackId(@Nullable String trackId, @NonNull boolean enabled) {
        Log.i(TAG, "Initialize Twilio Local video");
        this.trackId = trackId;
        this.setEnabled(enabled);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.i(TAG,  "onDetachedFromWindow: Attempting to clean up");
        this.setEnabled(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(TAG,  "onAttachedToWindow: Attempting to set enabled to: " + (this.enabled ? "True": "False"));
        this.setEnabled(this.enabled);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if(trackId == null )
        {
            Log.i(TAG, "Skipping setEnabled because trackId not set");
        }
        
        if(this.enabled) {
            CustomTwilioVideoView.publishLocalVideo(this.trackId, this.context);
            addSinks();
            CustomTwilioVideoView.setLocalVideoTrackStatus(this.trackId, this.enabled);
        } else {
            CustomTwilioVideoView.unpublishLocalVideo(this.trackId);
        }
    }

    public void takeSnapshot(Callback<ImageFileReference> callback) {
        this.snapshotSink.takeSnapshot(bitmap -> {
            try {
                File file = new File(
                        this.getContext().getCacheDir(),
                        "TempImage");
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.close();
                final ImageFileReference reference = new ImageFileReference(file.getAbsolutePath(),
                        bitmap.getWidth(),
                        bitmap.getHeight());
                callback.invoke(reference);
            }  catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void addSinks() {
        CustomTwilioVideoView.addLocalSink(this.getSurfaceViewRenderer(), this.trackId);
        CustomTwilioVideoView.addSnapshotSink(this.snapshotSink, this.trackId);
    }

    private void logCameras(String[] strings) {
        Log.i(TAG, "Available Cameras");
        for (String str: strings){
            Log.i(TAG, "\tCamera: " + str);
        }
    }

    private void logTracks(String[] strings) {
        Log.i(TAG, "Available Tracks");
        for (String str: strings){
            Log.i(TAG, "\tTrack: " + str);
        }
    }

    private String[] getAvaliableCameras(Context context) {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        return enumerator.getDeviceNames();
    }


    private String[] getAvaliableLocalVideoTracks() {
        return CustomTwilioVideoView.getAvailableLocalTracks();
    }


    public void release() {
        this.getSurfaceViewRenderer().release();
    }
}
