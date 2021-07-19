/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoTrack;

import tvi.webrtc.Camera1Enumerator;
import tvi.webrtc.Camera2Enumerator;


public class TwilioLocalView extends RNVideoViewGroup {
    public String cameraId = "";

    private static final String TAG = "TwilioLocalView";

    private String _trackName = "";
    private Camera2Capturer _cameraCapturer = null;
    private VideoTrack _localVideoTrack = null;
    private ThemedReactContext _reactContext;


    public TwilioLocalView(ThemedReactContext context) {
        super(context);
        logStr(getAvaliableCameras(context));
        this._reactContext = context;
    }

    public void setCameraId(@Nullable String cameraId) {
        Log.i(TAG, "Initialize Twilio Local video");
        this.cameraId = cameraId;

        _cameraCapturer = createCameraCapturer(_reactContext, this.cameraId);

        VideoTrack videoTrack = createLocalVideo(
                _reactContext,
                true,
                _cameraCapturer,
                cameraId);
        videoTrack.addSink(this.getSurfaceViewRenderer());
        _trackName = videoTrack.getName();

        this._localVideoTrack = videoTrack;
    }

    private void logStr(String[] strings) {
        Log.i(TAG, "Available Cameras");
        for (String str: strings){
            Log.i(TAG, "\tCamera: " + str);
        }
    }

    private String[] getAvaliableCameras(Context context) {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);
        return enumerator.getDeviceNames();
    }

    private Camera2Capturer createCameraCapturer(Context context, String cameraId) {
        Camera2Capturer newCameraCapturer = null;
        try {
            newCameraCapturer = new Camera2Capturer(context, cameraId, new Camera2Capturer.Listener() {
                @Override
                public void onFirstFrameAvailable() {

                }

                @Override
                public void onCameraSwitched(String newCameraId) {

                }

                @Override
                public void onError(Camera2Capturer.Exception camera2CapturerException) {

                }
            });
            return newCameraCapturer;
        } catch (Exception e) {
            Log.d(TAG, "createCameraCapturer: " + e.getMessage());
            return null;
        }
    }

    private VideoTrack createLocalVideo(ReactContext context, boolean enableVideo, Camera2Capturer cameraCapturer, String cameraId) {
        VideoTrack videoTrack = LocalVideoTrack.create(_reactContext, enableVideo, cameraCapturer, buildVideoFormat());
        return videoTrack;
    }

    private VideoFormat buildVideoFormat() {
        return new VideoFormat(VideoDimensions.CIF_VIDEO_DIMENSIONS, 15);
    }

}
