package com.twiliorn.library;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import tvi.webrtc.Camera2Enumerator;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@ReactModule(name = "RNTwilioModule" )
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
        cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);;
    }
    
    @ReactMethod
    public void getAvailableCameras(Promise promise) {
        try {
            String[] cameras = this.cameraManager.getCameraIdList();
            WritableArray writableArray = Arguments.createArray();
            for(String camera : cameras) {
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

            for (String str: tracks) {
                writableArray.pushString(str);
            }

             promise.resolve(writableArray);
        } catch(Exception e) {
             promise.reject("Create Event Error", e);    
        }
    }
}
