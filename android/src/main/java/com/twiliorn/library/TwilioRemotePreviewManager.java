
/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.webrtc.RendererCommon;
import com.facebook.react.bridge.ReadableArray;
import java.util.Map;
import com.facebook.react.common.MapBuilder;

public class TwilioRemotePreviewManager extends SimpleViewManager<TwilioRemotePreview> {

    public static final String REACT_CLASS = "RNTwilioRemotePreview";
    public String myTrackSid = "";
    private static final int TAKE_SNAPSHOT = 10001;

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

    @Override
    public void receiveCommand(TwilioRemotePreview view, int commandId, @Nullable ReadableArray args) {
      switch (commandId) {
          case TAKE_SNAPSHOT:
              view.takeSnapshot();
              break;
      }
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.<String, Integer>builder()
                .put("takeSnapshot", TAKE_SNAPSHOT)
                .build();
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map<String, Map<String, String>> map = MapBuilder.of(
                "onSnapshot", MapBuilder.of("registrationName", "onSnapshot")
        );

        return map;
    }

    @Override
    protected TwilioRemotePreview createViewInstance(ThemedReactContext reactContext) {
        return new TwilioRemotePreview(reactContext, myTrackSid);
    }
}
