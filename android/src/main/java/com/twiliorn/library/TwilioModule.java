package com.twiliorn.library;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;

import javax.annotation.Nonnull;

public class TwilioModule extends ReactContextBaseJavaModule {
    static final String TAG = TwilioModule.class.getCanonicalName();

    @Nonnull
    @Override
    public String getName() {
        return "RNTwilioModule";
    }

    public TwilioModule(ReactApplicationContext context) {
        super(context);
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
