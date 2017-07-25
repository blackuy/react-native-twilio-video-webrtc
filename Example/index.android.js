import React, { Component } from 'react'
import {
  View,
  Button,
  StyleSheet,
  TouchableWithoutFeedback,
  Text,
  Alert
} from 'react-native'

import {
  TwilioVideoParticipantView,
  TwilioVideoLocalView,
  TwilioVideo
} from 'react-native-twilio-video-webrtc'

export default class VideoTry extends Component {
  _onVideoConnectButtonPress = () => {
    this.refs.twilioVideo.connect({roomName: "myroom_1", accessToken: "access token string"});
  }

  _onVideoDisconnectButtonPress = () => {
    this.refs.twilioVideo.disconnect();
  }

  _onVideoConnectFailure = (response) => {
      Alert.alert('', 'Connect failure: ' + JSON.stringify(response));
  };

  render() {
    return (
        <View style={styles.container}>
            <TwilioVideo ref="twilioVideo" onRoomDidFailToConnect={this._onVideoConnectFailure} />

            <View style={styles.videoViewParent}>
                <TwilioVideoLocalView style={styles.videoViewContainer} />
                <TwilioVideoParticipantView style={styles.videoViewContainer} />
            </View>
            <View style={styles.videoControls}>
                <TouchableWithoutFeedback onPress={this._onVideoConnectButtonPress}>
                    <View style={styles.videoControlBtn}>
                        <Text style={styles.videoControlBtnText}>CONNECT</Text>
                    </View>
                </TouchableWithoutFeedback>
                <TouchableWithoutFeedback onPress={this._onVideoDisconnectButtonPress}>
                    <View style={styles.videoControlBtn}>
                        <Text style={styles.videoControlBtnText}>DISCONNECT</Text>
                    </View>
                </TouchableWithoutFeedback>
            </View>
        </View>
    );
  }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#FFFFFF'
    },
    videoViewParent: {
        width: '100%',
        height: 150,
        flexDirection: 'row',
        justifyContent: 'center',
        backgroundColor: '#FFFFFF'
    },
    videoViewContainer: {
        flex: 0.5,
        backgroundColor: '#FFFFFF'
    },
    videoControls: {
        height: 50,
        width: '100%',
        flexDirection: 'row',
        alignItems: 'center'
    },
    videoControlBtn: {
        flex: 0.5,
        backgroundColor: '#FF0000'
    },
    videoControlBtnText: {
        fontSize: 20,
        padding: 10,
        color: '#FFFFFF',
        textAlign: 'center'
    }
});
