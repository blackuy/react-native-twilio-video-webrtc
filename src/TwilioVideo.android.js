/**
 * Component to orchestrate the Twilio Video connection and the various video
 * views.
 *
 * Authors:
 *   Ralph Pina <slycoder@gmail.com>
 *   Jonathan Chang <slycoder@gmail.com>
 */

import {
  Platform,
  UIManager,
  View,
  findNodeHandle,
  requireNativeComponent,
} from "react-native";
import React, { Component } from "react";

import PropTypes from "prop-types";

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
  onRoomDidFailToConnect: PropTypes.func,

  /**
   * Callback that is called when user is disconnected from room.
   */
  onRoomDidDisconnect: PropTypes.func,

  /**
   * Called when a new data track has been added
   *
   * @param {{participant, track}}
   */
  onParticipantAddedDataTrack: PropTypes.func,

  /**
   * Called when a data track has been removed
   *
   * @param {{participant, track}}
   */
  onParticipantRemovedDataTrack: PropTypes.func,

  /**
   * Called when an dataTrack receives a message
   *
   * @param {{message}}
   */
  onDataTrackMessageReceived: PropTypes.func,

  /**
   * Called when a new video track has been added
   *
   * @param {{participant, track, enabled}}
   */
  onParticipantAddedVideoTrack: PropTypes.func,

  /**
   * Called when a video track has been removed
   *
   * @param {{participant, track}}
   */
  onParticipantRemovedVideoTrack: PropTypes.func,

  /**
   * Called when a new audio track has been added
   *
   * @param {{participant, track}}
   */
  onParticipantAddedAudioTrack: PropTypes.func,

  /**
   * Called when a audio track has been removed
   *
   * @param {{participant, track}}
   */
  onParticipantRemovedAudioTrack: PropTypes.func,

  /**
   * Callback called a participant enters a room.
   */
  onRoomParticipantDidConnect: PropTypes.func,

  /**
   * Callback that is called when a participant exits a room.
   */
  onRoomParticipantDidDisconnect: PropTypes.func,
  /**
   * Called when a video track has been enabled.
   *
   * @param {{participant, track}}
   */
  onParticipantEnabledVideoTrack: PropTypes.func,
  /**
   * Called when a video track has been disabled.
   *
   * @param {{participant, track}}
   */
  onParticipantDisabledVideoTrack: PropTypes.func,
  /**
   * Called when an audio track has been enabled.
   *
   * @param {{participant, track}}
   */
  onParticipantEnabledAudioTrack: PropTypes.func,
  /**
   * Called when an audio track has been disabled.
   *
   * @param {{participant, track}}
   */
  onParticipantDisabledAudioTrack: PropTypes.func,
  /**
   * Callback that is called when stats are received (after calling getStats)
   */
  onStatsReceived: PropTypes.func,
  /**
   * Callback that is called when network quality levels are changed (only if enableNetworkQualityReporting in connect is set to true)
   */
  onNetworkQualityLevelsChanged: PropTypes.func,
  /**
   * Called when dominant speaker changes
   * @param {{ participant, room }} dominant participant and room
   */
  onDominantSpeakerDidChange: PropTypes.func,
  /**
   * Callback that is called after determining what codecs are supported
   */
  onLocalParticipantSupportedCodecs: PropTypes.func,
  // New events for multiple track support
  onAudioTrackCreated: PropTypes.func,
  onVideoTrackCreated: PropTypes.func,
  onTrackCreationError: PropTypes.func,
  onTrackPublishError: PropTypes.func,
  onTrackUnpublishError: PropTypes.func,
  onLocalTracksReceived: PropTypes.func,
};

const nativeEvents = {
  connectToRoom: 1,
  disconnect: 2,
  switchCamera: 3,
  toggleVideo: 4,
  toggleSound: 5,
  getStats: 6,
  disableOpenSLES: 7,
  toggleSoundSetup: 8,
  toggleRemoteSound: 9,
  releaseResource: 10,
  toggleBluetoothHeadset: 11,
  sendString: 12,
  setRemoteAudioPlayback: 13,
  // Multiple track support commands
  createLocalAudioTrack: 14,
  createLocalVideoTrack: 15,
  publishLocalAudioTrack: 16,
  publishLocalVideoTrack: 17,
  unpublishLocalAudioTrack: 18,
  unpublishLocalVideoTrack: 19,
  destroyLocalTrack: 20,
  enableLocalTrack: 21,
  getLocalTracks: 22,
};

class CustomTwilioVideoView extends Component {
  connect({
    roomName,
    accessToken,
    cameraType = "front",
    enableAudio = true,
    enableVideo = true,
    enableRemoteAudio = true,
    enableNetworkQualityReporting = false,
    dominantSpeakerEnabled = false,
    maintainVideoTrackInBackground = false,
    encodingParameters = {},
  }) {
    this.runCommand(nativeEvents.connectToRoom, [
      roomName,
      accessToken,
      enableAudio,
      enableVideo,
      enableRemoteAudio,
      enableNetworkQualityReporting,
      dominantSpeakerEnabled,
      maintainVideoTrackInBackground,
      cameraType,
      encodingParameters,
    ]);
  }

  sendString(message) {
    this.runCommand(nativeEvents.sendString, [message]);
  }

  disconnect() {
    this.runCommand(nativeEvents.disconnect, []);
  }

  componentWillUnmount() {
    this.runCommand(nativeEvents.releaseResource, []);
  }

  flipCamera() {
    this.runCommand(nativeEvents.switchCamera, []);
  }

  setRemoteAudioEnabled(enabled) {
    this.runCommand(nativeEvents.toggleRemoteSound, [enabled]);
    return Promise.resolve(enabled);
  }

  setBluetoothHeadsetConnected(enabled) {
    this.runCommand(nativeEvents.toggleBluetoothHeadset, [enabled]);
    return Promise.resolve(enabled);
  }

  setRemoteAudioPlayback({ participantSid, enabled }) {
    this.runCommand(nativeEvents.setRemoteAudioPlayback, [
      participantSid,
      enabled,
    ]);
  }

  getStats() {
    this.runCommand(nativeEvents.getStats, []);
  }

  disableOpenSLES() {
    this.runCommand(nativeEvents.disableOpenSLES, []);
  }

  toggleSoundSetup(speaker) {
    this.runCommand(nativeEvents.toggleSoundSetup, [speaker]);
  }

  runCommand(event, args) {
    switch (Platform.OS) {
      case "android":
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this.refs.videoView),
          event,
          args
        );
        break;
      default:
        break;
    }
  }

  // Multiple track support methods
  createLocalAudioTrack(config) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.createLocalAudioTrack, [
        config.trackName,
        config.enabled !== undefined ? config.enabled : true
      ]);
      // TODO: Add proper promise handling with event listeners
      resolve(config.trackName);
    });
  }

  createLocalVideoTrack(config) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.createLocalVideoTrack, [
        config.trackName,
        config.enabled !== undefined ? config.enabled : true,
        config.cameraType || "front"
      ]);
      // TODO: Add proper promise handling with event listeners
      resolve(config.trackName);
    });
  }

  publishLocalAudioTrack(trackName) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.publishLocalAudioTrack, [trackName]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  publishLocalVideoTrack(trackName) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.publishLocalVideoTrack, [trackName]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  unpublishLocalAudioTrack(trackName) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.unpublishLocalAudioTrack, [trackName]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  unpublishLocalVideoTrack(trackName) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.unpublishLocalVideoTrack, [trackName]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  destroyLocalTrack(trackName) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.destroyLocalTrack, [trackName]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  enableLocalTrack(trackName, enabled) {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.enableLocalTrack, [trackName, enabled]);
      // TODO: Add proper promise handling with event listeners
      resolve(true);
    });
  }

  getLocalTracks() {
    return new Promise((resolve, reject) => {
      this.runCommand(nativeEvents.getLocalTracks, []);
      // TODO: Add proper promise handling with event listeners - should resolve with tracks array
      resolve([]);
    });
  }

  buildNativeEventWrappers() {
    return [
      "onCameraSwitched",
      "onVideoChanged",
      "onAudioChanged",
      "onRoomDidConnect",
      "onRoomDidFailToConnect",
      "onRoomDidDisconnect",
      "onParticipantAddedDataTrack",
      "onParticipantRemovedDataTrack",
      "onDataTrackMessageReceived",
      "onParticipantAddedVideoTrack",
      "onParticipantRemovedVideoTrack",
      "onParticipantAddedAudioTrack",
      "onParticipantRemovedAudioTrack",
      "onRoomParticipantDidConnect",
      "onRoomParticipantDidDisconnect",
      "onParticipantEnabledVideoTrack",
      "onParticipantDisabledVideoTrack",
      "onParticipantEnabledAudioTrack",
      "onParticipantDisabledAudioTrack",
      "onStatsReceived",
      "onNetworkQualityLevelsChanged",
      "onDominantSpeakerDidChange",
      "onLocalParticipantSupportedCodecs",
      // New events for multiple track support
      "onAudioTrackCreated",
      "onVideoTrackCreated",
      "onTrackCreationError",
      "onTrackPublishError",
      "onTrackUnpublishError",
      "onLocalTracksReceived",
    ].reduce((wrappedEvents, eventName) => {
      if (this.props[eventName]) {
        return {
          ...wrappedEvents,
          [eventName]: (data) => this.props[eventName](data.nativeEvent),
        };
      }
      return wrappedEvents;
    }, {});
  }

  render() {
    return (
      <NativeCustomTwilioVideoView
        ref="videoView"
        {...this.props}
        {...this.buildNativeEventWrappers()}
      />
    );
  }
}

CustomTwilioVideoView.propTypes = propTypes;

const NativeCustomTwilioVideoView = requireNativeComponent(
  "RNCustomTwilioVideoView",
  CustomTwilioVideoView
);

module.exports = CustomTwilioVideoView;
