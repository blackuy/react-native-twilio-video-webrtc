package com.twiliorn.library.niceday;

import static com.twiliorn.library.niceday.NDHelper.parseDimensionsString;
import static com.twiliorn.library.niceday.NDHelper.parsePriorityString;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import android.support.annotation.Nullable;

import com.twilio.video.BandwidthProfileMode;
import com.twilio.video.BandwidthProfileOptions;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TrackPriority;
import com.twilio.video.VideoBandwidthProfileOptions;
import com.twilio.video.VideoDimensions;

/**
 * This class contains changes on CustomTwilioVideoView.
 * We put it on separate file to minimizes conflict when merging from upstream.
 * NOTE: This class should only be used on CustomTwilioVideoView.
 */
public class NDExtra {
    public static final String BT_INTENT = "com.twiliorn.library.niceday.bluetooth";
    private static final String TAG = "BandwidthProfile";
    private static final VideoDimensions DEFAULT_MAX_CAPTURE_RESOLUTION = VideoDimensions.CIF_VIDEO_DIMENSIONS;
    private static final int DEFAULT_MAX_CAPTURE_FPS = 25;
    public boolean enableH264Codec = false;
    public int audioBitrate = 16;   // Ideal bitrate for speech
    public int videoBitrate = 0;    // Use default video bitrate
    public BandwidthProfileOptions bandwidthProfile = null;
    public boolean isVideoEnabled;
    public VideoDimensions maxCaptureDimensions = parseDimensionsString("'640x480'");
    public int maxCaptureFPS = DEFAULT_MAX_CAPTURE_FPS;
    Context appContext;
    Handler handler = new Handler();
    int delay = 2000;
    Runnable run = new Runnable() {
        public void run() {
            Log.d(TAG,"RUNNING");
            Intent intent = new Intent(BT_INTENT);
            appContext.sendBroadcast(intent);
            handler.postDelayed(this, delay);
        }
    };

    public NDExtra(Context appContext) {
        this.appContext = appContext;
        prepareBandwidthProfile();
        //btHeadsetListener();
    }

    public void prepareBandwidthProfile() {
        BandwidthProfileMode mode = BandwidthProfileMode.COLLABORATION;
        @Nullable Long maxSubscriptionBitrate = (long) 2000;    // Parse max tracks to enabled during a call
        TrackPriority dominantSpeakerPriority = parsePriorityString("HIGH");
        VideoBandwidthProfileOptions videoBandwidthProfileOptions = new VideoBandwidthProfileOptions.Builder()
                .mode(mode)
                .dominantSpeakerPriority(dominantSpeakerPriority)
                .maxSubscriptionBitrate(maxSubscriptionBitrate)
                .build();
        this.bandwidthProfile = new BandwidthProfileOptions(videoBandwidthProfileOptions);
    }

    public void applyExtraParamsTo(ConnectOptions.Builder connectOptionsBuilder) {
        // If we have specified bit rates then use them
        if (this.audioBitrate >= 0 && this.videoBitrate >= 0) {
            connectOptionsBuilder.encodingParameters(new EncodingParameters(this.audioBitrate, this.videoBitrate));
            Log.d(TAG, "Setting max audio rate" + this.audioBitrate + " and max video rate: " + this.videoBitrate);
        } else {
            // If we have specified only 1 of the bit rate values
            if (this.audioBitrate >= 0 || this.videoBitrate >= 0) {
                // Then warn the user that we are ignoring the value
                Log.w(TAG, "Ignoring audio or video bitrate as only 1 of them is defined. Audio: " + this.audioBitrate + " Video:" + this.videoBitrate);
            }
        }
        connectOptionsBuilder.bandwidthProfile(this.bandwidthProfile);
    }

    public void setTrackPriority(String trackSid, String trackPriorityString, Room room) {
        TrackPriority priority = parsePriorityString(trackPriorityString);

        for (RemoteParticipant participant : room.getRemoteParticipants()) {
            for (RemoteVideoTrackPublication publication : participant.getRemoteVideoTracks()) {
                RemoteVideoTrack track = publication.getRemoteVideoTrack();
                if (track == null) {
                    continue;
                }
                if (publication.getTrackSid().equals(trackSid)) {
                    track.setPriority(priority);
                }
            }
        }
    }

    public void cleanUp() {
        try {
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG,"cleanup");
        } catch (Exception ignored) {
            Log.d(TAG,ignored.getMessage());
        }
    }

    public void btHeadsetListener() {
        handler.postDelayed(run, delay);
    }
}
