/**
 * Wrapper component for the Twilio Video View to facilitate easier layout.
 * <p>
 * Author:
 * Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.support.annotation.StringDef;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.twilio.video.VideoScaleType;

import tvi.webrtc.RendererCommon;
import tvi.webrtc.VideoFrame;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.twiliorn.library.RNVideoViewGroup.Events.ON_FRAME_DIMENSIONS_CHANGED;

public class RNVideoViewGroup extends ViewGroup {
    private PatchedVideoView surfaceViewRenderer = null;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private final Object layoutSync = new Object();
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
    private final RCTEventEmitter eventEmitter;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ON_FRAME_DIMENSIONS_CHANGED})
    public @interface Events {
        String ON_FRAME_DIMENSIONS_CHANGED = "onFrameDimensionsChanged";
    }

    void pushEvent(View view, String name, WritableMap data) {
        eventEmitter.receiveEvent(view.getId(), name, data);
    }

    public RNVideoViewGroup(ThemedReactContext themedReactContext) {
        super(themedReactContext);
        this.eventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
        surfaceViewRenderer = new PatchedVideoView(themedReactContext);
        surfaceViewRenderer.setVideoScaleType(VideoScaleType.ASPECT_FILL);
        addView(surfaceViewRenderer);
        surfaceViewRenderer.setListener(
                new RendererCommon.RendererEvents() {
                    @Override
                    public void onFirstFrameRendered() {

                    }

                    @Override
                    public void onFrameResolutionChanged(int vw, int vh, int rotation) {
                        synchronized (layoutSync) {
                            if (rotation == 90 || rotation == 270) {
                                videoHeight = vw;
                                videoWidth = vh;
                            } else {
                                videoHeight = vh;
                                videoWidth = vw;
                            }
                            RNVideoViewGroup.this.forceLayout();

                            WritableMap event = new WritableNativeMap();
                            event.putInt("height", vh);
                            event.putInt("width", vw);
                            event.putInt("rotation", rotation);
                            pushEvent(RNVideoViewGroup.this, ON_FRAME_DIMENSIONS_CHANGED, event);
                        }
                    }
                }
        );
    }

    public PatchedVideoView getSurfaceViewRenderer() {
        return surfaceViewRenderer;
    }

    public void setScalingType(RendererCommon.ScalingType scalingType) {
        this.scalingType = scalingType;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int height = b - t;
        int width = r - l;
        if (height == 0 || width == 0) {
            l = t = r = b = 0;
        } else {
            int videoHeight;
            int videoWidth;
            synchronized (layoutSync) {
                videoHeight = this.videoHeight;
                videoWidth = this.videoWidth;
            }

            if (videoHeight == 0 || videoWidth == 0) {
                // These are Twilio defaults.
                videoHeight = 480;
                videoWidth = 640;
            }

            Point displaySize = RendererCommon.getDisplaySize(
                    this.scalingType,
                    videoWidth / (float) videoHeight,
                    width,
                    height
            );

            l = (width - displaySize.x) / 2;
            t = (height - displaySize.y) / 2;
            r = l + displaySize.x;
            b = t + displaySize.y;
        }
        surfaceViewRenderer.layout(l, t, r, b);
    }
}
