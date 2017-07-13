/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;

public class TwilioRemotePreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioRemotePreview";

    public TwilioRemotePreview(Context context) {
        super(context);
        CustomTwilioVideoView.registerPrimaryVideoView(this.getSurfaceViewRenderer());
    }
}
