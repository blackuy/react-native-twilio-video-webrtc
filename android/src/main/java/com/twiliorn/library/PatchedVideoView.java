/**
 * Component for patching black screen bug coming from Twilio VideoView
 * Authors:
 * Aaron Alaniz (@aaalaniz) <aaron.a.alaniz@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.twilio.video.VideoView;

import tvi.webrtc.VideoFrame;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/*
 * VideoView that notifies Listener of the first frame rendered and the first frame after a reset
 * request. It also listens for frame capture events.
 */
public class PatchedVideoView extends VideoView {

    private boolean notifyFrameRendered = false;
    private boolean captureThisFrame = false;
    private Listener listener;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public PatchedVideoView(Context context) {
        super(context);
    }

    public PatchedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFrame(VideoFrame frame) {
        if (notifyFrameRendered) {
            notifyFrameRendered = false;
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFirstFrame();
                }
            });
        }
        super.onFrame(frame);

        if (captureThisFrame) {
            captureThisFrame = false;
            saveVideoFrame(frame);
        }
    }

    /**
     * Create an NV21Buffer with the same pixel content as the given I420 buffer. Ripped from here: https://stackoverflow.com/a/55158228
     */
    private byte[] createNV21Data(VideoFrame.I420Buffer i420Buffer) {
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        final int chromaStride = width;
        final int chromaWidth = (width + 1) / 2;
        final int chromaHeight = (height + 1) / 2;
        final int ySize = width * height;
        final ByteBuffer nv21Buffer = ByteBuffer.allocateDirect(ySize + chromaStride * chromaHeight);
        // We don't care what the array offset is since we only want an array that is direct.
        @SuppressWarnings("ByteBufferBackingArray") final byte[] nv21Data = nv21Buffer.array();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final byte yValue = i420Buffer.getDataY().get(y * i420Buffer.getStrideY() + x);
                nv21Data[y * width + x] = yValue;
            }
        }
        for (int y = 0; y < chromaHeight; ++y) {
            for (int x = 0; x < chromaWidth; ++x) {
                final byte uValue = i420Buffer.getDataU().get(y * i420Buffer.getStrideU() + x);
                final byte vValue = i420Buffer.getDataV().get(y * i420Buffer.getStrideV() + x);
                nv21Data[ySize + y * chromaStride + 2 * x + 0] = vValue;
                nv21Data[ySize + y * chromaStride + 2 * x + 1] = uValue;
            }
        }
        return nv21Data;
    }

    private void sendEvent(String event, WritableMap params) {
        ((ReactContext) this.getContext()).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }

    private void saveVideoFrame(VideoFrame frame) {
        frame.retain();
        Context context = this.getContext();
        String fileId = UUID.randomUUID().toString();
        String filename = "rntframe-" + fileId + ".jpeg";
        VideoFrame.I420Buffer i420Buffer = frame.getBuffer().toI420();
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        byte[] nv21Data = createNV21Data(i420Buffer);
        YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        byte[] imageBytes = out.toByteArray();
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            // write image to disk
            fos.write(imageBytes);
            Log.d(TwilioPackage.TAG, "saved frame to " + filename);
            // send event to JS w/ filename
            WritableMap params = Arguments.createMap();
            params.putString("filename", filename);
            sendEvent("onFrameCaptured", params);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            frame.release();
        }
    }

    public void notifyCaptureFrame() {
        this.captureThisFrame = true;
    }

    /*
     * Set your listener
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /*
     * Reset the listener so next frame rendered results in callback
     */
    public void resetListener() {
        notifyFrameRendered = true;
    }

    public interface Listener {
        void onFirstFrame();
    }
}
