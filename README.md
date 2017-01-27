# react-native-twilio-video-webrtc
Twilio Video (WebRTC) iOS Wrapper for React Native

# Installation
Go to your project directory and run these in your terminal:

```
yarn add https://github.com/eduedix/react-native-twilio-video-webrtc
react-native link react-native-twilio-video-webrtc
```

Open project in Xcode. Click Project in the left pane and go to the tab `General`. Then add Embedded Framework. Click `Add another` and go to `node_modules/react-native-twilio-video-webrtc`. Select TwilioVideo.framework. Select `Copy items if needed`.

# Usage

Note: This is a illustrative example. Not intended to be workable.

````javascript

import React, { Component } from 'react'
import {
  View,
} from 'react-native'

import {
  TwilioRemotePreview, 
  TwilioVideoPreview, 
  TwilioVideo
} from 'react-native-twilio-video-webrtc'

import Button from 'react-native-button'

class App extends Component {

  state = {
  }

  startCall = ({roomName, accessToken}) => {
    this.refs.twilioVideo.startCall({roomName, accessToken})
  }

  endCall = () => {
    this.refs.twilioVideo.endCall()
  }

  _onConnectButtonPress = () => {
    this.startCall({roomName: "ROOM_NAME_HERE", accessToken: "TWILIO_ACCESS_TOKEN_HERE"})
    // Note: You get TWILIO_ACCESS_TOKEN_HERE from your server probably
  }

  _onDisconnectButtonPress = () => {
    this.endCall()
  }


  render() {
    return (
      <View style={{ flex: 1 }}>

        // This is the Component who manages the call. Renders nothing or children if passed.
        // We use 'ref' to call methods
        <TwilioVideo
          ref="twilioVideo" 
          onRoomDidConnect={(data)=> {
            // ... participants = data["participantsNames"]
          }} 
          onRoomParticipantDidConnect={(data)=> {
            //... i.e. participant = data["participantName"]
          }} 
          onRoomParticipantDidDisconnec={(data) => {
            //...
          }}
          onRoomDidDisconnect={(data) => {
            //...
          }}
          onParticipantAddedVideoTrack={(data) => {
            //...
          }}
        />

        // This is your own Twilio camera preview feed
        <TwilioVideoPreview style={{flex: 1, backgroundColor: '#FFFF00'}} />

        // This is other participant camera video feed
        <TwilioRemotePreview style={{flex: 1, backgroundColor: '#FF00FF'}} />

        <Button
          style={{fontSize: 20, padding: 10, color: 'green'}}
          onPress={this._onConnectButtonPress}>
          Connect
        </Button>

        <Button
          style={{fontSize: 20, padding: 10, color: 'red'}}
          onPress={this._onDisconnectButtonPress}>
          Disconnect
        </Button>

      </View>
    );
  }
}


AppRegistry.registerComponent('app', () => App);
````
