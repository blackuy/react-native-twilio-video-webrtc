/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 * <p>
 * Authors:
 * Ralph Pina <ralph.pina@gmail.com>
 * Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.twilio.video.AudioTrackPublication;
import com.twilio.video.BandwidthProfileMode;
import com.twilio.video.BandwidthProfileOptions;
import com.twilio.video.BaseTrackStats;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.H264Codec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalAudioTrackStats;
import com.twilio.video.LocalDataTrackPublication;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalTrackStats;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.LocalVideoTrackStats;
import com.twilio.video.NetworkQualityConfiguration;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.NetworkQualityVerbosity;
import com.twilio.video.Participant;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteAudioTrackStats;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteTrackStats;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.RemoteVideoTrackStats;
import com.twilio.video.Room;
import com.twilio.video.Room.State;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.TrackPriority;
import com.twilio.video.TrackPublication;
import com.twilio.video.TrackSwitchOffMode;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoBandwidthProfileOptions;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;

import org.webrtc.voiceengine.WebRtcAudioManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_AUDIO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CAMERA_SWITCHED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECT_FAILURE;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DATATRACK_MESSAGE_RECEIVED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_NETWORK_QUALITY_LEVELS_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_DATA_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISABLED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISABLED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ENABLED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ENABLED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_DATA_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_STATS_RECEIVED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_VIDEO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DOMINANT_SPEAKER_CHANGED;

public class CustomTwilioVideoView extends View implements LifecycleEventListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "CustomTwilioVideoView";
    private static final String DATA_TRACK_MESSAGE_THREAD_NAME = "DataTrackMessages";

    private static final VideoDimensions DEFAULT_MAX_CAPTURE_RESOLUTION = VideoDimensions.CIF_VIDEO_DIMENSIONS;
    private static final int DEFAULT_MAX_CAPTURE_FPS = 25;

    private boolean enableNetworkQualityReporting = false;
    private boolean isVideoEnabled = false;
    private boolean dominantSpeakerEnabled = false;
    private boolean enableH264Codec = false;

    private int audioBitrate = -1;
    private int videoBitrate = -1;

    private BandwidthProfileOptions bandwidthProfile;

    private VideoDimensions maxCaptureDimensions = CustomTwilioVideoView.DEFAULT_MAX_CAPTURE_RESOLUTION;
    private int maxCaptureFPS  = CustomTwilioVideoView.DEFAULT_MAX_CAPTURE_FPS;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Events.ON_CAMERA_SWITCHED,
            Events.ON_VIDEO_CHANGED,
            Events.ON_AUDIO_CHANGED,
            Events.ON_CONNECTED,
            Events.ON_CONNECT_FAILURE,
            Events.ON_DISCONNECTED,
            Events.ON_PARTICIPANT_CONNECTED,
            Events.ON_PARTICIPANT_DISCONNECTED,
            Events.ON_PARTICIPANT_ADDED_VIDEO_TRACK,
            Events.ON_DATATRACK_MESSAGE_RECEIVED,
            Events.ON_PARTICIPANT_ADDED_DATA_TRACK,
            Events.ON_PARTICIPANT_REMOVED_DATA_TRACK,
            Events.ON_PARTICIPANT_REMOVED_VIDEO_TRACK,
            Events.ON_PARTICIPANT_ADDED_AUDIO_TRACK,
            Events.ON_PARTICIPANT_REMOVED_AUDIO_TRACK,
            Events.ON_PARTICIPANT_ENABLED_VIDEO_TRACK,
            Events.ON_PARTICIPANT_DISABLED_VIDEO_TRACK,
            Events.ON_PARTICIPANT_ENABLED_AUDIO_TRACK,
            Events.ON_PARTICIPANT_DISABLED_AUDIO_TRACK,
            Events.ON_STATS_RECEIVED,
            Events.ON_NETWORK_QUALITY_LEVELS_CHANGED,
            Events.ON_DOMINANT_SPEAKER_CHANGED
    })
    public @interface Events {
        String ON_CAMERA_SWITCHED = "onCameraSwitched";
        String ON_VIDEO_CHANGED = "onVideoChanged";
        String ON_AUDIO_CHANGED = "onAudioChanged";
        String ON_CONNECTED = "onRoomDidConnect";
        String ON_CONNECT_FAILURE = "onRoomDidFailToConnect";
        String ON_DISCONNECTED = "onRoomDidDisconnect";
        String ON_PARTICIPANT_CONNECTED = "onRoomParticipantDidConnect";
        String ON_PARTICIPANT_DISCONNECTED = "onRoomParticipantDidDisconnect";
        String ON_DATATRACK_MESSAGE_RECEIVED = "onDataTrackMessageReceived";
        String ON_PARTICIPANT_ADDED_DATA_TRACK = "onParticipantAddedDataTrack";
        String ON_PARTICIPANT_REMOVED_DATA_TRACK = "onParticipantRemovedDataTrack";
        String ON_PARTICIPANT_ADDED_VIDEO_TRACK = "onParticipantAddedVideoTrack";
        String ON_PARTICIPANT_REMOVED_VIDEO_TRACK = "onParticipantRemovedVideoTrack";
        String ON_PARTICIPANT_ADDED_AUDIO_TRACK = "onParticipantAddedAudioTrack";
        String ON_PARTICIPANT_REMOVED_AUDIO_TRACK = "onParticipantRemovedAudioTrack";
        String ON_PARTICIPANT_ENABLED_VIDEO_TRACK = "onParticipantEnabledVideoTrack";
        String ON_PARTICIPANT_DISABLED_VIDEO_TRACK = "onParticipantDisabledVideoTrack";
        String ON_PARTICIPANT_ENABLED_AUDIO_TRACK = "onParticipantEnabledAudioTrack";
        String ON_PARTICIPANT_DISABLED_AUDIO_TRACK = "onParticipantDisabledAudioTrack";
        String ON_STATS_RECEIVED = "onStatsReceived";
        String ON_NETWORK_QUALITY_LEVELS_CHANGED = "onNetworkQualityLevelsChanged";
        String ON_DOMINANT_SPEAKER_CHANGED = "onDominantSpeakerDidChange";
    }

    private final ThemedReactContext themedReactContext;
    private final RCTEventEmitter eventEmitter;

    private AudioFocusRequest audioFocusRequest;
    private AudioAttributes playbackAttributes;
    private Handler handler = new Handler();

    /*
     * A Room represents communication between the client and one or more participants.
     */
    private static Room room;
    private String roomName = null;
    private String accessToken = null;
    private LocalParticipant localParticipant;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private static PatchedVideoView thumbnailVideoView;
    private static LocalVideoTrack localVideoTrack;

    private static CameraCapturer cameraCapturer;
    private LocalAudioTrack localAudioTrack;
    private AudioManager audioManager;
    private int previousAudioMode;
    private boolean disconnectedFromOnDestroy;
    private IntentFilter intentFilter;
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver;

      // Dedicated thread and handler for messages received from a RemoteDataTrack
    private final HandlerThread dataTrackMessageThread =
            new HandlerThread(DATA_TRACK_MESSAGE_THREAD_NAME);
    private Handler dataTrackMessageThreadHandler;

    private LocalDataTrack localDataTrack;

    // Map used to map remote data tracks to remote participants
    private final Map<RemoteDataTrack, RemoteParticipant> dataTrackRemoteParticipantMap =
            new HashMap<>();

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
            themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager) themedReactContext.getSystemService(Context.AUDIO_SERVICE);
        myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
        intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

        // Create the local data track
       // localDataTrack = LocalDataTrack.create(this);
       localDataTrack = LocalDataTrack.create(getContext());

       // Start the thread where data messages are received
        dataTrackMessageThread.start();
        dataTrackMessageThreadHandler = new Handler(dataTrackMessageThread.getLooper());

    }

    // ===== SETUP =================================================================================

    private VideoConstraints buildVideoConstraints() {
        Log.d(TAG,"Setting camera constraints. Max Dimensions: "
            + this.maxCaptureDimensions + " - Max FPS: " + this.maxCaptureFPS);

        return new VideoConstraints.Builder()
                .maxVideoDimensions(this.maxCaptureDimensions)
                .maxFps(this.maxCaptureFPS)
                .build();
    }

    private CameraCapturer createCameraCaputer(Context context, CameraCapturer.CameraSource cameraSource) {
        CameraCapturer newCameraCapturer = null;
        try {
            newCameraCapturer = new CameraCapturer(
                    context,
                    cameraSource,
                    new CameraCapturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() {
                        }

                        @Override
                        public void onCameraSwitched() {
                            setThumbnailMirror();
                        }

                        @Override
                        public void onError(int i) {
                            Log.i("CustomTwilioVideoView", "Error getting camera");
                        }
                    }
            );
            return newCameraCapturer;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean createLocalVideo(boolean enableVideo) {
      isVideoEnabled = enableVideo;
        // Share your camera
        cameraCapturer = this.createCameraCaputer(getContext(), CameraCapturer.CameraSource.FRONT_CAMERA);
        if (cameraCapturer == null){
            cameraCapturer = this.createCameraCaputer(getContext(), CameraCapturer.CameraSource.BACK_CAMERA);
        }
        if (cameraCapturer == null){
            WritableMap event = new WritableNativeMap();
            event.putString("error", "No camera is supported on this device");
            pushEvent(CustomTwilioVideoView.this, ON_CONNECT_FAILURE, event);
            return false;
        }

        if (cameraCapturer.getSupportedFormats().size() > 0) {
            localVideoTrack = LocalVideoTrack.create(getContext(), enableVideo, cameraCapturer, buildVideoConstraints());
            if (thumbnailVideoView != null && localVideoTrack != null) {
                localVideoTrack.addRenderer(thumbnailVideoView);
            }
            setThumbnailMirror();
        }
        return true;
    }

    // ===== LIFECYCLE EVENTS ======================================================================


    @Override
    public void onHostResume() {
        /*
         * In case it wasn't set.
         */
        if (themedReactContext.getCurrentActivity() != null) {
            /*
             * If the local video track was released when the app was put in the background, recreate.
             */
            if (cameraCapturer != null && localVideoTrack == null) {
                localVideoTrack = LocalVideoTrack.create(getContext(), isVideoEnabled, cameraCapturer, buildVideoConstraints());
            }

            if (localVideoTrack != null) {
                if (thumbnailVideoView != null) {
                    localVideoTrack.addRenderer(thumbnailVideoView);
                }

                /*
                 * If connected to a Room then share the local video track.
                 */
                if (localParticipant != null) {
                    localParticipant.publishTrack(localVideoTrack);
                }
            }

            themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        }
    }

    @Override
    public void onHostPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, remove from local
             * participant before releasing the video track. Participants will be notified that
             * the track has been removed.
             */
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localVideoTrack);
            }

            localVideoTrack.release();
            localVideoTrack = null;
        }
    }

    @Override
    public void onHostDestroy() {
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
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

        // Quit the data track message thread
        dataTrackMessageThread.quit();


    }

    public void releaseResource() {
        themedReactContext.removeLifecycleEventListener(this);
        room = null;
        localVideoTrack = null;
        thumbnailVideoView = null;
        cameraCapturer = null;
    }

    // ====== CONNECTING ===========================================================================

    public void connectToRoomWrapper(
            String roomName, String accessToken, boolean enableAudio, boolean enableVideo,
            ReadableMap encodingParameters, boolean enableNetworkQualityReporting, boolean dominantSpeakerEnabled,
            ReadableMap bandwidthProfileOptions) {
        this.roomName = roomName;
        this.accessToken = accessToken;
        this.enableNetworkQualityReporting = enableNetworkQualityReporting;
        this.dominantSpeakerEnabled = dominantSpeakerEnabled;

        if (encodingParameters.hasKey("enableH264Codec")) {
            this.enableH264Codec = encodingParameters.getBoolean("enableH264Codec");
        }

        if (encodingParameters.hasKey("audioBitrate")) {
            this.audioBitrate = encodingParameters.getInt("audioBitrate");
        }

        if (encodingParameters.hasKey("videoBitrate")) {
            this.videoBitrate = encodingParameters.getInt("videoBitrate");
        }

        this.bandwidthProfile = prepareBandwidthProfile(bandwidthProfileOptions);

        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(getContext(), enableAudio);

        if (cameraCapturer == null) {
                boolean createVideoStatus = createLocalVideo(enableVideo);
                if (!createVideoStatus) {
                    // No need to connect to room if video creation failed
                    return;
            }
        }

        connectToRoom(enableAudio);
    }

    // Functions to parse the bandwidth profile map
    private TrackPriority parsePriorityString(@Nullable String priority) {
        if (priority != null && !priority.trim().isEmpty()) {
            if (priority.toUpperCase().equals("LOW")) {
                return TrackPriority.LOW;
            } else if (priority.toUpperCase().equals("STANDARD")) {
                return TrackPriority.STANDARD;
            } else if (priority.toUpperCase().equals("HIGH")) {
                return TrackPriority.HIGH;
            } else if (priority.toUpperCase().equals("NULL")) {
                return null;
            } else {
                Log.w(TAG, "Unknown priority string" + priority);
                return null;
            }
        }

        return null;
    }

    private VideoDimensions parseDimensionsString(@Nullable String dimensions) {
        if (dimensions != null && !dimensions.trim().isEmpty()) {
            String[] dimensions_array = dimensions.split("x");

            // There can only be 2 items for a correct <width>x<height> string
            if (dimensions_array.length != 2) {
                return null;
            }

            int w = Integer.parseInt(dimensions_array[0]);
            int h = Integer.parseInt(dimensions_array[1]);

            return new VideoDimensions(w,h);
        }

        return null;
    }

    private BandwidthProfileOptions prepareBandwidthProfile(ReadableMap options) {

        BandwidthProfileMode mode = null;
        TrackSwitchOffMode trackSwitchOffMode = null;
        @Nullable Long maxTracks = null;
        @Nullable Long maxSubscriptionBitrate = null;
        TrackPriority dominantSpeakerPriority = null;
        Map<TrackPriority, VideoDimensions> renderDimensions = new HashMap<>();

        if (options.hasKey("mode")) {
            String modeString = options.getString("mode");

            // Parse mode of the current call
            if (modeString != null) {
                if (modeString.toUpperCase().equals("GRID")) {
                    mode = BandwidthProfileMode.GRID;
                } else if (modeString.toUpperCase().equals("COLLABORATION")) {
                    mode = BandwidthProfileMode.COLLABORATION;
                } else if (modeString.toUpperCase().equals("PRESENTATION")) {
                    mode = BandwidthProfileMode.PRESENTATION;
                } else {
                    Log.w(TAG, "Unknown Bandwidth Profile Mode" + modeString);
                }
            }
        }

        if (options.hasKey("trackSwitchOffMode")) {
            String trackSwitchOffModeString = options.getString("trackSwitchOffMode");

            // Parse mode of the current call
            if (trackSwitchOffModeString != null) {
                if (trackSwitchOffModeString.toUpperCase().equals("DISABLED")) {
                    trackSwitchOffMode = TrackSwitchOffMode.DISABLED;
                } else if (trackSwitchOffModeString.toUpperCase().equals("PREDICTED")) {
                    trackSwitchOffMode = TrackSwitchOffMode.PREDICTED;
                } else if (trackSwitchOffModeString.toUpperCase().equals("DETECTED")) {
                    trackSwitchOffMode = TrackSwitchOffMode.DETECTED;
                } else {
                    Log.w(TAG, "Unknown Track Switch Off Mode" + trackSwitchOffModeString);
                }
            }
        }

        // Parse max tracks to enabled during a call
        if (options.hasKey("maxTracks")) {
            int maxTracksAsInt = options.getInt("maxTracks");
            if (maxTracksAsInt > 0) {
                maxTracks = (long) maxTracksAsInt;
            }
        }

        // Parse max subscription bit rate
        if (options.hasKey("maxSubscriptionBitrate")) {
            int maxSubscriptionBitrateAsInt = options.getInt("maxSubscriptionBitrate");
            if (maxSubscriptionBitrateAsInt > 0) {
                maxSubscriptionBitrate = (long) maxSubscriptionBitrateAsInt;
            }
        }

        // Parse priority for dominant speaker
        if (options.hasKey("dominantSpeakerPriority")) {
            dominantSpeakerPriority = parsePriorityString(options.getString("dominantSpeakerPriority"));
        }

        // Parse Render Dimensions
        if (options.hasKey("renderDimensions")) {
            ReadableMap renderDimensionsMap = options.getMap("renderDimensions");
            if (renderDimensionsMap != null) {
                if (renderDimensionsMap.hasKey("low")) {
                    VideoDimensions dimensions = parseDimensionsString(renderDimensionsMap.getString("low"));
                    if (dimensions != null) {
                        renderDimensions.put(TrackPriority.LOW, dimensions);
                    }
                }

                if (renderDimensionsMap.hasKey("standard")) {
                    VideoDimensions dimensions = parseDimensionsString(renderDimensionsMap.getString("standard"));
                    if (dimensions != null) {
                        renderDimensions.put(TrackPriority.STANDARD, dimensions);
                    }
                }

                if (renderDimensionsMap.hasKey("high")) {
                    VideoDimensions dimensions = parseDimensionsString(renderDimensionsMap.getString("high"));
                    if (dimensions != null) {
                        renderDimensions.put(TrackPriority.HIGH, dimensions);
                    }
                }
            }
        }

        Log.d(TAG, "BandwidthProfile - mode: " + mode);
        Log.d(TAG, "BandwidthProfile - maxTracks: " + maxTracks);
        Log.d(TAG, "BandwidthProfile - dominantSpeakerPriority: " + dominantSpeakerPriority);
        Log.d(TAG, "BandwidthProfile - renderDimensions: " + renderDimensions);
        Log.d(TAG, "BandwidthProfile - trackSwitchOffMode: " + trackSwitchOffMode);

        VideoBandwidthProfileOptions videoBandwidthProfileOptions = new VideoBandwidthProfileOptions.Builder()
                .mode(mode)
                .maxTracks(maxTracks)
                .dominantSpeakerPriority(dominantSpeakerPriority)
                .maxSubscriptionBitrate(maxSubscriptionBitrate)
                .renderDimensions(renderDimensions)
                .trackSwitchOffMode(trackSwitchOffMode)
                .build();

        return new BandwidthProfileOptions(videoBandwidthProfileOptions);
    }

    public void connectToRoom(boolean enableAudio) {
        /*
         * Create a VideoClient allowing you to connect to a Room
         */
        setAudioFocus(enableAudio);
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(this.accessToken);

        if (this.roomName != null) {
            connectOptionsBuilder.roomName(this.roomName);
        }

        if (localAudioTrack != null) {
            connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
        }

        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }

        //LocalDataTrack localDataTrack = LocalDataTrack.create(getContext());

         if (localDataTrack != null) {
            connectOptionsBuilder.dataTracks(Collections.singletonList(localDataTrack));
        }

        connectOptionsBuilder.enableDominantSpeaker(this.dominantSpeakerEnabled);

         if (enableNetworkQualityReporting) {
             connectOptionsBuilder.enableNetworkQuality(true);
             connectOptionsBuilder.networkQualityConfiguration(new NetworkQualityConfiguration(
                     NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                     NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL));
         }

         // If we have specified bit rates then use them
        if (this.audioBitrate >= 0 && this.videoBitrate >= 0) {
            connectOptionsBuilder.encodingParameters(new EncodingParameters(this.audioBitrate, this.videoBitrate));
            Log.d(TAG, "Setting max audio rate" + String.valueOf(this.audioBitrate) + " and max video rate: " + String.valueOf(this.videoBitrate));
        } else {
            // If we have specified only 1 of the bit rate values
            if (this.audioBitrate >= 0 || this.videoBitrate >= 0) {
                // Then warn the user that we are ignoring the value
                Log.w(TAG, "Ignoring audio or video bitrate as only 1 of them is defined. Audio: " + String.valueOf(this.audioBitrate) + " Video:" + String.valueOf(this.videoBitrate));
            }
        }

         if (this.enableH264Codec) {
             connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(new H264Codec()));
             Log.d(TAG, "Preferring H264 Codec");
         }

        connectOptionsBuilder.bandwidthProfile(this.bandwidthProfile);

        room = Video.connect(getContext(), connectOptionsBuilder.build(), roomListener());
    }

    private void setAudioFocus(boolean focus) {
        if (focus) {
            previousAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(this,
                        AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            } else {
                playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                audioFocusRequest = new AudioFocusRequest
                        .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this, handler)
                        .build();
                audioManager.requestAudioFocus(audioFocusRequest);
            }
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
            getContext().registerReceiver(myNoisyAudioStreamReceiver, intentFilter);

        } else {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocus(this);
            } else if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }

            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(previousAudioMode);
            try {
                if (myNoisyAudioStreamReceiver != null) {
                    getContext().unregisterReceiver(myNoisyAudioStreamReceiver);
                }
                myNoisyAudioStreamReceiver = null;
            } catch (Exception e) {
                // already registered
                e.printStackTrace();
            }
        }
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            audioManager.setSpeakerphoneOn(true);
            if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                audioManager.setSpeakerphoneOn(!audioManager.isWiredHeadsetOn());
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.e(TAG, "onAudioFocusChange: focuschange: " + focusChange);
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
        setAudioFocus(false);
        if (cameraCapturer != null) {
            cameraCapturer.stopCapture();
            cameraCapturer = null;
        }
    }

    // ===== SEND STRING ON DATA TRACK ======================================================================
    public void sendString(String message) {
          if (localDataTrack != null) {
                localDataTrack.send(message);
          }
        }

    // ===== BUTTON LISTENERS ======================================================================
    private static void setThumbnailMirror() {
        if (cameraCapturer != null) {
            CameraCapturer.CameraSource cameraSource = cameraCapturer.getCameraSource();
            final boolean isBackCamera = (cameraSource == CameraCapturer.CameraSource.BACK_CAMERA);
            if (thumbnailVideoView != null && thumbnailVideoView.getVisibility() == View.VISIBLE) {
                thumbnailVideoView.setMirror(!isBackCamera);
            }
        }
    }

    public void switchCamera() {
        if (cameraCapturer != null) {
            cameraCapturer.switchCamera();
            CameraCapturer.CameraSource cameraSource = cameraCapturer.getCameraSource();
            final boolean isBackCamera = cameraSource == CameraCapturer.CameraSource.BACK_CAMERA;
            WritableMap event = new WritableNativeMap();
            event.putBoolean("isBackCamera", isBackCamera);
            pushEvent(CustomTwilioVideoView.this, ON_CAMERA_SWITCHED, event);
        }
    }

    public void toggleVideo(boolean enabled, ReadableMap cameraSettings) {

        if (cameraSettings != null) {
            if (cameraSettings.hasKey("maxDimensions")) {
                this.maxCaptureDimensions = parseDimensionsString(cameraSettings.getString("maxDimensions"));
            }

            if (cameraSettings.hasKey("maxFPS")) {
                this.maxCaptureFPS = cameraSettings.getInt("maxFPS");
            }
        }

        if (this.maxCaptureDimensions == null) {
            this.maxCaptureDimensions = CustomTwilioVideoView.DEFAULT_MAX_CAPTURE_RESOLUTION;
        }

        if (this.maxCaptureFPS < 1) {
            this.maxCaptureFPS = CustomTwilioVideoView.DEFAULT_MAX_CAPTURE_FPS;;
        }

        if (enabled && localVideoTrack == null) {
            createLocalVideo(enabled);
            if (localParticipant != null) {
                localParticipant.publishTrack(localVideoTrack);
            }
        }
        
        isVideoEnabled = enabled;

        if (localVideoTrack != null) {
            localVideoTrack.enable(enabled);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("videoEnabled", enabled);
            pushEvent(CustomTwilioVideoView.this, ON_VIDEO_CHANGED, event);
        }
    }

    public void toggleSoundSetup(boolean speaker){
      AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
      if(speaker){
        audioManager.setSpeakerphoneOn(true);
      } else {
        audioManager.setSpeakerphoneOn(false);
      }
    }

    public void toggleAudio(boolean enabled) {
        if (localAudioTrack != null) {
            localAudioTrack.enable(enabled);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("audioEnabled", enabled);
            pushEvent(CustomTwilioVideoView.this, ON_AUDIO_CHANGED, event);
        }
    }

    public void toggleBluetoothHeadset(boolean enabled) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if(enabled){
            audioManager.startBluetoothSco();
        } else {
            audioManager.stopBluetoothSco();
        }
    }

    public void toggleRemoteAudio(boolean enabled) {
        if (room != null) {
            for (RemoteParticipant rp : room.getRemoteParticipants()) {
                for(AudioTrackPublication at : rp.getAudioTracks()) {
                    if(at.getAudioTrack() != null) {
                        ((RemoteAudioTrack)at.getAudioTrack()).enablePlayback(enabled);
                    }
                }
            }
        }
    }

    public void setTrackPriority(String trackSid, String trackPriorityString) {
        TrackPriority priority = this.parsePriorityString(trackPriorityString);

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

    public void publishLocalVideo(boolean enabled) {
        if (localParticipant != null && localVideoTrack != null) {
            if (enabled) {
                localParticipant.publishTrack(localVideoTrack);
            } else {
                localParticipant.unpublishTrack(localVideoTrack);
            }
        }
    }

    public void publishLocalAudio(boolean enabled) {
        if (localParticipant != null && localAudioTrack != null) {
            if (enabled) {
                localParticipant.publishTrack(localAudioTrack);
            } else {
                localParticipant.unpublishTrack(localAudioTrack);
            }
        }
    }


    private void convertBaseTrackStats(BaseTrackStats bs, WritableMap result) {
        result.putString("codec", bs.codec);
        result.putInt("packetsLost", bs.packetsLost);
        result.putString("ssrc", bs.ssrc);
        result.putDouble("timestamp", bs.timestamp);
        result.putString("trackSid", bs.trackSid);
    }

    private void convertLocalTrackStats(LocalTrackStats ts, WritableMap result) {
        result.putDouble("bytesSent", ts.bytesSent);
        result.putInt("packetsSent", ts.packetsSent);
        result.putDouble("roundTripTime", ts.roundTripTime);
    }

    private void convertRemoteTrackStats(RemoteTrackStats ts, WritableMap result) {
        result.putDouble("bytesReceived", ts.bytesReceived);
        result.putInt("packetsReceived", ts.packetsReceived);
    }

    private WritableMap convertAudioTrackStats(RemoteAudioTrackStats as) {
        WritableMap result = new WritableNativeMap();
        result.putInt("audioLevel", as.audioLevel);
        result.putInt("jitter", as.jitter);
        convertBaseTrackStats(as, result);
        convertRemoteTrackStats(as, result);
        return result;
    }

    private WritableMap convertLocalAudioTrackStats(LocalAudioTrackStats as) {
        WritableMap result = new WritableNativeMap();
        result.putInt("audioLevel", as.audioLevel);
        result.putInt("jitter", as.jitter);
        convertBaseTrackStats(as, result);
        convertLocalTrackStats(as, result);
        return result;
    }

    private WritableMap convertVideoTrackStats(RemoteVideoTrackStats vs) {
        WritableMap result = new WritableNativeMap();
        WritableMap dimensions = new WritableNativeMap();
        dimensions.putInt("height", vs.dimensions.height);
        dimensions.putInt("width", vs.dimensions.width);
        result.putMap("dimensions", dimensions);
        result.putInt("frameRate", vs.frameRate);
        convertBaseTrackStats(vs, result);
        convertRemoteTrackStats(vs, result);
        return result;
    }

    private WritableMap convertLocalVideoTrackStats(LocalVideoTrackStats vs) {
        WritableMap result = new WritableNativeMap();
        WritableMap dimensions = new WritableNativeMap();
        dimensions.putInt("height", vs.dimensions.height);
        dimensions.putInt("width", vs.dimensions.width);
        result.putMap("dimensions", dimensions);
        result.putInt("frameRate", vs.frameRate);
        convertBaseTrackStats(vs, result);
        convertLocalTrackStats(vs, result);
        return result;
    }

    public void getStats() {
        if (room != null) {
            room.getStats(new StatsListener() {
                @Override
                public void onStats(List<StatsReport> statsReports) {
                    WritableMap event = new WritableNativeMap();
                    for (StatsReport sr : statsReports) {
                        WritableMap connectionStats = new WritableNativeMap();
                        WritableArray as = new WritableNativeArray();
                        for (RemoteAudioTrackStats s : sr.getRemoteAudioTrackStats()) {
                            as.pushMap(convertAudioTrackStats(s));
                        }
                        connectionStats.putArray("remoteAudioTrackStats", as);

                        WritableArray vs = new WritableNativeArray();
                        for (RemoteVideoTrackStats s : sr.getRemoteVideoTrackStats()) {
                            vs.pushMap(convertVideoTrackStats(s));
                        }
                        connectionStats.putArray("remoteVideoTrackStats", vs);

                        WritableArray las = new WritableNativeArray();
                        for (LocalAudioTrackStats s : sr.getLocalAudioTrackStats()) {
                            las.pushMap(convertLocalAudioTrackStats(s));
                        }
                        connectionStats.putArray("localAudioTrackStats", las);

                        WritableArray lvs = new WritableNativeArray();
                        for (LocalVideoTrackStats s : sr.getLocalVideoTrackStats()) {
                            lvs.pushMap(convertLocalVideoTrackStats(s));
                        }
                        connectionStats.putArray("localVideoTrackStats", lvs);
                        event.putMap(sr.getPeerConnectionId(), connectionStats);
                    }
                    pushEvent(CustomTwilioVideoView.this, ON_STATS_RECEIVED, event);
                }
            });
        }
    }

    public void disableOpenSLES() {
        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true);
    }

    // ====== ROOM LISTENER ========================================================================

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                localParticipant.setListener(localListener());

                WritableMap event = new WritableNativeMap();
                event.putString("roomName", room.getName());
                event.putString("roomSid", room.getSid());
                List<RemoteParticipant> participants = room.getRemoteParticipants();

                WritableArray participantsArray = new WritableNativeArray();
                for (RemoteParticipant participant : participants) {
                    participantsArray.pushMap(buildParticipant(participant));
                }
                participantsArray.pushMap(buildParticipant(localParticipant));
                event.putArray("participants", participantsArray);

                pushEvent(CustomTwilioVideoView.this, ON_CONNECTED, event);


                //There is not .publish it's publishTrack
                localParticipant.publishTrack(localDataTrack);

                for (RemoteParticipant participant : participants) {
                    addParticipant(room, participant);
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                WritableMap event = new WritableNativeMap();
                event.putString("roomName", room.getName());
                event.putString("roomSid", room.getSid());
                event.putString("error", e.getMessage());
                pushEvent(CustomTwilioVideoView.this, ON_CONNECT_FAILURE, event);
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onReconnected(@NonNull Room room) {

            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                WritableMap event = new WritableNativeMap();

                if (localParticipant != null) {
                  event.putString("participant", localParticipant.getIdentity());
                }
                event.putString("roomName", room.getName());
                event.putString("roomSid", room.getSid());
                if (e != null) {
                  event.putString("error", e.getMessage());
                }
                pushEvent(CustomTwilioVideoView.this, ON_DISCONNECTED, event);

                localParticipant = null;
                roomName = null;
                accessToken = null;


                CustomTwilioVideoView.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    setAudioFocus(false);
                }
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant participant) {
                addParticipant(room, participant);

            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant participant) {
                removeParticipant(room, participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
            }

            @Override
            public void onRecordingStopped(Room room) {
            }

            @Override
            public void onDominantSpeakerChanged(Room room, RemoteParticipant remoteParticipant) {
                WritableMap event = new WritableNativeMap();

                event.putString("roomName", room.getName());
                event.putString("roomSid", room.getSid());

                if (remoteParticipant == null) {
                    event.putString("participant", "");
                } else {
                    event.putMap("participant", buildParticipant(remoteParticipant));
                }

                pushEvent(CustomTwilioVideoView.this, ON_DOMINANT_SPEAKER_CHANGED, event);
            }
        };
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(Room room, RemoteParticipant remoteParticipant) {

        WritableMap event = new WritableNativeMap();
        event.putString("roomName", room.getName());
        event.putString("roomSid", room.getSid());
        event.putMap("participant", buildParticipant(remoteParticipant));

        pushEvent(this, ON_PARTICIPANT_CONNECTED, event);

        /*
         * Start listening for participant media events
         */
        remoteParticipant.setListener(mediaListener());

        for (final RemoteDataTrackPublication remoteDataTrackPublication :
              remoteParticipant.getRemoteDataTracks()) {
          /*
            * Data track messages are received on the thread that calls setListener. Post the
            * invocation of setting the listener onto our dedicated data track message thread.
            */
          if (remoteDataTrackPublication.isTrackSubscribed()) {
              dataTrackMessageThreadHandler.post(() -> addRemoteDataTrack(remoteParticipant,
                      remoteDataTrackPublication.getRemoteDataTrack()));
          }
      }
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(Room room, RemoteParticipant participant) {
        WritableMap event = new WritableNativeMap();
        event.putString("roomName", room.getName());
        event.putString("roomSid", room.getSid());
        event.putMap("participant", buildParticipant(participant));
        pushEvent(this, ON_PARTICIPANT_DISCONNECTED, event);
        //something about this breaking.
        //participant.setListener(null);
    }

    private void addRemoteDataTrack(RemoteParticipant remoteParticipant, RemoteDataTrack remoteDataTrack) {
        dataTrackRemoteParticipantMap.put(remoteDataTrack, remoteParticipant);
        remoteDataTrack.setListener(remoteDataTrackListener());
    }

    // ====== MEDIA LISTENER =======================================================================

    private RemoteParticipant.Listener mediaListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackSubscribed(RemoteParticipant participant, RemoteAudioTrackPublication publication, RemoteAudioTrack audioTrack) {
              WritableMap event = buildParticipantVideoEvent(participant, publication);
              pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ADDED_AUDIO_TRACK, event);
            }

            @Override
            public void onAudioTrackUnsubscribed(RemoteParticipant participant, RemoteAudioTrackPublication publication, RemoteAudioTrack audioTrack) {
              WritableMap event = buildParticipantVideoEvent(participant, publication);
              pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_REMOVED_AUDIO_TRACK, event);
            }

            @Override
            public void onAudioTrackSubscriptionFailed(RemoteParticipant participant, RemoteAudioTrackPublication publication, TwilioException twilioException) {

            }

            @Override
            public void onAudioTrackPublished(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
            }

            @Override
            public void onAudioTrackUnpublished(RemoteParticipant participant, RemoteAudioTrackPublication publication) {

            }

            @Override
            public void onDataTrackSubscribed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication, RemoteDataTrack remoteDataTrack) {
                 WritableMap event = buildParticipantDataEvent(remoteParticipant);
                 pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ADDED_DATA_TRACK, event);
                 dataTrackMessageThreadHandler.post(() -> addRemoteDataTrack(remoteParticipant, remoteDataTrack));
            }

            @Override
            public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication publication, RemoteDataTrack remoteDataTrack) {
                 WritableMap event = buildParticipantDataEvent(remoteParticipant);
                 pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_REMOVED_DATA_TRACK, event);
            }

            @Override
            public void onDataTrackSubscriptionFailed(RemoteParticipant participant, RemoteDataTrackPublication publication, TwilioException twilioException) {

            }

            @Override
            public void onDataTrackPublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {

            }

            @Override
            public void onDataTrackUnpublished(RemoteParticipant participant, RemoteDataTrackPublication publication) {

            }

            @Override
            public void onVideoTrackSubscribed(RemoteParticipant participant, RemoteVideoTrackPublication publication, RemoteVideoTrack videoTrack) {
                addParticipantVideo(participant, publication);
            }

            @Override
            public void onVideoTrackUnsubscribed(RemoteParticipant participant, RemoteVideoTrackPublication publication, RemoteVideoTrack videoTrack) {
                removeParticipantVideo(participant, publication);
            }

            @Override
            public void onVideoTrackSubscriptionFailed(RemoteParticipant participant, RemoteVideoTrackPublication publication, TwilioException twilioException) {
            }

            @Override
            public void onVideoTrackPublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {

            }

            @Override
            public void onVideoTrackUnpublished(RemoteParticipant participant, RemoteVideoTrackPublication publication) {

            }

            @Override
            public void onAudioTrackEnabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {//                Log.i(TAG, "onAudioTrackEnabled");
//                publication.getRemoteAudioTrack().enablePlayback(false);
                WritableMap event = buildParticipantVideoEvent(participant, publication);
                pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ENABLED_AUDIO_TRACK, event);
            }

            @Override
            public void onAudioTrackDisabled(RemoteParticipant participant, RemoteAudioTrackPublication publication) {
                WritableMap event = buildParticipantVideoEvent(participant, publication);
                pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_DISABLED_AUDIO_TRACK, event);
            }

            @Override
            public void onVideoTrackEnabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
                WritableMap event = buildParticipantVideoEvent(participant, publication);
                pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ENABLED_VIDEO_TRACK, event);
            }

            @Override
            public void onVideoTrackDisabled(RemoteParticipant participant, RemoteVideoTrackPublication publication) {
                WritableMap event = buildParticipantVideoEvent(participant, publication);
                pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_DISABLED_VIDEO_TRACK, event);
            }

            @Override
            public void onNetworkQualityLevelChanged(RemoteParticipant remoteParticipant, NetworkQualityLevel networkQualityLevel) {
                WritableMap event = new WritableNativeMap();
                event.putMap("participant", buildParticipant(remoteParticipant));
                event.putBoolean("isLocalUser", false);

                // Twilio SDK defines Enum 0 as UNKNOWN and 1 as Quality ZERO, so we subtract one to get the correct quality level as an integer
                event.putInt("quality", networkQualityLevel.ordinal() - 1);

                pushEvent(CustomTwilioVideoView.this, ON_NETWORK_QUALITY_LEVELS_CHANGED, event);
            }
        };
    }

    // ====== LOCAL LISTENER =======================================================================
    private LocalParticipant.Listener localListener() {
        return new LocalParticipant.Listener() {

            @Override
            public void onAudioTrackPublished(LocalParticipant localParticipant, LocalAudioTrackPublication localAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackPublicationFailed(LocalParticipant localParticipant, LocalAudioTrack localAudioTrack, TwilioException twilioException) {

            }

            @Override
            public void onVideoTrackPublished(LocalParticipant localParticipant, LocalVideoTrackPublication localVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackPublicationFailed(LocalParticipant localParticipant, LocalVideoTrack localVideoTrack, TwilioException twilioException) {

            }

            @Override
            public void onDataTrackPublished(LocalParticipant localParticipant, LocalDataTrackPublication localDataTrackPublication) {

            }

            @Override
            public void onDataTrackPublicationFailed(LocalParticipant localParticipant, LocalDataTrack localDataTrack, TwilioException twilioException) {

            }

            @Override
            public void onNetworkQualityLevelChanged(LocalParticipant localParticipant, NetworkQualityLevel networkQualityLevel) {
                WritableMap event = new WritableNativeMap();
                event.putMap("participant", buildParticipant(localParticipant));
                event.putBoolean("isLocalUser", true);

                // Twilio SDK defines Enum 0 as UNKNOWN and 1 as Quality ZERO, so we subtract one to get the correct quality level as an integer
                event.putInt("quality", networkQualityLevel.ordinal() - 1);

                pushEvent(CustomTwilioVideoView.this, ON_NETWORK_QUALITY_LEVELS_CHANGED, event);
            }
        };
    }

    private WritableMap buildParticipant(Participant participant) {
        WritableMap participantMap = new WritableNativeMap();
        participantMap.putString("identity", participant.getIdentity());
        participantMap.putString("sid", participant.getSid());
        return participantMap;
    }


    private WritableMap buildParticipantDataEvent(Participant participant) {
        WritableMap participantMap = buildParticipant(participant);
        WritableMap participantMap2 = buildParticipant(participant);

        WritableMap event = new WritableNativeMap();
        event.putMap("participant", participantMap);
        event.putMap("track", participantMap2);
        return event;
    }

    private WritableMap buildParticipantVideoEvent(Participant participant, TrackPublication publication) {
        WritableMap participantMap = buildParticipant(participant);

        WritableMap trackMap = new WritableNativeMap();
        trackMap.putString("trackSid", publication.getTrackSid());
        trackMap.putString("trackName", publication.getTrackName());
        trackMap.putBoolean("enabled", publication.isTrackEnabled());

        WritableMap event = new WritableNativeMap();
        event.putMap("participant", participantMap);
        event.putMap("track", trackMap);
        return event;
    }

    private WritableMap buildDataTrackEvent(String message) {
        WritableMap event = new WritableNativeMap();
        event.putString("message", message);
        return event;
    }

    private void addParticipantVideo(Participant participant, RemoteVideoTrackPublication publication) {
        WritableMap event = this.buildParticipantVideoEvent(participant, publication);
        pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ADDED_VIDEO_TRACK, event);
    }

    private void removeParticipantVideo(Participant participant, RemoteVideoTrackPublication deleteVideoTrack) {
        WritableMap event = this.buildParticipantVideoEvent(participant, deleteVideoTrack);
        pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_REMOVED_VIDEO_TRACK, event);
    }
    // ===== EVENTS TO RN ==========================================================================

    void pushEvent(View view, String name, WritableMap data) {
        eventEmitter.receiveEvent(view.getId(), name, data);
    }

    public static void registerPrimaryVideoView(PatchedVideoView v, String trackSid) {
        if (room != null) {

            for (RemoteParticipant participant : room.getRemoteParticipants()) {
                for (RemoteVideoTrackPublication publication : participant.getRemoteVideoTracks()) {
                    RemoteVideoTrack track = publication.getRemoteVideoTrack();
                    if (track == null) {
                        continue;
                    }
                    if (publication.getTrackSid().equals(trackSid)) {
                        track.addRenderer(v);
                    } else {
                        track.removeRenderer(v);
                    }
                }
            }
        }
    }

    public static void registerThumbnailVideoView(PatchedVideoView v) {
        thumbnailVideoView = v;
        if (localVideoTrack != null) {
            localVideoTrack.addRenderer(v);
        }
        setThumbnailMirror();
    }

    private RemoteDataTrack.Listener remoteDataTrackListener() {
        return new RemoteDataTrack.Listener() {

            @Override
            public void onMessage(RemoteDataTrack remoteDataTrack, ByteBuffer byteBuffer) {

            }


            @Override
            public void onMessage(RemoteDataTrack remoteDataTrack, String message) {
                WritableMap event = buildDataTrackEvent(message);
                pushEvent(CustomTwilioVideoView.this, ON_DATATRACK_MESSAGE_RECEIVED, event);
            }
        };
    }
}
