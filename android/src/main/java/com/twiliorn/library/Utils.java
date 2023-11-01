package com.twiliorn.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import tvi.webrtc.VideoFrame;

public final class Utils {
    private Utils() {}

    /**
     * TODO: refactor to use more optimized Streem code: https://github.com/streem/streem-app/blob/main/android/StreemSDK/src/main/java/pro/streem/sdk/internal/util/ImageUtil.kt
     * Create an NV21Buffer with the same pixel content as the given I420 buffer. Ripped from here: https://stackoverflow.com/a/55158228
     */
    private static byte[] createNV21Data(VideoFrame.I420Buffer i420Buffer) {
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

    // i420 -> nv21 -> yuv -> jpeg
    public static void saveVideoFrame(VideoFrame frame, Context context, String filename) {
        Log.d(TwilioPackage.TAG, "saving video frame");
        String fileId = UUID.randomUUID().toString();
        String filePath = filename + ".jpeg";
        Log.d(TwilioPackage.TAG, "saving frame for file " + filePath);

        VideoFrame.I420Buffer i420Buffer = frame.getBuffer().toI420();
        final int width = i420Buffer.getWidth();
        final int height = i420Buffer.getHeight();
        Log.w(TwilioPackage.TAG, "frame width: " + width + ", height: " + height);

        byte[] nv21Data = createNV21Data(i420Buffer);

        YuvImage yuvImage = new YuvImage(nv21Data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 90, out);
        byte[] rawImageBytes = out.toByteArray();
        byte[] imageBytes = rotateJPEGByteArray(rawImageBytes, frame.getRotation());

        try (FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE)) {
            // write image to disk
            fos.write(imageBytes);
            Log.d(TwilioPackage.TAG, "saved frame to " + filePath);
            // send event to JS w/ filename
            WritableMap params = Arguments.createMap();
            params.putString("filename", filePath);
            sendEvent(context, "onFrameCaptured", params);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            frame.release();
            i420Buffer.release();
        }

    }

    private static byte[] rotateJPEGByteArray(
        byte[] data,
        float rotation
    ) {
        if (rotation == 0f) {
            return data;
        }
        else {
            Bitmap sourceBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap rotatedBitmap = rotateBitmap(sourceBitmap, rotation);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            sourceBitmap.recycle();
            rotatedBitmap.recycle();
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static Bitmap rotateBitmap(
        Bitmap sourceBitmap,
        float rotation
    ) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
    }

    private static void sendEvent(Context context, String event, WritableMap params) {
        ((ReactContext) context).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(event, params);
    }
}
