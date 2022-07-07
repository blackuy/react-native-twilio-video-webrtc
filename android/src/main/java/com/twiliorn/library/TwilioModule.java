package com.twiliorn.library;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.module.annotations.ReactModule;
import com.twiliorn.library.stethoscope.StethoscopeDevice;
import com.twiliorn.library.utils.CustomAudioDevice;
import com.twiliorn.library.utils.SafePromise;

import javax.annotation.Nonnull;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@ReactModule(name = "RNTwilioModule")
public class TwilioModule extends ReactContextBaseJavaModule {
    static final String TAG = TwilioModule.class.getCanonicalName();
    final CameraManager cameraManager;

    @Nonnull
    @Override
    public String getName() {
        return "RNTwilioModule";
    }

    public TwilioModule(ReactApplicationContext context) {
        super(context);
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        if (CustomTwilioVideoView.getCustomAudioDevice() == null) {
            Log.d(TAG, "customAudioDevice is null - Initializing new custom audio device");
            CustomTwilioVideoView.setCustomAudioDevice(new CustomAudioDevice(context));
        }

        if (CustomTwilioVideoView.getStethoscopeDevice() == null) {
            Log.d(TAG, "stethoscopeDevice is null - Initializing new get stethoscope device");
            CustomTwilioVideoView.setStethoscopeDevice(new StethoscopeDevice(context));
        }
    }

    @ReactMethod
    public void getAvailableCameras(Promise promise) {
        try {
            String[] cameras = this.cameraManager.getCameraIdList();
            WritableArray writableArray = Arguments.createArray();
            for (String camera : cameras) {
                writableArray.pushString(camera);
            }
            promise.resolve(writableArray);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }


    @ReactMethod
    public void getAvailableLocalTracks(Promise promise) {
        try {
            String[] tracks = CustomTwilioVideoView.getAvailableLocalTracks();
            WritableArray writableArray = Arguments.createArray();

            for (String str : tracks) {
                writableArray.pushString(str);
            }

            promise.resolve(writableArray);
        } catch (Exception e) {
            promise.reject("Create Event Error", e);
        }
    }

    @ReactMethod
    public void stethoscopeRecordToFile(String path, Integer timeout, Promise promise) {
        SafePromise<String> stringSafePromise = new SafePromise(promise);
        if (timeout == null) {
            CustomTwilioVideoView.stethoscopeRecordToFile(path, 20, stringSafePromise);
            return;
        }
        CustomTwilioVideoView.stethoscopeRecordToFile(path, timeout, stringSafePromise);
    }

    @ReactMethod
    public void startStethoscope(Promise promise) {
        SafePromise<String> stringSafePromise = new SafePromise(promise);
        CustomTwilioVideoView.startStethoscope(stringSafePromise);
    }

    @ReactMethod
    public void stopStethoscope(Promise promise) {
        SafePromise safePromise = new SafePromise(promise);
        CustomTwilioVideoView.stopStethoscope(safePromise);
    }
}
