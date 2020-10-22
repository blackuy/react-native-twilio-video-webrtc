/**
 * Component for Twilio Video participant views.
 * <p>
 * Authors:
 * Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.util.Log;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.uimanager.ThemedReactContext;
import java.io.File;
import java.io.IOException;
import androidx.annotation.NonNull;

public class TwilioRemotePreview extends RNVideoViewGroup {

    private static final String TAG = "TwilioRemotePreview";
    private PatchedVideoView video;
    private final RCTEventEmitter eventEmitter;
    private final ThemedReactContext mContext;

    public TwilioRemotePreview(ThemedReactContext context, String trackSid) {
        super(context);
        Log.i("CustomTwilioVideoView", "Remote Prview Construct");
        Log.i("CustomTwilioVideoView", trackSid);
        this.eventEmitter = context.getJSModule(RCTEventEmitter.class);
        this.video = this.getSurfaceViewRenderer();
        this.mContext = context;

        CustomTwilioVideoView.registerPrimaryVideoView(this.getSurfaceViewRenderer(), trackSid);
    }

    public void takeSnapshot() {
        try {
            File outputFile = createTempFile(mContext);
            video.takeSnapshot(eventEmitter, TwilioRemotePreview.this.getId(), outputFile, video);
        } catch (final Throwable ex) {
            Log.e(TAG, "Failed to take snapshot", ex);
        }
    }

    /**
     * Create a temporary file in the cache directory on either internal or external storage,
     * whichever is available and has more free space.
     */
    @NonNull
    private File createTempFile(@NonNull final Context context) throws IOException {
        final File externalCacheDir = context.getExternalCacheDir();
        final File internalCacheDir = context.getCacheDir();
        final File cacheDir;

        if (externalCacheDir == null && internalCacheDir == null) {
            throw new IOException("No cache directory available");
        }

        if (externalCacheDir == null) {
            cacheDir = internalCacheDir;
        } else if (internalCacheDir == null) {
            cacheDir = externalCacheDir;
        } else {
            cacheDir = externalCacheDir.getFreeSpace() > internalCacheDir.getFreeSpace() ?
                    externalCacheDir : internalCacheDir;
        }

        final String suffix = "." + "png";
        return File.createTempFile("snapshot_video", suffix, cacheDir);
    }
}
