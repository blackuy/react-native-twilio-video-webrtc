<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
# React Component Reference

- [TwilioVideo](#twiliovideo)
- [TwilioVideoLocalView](#twiliovideolocalview)
- [TwilioVideoParticipantView](#twiliovideoparticipantview)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## TwilioVideo

From [`../src/TwilioVideo.js`](../src/TwilioVideo.js)

#### onCameraDidStart

```js
onCameraDidStart: Function
```

Called when the camera has started

#### onCameraDidStopRunning

```js
onCameraDidStopRunning: Function
```

Called when the camera has stopped running with an error

@param {{error}} The error message description

#### onCameraWasInterrupted

```js
onCameraWasInterrupted: Function
```

Called when the camera has been interrupted

#### onParticipantAddedAudioTrack

```js
onParticipantAddedAudioTrack: Function
```

Called when a new audio track has been added

@param {{participant, track}}

#### onParticipantAddedVideoTrack

```js
onParticipantAddedVideoTrack: Function
```

Called when a new video track has been added

@param {{participant, track}}

#### onParticipantDisabledTrack

```js
onParticipantDisabledTrack: Function
```

Called when a track has been disabled. This can be audio or video tracks

@param {{participant, track}}

#### onParticipantEnabledTrack

```js
onParticipantEnabledTrack: Function
```

Called when a track has been enabled. This can be audio or video tracks

@param {{participant, track}}

#### onParticipantRemovedAudioTrack

```js
onParticipantRemovedAudioTrack: Function
```

Called when a audio track has been removed

@param {{participant, track}}

#### onParticipantRemovedVideoTrack

```js
onParticipantRemovedVideoTrack: Function
```

Called when a video track has been removed

@param {{participant, track}}

#### onRoomDidConnect

```js
onRoomDidConnect: Function
```

Called when the room has connected

@param {{roomName, participants}}

#### onRoomDidDisconnect

```js
onRoomDidDisconnect: Function
```

Called when the room has disconnected

@param {{roomName, error}}

#### onRoomDidFailToConnect

```js
onRoomDidFailToConnect: Function
```

Called when connection with room failed

@param {{roomName, error}}

#### onRoomParticipantDidConnect

```js
onRoomParticipantDidConnect: Function
```

Called when a new participant has connected

@param {{roomName, participant}}

#### onRoomParticipantDidDisconnect

```js
onRoomParticipantDidDisconnect: Function
```

Called when a participant has disconnected

@param {{roomName, participant}}

<br><br>

## TwilioVideoLocalView

From [`../src/TwilioVideoLocalView.js`](../src/TwilioVideoLocalView.js)

#### enabled

```js
// Required
enabled: Boolean
```

Indicate if video feed is enabled.

<br><br>

## TwilioVideoParticipantView

From [`../src/TwilioVideoParticipantView.js`](../src/TwilioVideoParticipantView.js)

#### trackIdentifier

```js
trackIdentifier: {
    participantIdentity: String
    videoTrackId: String
}
```

<br><br>
