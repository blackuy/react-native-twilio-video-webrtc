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
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoTrack;

import tvi.webrtc.Camera1Enumerator;


public class TwilioLocalView extends RNVideoViewGroup {
    public String cameraId = "";

    private static final String TAG = "TwilioLocalView";

    private String _trackName = "";
    private CameraCapturer _cameraCapturer = null;
    private VideoTrack _localVideoTrack = null;
    private ThemedReactContext _reactContext;


    public TwilioLocalView(ThemedReactContext context) {
        super(context);
        logStr(getAvaliableCameras());
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

    private String[] getAvaliableCameras() {
        Camera1Enumerator enumerator = new Camera1Enumerator();
        return enumerator.getDeviceNames();
    }

    private CameraCapturer createCameraCapturer(Context context, String cameraId) {
        CameraCapturer newCameraCapturer = null;
        try {
            newCameraCapturer = new CameraCapturer(
                    context,
                    cameraId,
                    new CameraCapturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() {
                        }

                        @Override
                        public void onCameraSwitched(String newCameraId) {

                        }


                        @Override
                        public void onError(int i) {
                            Log.i("CustomTwilioVideoView", "Error getting camera");
                        }
                    }
            );
            return newCameraCapturer;
        } catch (Exception e) {
            Log.d(TAG, "createCameraCapturer: " + e.getMessage());
            return null;
        }
    }

    private VideoFormat buildVideoFormat() {
        return new VideoFormat(VideoDimensions.CIF_VIDEO_DIMENSIONS, 15);
    }

    private VideoTrack createLocalVideo(ReactContext context, boolean enableVideo, CameraCapturer cameraCapturer, String cameraId) {

        VideoTrack videoTrack = LocalVideoTrack.create(getContext(), enableVideo, cameraCapturer, buildVideoFormat());

        return videoTrack;
    }
}
