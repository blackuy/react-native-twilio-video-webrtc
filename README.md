![GitHub Logo](/logo.png)

# Twilio Video (WebRTC) for React Native

Platforms:
- iOS
- Android

People using a version < 1.0.1 please move to 1.0.1 since the project changed a lot internally to support the stable TwilioVideo version.

## Installation

- react-native >= 0.40.0: install react-native-twilio-video-webrtc@1.0.1
- react-native < 0.40.0: install react-native-twilio-video-webrtc@1.0.0

### Install Node Package

#### Option A: yarn

```shell
yarn add https://github.com/blackuy/react-native-twilio-video-webrtc
```

#### Option B: npm

```shell
npm install https://github.com/blackuy/react-native-twilio-video-webrtc --save
```

### iOS

#### Option A: Install with CocoaPods (recommended)

1. Add this package to your Podfile

```ruby
pod 'react-native-twilio-video-webrtc', path: '../node_modules/react-native-twilio-video-webrtc'
```

Note that this will automatically pull in the appropriate version of the underlying `TwilioVideo` pod.

2. Install Pods with

```shell
pod install
```

#### Option B: Install without CocoaPods (manual approach)

1. Add the Twilio dependency to your Podfile

```ruby
pod 'TwilioVideo'
```

2. Install Pods with

```shell
pod install
```

3. Add the XCode project to your own XCode project's `Libraries` directory from

```
node_modules/react-native-twilio-video-webrtc/ios/RNTwilioVideoWebRTC.xcodeproj
```

4. Add `libRNTwilioVideoWebRTC.a` to your XCode project target's `Linked Frameworks and Libraries`

5. Update `Build Settings`

Find `Search Paths` and add `$(SRCROOT)/../node_modules/react-native-twilio-video-webrtc/ios` with `recursive` to `Framework Search Paths` and `Library Search Paths`

#### Permissions

To enable camera usage and microphone usage you will need to add the following entries to your `Info.plist` file:

```
<key>NSCameraUsageDescription</key>
<string>Your message to user when the camera is accessed for the first time</string>
<key>NSMicrophoneUsageDescription</key>
<string>Your message to user when the microphone is accessed for the first time</string>
```

#### Known Issues

TwilioVideo version 1.3.8 (latest) has the following know issues.

- Participant disconnect event can take up to 120 seconds to occur. [Issue 99](https://github.com/twilio/video-quickstart-swift/issues/99)
- AVPlayer audio content does not mix properly with Room audio. [Issue 62](https://github.com/twilio/video-quickstart-objc/issues/62)

### Android

As with iOS, make sure the package is installed:

```shell
yarn add https://github.com/blackuy/react-native-twilio-video-webrtc
```

Then add the library to your `settings.gradle` file:

```
include ':react-native-twilio-video-webrtc'
project(':react-native-twilio-video-webrtc').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-twilio-video-webrtc/android')
```

And include the library in your dependencies in `android/app/build.gradle`:

```
dependencies {
    .....
    .....
    .....
    compile project(':react-native-twilio-video-webrtc')
}
```

You will also need to update this file so that you compile with java 8 features:

```
android {
    compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
}
```

Now you're ready to load the package in `MainApplication.java`.  In the imports section, add this:

```java
import com.twiliorn.library.TwilioPackage;
```

Then update the `getPackages()` method:

```java
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            ...
            new TwilioPackage()
        );
    }
```

### Permissions

For most applications, you'll want to add camera and audio permissions to your `AndroidManifest.xml` file:

```xml
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
    <uses-feature android:name="android.hardware.microphone" android:required="false" />
```

Newer versions of Android have a different permissions model.  You will need to use the `PermissionsAndroid`
class in `react-native` in order to request the `CAMERA` and `RECORD_AUDIO` permissions.

### Additional Tips

Under default settings, the Android build will fail if the total number of symbols exceeds a certain threshold. If you should encounter this issue when adding this library (e.g., if your build fails with `com.android.dex.DexIndexOverflowException`), you can turn on jumbo mode by editing your `app/build.gradle`:

```
android {
  ...
  dexOptions {
    jumboMode true
  }
}
```

If you are using proguard (very likely), you will also need to ensure that the symbols needed by
this library are not stripped. To do that, add these two lines to `proguard-rules.pro`:

```
  -keep class org.webrtc.** { *; }
  -keep class com.twilio.** { *; }
```

## Docs
You can see the documentation [here](./docs).

## Usage

We have three important components to understand:

```javascript
import {
  TwilioVideo,
  TwilioVideoLocalView,
  TwilioVideoParticipantView
} from 'react-native-twilio-video-webrtc'
```

- `TwilioVideo` / is responsible for connecting to rooms, events delivery and camera/audio.
- `TwilioVideoLocalView` / is responsible local camera feed view
- `TwilioVideoParticipantView` / is responsible remote peer's camera feed view

Here you can see a complete example of a simple application that uses almost all the apis:

````javascript
import React, { Component } from 'react';
import {
  TwilioVideoLocalView,
  TwilioVideoParticipantView,
  TwilioVideo
} from 'react-native-twilio-video-webrtc'

export default class Example extends Component {
  state = {
    isAudioEnabled: true,
    isVideoEnabled: true,
    status: 'disconnected',
    participants: new Map(),
    videoTracks: new Map(),
    roomName: '',
    token: ''
  }

  _onConnectButtonPress = () => {
    this.refs.twilioVideo.connect({ roomName: this.state.roomName, accessToken: this.state.token })
    this.setState({status: 'connecting'})
  }

  _onEndButtonPress = () => {
    this.refs.twilioVideo.disconnect()
  }

  _onMuteButtonPress = () => {
    this.refs.twilioVideo.setLocalAudioEnabled(!this.state.isAudioEnabled)
      .then(isEnabled => this.setState({isAudioEnabled: isEnabled}))
  }

  _onFlipButtonPress = () => {
    this.refs.twilioVideo.flipCamera()
  }

  _onRoomDidDisconnect = ({roomName, error}) => {
    console.log("ERROR: ", error)

    this.setState({status: 'disconnected'})
  }

  _onRoomDidFailToConnect = (error) => {
    console.log("ERROR: ", error)

    this.setState({status: 'disconnected'})
  }

  _onParticipantAddedVideoTrack = ({participant, track}) => {
    console.log("onParticipantAddedVideoTrack: ", participant, track)

    this.setState({
      videoTracks: new Map([
        ...this.state.videoTracks,
        [track.trackSid, { participantSid: participant.sid, videoTrackSid: track.trackSid }]
      ]),
    });
  }

  _onParticipantRemovedVideoTrack = ({participant, track}) => {
    console.log("onParticipantRemovedVideoTrack: ", participant, track)

    const videoTracks = this.state.videoTracks
    videoTracks.delete(track.trackSid)

    this.setState({videoTracks: { ...videoTracks }})
  }

  render() {
    return (
      <View style={styles.container}>
        {
          this.state.status === 'disconnected' &&
          <View>
            <Text style={styles.welcome}>
              React Native Twilio Video
            </Text>
            <TextInput
              style={styles.input}
              autoCapitalize='none'
              value={this.state.roomName}
              onChangeText={(text) => this.setState({roomName: text})}>
            </TextInput>
            <TextInput
              style={styles.input}
              autoCapitalize='none'
              value={this.state.token}
              onChangeText={(text) => this.setState({token: text})}>
            </TextInput>
            <Button
              title="Connect"
              style={styles.button}
              onPress={this._onConnectButtonPress}>
            </Button>
          </View>
        }

        {
          (this.state.status === 'connected' || this.state.status === 'connecting') &&
            <View style={styles.callContainer}>
            {
              this.state.status === 'connected' &&
              <View style={styles.remoteGrid}>
                {
                  Array.from(this.state.videoTracks, ([trackSid, trackIdentifier]) => {
                    return (
                      <TwilioVideoParticipantView
                        style={styles.remoteVideo}
                        key={trackSid}
                        trackIdentifier={trackIdentifier}
                      />
                    )
                  })
                }
              </View>
            }
            <View
              style={styles.optionsContainer}>
              <TouchableOpacity
                style={styles.optionButton}
                onPress={this._onEndButtonPress}>
                <Text style={{fontSize: 12}}>End</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.optionButton}
                onPress={this._onMuteButtonPress}>
                <Text style={{fontSize: 12}}>{ this.state.isAudioEnabled ? "Mute" : "Unmute" }</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.optionButton}
                onPress={this._onFlipButtonPress}>
                <Text style={{fontSize: 12}}>Flip</Text>
              </TouchableOpacity>
              <TwilioVideoLocalView
                enabled={true}
                style={styles.localVideo}
              />
            </View>
          </View>
        }

        <TwilioVideo
          ref="twilioVideo"
          onRoomDidConnect={ this._onRoomDidConnect }
          onRoomDidDisconnect={ this._onRoomDidDisconnect }
          onRoomDidFailToConnect= { this._onRoomDidFailToConnect }
          onParticipantAddedVideoTrack={ this._onParticipantAddedVideoTrack }
          onParticipantRemovedVideoTrack= { this._onParticipantRemovedVideoTrack }
        />
      </View>
    );
  }
}

AppRegistry.registerComponent('Example', () => Example);
````

## Run the Example Application

To run the example application:

- Move to the Example directory: `cd Example`
- Install node dependencies: `yarn install`
- Install objective-c dependencies: `cd ios && pod install`
- Open the xcworkspace and run the app: `open Example.xcworkspace`

## Migrating from 1.x to 2.x

* Make sure your pod dependencies are updated.  If you manually specified a pod version, you'll want to update it as follows:

```
  s.dependency 'TwilioVideo', '~> 2.2.0'
```

* Both participants and tracks are uniquely identified by their `sid`/`trackSid` field.
The `trackId` field no longer exists and should be replaced by `trackSid`.  Commensurate with this change,
participant views now expect `participantSid` and `videoTrackSid` keys in the `trackIdentity` prop (instead of
`identity` and `trackId`).

* Make sure you're listening to participant events via `onParticipant{Added/Removed}VideoTrack` rather than `onParticipant{Enabled/Disabled}Track`.

## Contact

- Martín Fernández <fmartin91@gmail.com>
- Gaston Morixe <gaston@gastonmorixe.com>
