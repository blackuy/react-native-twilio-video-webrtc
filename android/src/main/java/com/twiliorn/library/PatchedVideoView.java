/**
 * Component for patching black screen bug coming from Twilio VideoView
 * Authors:
 * Aaron Alaniz (@aaalaniz) <aaron.a.alaniz@gmail.com>
 */

package com.twiliorn.library;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoView;

/*
 * VideoView that notifies Listener of the first frame rendered and the first frame after a reset
 * request.
 */
public class PatchedVideoView extends VideoView {

    private boolean notifyFrameRendered = false;
    private Listener listener;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public PatchedVideoView(Context context) {
        super(context);
    }

    public PatchedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void renderFrame(I420Frame frame) {
        if (notifyFrameRendered) {
            notifyFrameRendered = false;
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFirstFrame();
                }
            });
        }
        super.renderFrame(frame);
    }

    /*
     * Set your listener
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /*
     * Reset the listener so next frame rendered results in callback
     */
    public void resetListener() {
        notifyFrameRendered = true;
    }

    public interface Listener {
        void onFirstFrame();
    }
}
