/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 * <p>
 * Authors:
 * Ralph Pina <ralph.pina@gmail.com>
 * Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_AUDIO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CAMERA_SWITCHED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECT_FAILURE;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_VIDEO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_DATA_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_DATA_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DATATRACK_MESSAGE_RECEIVED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_REMOVED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ENABLED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISABLED_VIDEO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ENABLED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISABLED_AUDIO_TRACK;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_STATS_RECEIVED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_NETWORK_QUALITY_LEVELS_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DOMINANT_SPEAKER_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS;

public class CustomTwilioVideoViewManager extends SimpleViewManager<CustomTwilioVideoView> {
    public static final String REACT_CLASS = "RNCustomTwilioVideoView";

    private static final int CONNECT_TO_ROOM = 1;
    private static final int DISCONNECT = 2;
    private static final int SWITCH_CAMERA = 3;
    private static final int TOGGLE_VIDEO = 4;
    private static final int TOGGLE_SOUND = 5;
    private static final int GET_STATS = 6;
    private static final int DISABLE_OPENSL_ES = 7;
    private static final int TOGGLE_SOUND_SETUP = 8;
    private static final int TOGGLE_REMOTE_SOUND = 9;
    private static final int RELEASE_RESOURCE = 10;
    private static final int TOGGLE_BLUETOOTH_HEADSET = 11;
    private static final int SEND_STRING = 12;
    private static final int PUBLISH_VIDEO = 13;
    private static final int PUBLISH_AUDIO = 14;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CustomTwilioVideoView createViewInstance(ThemedReactContext reactContext) {
        return new CustomTwilioVideoView(reactContext);
    }

    @Override
    public void receiveCommand(CustomTwilioVideoView view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case CONNECT_TO_ROOM:
                String roomName = args.getString(0);
                String accessToken = args.getString(1);
                boolean enableAudio = args.getBoolean(2);
                boolean enableVideo = args.getBoolean(3);
                boolean enableRemoteAudio = args.getBoolean(4);
                boolean enableNetworkQualityReporting = args.getBoolean(5);
                boolean dominantSpeakerEnabled = args.getBoolean(6);
                boolean maintainVideoTrackInBackground = args.getBoolean(7);
                String cameraType = args.getString(8);
                ReadableMap encodingParameters = args.getMap(9);
                boolean enableH264Codec = encodingParameters.hasKey("enableH264Codec") ? encodingParameters.getBoolean("enableH264Codec") : false;
                view.connectToRoomWrapper(
                    roomName,
                    accessToken,
                    enableAudio,
                    enableVideo,
                    enableRemoteAudio,
                    enableNetworkQualityReporting,
                    dominantSpeakerEnabled,
                    maintainVideoTrackInBackground,
                    cameraType,
                    enableH264Codec
                  );
                break;
            case DISCONNECT:
                view.disconnect();
                break;
            case SWITCH_CAMERA:
                view.switchCamera();
                break;
            case TOGGLE_VIDEO:
                Boolean videoEnabled = args.getBoolean(0);
                view.toggleVideo(videoEnabled);
                break;
            case TOGGLE_SOUND:
                Boolean audioEnabled = args.getBoolean(0);
                view.toggleAudio(audioEnabled);
                break;
            case GET_STATS:
                view.getStats();
                break;
            case DISABLE_OPENSL_ES:
                view.disableOpenSLES();
                break;
            case TOGGLE_SOUND_SETUP:
                Boolean speaker = args.getBoolean(0);
                view.toggleSoundSetup(speaker);
                break;
            case TOGGLE_REMOTE_SOUND:
                Boolean remoteAudioEnabled = args.getBoolean(0);
                view.toggleRemoteAudio(remoteAudioEnabled);
                break;
            case RELEASE_RESOURCE:
                view.releaseResource();
                break;
            case TOGGLE_BLUETOOTH_HEADSET:
                Boolean headsetEnabled = args.getBoolean(0);
                view.toggleBluetoothHeadset(headsetEnabled);
                break;
            case SEND_STRING:
                view.sendString(args.getString(0));
                break;
            case PUBLISH_VIDEO:
                view.publishLocalVideo(args.getBoolean(0));
                break;
            case PUBLISH_AUDIO:
                view.publishLocalAudio(args.getBoolean(0));
                break;
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map<String, Map<String, String>> map = MapBuilder.of(
                ON_CAMERA_SWITCHED, MapBuilder.of("registrationName", ON_CAMERA_SWITCHED),
                ON_VIDEO_CHANGED, MapBuilder.of("registrationName", ON_VIDEO_CHANGED),
                ON_AUDIO_CHANGED, MapBuilder.of("registrationName", ON_AUDIO_CHANGED),
                ON_CONNECTED, MapBuilder.of("registrationName", ON_CONNECTED),
                ON_CONNECT_FAILURE, MapBuilder.of("registrationName", ON_CONNECT_FAILURE),
                ON_DISCONNECTED, MapBuilder.of("registrationName", ON_DISCONNECTED),
                ON_PARTICIPANT_CONNECTED, MapBuilder.of("registrationName", ON_PARTICIPANT_CONNECTED)
        );

        map.putAll(MapBuilder.of(
                ON_PARTICIPANT_DISCONNECTED, MapBuilder.of("registrationName", ON_PARTICIPANT_DISCONNECTED),
                ON_DATATRACK_MESSAGE_RECEIVED, MapBuilder.of("registrationName", ON_DATATRACK_MESSAGE_RECEIVED),
                ON_PARTICIPANT_ADDED_DATA_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_ADDED_DATA_TRACK),
                ON_PARTICIPANT_ADDED_VIDEO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_ADDED_VIDEO_TRACK),
                ON_PARTICIPANT_REMOVED_VIDEO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_REMOVED_VIDEO_TRACK),
                ON_PARTICIPANT_ADDED_AUDIO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_ADDED_AUDIO_TRACK),
                ON_PARTICIPANT_REMOVED_AUDIO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_REMOVED_AUDIO_TRACK)
        ));

        map.putAll(MapBuilder.of(
                ON_PARTICIPANT_REMOVED_DATA_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_REMOVED_DATA_TRACK),
                ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS, MapBuilder.of("registrationName", ON_LOCAL_PARTICIPANT_SUPPORTED_CODECS)
        ));

        map.putAll(MapBuilder.of(
                ON_PARTICIPANT_ENABLED_VIDEO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_ENABLED_VIDEO_TRACK),
                ON_PARTICIPANT_DISABLED_VIDEO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_DISABLED_VIDEO_TRACK),
                ON_PARTICIPANT_ENABLED_AUDIO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_ENABLED_AUDIO_TRACK),
                ON_PARTICIPANT_DISABLED_AUDIO_TRACK, MapBuilder.of("registrationName", ON_PARTICIPANT_DISABLED_AUDIO_TRACK),
                ON_STATS_RECEIVED, MapBuilder.of("registrationName", ON_STATS_RECEIVED),
                ON_NETWORK_QUALITY_LEVELS_CHANGED, MapBuilder.of("registrationName", ON_NETWORK_QUALITY_LEVELS_CHANGED),
                ON_DOMINANT_SPEAKER_CHANGED, MapBuilder.of("registrationName", ON_DOMINANT_SPEAKER_CHANGED)
        ));

        return map;
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.<String, Integer>builder()
                .put("connectToRoom", CONNECT_TO_ROOM)
                .put("disconnect", DISCONNECT)
                .put("switchCamera", SWITCH_CAMERA)
                .put("toggleVideo", TOGGLE_VIDEO)
                .put("toggleSound", TOGGLE_SOUND)
                .put("getStats", GET_STATS)
                .put("disableOpenSLES", DISABLE_OPENSL_ES)
                .put("toggleRemoteSound", TOGGLE_REMOTE_SOUND)
                .put("toggleBluetoothHeadset", TOGGLE_BLUETOOTH_HEADSET)
                .put("sendString", SEND_STRING)
                .build();
    }
}
