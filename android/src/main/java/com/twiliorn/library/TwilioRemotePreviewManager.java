/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import tvi.webrtc.RendererCommon;

import java.util.Map;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

public class TwilioRemotePreviewManager extends SimpleViewManager<TwilioRemotePreview> {

    public static final String REACT_CLASS = "RNTwilioRemotePreview";
    public String myTrackSid = "";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactProp(name = "scaleType")
    public void setScaleType(TwilioRemotePreview view, @Nullable String scaleType) {

      if (scaleType.equals("fit")) {
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
      } else {
        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
      }
    }

    @ReactProp(name = "trackSid")
    public void setTrackId(TwilioRemotePreview view, @Nullable String trackSid) {

        Log.i("CustomTwilioVideoView", "Initialize Twilio REMOTE");
        Log.i("CustomTwilioVideoView", trackSid);
        myTrackSid = trackSid;
        CustomTwilioVideoView.registerPrimaryVideoView(view.getSurfaceViewRenderer(), trackSid);
    }

    @ReactProp(name = "applyZOrder", defaultBoolean = false)
    public void setApplyZOrder(TwilioRemotePreview view, boolean applyZOrder) {
      view.applyZOrder(applyZOrder);
    }

    @Override
    protected TwilioRemotePreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioRemotePreview(reactContext, myTrackSid);
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
