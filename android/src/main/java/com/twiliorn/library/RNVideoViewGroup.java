/**
 * Wrapper component for the Twilio Video View to facilitate easier layout.
 *
 * Author:
 *   Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import android.content.Context;
import android.graphics.Point;
import android.view.ViewGroup;

import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoView;

import org.webrtc.RendererCommon;

public class RNVideoViewGroup extends ViewGroup {
  private VideoView surfaceViewRenderer = null;
  private int videoWidth = 0;
  private int videoHeight = 0;
  private final Object layoutSync = new Object();
  private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;


  public RNVideoViewGroup(Context context) {
    super(context);

    surfaceViewRenderer = new VideoView(context);
    surfaceViewRenderer.setVideoScaleType(VideoScaleType.ASPECT_FILL);
    addView(surfaceViewRenderer);
    surfaceViewRenderer.setListener(
        new VideoRenderer.Listener() {
          @Override
          public void onFirstFrame() {

          }

          @Override
          public void onFrameDimensionsChanged(int vw, int vh, int rotation) {
            synchronized (layoutSync) {
              videoHeight = vh;
              videoWidth = vw;
              RNVideoViewGroup.this.forceLayout();
            }
          }
        }
    );
  }

  public VideoView getSurfaceViewRenderer() {
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
