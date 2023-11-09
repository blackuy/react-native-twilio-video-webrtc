package com.twiliorn.library;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import tvi.webrtc.VideoFrame;
import tvi.webrtc.VideoProcessor;
import tvi.webrtc.VideoSink;

/**
 * This custom video processor allows us to capture frames before they are adapted & rendered to the screen.
 * https://github.com/twilio/video-quickstart-android/blob/master/exampleAdvancedCameraCapturer/src/main/java/com/twilio/video/examples/advancedcameracapturer/Photographer.kt
 */
public class CustomVideoProcessor implements VideoProcessor {
    private final AtomicBoolean canCapture = new AtomicBoolean(false);
    private final AtomicBoolean captureThisFrame = new AtomicBoolean(false);
    // TODO: FIX THIS. ITS A POTENTIAL MEMORY LEAK.
    private ReactContext context;
    // choosing to use single thread since limited i/o resources may be a bottleneck anyways
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String filename = "";
    private VideoSink videoSink;

    public CustomVideoProcessor() {}

    public void captureFrame(String filename) {
        if (canCapture.get()) {
            this.filename = filename;
            Log.d(TwilioPackage.TAG, "Setting captureThisFrame flag to true for file " + filename);
            captureThisFrame.set(true);
        }
        else {
            Log.e(TwilioPackage.TAG, "Cannot capture frame. Capturer is not started.");
        }
    }

    public void setContext(ReactContext context) {
        this.context = context;
    }

    @Override
    public void onFrameCaptured(VideoFrame frame, FrameAdaptationParameters parameters) {
        if (frame == null) {
            Log.w(TwilioPackage.TAG, "Cannot process null frame.");
            return;
        }

        if (captureThisFrame.compareAndSet(true, false)) {
            frame.retain(); // retain frame so we can save it on background thread, bg thread will handle releasing
            Log.d(TwilioPackage.TAG, "Capturing frame on background thread.");
            // save frame on background thread
            executorService.execute(() -> {
                Utils.saveVideoFrame(frame, context, this.filename);
            });
        }

        // adapt frame and apply to video view component
        VideoFrame adaptedFrame = VideoProcessor.applyFrameAdaptationParameters(frame, parameters);
        if (adaptedFrame != null) {
            videoSink.onFrame(adaptedFrame);
            adaptedFrame.release();
        } else {
            videoSink.onFrame(frame);
        }
    }

    @Override
    public void onCapturerStarted(boolean isSuccess) {
        if (isSuccess) {
            canCapture.set(true);
        }
    }

    @Override
    public void onCapturerStopped() {
        canCapture.set(false);
        captureThisFrame.set(false);
    }

    @Override
    public void setSink(@Nullable VideoSink videoSink) {
        this.videoSink = videoSink;
    }

    @Override
    public void onFrameCaptured(VideoFrame videoFrame) {
        // noop
    }
}
