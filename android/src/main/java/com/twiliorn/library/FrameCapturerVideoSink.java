package com.twiliorn.library;

import android.util.Log;

import com.facebook.react.bridge.ReactContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import tvi.webrtc.VideoFrame;
import tvi.webrtc.VideoSink;

public class FrameCapturerVideoSink implements VideoSink {
    private final AtomicBoolean captureThisFrame = new AtomicBoolean(false);
    // TODO: FIX THIS. ITS A POTENTIAL MEMORY LEAK.
    private ReactContext context;
    // choosing to use single thread since limited i/o resources may be a bottleneck anyways
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String filename = "";

    @Override
    public void onFrame(VideoFrame frame) {
        if (frame == null) {
            Log.w(TwilioPackage.TAG, "Cannot capture null frame.");
            return;
        }

        if (captureThisFrame.compareAndSet(true, false)) {
            frame.retain(); // retain frame so we can save it on background thread, bg thread will handle releasing
            Log.d(TwilioPackage.TAG, "Capturing frame on background thread.");
            String filename = this.filename;
            // save frame on background thread
            executorService.execute(() -> {
                Utils.saveVideoFrame(frame, context, filename);
            });
        }
    }

    public void setContext(ReactContext context) {
        this.context = context;
    }

    public void captureFrame(String filename) {
        Log.d(TwilioPackage.TAG, "Setting captureThisFrame flag to true for file " + filename);
        this.filename = filename;
        this.captureThisFrame.set(true);
    }
}
