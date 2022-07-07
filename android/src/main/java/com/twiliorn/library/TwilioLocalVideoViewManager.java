/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import androidx.annotation.Nullable;

import android.util.Log;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.twiliorn.library.events.TakeSnapshotReturnedEvent;

import java.util.Map;

import javax.annotation.Nonnull;

import tvi.webrtc.RendererCommon;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

public class TwilioLocalVideoViewManager extends SimpleViewManager<TwilioLocalVideoView> {
    private static final int TAKE_SNAPSHOT = 1;

    private boolean _enabled = false;
    public static final String REACT_CLASS = "RNTwilioLocalVideoView";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactProp(name = "scaleType")
    @ReactMethod
    public void setScaleType(TwilioRemotePreview view, @Nullable String scaleType) {

        if (scaleType.equals("fit")) {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        } else {
            view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        }
    }

    @ReactProp(name = "trackId")
    public void setTrackId(TwilioLocalVideoView view, @Nullable String trackId) {
        Log.i(REACT_CLASS, "Initialize Twilio Local video");
        Log.i(REACT_CLASS, "\tTrackId: " + trackId + " Enabled: " + (_enabled ? "True" : "False"));
        view.setTrackId(trackId, _enabled);
    }

    @ReactProp(name = "enabled")
    public void setEnabled(TwilioLocalVideoView view, @Nullable boolean enabled) {
        Log.i(REACT_CLASS, "Attempting to set video Enabled");
        this._enabled = enabled;
        view.setEnabled(enabled);
    }

    @Override
    protected TwilioLocalVideoView createViewInstance(ThemedReactContext reactContext) {
        return new TwilioLocalVideoView(reactContext);
    }

    @Override
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put(
                        ON_FRAME_DIMENSIONS_CHANGED,
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", ON_FRAME_DIMENSIONS_CHANGED)))
                .build();
    }

    @Override
    public void receiveCommand(@Nonnull TwilioLocalVideoView root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case TAKE_SNAPSHOT:
                final int requestId = args.getInt(0);
                root.takeSnapshot(ref -> {
                    TakeSnapshotReturnedEvent event = new TakeSnapshotReturnedEvent(root.getId(), requestId, ref);
                    root.pushEvent(root,
                            event.EVENT_NAME,
                            event.toWritableMap());
                });
                break;
            default:
                super.receiveCommand(root, commandId, args);
        }
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "takeSnapshot",
                TAKE_SNAPSHOT
        );
    }

    @Nullable
    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(TakeSnapshotReturnedEvent.EVENT_NAME, MapBuilder.of("registrationName", "onDataReturned"));
    }
}
