package com.twiliorn.library;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import javax.annotation.Nonnull;

public class TwilioModule extends ReactContextBaseJavaModule {
    static final String TAG = TwilioModule.class.getCanonicalName();

    @Nonnull
    @Override
    public String getName() {
        return "RNTwilioModule";
    }

    public TwilioModule(ReactContext context) {
        super(context);
    }

    @ReactMethod
    public void getAvailableLocalTracks(Promise promise) {    
        try {         

        } catch(Exception e) {
             promise.reject("Create Event Error", e);    
        }
    }
}
