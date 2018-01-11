/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 *
 * Authors:
 *   Ralph Pina <ralph.pina@gmail.com>
 *   Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.functions.Action1;

import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_AUDIO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CAMERA_SWITCHED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECT_FAILURE;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DICONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_VIDEO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_VIDEO;


public class CustomTwilioVideoView extends View implements LifecycleEventListener {

    private static final String TAG = "CustomTwilioVideoView";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Events.ON_CAMERA_SWITCHED,
            Events.ON_VIDEO_CHANGED,
            Events.ON_AUDIO_CHANGED,
            Events.ON_CONNECTED,
            Events.ON_CONNECT_FAILURE,
            Events.ON_DICONNECTED,
            Events.ON_PARTICIPANT_CONNECTED,
            Events.ON_PARTICIPANT_DISCONNECTED,
            Events.ON_PARTICIPANT_ADDED_VIDEO})
    public @interface Events {
        String ON_CAMERA_SWITCHED          = "onCameraSwitched";
        String ON_VIDEO_CHANGED            = "onVideoChanged";
        String ON_AUDIO_CHANGED            = "onAudioChanged";
        String ON_CONNECTED                = "onRoomDidConnect";
        String ON_CONNECT_FAILURE          = "onConnectFailure";
        String ON_DICONNECTED              = "onRoomDidDisconnect";
        String ON_PARTICIPANT_CONNECTED    = "onRoomParticipantDidConnect";
        String ON_PARTICIPANT_DISCONNECTED = "onRoomParticipantDidDisconnect";
        String ON_PARTICIPANT_ADDED_VIDEO = "onParticipantAddedVideoTrack";
    }

    private final ThemedReactContext themedReactContext;
    private final RCTEventEmitter    eventEmitter;

    /*
     * A Room represents communication between the client and one or more participants.
     */
    private Room room;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private static VideoView primaryVideoView;
    private static VideoView thumbnailVideoView;
    private static VideoTrack  participantVideoTrack;
    private static LocalVideoTrack localVideoTrack;

    private CameraCapturer  cameraCapturer;
    private LocalAudioTrack localAudioTrack;
    private AudioManager    audioManager;
    private String          participantIdentity;
    private int             previousAudioMode;
    private boolean         disconnectedFromOnDestroy;
    private IntentFilter intentFilter;
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver;

    public CustomTwilioVideoView(ThemedReactContext context) {
        super(context);
        this.themedReactContext = context;
        this.eventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);

        // add lifecycle for onResume and on onPause
        themedReactContext.addLifecycleEventListener(this);

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        if (themedReactContext.getCurrentActivity() != null) {
            themedReactContext.getCurrentActivity()
                              .setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager) themedReactContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    // ===== SETUP =================================================================================

    private void createLocalMedia(final String accessToken) {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(getContext(), true);

        // Share your camera
        cameraCapturer = new CameraCapturer(
            getContext(),
            CameraCapturer.CameraSource.FRONT_CAMERA,
            new CameraCapturer.Listener() {
                @Override
                public void onFirstFrameAvailable() {
                    Log.i("CustomTwilioVideoView", "Got a local camera track");
                }

                @Override
                public void onCameraSwitched() {

                }

                @Override
                public void onError(int i) {
                    Log.i("CustomTwilioVideoView", "Error getting camera");
                }
            }
        );

        if (cameraCapturer.getSupportedFormats().size() > 0) {
            localVideoTrack = LocalVideoTrack.create(getContext(), true, cameraCapturer);
            if (thumbnailVideoView != null && localVideoTrack != null) {
                localVideoTrack.addRenderer(thumbnailVideoView);
            }
        }
        connectToRoom(accessToken);
    }

    // ===== LIFECYCLE EVENTS ======================================================================

    @Override
    public void onHostResume() {
        /*
         * In case it wasn't set.
         */
        if (themedReactContext.getCurrentActivity() != null) {
            themedReactContext.getCurrentActivity()
                              .setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
    }

    @Override
    public void onHostPause() {
        Log.i("CustomTwilioVideoView", "Host pause");
    }

    @Override
    public void onHostDestroy() {
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local media ensuring any memory allocated to audio or video is freed.
         */
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
    }

    // ====== CONNECTING ===========================================================================

    public void connectToRoomWrapper(String accessToken) {
        /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        Log.i("CustomTwilioVideoView", "Starting connect flow");
        createLocalMedia(accessToken);
    }

    public void connectToRoom(String accessToken) {
        /*
         * Create a VideoClient allowing you to connect to a Room
         */
        setAudioFocus(true);
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken);

        if (localAudioTrack != null) {
            connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
        }
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }
        //Log.i("CustomTwilioVideoView", "Primary Video Track: "+localVideoTrack);
        //addParticipantVideo(localVideoTrack);

        room = Video.connect(getContext(), connectOptionsBuilder.build(), roomListener());
    }

    private void setAudioFocus(boolean focus) {
        if (focus) {
            previousAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                                           AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //audioManager.setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
            audioManager.setSpeakerphoneOn(true);
            getContext().registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
        } else {
            audioManager.setMode(previousAudioMode);
            audioManager.abandonAudioFocus(null);
            //audioManager.setSpeakerphoneOn(false);
            audioManager.setSpeakerphoneOn(true);
            getContext().unregisterReceiver(myNoisyAudioStreamReceiver);
        }
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                //audioManager.setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
                audioManager.setSpeakerphoneOn(true);
            }
        }
    }

    // ====== DISCONNECTING ========================================================================

    public void disconnect() {
        if (room != null) {
            room.disconnect();
        }
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }
    }

    // ===== BUTTON LISTENERS ======================================================================

    public void switchCamera() {
        if (cameraCapturer != null) {
            CameraCapturer.CameraSource cameraSource = cameraCapturer.getCameraSource();
            final boolean isBackCamera = cameraSource == CameraCapturer.CameraSource.BACK_CAMERA;
            cameraCapturer.switchCamera();
            if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                thumbnailVideoView.setMirror(isBackCamera);
            } else {
                primaryVideoView.setMirror(isBackCamera);
            }

            Log.d("CustomTwilioVideoView", "CAMERA SOURCE: "+isBackCamera);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("isBackCamera", isBackCamera);
            pushEvent(CustomTwilioVideoView.this, ON_CAMERA_SWITCHED, event);
        }
    }

    public void toggleVideo() {
        if (localVideoTrack != null) {
            boolean enable = !localVideoTrack.isEnabled();
            localVideoTrack.enable(enable);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("videoEnabled", enable);
            pushEvent(CustomTwilioVideoView.this, ON_VIDEO_CHANGED, event);
        }
    }

    public void toggleAudio() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("audioEnabled", enable);
            pushEvent(CustomTwilioVideoView.this, ON_AUDIO_CHANGED, event);
        }
    }

    // ====== ROOM LISTENER ========================================================================

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                WritableMap event = new WritableNativeMap();
                event.putString("room", room.getName());
                List<Participant> participants = room.getParticipants();

                WritableArray participantsNames = new WritableNativeArray();
                for (Participant participant : participants) {
                    participantsNames.pushString(participant.getIdentity());
                }
                event.putArray("participantsNames", participantsNames);

                event.putString("videoID", localVideoTrack.getTrackId());
                Log.i("CustomTwilioVideoView", "Track ID: "+localVideoTrack.getTrackId());

                pushEvent(CustomTwilioVideoView.this, ON_CONNECTED, event);

                //noinspection LoopStatementThatDoesntLoop
                for (Participant participant : participants) {
                    addParticipant(participant);
                    break;
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                WritableMap event = new WritableNativeMap();
                Log.i("CustomTwilioVideoView", "Connect Failure: "+e);
                event.putString("reason", e.getExplanation());
                pushEvent(CustomTwilioVideoView.this, ON_CONNECT_FAILURE, event);
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                WritableMap event = new WritableNativeMap();
                event.putString("participant", participantIdentity);
                pushEvent(CustomTwilioVideoView.this, ON_DICONNECTED, event);

                CustomTwilioVideoView.this.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    setAudioFocus(false);
                }
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                addParticipant(participant);
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                removeParticipant(participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
            }

            @Override
            public void onRecordingStopped(Room room) {
            }
        };
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(Participant participant) {
        participantIdentity = participant.getIdentity();
        WritableMap event = new WritableNativeMap();
        event.putString("participant", participantIdentity);
        pushEvent(this, ON_PARTICIPANT_CONNECTED, event);

        /*
         * Add participant renderer
         */
        if (participant.getVideoTracks()
                       .size() > 0) {
            addParticipantVideo(participant.getVideoTracks()
                                           .get(0));
        }

        /*
         * Start listening for participant media events
         */
        participant.setListener(mediaListener());
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(Participant participant) {
        WritableMap event = new WritableNativeMap();
        event.putString("participant", participantIdentity);
        pushEvent(this, ON_PARTICIPANT_DISCONNECTED, event);
        if (!participant.getIdentity()
                        .equals(participantIdentity)) {
            return;
        }

        /*
         * Remove participant renderer
         */
        if (participant.getVideoTracks()
                       .size() > 0) {
            removeParticipantVideo(participant.getVideoTracks()
                                              .get(0));
        }
        participant.setListener(null);
    }


    // ====== MEDIA LISTENER =======================================================================

    private Participant.Listener mediaListener() {
        return new Participant.Listener() {
            @Override
            public void onAudioTrackAdded(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onAudioTrackRemoved(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onVideoTrackAdded(Participant participant, VideoTrack videoTrack) {
                addParticipantVideo(videoTrack);
            }

            @Override
            public void onVideoTrackRemoved(Participant participant, VideoTrack videoTrack) {
                removeParticipantVideo(videoTrack);
            }

            @Override
            public void onAudioTrackEnabled(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onAudioTrackDisabled(Participant participant, AudioTrack audioTrack) {

            }

            @Override
            public void onVideoTrackEnabled(Participant participant, VideoTrack videoTrack) {

            }

            @Override
            public void onVideoTrackDisabled(Participant participant, VideoTrack videoTrack) {

            }
        };
    }

    private void addParticipantVideo(VideoTrack videoTrack) {
        if (participantVideoTrack != null && primaryVideoView != null) {
            participantVideoTrack.removeRenderer(primaryVideoView);
        }
        participantVideoTrack = videoTrack;
        if (primaryVideoView != null) {
            participantVideoTrack.addRenderer(primaryVideoView);
        }

        WritableMap event = new WritableNativeMap();
        event.putString("videoTrackId", videoTrack.getTrackId());
        pushEvent(this, ON_PARTICIPANT_ADDED_VIDEO, event);
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        if (participantVideoTrack != null && primaryVideoView != null) {
            participantVideoTrack.removeRenderer(primaryVideoView);
        }
        participantVideoTrack = null;
    }
    // ===== EVENTS TO RN ==========================================================================

    private void pushEvent(View view, String name, WritableMap data) {
        Log.i("CustomTwilioVideoView", "SEND EVENT: "+name);
        Log.i("CustomTwilioVideoView", "EVENT DATA: "+data);
        this.themedReactContext.getJSModule(RCTNativeAppEventEmitter.class)
          .emit(name, data);
    }

    public static void registerPrimaryVideoView(VideoView v) {
        primaryVideoView = v;
        Log.i("CustomTwilioVideoView", "Participant track: "+participantVideoTrack);
        if (participantVideoTrack != null) {
            participantVideoTrack.addRenderer(v);
        }
    }

    public static void registerThumbnailVideoView(VideoView v) {
        thumbnailVideoView = v;
        if (localVideoTrack != null) {
            localVideoTrack.addRenderer(v);
        }
    }

}
