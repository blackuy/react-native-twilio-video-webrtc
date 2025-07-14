/**
 * Component for Twilio Video local views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;

public class TwilioVideoPreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioVideoPreview";
    private boolean enabled = true;
    private String trackName = null;

    public TwilioVideoPreview(ThemedReactContext themedReactContext) {
        super(themedReactContext);
        // Don't register immediately - wait for properties to be set
    }

    public void applyZOrder(boolean applyZOrder) {
        this.getSurfaceViewRenderer().applyZOrder(applyZOrder);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateVideoView();
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
        updateVideoView();
    }

    private void updateVideoView() {
        if (enabled) {
            if (trackName != null) {
                CustomTwilioVideoView.registerThumbnailVideoView(this.getSurfaceViewRenderer(), trackName);
            } else {
                CustomTwilioVideoView.registerThumbnailVideoView(this.getSurfaceViewRenderer());
            }
        } else {
            // TODO: Implement view unregistration if needed
        }
    }
}
