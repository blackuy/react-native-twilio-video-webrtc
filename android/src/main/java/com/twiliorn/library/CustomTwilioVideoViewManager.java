/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 *
 * Authors:
 *   Ralph Pina <ralph.pina@gmail.com>
 *   Jonathan Chang <slycoder@gmail.com>
 */
package com.twiliorn.library;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;
import android.util.Log;

import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_AUDIO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CAMERA_SWITCHED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_CONNECT_FAILURE;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_DICONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_CONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_DISCONNECTED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_VIDEO_CHANGED;
import static com.twiliorn.library.CustomTwilioVideoView.Events.ON_PARTICIPANT_ADDED_VIDEO;

public class CustomTwilioVideoViewManager extends SimpleViewManager<CustomTwilioVideoView> {

    public static final String REACT_CLASS = "RNCustomTwilioVideoView";

    private static final int CONNECT_TO_ROOM = 7;
    private static final int DISCONNECT = 8;
    private static final int SWITCH_CAMERA = 9;
    private static final int TOGGLE_VIDEO = 10;
    private static final int TOGGLE_SOUND = 11;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CustomTwilioVideoView createViewInstance(ThemedReactContext reactContext) {
      Log.d("VideoActivity", "CREATE Local Media");
        return new CustomTwilioVideoView(reactContext);
    }

    @Override
    public void receiveCommand(CustomTwilioVideoView view, int commandId, @Nullable ReadableArray args) {
      Log.d("VideoActivity", "CREATE Local Media");
        switch (commandId) {
            case CONNECT_TO_ROOM:

                String accessToken = args.getString(0);
                view.connectToRoomWrapper(accessToken);
                break;
            case DISCONNECT:
                view.disconnect();
                break;

            case SWITCH_CAMERA:
                view.switchCamera();
                break;

            case TOGGLE_VIDEO:
                view.toggleVideo();
                break;
            case TOGGLE_SOUND:
                view.toggleAudio();
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
                ON_DICONNECTED, MapBuilder.of("registrationName", ON_DICONNECTED),
                ON_PARTICIPANT_CONNECTED, MapBuilder.of("registrationName", ON_PARTICIPANT_CONNECTED)
        );

        map.putAll(MapBuilder.of(
                ON_PARTICIPANT_DISCONNECTED, MapBuilder.of("registrationName", ON_PARTICIPANT_DISCONNECTED),
                ON_PARTICIPANT_ADDED_VIDEO, MapBuilder.of("registrationName", ON_PARTICIPANT_ADDED_VIDEO)
        ));

        return map;
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "connectToRoom", CONNECT_TO_ROOM,
                "disconnect", DISCONNECT,
                "switchCamera", SWITCH_CAMERA,
                "toggleVideo", TOGGLE_VIDEO,
                "toggleSound", TOGGLE_SOUND
        );
    }
}
