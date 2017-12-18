/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.support.annotation.Nullable;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class TwilioRemotePreviewManager extends SimpleViewManager<TwilioRemotePreview> {

    public static final String REACT_CLASS = "RNTwilioRemotePreview";
    public String myTrackId = "";
    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @ReactProp(name = "trackId")
    public void setTrackId(TwilioRemotePreview view, @Nullable String trackId) {
     myTrackId =  trackId;
        CustomTwilioVideoView.registerPrimaryVideoView(view.getSurfaceViewRenderer(), trackId);
    }


    @Override
    protected TwilioRemotePreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioRemotePreview(reactContext, myTrackId);
    }
}
