/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

import tvi.webrtc.RendererCommon;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

public class TwilioLocalVideoViewManager extends SimpleViewManager<TwilioLocalVideoView> {

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
        Log.i(REACT_CLASS, "\tTrackId: " + trackId + " Enabled: " + (_enabled ? "True" : "False") );
        view.setTrackId(trackId, _enabled);
    }

    @ReactProp(name = "enabled")
    public void setEnabled(TwilioLocalVideoView view, @Nullable boolean enabled) {
        Log.i(REACT_CLASS,  "Attempting to set video Enabled");
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
}
