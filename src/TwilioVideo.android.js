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
  findNodeHandle
} from 'react-native'
import React, { Component } from 'react'
import PropTypes from 'prop-types'

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
  onStatsReceived: PropTypes.func
}

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
  releaseResource: 10
}

class CustomTwilioVideoView extends Component {
  connect ({
    roomName,
    accessToken,
    enableAudio = true,
    enableVideo = true,
    enableRemoteAudio = true
  }) {
    this.runCommand(nativeEvents.connectToRoom, [
      roomName,
      accessToken,
      enableAudio,
      enableVideo,
      enableRemoteAudio
    ])
  }

  disconnect () {
    this.runCommand(nativeEvents.disconnect, [])
  }

  componentWillUnmount () {
    this.runCommand(nativeEvents.releaseResource, [])
  }

  flipCamera () {
    this.runCommand(nativeEvents.switchCamera, [])
  }

  setLocalVideoEnabled (enabled) {
    this.runCommand(nativeEvents.toggleVideo, [enabled])
    return Promise.resolve(enabled)
  }

  setLocalAudioEnabled (enabled) {
    this.runCommand(nativeEvents.toggleSound, [enabled])
    return Promise.resolve(enabled)
  }

  setRemoteAudioEnabled (enabled) {
    this.runCommand(nativeEvents.toggleRemoteSound, [enabled])
    return Promise.resolve(enabled)
  }

  getStats () {
    this.runCommand(nativeEvents.getStats, [])
  }

  disableOpenSLES () {
    this.runCommand(nativeEvents.disableOpenSLES, [])
  }

  toggleSoundSetup (speaker) {
    this.runCommand(nativeEvents.toggleSoundSetup, [speaker])
  }

  runCommand (event, args) {
    switch (Platform.OS) {
      case 'android':
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this.refs.videoView),
          event,
          args
        )
        break
      default:
        break
    }
  }

  buildNativeEventWrappers () {
    return [
      'onCameraSwitched',
      'onVideoChanged',
      'onAudioChanged',
      'onRoomDidConnect',
      'onRoomDidFailToConnect',
      'onRoomDidDisconnect',
      'onParticipantAddedVideoTrack',
      'onParticipantRemovedVideoTrack',
      'onParticipantAddedAudioTrack',
      'onParticipantRemovedAudioTrack',
      'onRoomParticipantDidConnect',
      'onRoomParticipantDidDisconnect',
      'onParticipantEnabledVideoTrack',
      'onParticipantDisabledVideoTrack',
      'onParticipantEnabledAudioTrack',
      'onParticipantDisabledAudioTrack',
      'onStatsReceived'
    ].reduce((wrappedEvents, eventName) => {
      if (this.props[eventName]) {
        return {
          ...wrappedEvents,
          [eventName]: data => this.props[eventName](data.nativeEvent)
        }
      }
      return wrappedEvents
    }, {})
  }

  render () {
    return (
      <NativeCustomTwilioVideoView
        ref='videoView'
        {...this.props}
        {...this.buildNativeEventWrappers()}
      />
    )
  }
}

CustomTwilioVideoView.propTypes = propTypes

const NativeCustomTwilioVideoView = requireNativeComponent(
  'RNCustomTwilioVideoView',
  CustomTwilioVideoView
)

module.exports = CustomTwilioVideoView
