/**
 * Component for patching black screen bug coming from Twilio VideoView
 * Authors:
 * Aaron Alaniz (@aaalaniz) <aaron.a.alaniz@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoTextureView;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import androidx.annotation.NonNull;

import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.net.Uri;

import static android.graphics.ImageFormat.NV21;

/*
 * VideoView that notifies Listener of the first frame rendered and the first frame after a reset
 * request.
 */
public class PatchedVideoView extends VideoTextureView {

    private boolean notifyFrameRendered = false;
    private Listener listener;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final AtomicBoolean snapshotRequsted = new AtomicBoolean(false);
    private RCTEventEmitter eventEmitter;
    private int viewId;
    private File outputFile;
    private VideoTextureView videoTextureView;

    public PatchedVideoView(Context context) {
        super(context);
    }

    public PatchedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void renderFrame(I420Frame frame) {
        if (notifyFrameRendered) {
            notifyFrameRendered = false;
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFirstFrame();
                }
            });
        }

        if (snapshotRequsted.compareAndSet(true, false)) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                  final Bitmap bitmap = frame.yuvPlanes == null ?
                          captureBitmapFromTexture(frame) :
                          captureBitmapFromYuvFrame(frame);

                  WritableMap event = new WritableNativeMap();
                  try (FileOutputStream output = new FileOutputStream(outputFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                    String uri = Uri.fromFile(outputFile).toString();
                    event.putString("uri", uri);
                  } catch (final Throwable ex) {
                    event.putString("error", "Error saving snapshot.");
                  }
                  pushEvent("onSnapshot", event);
                }
            });
        }

        super.renderFrame(frame);
    }

    public Bitmap captureBitmapFromTexture(I420Frame frame) {
        Bitmap bitmap = videoTextureView.getBitmap();
        return bitmap;
    }

    public Bitmap captureBitmapFromYuvFrame(I420Frame frame) {
        YuvImage yuvImage = i420ToYuvImage(frame.yuvPlanes,
                frame.yuvStrides,
                frame.width,
                frame.height);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight());

      // Compress YuvImage to jpeg
        yuvImage.compressToJpeg(rect, 100, stream);

        // Convert jpeg to Bitmap
        byte[] imageBytes = stream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        Matrix matrix = new Matrix();

        // Apply any needed rotation
        matrix.postRotate(frame.rotationDegree);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);

        return bitmap;
    }

    private YuvImage i420ToYuvImage(ByteBuffer[] yuvPlanes,
                                        int[] yuvStrides,
                                        int width,
                                        int height) {
        if (yuvStrides[0] != width) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }
        if (yuvStrides[1] != width / 2) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }
        if (yuvStrides[2] != width / 2) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }

        byte[] bytes = new byte[yuvStrides[0] * height +
                yuvStrides[1] * height / 2 +
                yuvStrides[2] * height / 2];
        ByteBuffer tmp = ByteBuffer.wrap(bytes, 0, width * height);
        copyPlane(yuvPlanes[0], tmp);

        byte[] tmpBytes = new byte[width / 2 * height / 2];
        tmp = ByteBuffer.wrap(tmpBytes, 0, width / 2 * height / 2);

        copyPlane(yuvPlanes[2], tmp);
        for (int row = 0 ; row < height / 2 ; row++) {
            for (int col = 0 ; col < width / 2 ; col++) {
                bytes[width * height + row * width + col * 2]
                        = tmpBytes[row * width / 2 + col];
            }
        }
        copyPlane(yuvPlanes[1], tmp);
        for (int row = 0 ; row < height / 2 ; row++) {
            for (int col = 0 ; col < width / 2 ; col++) {
                bytes[width * height + row * width + col * 2 + 1] =
                        tmpBytes[row * width / 2 + col];
            }
        }
        return new YuvImage(bytes, NV21, width, height, null);
    }

    private YuvImage fastI420ToYuvImage(ByteBuffer[] yuvPlanes,
                                        int[] yuvStrides,
                                        int width,
                                        int height) {
        byte[] bytes = new byte[width * height * 3 / 2];
        int i = 0;
        for (int row = 0 ; row < height ; row++) {
            for (int col = 0 ; col < width ; col++) {
                bytes[i++] = yuvPlanes[0].get(col + row * yuvStrides[0]);
            }
        }
        for (int row = 0 ; row < height / 2 ; row++) {
            for (int col = 0 ; col < width / 2; col++) {
                bytes[i++] = yuvPlanes[2].get(col + row * yuvStrides[2]);
                bytes[i++] = yuvPlanes[1].get(col + row * yuvStrides[1]);
            }
        }
        return new YuvImage(bytes, NV21, width, height, null);
    }

    private void copyPlane(ByteBuffer src, ByteBuffer dst) {
        src.position(0).limit(src.capacity());
        dst.put(src);
        dst.position(0).limit(dst.capacity());
    }

    public void takeSnapshot(RCTEventEmitter eventEmitter, int viewId, File outputFile, VideoTextureView video) {
        this.videoTextureView = video;
        this.eventEmitter = eventEmitter;
        this.viewId = viewId;
        this.outputFile = outputFile;
        snapshotRequsted.set(true);
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

    void pushEvent(String name, WritableMap data) {
        eventEmitter.receiveEvent(viewId, name, data);
    }
}
