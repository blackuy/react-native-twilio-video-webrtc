/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.uimanager.ThemedReactContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import tvi.webrtc.Camera2Enumerator;


public class TwilioLocalVideoView extends RNVideoViewGroup {
    public String trackId = "";
    public boolean enabled = false;
    private final SnapshotVideoSink snapshotSink = new SnapshotVideoSink();
    private static final String TAG = "TwilioLocalVideoView";

    public TwilioLocalVideoView(ThemedReactContext context) {
        super(context);
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
        Log.i(TAG,  "onDetachedFromWindow: Attempting to clean up");
        setEnabled(this.enabled);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if(trackId == null )
        {
            Log.i(TAG, "Skipping setEnabled because trackId not set");
        }
        if(this.enabled)
            this.addSinks();
        else
            this.removeSinks();

        CustomTwilioVideoView.setLocalVideoTrackStatus(this.trackId, this.enabled);
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

    private void removeSinks() {
        CustomTwilioVideoView.removeLocalSink(this.getSurfaceViewRenderer(), this.trackId);
        CustomTwilioVideoView.removeSnapshotSink(this.snapshotSink, this.trackId);
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
