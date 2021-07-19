/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoTrack;

import java.util.Map;

import tvi.webrtc.RendererCommon;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

public class TwilioLocalViewManager extends SimpleViewManager<TwilioLocalView> {

    public static final String REACT_CLASS = "RNTwilioLocalView";

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

    @ReactProp(name = "cameraId")
    public void setCameraId(TwilioLocalView view, @Nullable String cameraId) {
        Log.i(REACT_CLASS, "Initialize Twilio Local video");
        view.setCameraId(cameraId);
    }


    @Override
    protected TwilioLocalView createViewInstance(ThemedReactContext reactContext) {
        return new TwilioLocalView(reactContext);
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
