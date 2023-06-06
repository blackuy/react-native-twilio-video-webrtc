/**
 * Component for Twilio Video local views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import tvi.webrtc.RendererCommon;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

import android.util.Log;

public class TwilioVideoPreviewManager extends SimpleViewManager<TwilioVideoPreview> {

    public static final String REACT_CLASS = "RNTwilioVideoPreview";
    public static final int COMMAND_CAPTURE_FRAME = 1;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public void receiveCommand(@NonNull TwilioVideoPreview view, String commandId, @Nullable ReadableArray args) {
        super.receiveCommand(view, commandId, args);
        switch (commandId) {
            case COMMAND_CAPTURE_FRAME + "":
                view.notifyCaptureFrame();
                break;
        }
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "captureFrame", COMMAND_CAPTURE_FRAME
        );
    }

    @ReactProp(name = "scaleType")
    public void setScaleType(TwilioVideoPreview view, @Nullable String scaleType) {
        if (scaleType.equals("fit")) {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        } else {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        }
    }

    @ReactProp(name = "applyZOrder", defaultBoolean = true)
    public void setApplyZOrder(TwilioVideoPreview view, boolean applyZOrder) {
        view.applyZOrder(applyZOrder);
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map<String, Map<String, String>> map = MapBuilder.of(
                ON_FRAME_DIMENSIONS_CHANGED, MapBuilder.of("registrationName", ON_FRAME_DIMENSIONS_CHANGED)
        );

        return map;
    }

    @Override
    protected TwilioVideoPreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioVideoPreview(reactContext);
    }
}
