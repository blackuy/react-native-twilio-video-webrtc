/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.support.annotation.Nullable;
import com.facebook.react.uimanager.annotations.ReactProp;

public class TwilioRemotePreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioRemotePreview";

    public TwilioRemotePreview(Context context, String trackId) {
        super(context);
        CustomTwilioVideoView.registerPrimaryVideoView(this.getSurfaceViewRenderer(), trackId);
    }
}
