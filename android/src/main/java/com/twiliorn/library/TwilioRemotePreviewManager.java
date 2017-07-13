/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;


public class TwilioRemotePreviewManager extends SimpleViewManager<TwilioRemotePreview> {

    public static final String REACT_CLASS = "RNTwilioRemotePreview";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected TwilioRemotePreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioRemotePreview(reactContext);
    }
}
