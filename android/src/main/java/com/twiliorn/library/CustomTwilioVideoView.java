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
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.twilio.video.AudioTrackPublication;
import com.twilio.video.BaseTrackStats;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
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
import com.twilio.video.TrackPublication;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoCodec;

import org.webrtc.voiceengine.WebRtcAudioManager;

import tvi.webrtc.Camera1Enumerator;
import tvi.webrtc.HardwareVideoEncoderFactory;
import tvi.webrtc.HardwareVideoDecoderFactory;
import tvi.webrtc.VideoCodecInfo;
import com.twilio.video.H264Codec;
import com.twilio.video.Vp8Codec;

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
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS;

public class CustomTwilioVideoView extends View implements LifecycleEventListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "CustomTwilioVideoView";
    private static final String DATA_TRACK_MESSAGE_THREAD_NAME = "DataTrackMessages";
    private static final String FRONT_CAMERA_TYPE = "front";
    private static final String BACK_CAMERA_TYPE = "back";
    private boolean enableRemoteAudio = false;
    private boolean enableNetworkQualityReporting = false;
    private boolean isVideoEnabled = false;
    private boolean dominantSpeakerEnabled = false;
    private static String frontFacingDevice;
    private static String backFacingDevice;
    private boolean maintainVideoTrackInBackground = false;
    private String cameraType = "";
    private boolean enableH264Codec = false;

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
            Events.ON_DOMINANT_SPEAKER_CHANGED,
            Events.ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS,
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
        String ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS = "onLocalParticipantSupportedCodecs";
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

    private VideoFormat buildVideoFormat() {
        return new VideoFormat(VideoDimensions.CIF_VIDEO_DIMENSIONS, 15);
    }

    private CameraCapturer createCameraCaputer(Context context, String cameraId) {
        CameraCapturer newCameraCapturer = null;
        try {
            newCameraCapturer = new CameraCapturer(
                    context,
                    cameraId,
                    new CameraCapturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() {
                        }

                        @Override
                        public void onCameraSwitched(String newCameraId) {
                            setThumbnailMirror();
                            WritableMap event = new WritableNativeMap();
                            event.putBoolean("isBackCamera", isCurrentCameraSourceBackFacing());
                            pushEvent(CustomTwilioVideoView.this, ON_CAMERA_SWITCHED, event);
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

    private void buildDeviceInfo() {
        Camera1Enumerator enumerator = new Camera1Enumerator();
        String[] deviceNames = enumerator.getDeviceNames();
        backFacingDevice = null;
        frontFacingDevice = null;
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName) && enumerator.getSupportedFormats(deviceName).size() > 0) {
                backFacingDevice = deviceName;
            } else if (enumerator.isFrontFacing(deviceName) && enumerator.getSupportedFormats(deviceName).size() > 0) {
                frontFacingDevice = deviceName;
            }
        }
    }

    private boolean createLocalVideo(boolean enableVideo, String cameraType) {
        isVideoEnabled = enableVideo;

        // Share your camera
        buildDeviceInfo();

        if (cameraType.equals(CustomTwilioVideoView.FRONT_CAMERA_TYPE)) {
            if (frontFacingDevice != null) {
                cameraCapturer = this.createCameraCaputer(getContext(), frontFacingDevice);
            } else {
                // IF the camera is unavailable try the other camera
                cameraCapturer = this.createCameraCaputer(getContext(), backFacingDevice);
            }
        } else {
            if (backFacingDevice != null) {
                cameraCapturer = this.createCameraCaputer(getContext(), backFacingDevice);
            } else {
                // IF the camera is unavailable try the other camera
                cameraCapturer = this.createCameraCaputer(getContext(), frontFacingDevice);
            }
        }

        // If no camera is available let the caller know
        if (cameraCapturer == null) {
            WritableMap event = new WritableNativeMap();
            event.putString("error", "No camera is supported on this device");
            pushEvent(CustomTwilioVideoView.this, ON_CONNECT_FAILURE, event);
            return false;
        }

        localVideoTrack = LocalVideoTrack.create(getContext(), enableVideo, cameraCapturer, buildVideoFormat());
        if (thumbnailVideoView != null && localVideoTrack != null) {
            localVideoTrack.addSink(thumbnailVideoView);
        }
        setThumbnailMirror();
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
                localVideoTrack = LocalVideoTrack.create(getContext(), isVideoEnabled, cameraCapturer, buildVideoFormat());
            }

            if (localVideoTrack != null) {
                if (thumbnailVideoView != null) {
                    localVideoTrack.addSink(thumbnailVideoView);
                }

                /*
                 * If connected to a Room then share the local video track.
                 */
                if (localParticipant != null) {
                    localParticipant.publishTrack(localVideoTrack);
                }
            }

            if (room != null) {
                themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            }


        }
    }

    @Override
    public void onHostPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null && !maintainVideoTrackInBackground) {
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
         * Remove stream voice control
         */
        if (themedReactContext.getCurrentActivity() != null) {
            themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }
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
            audioManager.stopBluetoothSco();
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
            String roomName,
            String accessToken,
            boolean enableAudio,
            boolean enableVideo,
            boolean enableRemoteAudio,
            boolean enableNetworkQualityReporting,
            boolean dominantSpeakerEnabled,
            boolean maintainVideoTrackInBackground,
            String cameraType,
            boolean enableH264Codec
    ) {
        this.roomName = roomName;
        this.accessToken = accessToken;
        this.enableRemoteAudio = enableRemoteAudio;
        this.enableNetworkQualityReporting = enableNetworkQualityReporting;
        this.dominantSpeakerEnabled = dominantSpeakerEnabled;
        this.maintainVideoTrackInBackground = maintainVideoTrackInBackground;
        this.cameraType = cameraType;
        this.enableH264Codec = enableH264Codec;

        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(getContext(), enableAudio);

        if (cameraCapturer == null && enableVideo) {
            boolean createVideoStatus = createLocalVideo(enableVideo, cameraType);
            if (!createVideoStatus) {
                Log.d("RNTwilioVideo", "Failed to create local video");
                // No need to connect to room if video creation failed
                return;
            }
        } else {
            isVideoEnabled = false;
        }

        setAudioFocus(enableAudio);
        connectToRoom();
    }

    public void connectToRoom() {
        /*
         * Create a VideoClient allowing you to connect to a Room
         */
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

        // H264 Codec Support Detection: https://www.twilio.com/docs/video/managing-codecs
        HardwareVideoEncoderFactory hardwareVideoEncoderFactory = new HardwareVideoEncoderFactory(null, true, true);
        HardwareVideoDecoderFactory hardwareVideoDecoderFactory = new HardwareVideoDecoderFactory(null);

        boolean h264EncoderSupported = false;
        for (VideoCodecInfo videoCodecInfo : hardwareVideoEncoderFactory.getSupportedCodecs()) {
            if (videoCodecInfo.name.equalsIgnoreCase("h264")) {
                h264EncoderSupported = true;
                break;
            }
        }
        boolean h264DecoderSupported = false;
        for (VideoCodecInfo videoCodecInfo : hardwareVideoDecoderFactory.getSupportedCodecs()) {
            if (videoCodecInfo.name.equalsIgnoreCase("h264")) {
                h264DecoderSupported = true;
                break;
            }
        }

        boolean isH264Supported = h264EncoderSupported && h264DecoderSupported;

        Log.d("RNTwilioVideo", "H264 supported by hardware: " + isH264Supported);

        WritableArray supportedCodecs = new WritableNativeArray();

        VideoCodec videoCodec =  new Vp8Codec();
        // VP8 is supported on all android devices by default
        supportedCodecs.pushString(videoCodec.toString());

        if (isH264Supported && this.enableH264Codec) {
            videoCodec = new H264Codec();
            supportedCodecs.pushString(videoCodec.toString());
        }

        WritableMap event = new WritableNativeMap();

        event.putArray("supportedCodecs", supportedCodecs);

        pushEvent(CustomTwilioVideoView.this, ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS, event);

        connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

        connectOptionsBuilder.enableDominantSpeaker(this.dominantSpeakerEnabled);

        if (enableNetworkQualityReporting) {
            connectOptionsBuilder.enableNetworkQuality(true);
            connectOptionsBuilder.networkQualityConfiguration(new NetworkQualityConfiguration(
                    NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                    NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL));
        }

        room = Video.connect(getContext(), connectOptionsBuilder.build(), roomListener());
    }

    public void setAudioType() {
        AudioDeviceInfo[] devicesInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        boolean hasNonSpeakerphoneDevice = false;
        for (int i = 0; i < devicesInfo.length; i++) {
            int deviceType = devicesInfo[i].getType();
            if (
                deviceType == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                deviceType == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
            ) {
                hasNonSpeakerphoneDevice = true;
            }
            if (
                deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                deviceType == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            ) {
                audioManager.startBluetoothSco();
                audioManager.setBluetoothScoOn(true);
                hasNonSpeakerphoneDevice = true;
            }
        }
        audioManager.setSpeakerphoneOn(!hasNonSpeakerphoneDevice);
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
            setAudioType();
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
                setAudioType();
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
            audioManager.stopBluetoothSco();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
            audioManager.stopBluetoothSco();
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

    private static boolean isCurrentCameraSourceBackFacing() {
        return cameraCapturer != null && cameraCapturer.getCameraId() == backFacingDevice;
    }

    // ===== BUTTON LISTENERS ======================================================================
    private static void setThumbnailMirror() {
        if (cameraCapturer != null) {
            final boolean isBackCamera = isCurrentCameraSourceBackFacing();
            if (thumbnailVideoView != null && thumbnailVideoView.getVisibility() == View.VISIBLE) {
                thumbnailVideoView.setMirror(!isBackCamera);
            }
        }
    }

    public void switchCamera() {
        if (cameraCapturer != null) {
            final boolean isBackCamera = isCurrentCameraSourceBackFacing();
            if (frontFacingDevice != null && (isBackCamera || backFacingDevice == null)) {
                cameraCapturer.switchCamera(frontFacingDevice);
                cameraType = CustomTwilioVideoView.FRONT_CAMERA_TYPE;
            } else {
                cameraCapturer.switchCamera(backFacingDevice);
                cameraType = CustomTwilioVideoView.BACK_CAMERA_TYPE;
            }
        }
    }

    public void toggleVideo(boolean enabled) {
        isVideoEnabled = enabled;

        if (cameraCapturer == null && enabled) {
            String fallbackCameraType = cameraType == null ? CustomTwilioVideoView.FRONT_CAMERA_TYPE : cameraType;
            boolean createVideoStatus = createLocalVideo(true, fallbackCameraType);
            if (!createVideoStatus) {
                Log.d("RNTwilioVideo", "Failed to create local video");
                return;
            }
        }

        if (localVideoTrack != null) {
            localVideoTrack.enable(enabled);
            publishLocalVideo(enabled);

            WritableMap event = new WritableNativeMap();
            event.putBoolean("videoEnabled", enabled);
            pushEvent(CustomTwilioVideoView.this, ON_VIDEO_CHANGED, event);
        }
    }

    public void toggleSoundSetup(boolean speaker) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (speaker) {
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
        if (enabled) {
            audioManager.startBluetoothSco();
        } else {
            audioManager.stopBluetoothSco();
        }
    }

    public void toggleRemoteAudio(boolean enabled) {
        if (room != null) {
            for (RemoteParticipant rp : room.getRemoteParticipants()) {
                for (AudioTrackPublication at : rp.getAudioTracks()) {
                    if (at.getAudioTrack() != null) {
                        ((RemoteAudioTrack) at.getAudioTrack()).enablePlayback(enabled);
                    }
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
                /*
                 * Enable changing the volume using the up/down keys during a conversation
                 */
                if (themedReactContext.getCurrentActivity() != null) {
                    themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                }

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
                event.putMap("localParticipant", buildParticipant(localParticipant));

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

                /*
                 * Remove stream voice control
                 */
                if (themedReactContext.getCurrentActivity() != null) {
                    themedReactContext.getCurrentActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                }
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
                audioTrack.enablePlayback(enableRemoteAudio);
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
                WritableMap event = buildParticipantDataEvent(remoteParticipant, remoteDataTrackPublication);
                pushEvent(CustomTwilioVideoView.this, ON_PARTICIPANT_ADDED_DATA_TRACK, event);
                dataTrackMessageThreadHandler.post(() -> addRemoteDataTrack(remoteParticipant, remoteDataTrack));
            }

            @Override
            public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant, RemoteDataTrackPublication remoteDataTrackPublication, RemoteDataTrack remoteDataTrack) {
                WritableMap event = buildParticipantDataEvent(remoteParticipant, remoteDataTrackPublication);
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

    private WritableMap buildTrack(TrackPublication publication) {
        WritableMap trackMap = new WritableNativeMap();
        trackMap.putString("trackSid", publication.getTrackSid());
        trackMap.putString("trackName", publication.getTrackName());
        trackMap.putBoolean("enabled", publication.isTrackEnabled());
        return trackMap;
    }

    private WritableMap buildParticipantDataEvent(Participant participant, TrackPublication publication) {
        WritableMap participantMap = buildParticipant(participant);
        WritableMap trackMap = buildTrack(publication);

        WritableMap event = new WritableNativeMap();
        event.putMap("participant", participantMap);
        event.putMap("track", trackMap);
        return event;
    }

    private WritableMap buildParticipantVideoEvent(Participant participant, TrackPublication publication) {
        WritableMap participantMap = buildParticipant(participant);
        WritableMap trackMap = buildTrack(publication);

        WritableMap event = new WritableNativeMap();
        event.putMap("participant", participantMap);
        event.putMap("track", trackMap);
        return event;
    }

    private WritableMap buildDataTrackEvent(RemoteDataTrack remoteDataTrack, String message) {
        WritableMap event = new WritableNativeMap();
        event.putString("message", message);
        event.putString("trackSid", remoteDataTrack.getSid());
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
                        track.addSink(v);
                    } else {
                        track.removeSink(v);
                    }
                }
            }
        }
    }

    public static void registerThumbnailVideoView(PatchedVideoView v) {
        thumbnailVideoView = v;
        if (localVideoTrack != null) {
            localVideoTrack.addSink(v);
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
                WritableMap event = buildDataTrackEvent(remoteDataTrack, message);
                pushEvent(CustomTwilioVideoView.this, ON_DATATRACK_MESSAGE_RECEIVED, event);
            }
        };
    }
}
