# Twilio Video (WebRTC) for React Native

> [!NOTE]  
> **October 21 2024:** _Good news!_ Twilio just announced **Twilio Video service is here to stay**, they are reversing the deprecation decision. [Here's their official announcement.](https://www.twilio.com/en-us/blog/twilio-video-update-2024). 
> 
> If you or your company need React Native support, contact me [gaston@gastonmorixe.com](mailto:gaston@gastonmorixe.com). We have premium react native features  like PiP support, Live Activities, Typescript, and many more. - Gaston


[![react-native-twilio-video-webrtc](./docs/react-native-banner.svg)](https://github.com/blackuy/react-native-twilio-video-webrtc)

[![GitHub Repo stars](https://img.shields.io/github/stars/blackuy/react-native-twilio-video-webrtc)](https://github.com/blackuy/react-native-twilio-video-webrtc/stargazers)
[![Weekly Views](https://shieldsdev.tech/badge/react-native-twilio-video-webrtc/totals)](https://npm-stat.com/charts.html?package=react-native-twilio-video-webrtc&from=2016-01-01)
[![GitHub License](https://img.shields.io/github/license/blackuy/react-native-twilio-video-webrtc)](https://github.com/blackuy/react-native-twilio-video-webrtc/blob/master/LICENSE)
[![NPM version](https://img.shields.io/npm/v/react-native-twilio-video-webrtc)](https://www.npmjs.com/package/react-native-twilio-video-webrtc)
[![NPM Downloads](https://img.shields.io/npm/dy/react-native-twilio-video-webrtc)](https://npm-stat.com/charts.html?package=react-native-twilio-video-webrtc&from=2016-01-01)

Platforms:

- iOS
- Android

People using a version < 1.0.1 please move to 1.0.1 since the project changed a lot internally to support the stable TwilioVideo version.

## Installation

- react-native >= 0.40.0: install react-native-twilio-video-webrtc@1.0.1
- react-native < 0.40.0: install react-native-twilio-video-webrtc@1.0.0

### Install Node Package

[![NPM version](https://img.shields.io/npm/v/react-native-twilio-video-webrtc)](https://www.npmjs.com/package/react-native-twilio-video-webrtc)

#### Option A: yarn

```shell
yarn add react-native-twilio-video-webrtc
```

#### Option B: npm

```shell
npm install react-native-twilio-video-webrtc
```

### Usage with Expo

To use this library with [`Expo`](https://expo.dev) we recommend using our config plugin that you can configure like the following example:

```json
{
  "name": "my app",
  "plugins": [
    [
      "react-native-twilio-video-webrtc",
      {
        "cameraPermission": "Allow $(PRODUCT_NAME) to access your camera",
        "microphonePermission": "Allow $(PRODUCT_NAME) to access your microphone"
      }
    ]
  ]
}
```

Also you will need to install `expo-build-properties` package:

```shell
npx expo install expo-build-properties
```

#### Expo Config Plugin Props

The plugin support the following properties:

- `cameraPermission`: Specifies the text to show when requesting the camera permission to the user.

- `microphonePermission`: Specifies the text to show when requesting the microphone permission to the user.

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

#### Post install

Be sure to increment your iOS Deployment Target to at least iOS 11 through XCode and your `Podfile` contains

```
platform :ios, '11.0'
```

#### Permissions

To enable camera usage and microphone usage you will need to add the following entries to your `Info.plist` file:

```
<key>NSCameraUsageDescription</key>
<string>Your message to user when the camera is accessed for the first time</string>
<key>NSMicrophoneUsageDescription</key>
<string>Your message to user when the microphone is accessed for the first time</string>
```

#### Known Issues

TwilioVideo version 1.3.8 has the following know issues.

- Participant disconnect event can take up to 120 seconds to occur. [Issue 99](https://github.com/twilio/video-quickstart-swift/issues/99)
- AVPlayer audio content does not mix properly with Room audio. [Issue 62](https://github.com/twilio/video-quickstart-objc/issues/62)

### Android

As with iOS, make sure the package is installed:

```shell
yarn add react-native-twilio-video-webrtc
```

Then add the library to your `settings.gradle` file:

```
include ':react-native-twilio-video-webrtc'
project(':react-native-twilio-video-webrtc').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-twilio-video-webrtc/android')
```

And include the library in your dependencies in `android/app/build.gradle`:
(if using gradle 4 or lower, replace `implementation` with `compile` below)

```
dependencies {
    .....
    .....
    .....
    implementation project(':react-native-twilio-video-webrtc')
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

Now you're ready to load the package in `MainApplication.java`. In the imports section, add this:

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

Newer versions of Android have a different permissions model. You will need to use the `PermissionsAndroid`
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
  -keep class com.twilio.** { *; }
  -keep class tvi.webrtc.** { *; }
```

## Docs

You can see the documentation [here](./docs).

## Usage

We have three important components to understand:

```javascript
import {
  TwilioVideo,
  TwilioVideoLocalView,
  TwilioVideoParticipantView,
} from "react-native-twilio-video-webrtc";
```

- `TwilioVideo` / is responsible for connecting to rooms, events delivery and camera/audio.
- `TwilioVideoLocalView` / is responsible local camera feed view
- `TwilioVideoParticipantView` / is responsible remote peer's camera feed view

Here you can see a complete example of a simple application that uses almost all the apis:

```javascript
import React, { Component, useRef } from "react";
import {
  TwilioVideoLocalView,
  TwilioVideoParticipantView,
  TwilioVideo,
} from "react-native-twilio-video-webrtc";

const Example = (props) => {
  const [isAudioEnabled, setIsAudioEnabled] = useState(true);
  const [isVideoEnabled, setIsVideoEnabled] = useState(true);
  const [status, setStatus] = useState("disconnected");
  const [participants, setParticipants] = useState(new Map());
  const [videoTracks, setVideoTracks] = useState(new Map());
  const [token, setToken] = useState("");
  const twilioRef = useRef(null);

  const _createTracks = async () => {
    try {
      // Create audio track
      await twilioRef.current.createLocalAudioTrack({
        trackName: 'microphone',
        enabled: isAudioEnabled
      });
      
      // Create video track
      await twilioRef.current.createLocalVideoTrack({
        trackName: 'camera',
        enabled: isVideoEnabled,
        cameraType: 'front'
      });
    } catch (error) {
      console.error('Error creating tracks:', error);
    }
  };

  const _onConnectButtonPress = async () => {
    // Create tracks before connecting
    await _createTracks();
    
    twilioRef.current.connect({ accessToken: token });
    setStatus("connecting");
  };

  const _onEndButtonPress = () => {
    twilioRef.current.disconnect();
  };

  const _onMuteButtonPress = () => {
    twilioRef.current
      .enableLocalTrack('microphone', !isAudioEnabled)
      .then(() => setIsAudioEnabled(!isAudioEnabled));
  };

  const _onFlipButtonPress = () => {
    twilioRef.current.flipCamera();
  };

  const _onRoomDidConnect = async ({ roomName, error }) => {
    console.log("onRoomDidConnect: ", roomName);

    // Publish tracks after connecting
    await twilioRef.current.publishLocalAudioTrack('microphone');
    await twilioRef.current.publishLocalVideoTrack('camera');

    setStatus("connected");
  };

  const _onRoomDidDisconnect = ({ roomName, error }) => {
    console.log("[Disconnect]ERROR: ", error);

    setStatus("disconnected");
  };

  const _onRoomDidFailToConnect = (error) => {
    console.log("[FailToConnect]ERROR: ", error);

    setStatus("disconnected");
  };

  const _onParticipantAddedVideoTrack = ({ participant, track }) => {
    console.log("onParticipantAddedVideoTrack: ", participant, track);

    setVideoTracks((originalVideoTracks) => {
      originalVideoTracks.set(track.trackSid, {
        participantSid: participant.sid,
        videoTrackSid: track.trackSid,
      });
      return new Map(originalVideoTracks);
    });
  };

  const _onParticipantRemovedVideoTrack = ({ participant, track }) => {
    console.log("onParticipantRemovedVideoTrack: ", participant, track);

    setVideoTracks((originalVideoTracks) => {
      originalVideoTracks.delete(track.trackSid);
      return new Map(originalVideoTracks);
    });
  };

  return (
    <View style={styles.container}>
      {status === "disconnected" && (
        <View>
          <Text style={styles.welcome}>React Native Twilio Video</Text>
          <TextInput
            style={styles.input}
            autoCapitalize="none"
            value={token}
            onChangeText={(text) => setToken(text)}
          ></TextInput>
          <Button
            title="Connect"
            style={styles.button}
            onPress={_onConnectButtonPress}
          ></Button>
        </View>
      )}

      {(status === "connected" || status === "connecting") && (
        <View style={styles.callContainer}>
          {status === "connected" && (
            <View style={styles.remoteGrid}>
              {Array.from(videoTracks, ([trackSid, trackIdentifier]) => {
                return (
                  <TwilioVideoParticipantView
                    style={styles.remoteVideo}
                    key={trackSid}
                    trackIdentifier={trackIdentifier}
                  />
                );
              })}
            </View>
          )}
          <View style={styles.optionsContainer}>
            <TouchableOpacity
              style={styles.optionButton}
              onPress={_onEndButtonPress}
            >
              <Text style={{ fontSize: 12 }}>End</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.optionButton}
              onPress={_onMuteButtonPress}
            >
              <Text style={{ fontSize: 12 }}>
                {isAudioEnabled ? "Mute" : "Unmute"}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.optionButton}
              onPress={_onFlipButtonPress}
            >
              <Text style={{ fontSize: 12 }}>Flip</Text>
            </TouchableOpacity>
            <TwilioVideoLocalView enabled={true} trackName="camera" style={styles.localVideo} />
          </View>
        </View>
      )}

      <TwilioVideo
        ref={twilioRef}
        onRoomDidConnect={_onRoomDidConnect}
        onRoomDidDisconnect={_onRoomDidDisconnect}
        onRoomDidFailToConnect={_onRoomDidFailToConnect}
        onParticipantAddedVideoTrack={_onParticipantAddedVideoTrack}
        onParticipantRemovedVideoTrack={_onParticipantRemovedVideoTrack}
      />
    </View>
  );
};

AppRegistry.registerComponent("Example", () => Example);
```

## Run the Example Application

To run the example application:

- Move to the Example directory: `cd Example`
- Install node dependencies: `yarn install`
- Install objective-c dependencies: `cd ios && pod install`
- Open the xcworkspace and run the app: `open Example.xcworkspace`

## Migrating from 1.x to 2.x

- Make sure your pod dependencies are updated. If you manually specified a pod version, you'll want to update it as follows:

```
  s.dependency 'TwilioVideo', '~> 2.2.0'
```

- Both participants and tracks are uniquely identified by their `sid`/`trackSid` field.
  The `trackId` field no longer exists and should be replaced by `trackSid`. Commensurate with this change,
  participant views now expect `participantSid` and `videoTrackSid` keys in the `trackIdentity` prop (instead of
  `identity` and `trackId`).

- Make sure you're listening to participant events via `onParticipant{Added/Removed}VideoTrack` rather than `onParticipant{Enabled/Disabled}Track`.

## Contact

- Original Author: **Gaston Morixe** ([@gastonmorixe](https://github.com/gastonmorixe)) <gaston@gastonmorixe.com>
- Core Contributor: **Martín Fernández** ([@bilby91](https://github.com/bilby91)) <fmartin91@gmail.com>

## License

The MIT License (MIT)

Copyright (c) 2016-2024 Gaston Morixe <gaston@gastonmorixe.com>

**Full License text** you must include and attribute in your project: [LICENSE](/LICENSE).

**Compliance Requirement:** All users must include the full text of the MIT License, including the copyright notice and permission notice, in any copies or substantial portions of the Software.

**Commercial Use:** Commercial entities using this software please ensure compliance with the license terms and proper attribution.

**Consequences of Violation:** Failure to comply with the MIT License constitutes copyright infringement and may result in legal action, including injunctions and monetary damages. Please ensure to respect the open source project.

For any questions regarding licensing or to request additional permissions, please contact the author.

## Star History

<a href="https://star-history.com/#blackuy/react-native-twilio-video-webrtc&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=blackuy/react-native-twilio-video-webrtc&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=blackuy/react-native-twilio-video-webrtc&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=blackuy/react-native-twilio-video-webrtc&type=Date" />
 </picture>
</a>

## Multiple Track Support

This library supports multiple audio and video tracks, similar to the Twilio Video JS SDK. This allows you to publish multiple audio/video tracks simultaneously, which is useful for scenarios like:

- Screen sharing alongside camera video
- Multiple camera feeds (front/back camera)
- Multiple audio sources (microphone, system audio, etc.)
- More complex video conferencing scenarios

### API Methods

#### Track Creation
```javascript
// Create a local audio track
const audioTrackName = await twilioVideo.createLocalAudioTrack({
  trackName: 'microphone',
  enabled: true
});

// Create a local video track
const videoTrackName = await twilioVideo.createLocalVideoTrack({
  trackName: 'camera',
  enabled: true,
  cameraType: 'front' // or 'back'
});
```

#### Track Publishing
```javascript
// Publish a specific track
await twilioVideo.publishLocalAudioTrack('microphone');
await twilioVideo.publishLocalVideoTrack('camera');

// Unpublish a specific track
await twilioVideo.unpublishLocalAudioTrack('microphone');
await twilioVideo.unpublishLocalVideoTrack('camera');
```

#### Track Management
```javascript
// Enable/disable a specific track
await twilioVideo.enableLocalTrack('microphone', false); // mute audio
await twilioVideo.enableLocalTrack('camera', true); // enable video

// Get all local tracks
const tracks = await twilioVideo.getLocalTracks();
// Returns: [{ trackName: 'microphone', enabled: true, type: 'audio' }, { trackName: 'camera', enabled: true, type: 'video' }]

// Destroy a track
await twilioVideo.destroyLocalTrack('microphone');
```

### New Events

The library emits additional events for track lifecycle management:

```javascript
<TwilioVideo
  onAudioTrackCreated={({ trackName, trackSid, enabled }) => {
    console.log('Audio track created:', trackName);
  }}
  onVideoTrackCreated={({ trackName, trackSid, enabled }) => {
    console.log('Video track created:', trackName);
  }}
  onTrackCreationError={({ error }) => {
    console.error('Track creation failed:', error);
  }}
  onTrackPublishError={({ error }) => {
    console.error('Track publish failed:', error);
  }}
  onTrackUnpublishError={({ error }) => {
    console.error('Track unpublish failed:', error);
  }}
  onLocalTracksReceived={({ tracks }) => {
    console.log('Local tracks:', tracks);
  }}
  // ... other props
/>
```

### Local Video View with Track Names

The `TwilioVideoLocalView` supports an optional `trackName` prop to display a specific track:

```javascript
{/* Display specific track */}
<TwilioVideoLocalView 
  enabled={true} 
  trackName="camera" 
/>

{/* Display another track */}
<TwilioVideoLocalView 
  enabled={true} 
  trackName="frontCamera" 
/>
```

### Example Usage

Here's a complete example showing how to use multiple tracks:

```javascript
import React, { useState, useEffect } from 'react';
import { View, Button } from 'react-native';
import { TwilioVideo, TwilioVideoLocalView, TwilioVideoParticipantView } from 'react-native-twilio-video-webrtc';

const MultiTrackExample = () => {
  const [twilioRef, setTwilioRef] = useState(null);
  const [tracks, setTracks] = useState([]);

  const createTracks = async () => {
    try {
      // Create audio track
      await twilioRef.createLocalAudioTrack({
        trackName: 'microphone',
        enabled: true
      });
      
      // Create video track
      await twilioRef.createLocalVideoTrack({
        trackName: 'camera',
        enabled: true,
        cameraType: 'front'
      });
      
      // Get all tracks
      const allTracks = await twilioRef.getLocalTracks();
      setTracks(allTracks);
    } catch (error) {
      console.error('Error creating tracks:', error);
    }
  };

  const connectToRoom = async () => {
    try {
      // Create tracks before connecting
      await createTracks();
      
      // Connect to room
      twilioRef.connect({
        roomName: 'my-room',
        accessToken: 'your-access-token',
        enableAudio: true,
        enableVideo: true
      });
    } catch (error) {
      console.error('Error connecting:', error);
    }
  };

  const publishTracks = async () => {
    try {
      await twilioRef.publishLocalAudioTrack('microphone');
      await twilioRef.publishLocalVideoTrack('camera');
    } catch (error) {
      console.error('Error publishing tracks:', error);
    }
  };

  const toggleAudio = async () => {
    try {
      const currentTrack = tracks.find(t => t.trackName === 'microphone');
      if (currentTrack) {
        await twilioRef.enableLocalTrack('microphone', !currentTrack.enabled);
      }
    } catch (error) {
      console.error('Error toggling audio:', error);
    }
  };

  return (
    <View style={{ flex: 1 }}>
      <TwilioVideo
        ref={ref => setTwilioRef(ref)}
        onRoomDidConnect={() => {
          console.log('Connected to room');
          publishTracks();
        }}
        onAudioTrackCreated={({ trackName }) => {
          console.log('Audio track created:', trackName);
        }}
        onVideoTrackCreated={({ trackName }) => {
          console.log('Video track created:', trackName);
        }}
        // ... other props
      />
      
      {/* Display video track */}
      <TwilioVideoLocalView 
        enabled={true} 
        trackName="camera"
        style={{ width: 200, height: 300 }}
      />
      
      <Button title="Connect" onPress={connectToRoom} />
      <Button title="Toggle Audio" onPress={toggleAudio} />
    </View>
  );
};

export default MultiTrackExample;
```

### Important Notes

- **No automatic track creation**: You must explicitly create all tracks using `createLocalAudioTrack()` and `createLocalVideoTrack()` before connecting to a room
- **Track management**: Use `enableLocalTrack(trackName, enabled)` to enable/disable specific tracks
- **Explicit track names**: All track operations require specifying the exact track name you want to work with
- **Lifecycle management**: Remember to call `destroyLocalTrack()` for tracks you no longer need

### TypeScript Support

The library includes full TypeScript definitions for the multiple track APIs:

```typescript
interface LocalTrackConfig {
  trackName: string;
  enabled?: boolean;
}

interface LocalVideoTrackConfig extends LocalTrackConfig {
  cameraType?: 'front' | 'back';
}

interface LocalAudioTrackConfig extends LocalTrackConfig {
  // Audio-specific configs can be added here
}

interface TrackPublication {
  trackSid: string;
  trackName: string;
  enabled: boolean;
}
```

### Platform Support

- ✅ Android: Fully implemented
- ⚠️ iOS: Implementation in progress (some features may not be fully functional yet)

For more complex scenarios, you can now:
- Create multiple tracks of the same type
- Publish/unpublish tracks independently
- Enable/disable tracks without affecting others
- Get detailed information about all active tracks
