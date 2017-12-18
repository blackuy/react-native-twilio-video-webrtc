/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.uimanager.annotations.ReactProp;
import android.util.Log;




public class TwilioRemotePreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioRemotePreview";




    public TwilioRemotePreview(Context context, String trackId) {
        super(context);
        Log.i("CustomTwilioVideoView","Remote Prview Construct" );
        Log.i("CustomTwilioVideoView",trackId );


        CustomTwilioVideoView.registerPrimaryVideoView(this.getSurfaceViewRenderer(), trackId);
    }
}
