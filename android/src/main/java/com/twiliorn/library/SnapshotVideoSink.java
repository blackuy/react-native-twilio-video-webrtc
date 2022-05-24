package com.twiliorn.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import tvi.webrtc.VideoFrame;
import tvi.webrtc.VideoSink;
import tvi.webrtc.YuvConverter;

public class SnapshotVideoSink implements VideoSink {
    private Callback<Bitmap> callback;
    private final AtomicBoolean snapshotRequested = new AtomicBoolean(false);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Bitmap bitmap;

    @Override
    public void onFrame(VideoFrame videoFrame) {
        videoFrame.retain();
        if (this.snapshotRequested.compareAndSet(true, false)) {
            this.bitmap = this.toBitmap(videoFrame);
            handler.post(() -> {
                callback.invoke(bitmap);
                videoFrame.release();
            });
        } else {
            videoFrame.release();
        }
    }

    public void takeSnapshot(Callback<Bitmap> callback) {
        this.callback = callback;
        this.snapshotRequested.set(true);
    }

    private Bitmap toBitmap(VideoFrame videoFrame) {
        VideoFrame.Buffer buffer = videoFrame.getBuffer();
        VideoFrame.I420Buffer i420Buffer;
        if(buffer instanceof VideoFrame.TextureBuffer) {
            YuvConverter yuvConverter = new YuvConverter();
            VideoFrame.I420Buffer temp = yuvConverter.convert((VideoFrame.TextureBuffer) buffer);
            yuvConverter.release();
            i420Buffer = temp;
        } else {
            i420Buffer = buffer.toI420();
        }
        YuvImage yuvImage = i420ToYuvImage(i420Buffer, buffer.getHeight(), buffer.getWidth());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Rect rect = new Rect(0,0,yuvImage.getWidth(), yuvImage.getHeight());
        yuvImage.compressToJpeg(rect, 100, stream);

        byte[] imageBytes = stream.toByteArray();
        Bitmap bitmap;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            ByteBuffer buffer = ByteBuffer.wrap(imageBytes)
//            src =
//                    ImageDecoder.createSource(buffer)
//            try {
//                ImageDecoder.decodeBitmap(src)
//            } catch (e: IOException) {
//                e.printStackTrace()
//                return null
//            }
//        } else {
        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        }
        Matrix matrix = new Matrix();

        // Apply any needed rotation
        matrix.postRotate(videoFrame.getRotation());
        bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    private YuvImage i420ToYuvImage(VideoFrame.I420Buffer buffer, int height, int width) {
        ByteBuffer[] yuvPlanes = {buffer.getDataY(), buffer.getDataU(), buffer.getDataV()};
        int[] yuvStrides = {buffer.getStrideY(), buffer.getStrideU(), buffer.getStrideV()};
        if (yuvStrides[0] != width) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }
        if (yuvStrides[1] != width / 2) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }
        if (yuvStrides[2] != width / 2) {
            return fastI420ToYuvImage(yuvPlanes, yuvStrides, width, height);
        }
        byte[] bytes = new byte[yuvStrides[0] * height + yuvStrides[1] * height / 2 + yuvStrides[2] * height / 2];
        ByteBuffer tmp = ByteBuffer.wrap(bytes, 0, width * height);
        copyPlane(yuvPlanes[0], tmp);
        byte[] tmpBytes = new byte[width / 2 * height / 2];
        tmp = ByteBuffer.wrap(tmpBytes, 0, width / 2 * height / 2);
        copyPlane(yuvPlanes[2], tmp);
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width /2; col++) {
                bytes[width * height + row * width + col * 2] =
                        tmpBytes[row * width / 2 + col];
            }
        }
        copyPlane(yuvPlanes[1], tmp);
        for (int row = 0; row < height /2; row++) {
            for (int col = 0; col < width/2; col++) {
                bytes[width * height + row * width + col * 2 + 1] =
                        tmpBytes[row * width / 2 + col];
            }
        }
        return new YuvImage(bytes, ImageFormat.NV21, width, height, null);

    }

    private YuvImage fastI420ToYuvImage(ByteBuffer[] yuvPlanes, int[] yuvStrides, int width, int height) {
        byte[] bytes = new byte[width * height * 3 / 2];
        int i = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++ ) {
                bytes[i++] = yuvPlanes[0].get(col + row * yuvStrides[0]);
            }
        }
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                bytes[i++] = yuvPlanes[2].get(col + row * yuvStrides[2]);
                bytes[i++] = yuvPlanes[1].get(col + row * yuvStrides[1]);
            }
        }
        return new YuvImage(bytes, ImageFormat.NV21, width, height, null);
    }

    private void copyPlane(ByteBuffer src, ByteBuffer dst) {
        src.position(0).limit(src.capacity());
        dst.put(src);
        dst.position(0).limit(dst.capacity());
    }
}
