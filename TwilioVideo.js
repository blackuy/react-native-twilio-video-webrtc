import React, { Component, PropTypes } from 'react'
import {
  NativeModules,
  NativeEventEmitter,
  View,
} from 'react-native';

const {TWVideoModule} = NativeModules


export default class TwilioVideo extends Component {

  state = {

  }

  _subscriptions = []

  constructor(props){
    super(props)

    this.flipCamera = this.flipCamera.bind(this)
    this.startCall = this.startCall.bind(this)
    this.endCall = this.endCall.bind(this)

    this._eventEmitter = new NativeEventEmitter(TWVideoModule)
  }


  //
  // Methods

  flipCamera(){
    TWVideoModule.flipCamera()
  }

  detachVideoTrack() {
    TWVideoModule.detachVideoTrack()
  }

  detachAudioTrack() {
    TWVideoModule.detachAudioTrack()
  }

  attachVideoTrack() {
    TWVideoModule.attachVideoTrack()
  }

  attachAudioTrack() {
    TWVideoModule.attachAudioTrack()
  }

  startCall({roomName, accessToken}) {
    TWVideoModule.startCallWithAccessToken(accessToken,roomName)
  }

  endCall(){
    TWVideoModule.disconnect()
  }


  _unregisterEvents() {
    this._subscriptions.forEach(e => e.remove())
    this._subscriptions = []
  }

  _registerEvents() {

    this._subscriptions = [

      this._eventEmitter.addListener('roomDidConnect', (data) => {
        if(this.props.onRoomDidConnect){ this.props.onRoomDidConnect(data) }
      }),

      this._eventEmitter.addListener('roomDidDisconnect', (data) => {
        if(this.props.onRoomDidDisconnect){ this.props.onRoomDidDisconnect(data) }
      }),

      this._eventEmitter.addListener('roomDidFailToConnect', (data) => {
        if(this.props.onRoomDidFailToConnect){ this.props.onRoomDidFailToConnect(data) }
      }),

      this._eventEmitter.addListener('roomParticipantDidConnect', (data) => {
        if(this.props.onRoomParticipantDidConnect){ this.props.onRoomParticipantDidConnect(data) }
      }),

      this._eventEmitter.addListener('roomParticipantDidDisconnect', (data) => {
        if(this.props.onRoomParticipantDidDisconnect){ this.props.onRoomParticipantDidDisconnect(data) }
      }),

      this._eventEmitter.addListener('participantAddedVideoTrack', (data) => {
        if(this.props.onParticipantAddedVideoTrack){ this.props.onParticipantAddedVideoTrack(data) }
      }),

      this._eventEmitter.addListener('participantRemovedVideoTrack', (data) => {
        if(this.props.onParticipantRemovedVideoTrack){ this.props.onParticipantRemovedVideoTrack(data) }
      }),

      this._eventEmitter.addListener('participantAddedAudioTrack', (data) => {
        if(this.props.onParticipantAddedAudioTrack){ this.props.onParticipantAddedAudioTrack(data) }
      }),

      this._eventEmitter.addListener('participantRemovedAudioTrack', (data) => {
        if(this.props.onParticipantRemovedAudioTrack){ this.props.onParticipantRemovedAudioTrack(data) }
      }),

      this._eventEmitter.addListener('participantEnabledTrack', (data) => {
        if(this.props.onParticipantEnabledTrack){ this.props.onParticipantEnabledTrack(data) }
      }),

      this._eventEmitter.addListener('participantDisabledTrack', (data) => {
        if(this.props.onParticipantDisabledTrack){ this.props.onParticipantDisabledTrack(data) }
      }),

      this._eventEmitter.addListener('cameraDidStart', (data) => {
        if(this.props.onCameraDidStart){ this.props.onCameraDidStart(data) }
      }),

      this._eventEmitter.addListener('cameraWasInterrumpted', (data) => {
        if(this.props.onCameraWasInterrumpted){ this.props.onCameraWasInterrumpted(data) }
      }),

      this._eventEmitter.addListener('cameraDidStopRunning', (data) => {
        if(this.props.onCameraDidStopRunning){ this.props.onCameraDidStopRunning(data) }
      })

    ]

  }

  componentWillMount() {
    this._registerEvents()
  }

  componentWillUnmount() {
    this._unregisterEvents()
  }


  render() {
    return this.props.children || null
  }
}


TwilioVideo.propTypes = {
    onRoomDidConnect: PropTypes.func,
    onRoomDidDisconnect: PropTypes.func,
    onRoomDidFailToConnect: PropTypes.func,
    onRoomParticipantDidConnect: PropTypes.func,
    onRoomParticipantDidDisconnect: PropTypes.func,
    onParticipantAddedVideoTrack: PropTypes.func,
    onParticipantRemovedVideoTrack: PropTypes.func,
    onParticipantAddedAudioTrack: PropTypes.func,
    onParticipantRemovedAudioTrack: PropTypes.func,
    onParticipantEnabledTrack: PropTypes.func,
    onParticipantDisabledTrack: PropTypes.func,
    onCameraDidStart: PropTypes.func,
    onCameraWasInterrumpted: PropTypes.func,
    onCameraDidStopRunning: PropTypes.func,
    ...View.propTypes,
}

// TwilioVideo.defaultProps = {
// }
