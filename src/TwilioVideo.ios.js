//
//  TwilioVideo.js
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

import { Component } from 'react'
import PropTypes from 'prop-types'
import { NativeModules, NativeEventEmitter, View } from 'react-native'

const { TWVideoModule } = NativeModules

export default class extends Component {
  static propTypes = {
    /**
     * Flag that enables screen sharing RCTRootView instead of camera capture
     */
    screenShare: PropTypes.bool,
    /**
     * Called when the room has connected
     *
     * @param {{roomName, participants}}
     */
    onRoomDidConnect: PropTypes.func,
    /**
     * Called when the room has disconnected
     *
     * @param {{roomName, error}}
     */
    onRoomDidDisconnect: PropTypes.func,
    /**
     * Called when connection with room failed
     *
     * @param {{roomName, error}}
     */
    onRoomDidFailToConnect: PropTypes.func,
    /**
     * Called when a new participant has connected
     *
     * @param {{roomName, participant}}
     */
    onRoomParticipantDidConnect: PropTypes.func,
    /**
     * Called when a participant has disconnected
     *
     * @param {{roomName, participant}}
     */
    onRoomParticipantDidDisconnect: PropTypes.func,
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
     * Called when the camera has started
     *
     */
    onCameraDidStart: PropTypes.func,
    /**
     * Called when the camera has been interrupted
     *
     */
    onCameraWasInterrupted: PropTypes.func,
    /**
     * Called when the camera has stopped runing with an error
     *
     * @param {{error}} The error message description
     */
    onCameraDidStopRunning: PropTypes.func,
    /**
     * Called when stats are received (after calling getStats)
     *
     */
    onStatsReceived: PropTypes.func,
    ...View.propTypes
  }

  constructor (props) {
    super(props)

    this._subscriptions = []
    this._eventEmitter = new NativeEventEmitter(TWVideoModule)

    this.setLocalVideoEnabled = this.setLocalVideoEnabled.bind(this)
    this.setLocalAudioEnabled = this.setLocalAudioEnabled.bind(this)
    this.flipCamera = this.flipCamera.bind(this)
    this.connect = this.connect.bind(this)
    this.disconnect = this.disconnect.bind(this)
    this.setRemoteAudioPlayback = this.setRemoteAudioPlayback.bind(this)
  }

  componentWillMount () {
    this._registerEvents()
    this._startLocalVideo()
    this._startLocalAudio()
  }

  componentWillUnmount () {
    this._unregisterEvents()
    this._stopLocalVideo()
    this._stopLocalAudio()
  }

  /**
   * Locally mute/ unmute all remote audio tracks from a given participant
   */
  setRemoteAudioPlayback ({ participantSid, enabled }) {
    TWVideoModule.setRemoteAudioPlayback(participantSid, enabled)
  }

  setRemoteAudioEnabled (enabled) {
    return Promise.resolve(enabled)
  }

  setBluetoothHeadsetConnected (enabled) {
    return Promise.resolve(enabled)
  }

  /**
   * Enable or disable local video
   */
  setLocalVideoEnabled (enabled) {
    return TWVideoModule.setLocalVideoEnabled(enabled)
  }

  /**
   * Enable or disable local audio
   */
  setLocalAudioEnabled (enabled) {
    return TWVideoModule.setLocalAudioEnabled(enabled)
  }

  /**
   * Filp between the front and back camera
   */
  flipCamera () {
    TWVideoModule.flipCamera()
  }

  /**
   * Toggle audio setup from speaker (default) and headset
   */
  toggleSoundSetup (speaker) {
    TWVideoModule.toggleSoundSetup(speaker)
  }

  /**
   * Get connection stats
   */
  getStats () {
    TWVideoModule.getStats()
  }

  /**
   * Connect to given room name using the JWT access token
   * @param  {String} roomName    The connecting room name
   * @param  {String} accessToken The Twilio's JWT access token
   */
  connect ({ roomName, accessToken }) {
    TWVideoModule.connect(accessToken, roomName)
  }

  /**
   * Disconnect from current room
   */
  disconnect () {
    TWVideoModule.disconnect()
  }

  _startLocalVideo () {
    const screenShare = this.props.screenShare || false
    TWVideoModule.startLocalVideo(screenShare)
  }

  _stopLocalVideo () {
    TWVideoModule.stopLocalVideo()
  }

  _startLocalAudio () {
    TWVideoModule.startLocalAudio()
  }

  _stopLocalAudio () {
    TWVideoModule.stopLocalAudio()
  }

  _unregisterEvents () {
    TWVideoModule.changeListenerStatus(false)
    this._subscriptions.forEach(e => e.remove())
    this._subscriptions = []
  }

  _registerEvents () {
    TWVideoModule.changeListenerStatus(true)
    this._subscriptions = [
      this._eventEmitter.addListener('roomDidConnect', data => {
        if (this.props.onRoomDidConnect) {
          this.props.onRoomDidConnect(data)
        }
      }),
      this._eventEmitter.addListener('roomDidDisconnect', data => {
        if (this.props.onRoomDidDisconnect) {
          this.props.onRoomDidDisconnect(data)
        }
      }),
      this._eventEmitter.addListener('roomDidFailToConnect', data => {
        if (this.props.onRoomDidFailToConnect) {
          this.props.onRoomDidFailToConnect(data)
        }
      }),
      this._eventEmitter.addListener('roomParticipantDidConnect', data => {
        if (this.props.onRoomParticipantDidConnect) {
          this.props.onRoomParticipantDidConnect(data)
        }
      }),
      this._eventEmitter.addListener('roomParticipantDidDisconnect', data => {
        if (this.props.onRoomParticipantDidDisconnect) {
          this.props.onRoomParticipantDidDisconnect(data)
        }
      }),
      this._eventEmitter.addListener('participantAddedVideoTrack', data => {
        if (this.props.onParticipantAddedVideoTrack) {
          this.props.onParticipantAddedVideoTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantRemovedVideoTrack', data => {
        if (this.props.onParticipantRemovedVideoTrack) {
          this.props.onParticipantRemovedVideoTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantAddedAudioTrack', data => {
        if (this.props.onParticipantAddedAudioTrack) {
          this.props.onParticipantAddedAudioTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantRemovedAudioTrack', data => {
        if (this.props.onParticipantRemovedAudioTrack) {
          this.props.onParticipantRemovedAudioTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantEnabledVideoTrack', data => {
        if (this.props.onParticipantEnabledVideoTrack) {
          this.props.onParticipantEnabledVideoTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantDisabledVideoTrack', data => {
        if (this.props.onParticipantDisabledVideoTrack) {
          this.props.onParticipantDisabledVideoTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantEnabledAudioTrack', data => {
        if (this.props.onParticipantEnabledAudioTrack) {
          this.props.onParticipantEnabledAudioTrack(data)
        }
      }),
      this._eventEmitter.addListener('participantDisabledAudioTrack', data => {
        if (this.props.onParticipantDisabledAudioTrack) {
          this.props.onParticipantDisabledAudioTrack(data)
        }
      }),
      this._eventEmitter.addListener('cameraDidStart', data => {
        if (this.props.onCameraDidStart) {
          this.props.onCameraDidStart(data)
        }
      }),
      this._eventEmitter.addListener('cameraWasInterrupted', data => {
        if (this.props.onCameraWasInterrupted) {
          this.props.onCameraWasInterrupted(data)
        }
      }),
      this._eventEmitter.addListener('cameraDidStopRunning', data => {
        if (this.props.onCameraDidStopRunning) {
          this.props.onCameraDidStopRunning(data)
        }
      }),
      this._eventEmitter.addListener('statsReceived', data => {
        if (this.props.onStatsReceived) {
          this.props.onStatsReceived(data)
        }
      })
    ]
  }

  render () {
    return this.props.children || null
  }
}
