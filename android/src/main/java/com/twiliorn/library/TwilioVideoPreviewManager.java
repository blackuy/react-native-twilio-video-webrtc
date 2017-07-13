/**
 * Component for Twilio Video local views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;


public class TwilioVideoPreviewManager extends SimpleViewManager<TwilioVideoPreview> {

    public static final String REACT_CLASS = "RNTwilioVideoPreview";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected TwilioVideoPreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioVideoPreview(reactContext);
    }
}
