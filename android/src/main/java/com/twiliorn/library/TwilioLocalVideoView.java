/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoTrack;

import tvi.webrtc.Camera2Enumerator;


public class TwilioLocalVideoView extends RNVideoViewGroup {
    public String trackId = "";

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
        CustomTwilioVideoView.addLocalSink(this.getSurfaceViewRenderer(), trackId);
    }

    public void setEnabled(boolean enabled)
    {
        if(trackId != null)
            CustomTwilioVideoView.setLocalVideoTrackStatus(this.trackId, enabled);
        else
            Log.i(TAG, "Skipping setEnabled because trackId not set");
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
