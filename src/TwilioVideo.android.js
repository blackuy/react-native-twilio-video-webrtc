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
  NativeAppEventEmitter
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
   * Callback called a participant enters a room.
   */
  onRoomParticipantDidConnect: PropTypes.func,

  /**
   * Callback that is called when a participant exits a room.
   */
  onRoomParticipantDidDisconnect: PropTypes.func,
};

const nativeEvents = {
  connectToRoom: 7,
  disconnect: 8,
  switchCamera: 9,
  toggleVideo: 10,
  toggleSound: 11,
};

class CustomTwilioVideoView extends Component {
  constructor(props) {
    super(props)

    this._subscriptions = [];
    this._eventEmitter = NativeAppEventEmitter;

    this.setLocalVideoEnabled = this.toggleVideo.bind(this)
    this.setLocalAudioEnabled = this.toggleSound.bind(this)
    this.connect = this.startCall.bind(this)
    this.disconnect = this.endCall.bind(this)
    this.switchCamera = this.switchCamera.bind(this);
  }

  componentWillMount() {
    this._registerEvents();
  }

  startCall({ accessToken }) {
    this.runCommand(nativeEvents.connectToRoom, [accessToken]);
  }

  endCall() {
    this.runCommand(nativeEvents.disconnect, []);
  }

  switchCamera() {
    this.runCommand(nativeEvents.switchCamera, []);
  }

  toggleVideo() {
    this.runCommand(nativeEvents.toggleVideo, []);
  }

  toggleSound() {
    this.runCommand(nativeEvents.toggleSound, []);
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

  _registerEvents() {
      this._eventEmitter.addListener('onRoomDidConnect', (data) => {
        if(this.props.onRoomDidConnect){ this.props.onRoomDidConnect(data)  }
      });
      this._eventEmitter.addListener('onConnectFailure', (data) => {
        if(this.props.onRoomDidFailToConnect){ this.props.onRoomDidFailToConnect(data) }
      });
      this._eventEmitter.addListener('onRoomDidDisconnect', (data) => {
        if(this.props.onRoomDidDisconnect){ this.props.onRoomDidDisconnect(data) }
      });
      this._eventEmitter.addListener('onRoomDidFailToConnect', (data) => {
        if(this.props.onRoomDidFailToConnect){ this.props.onRoomDidFailToConnect(data) }
      });
      this._eventEmitter.addListener('onRoomParticipantDidConnect', (data) => {
        if(this.props.onRoomParticipantDidConnect){ this.props.onRoomParticipantDidConnect(data) }
      });
      this._eventEmitter.addListener('onRoomParticipantDidDisconnect', (data) => {
        if(this.props.onRoomParticipantDidDisconnect){ this.props.onRoomParticipantDidDisconnect(data) }
      });
      this._eventEmitter.addListener('onParticipantAddedVideoTrack', (data) => {
        if(this.props.onParticipantAddedVideoTrack){ this.props.onParticipantAddedVideoTrack(data) }
      });
      this._eventEmitter.addListener('onParticipantRemovedVideoTrack', (data) => {
        if(this.props.onParticipantRemovedVideoTrack){ this.props.onParticipantRemovedVideoTrack(data) }
      });
      this._eventEmitter.addListener('onParticipantAddedAudioTrack', (data) => {
        if(this.props.onParticipantAddedAudioTrack){ this.props.onParticipantAddedAudioTrack(data) }
      });
      this._eventEmitter.addListener('onParticipantRemovedAudioTrack', (data) => {
        if(this.props.onParticipantRemovedAudioTrack){ this.props.onParticipantRemovedAudioTrack(data) }
      });
      this._eventEmitter.addListener('participantEnabledTrack', (data) => {
        if(this.props.onParticipantEnabledTrack){ this.props.onParticipantEnabledTrack(data) }
      });
      this._eventEmitter.addListener('participantDisabledTrack', (data) => {
        if(this.props.onParticipantDisabledTrack){ this.props.onParticipantDisabledTrack(data) }
      });
      this._eventEmitter.addListener('onCameraDidStart', (data) => {
        if(this.props.onCameraDidStart){ this.props.onCameraDidStart(data) }
      });
      this._eventEmitter.addListener('onCameraWasInterrupted', (data) => {
        if(this.props.onCameraWasInterrupted){ this.props.onCameraWasInterrupted(data) }
      });
      this._eventEmitter.addListener('onCameraDidStopRunning', (data) => {
        if(this.props.onCameraDidStopRunning){ this.props.onCameraDidStopRunning(data) }
      });

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
