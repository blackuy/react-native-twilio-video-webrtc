/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 *
 * Authors:
 *   Ralph Pina <slycoder@gmail.com>
 *   Jonathan Chang <slycoder@gmail.com>
 */

import {
  requireNativeComponent,
  View,
  Platform,
  UIManager,
  NativeModules,
  findNodeHandle,
} from 'react-native';
import React, { PropTypes, Component } from 'react';

const propTypes = {
  ...View.propTypes,
  /**
   * Callback that is called when camera source changes
   */
  onCameraSwitched: PropTypes.func,

  /**
   * Callback that is called when video is toggled.
   */
  onVideoChanged: PropTypes.func,

  /**
   * Callback that is called when a audio is toggled.
   */
  onAudioChanged: PropTypes.func,

  /**
   * Callback that is called when user is connected to a room.
   */
  onRoomDidConnect: PropTypes.func,

  /**
   * Callback that is called when connecting to room fails.
   */
  onConnectFailure: PropTypes.func,

  /**
   * Callback that is called when user is disconnected from room.
   */
  onRoomDidDisconnect: PropTypes.func,

  /**
   * Called when a new video track has been added
   *
   * @param {{participant, track}}
   */
  onParticipantAddedVideoTrack: PropTypes.func,

  /**
   * Called when a video track has been removed
   *
   * @param {{participant, track}}
   */
  onParticipantRemovedVideoTrack: PropTypes.func,

  /**
   * Callback called a participant enters a room.
   */
  onRoomParticipantDidConnect: PropTypes.func,

  /**
   * Callback that is called when a participant exits a room.
   */
  onRoomParticipantDidDisconnect: PropTypes.func,
};

const nativeEvents = {
  connectToRoom: 1,
  disconnect: 2,
  switchCamera: 3,
  toggleVideo: 4,
  toggleSound: 5,
};

class CustomTwilioVideoView extends Component {
  connect({ accessToken }) {
    this.runCommand(nativeEvents.connectToRoom, [accessToken]);
  }

  disconnect() {
    this.runCommand(nativeEvents.disconnect, []);
  }

  flipCamera() {
    this.runCommand(nativeEvents.switchCamera, []);
  }

  setLocalVideoEnabled(enabled) {
    this.runCommand(nativeEvents.toggleVideo, [enabled]);
  }

  setLocalAudioEnabled(enabled) {
    this.runCommand(nativeEvents.toggleSound, [enabled]);
  }

  runCommand(event, args) {
    switch (Platform.OS) {
      case 'android':
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this),
          event,
          args
        );
        break;
      default:
        break;
    }
  }

  render() {
    return (
      <NativeCustomTwilioVideoView
        onConnected={(event) => {
          this.props.onRoomDidConnect && this.props.onRoomDidConnect(event.nativeEvent);
        }}
        {...this.props}
      />
    );
  }
}

CustomTwilioVideoView.propTypes = propTypes;

const NativeCustomTwilioVideoView = requireNativeComponent('RNCustomTwilioVideoView', CustomTwilioVideoView);

module.exports = CustomTwilioVideoView;
