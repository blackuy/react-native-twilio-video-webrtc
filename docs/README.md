Components
----------


**src/TwilioVideo.android.js**


### 1. CustomTwilioVideoView




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
onCameraSwitched | func | no |  | Callback that is called when camera source changes
onVideoChanged | func | no |  | Callback that is called when video is toggled.
onAudioChanged | func | no |  | Callback that is called when a audio is toggled.
onRoomDidConnect | func | no |  | Called when the room has connected  @param {{roomName, participants, localParticipant}}
onRoomDidFailToConnect | func | no |  | Callback that is called when connecting to room fails.
onRoomDidDisconnect | func | no |  | Callback that is called when user is disconnected from room.
onParticipantAddedDataTrack | func | no |  | Called when a new data track has been added  @param {{participant, track}}
onParticipantRemovedDataTrack | func | no |  | Called when a data track has been removed  @param {{participant, track}}
onDataTrackMessageReceived | func | no |  | Called when an dataTrack receives a message  @param {{message, trackSid}}
onParticipantAddedVideoTrack | func | no |  | Called when a new video track has been added  @param {{participant, track, enabled}}
onParticipantRemovedVideoTrack | func | no |  | Called when a video track has been removed  @param {{participant, track}}
onParticipantAddedAudioTrack | func | no |  | Called when a new audio track has been added  @param {{participant, track}}
onParticipantRemovedAudioTrack | func | no |  | Called when a audio track has been removed  @param {{participant, track}}
onRoomParticipantDidConnect | func | no |  | Callback called a participant enters a room.
onRoomParticipantDidDisconnect | func | no |  | Callback that is called when a participant exits a room.
onParticipantEnabledVideoTrack | func | no |  | Called when a video track has been enabled.  @param {{participant, track}}
onParticipantDisabledVideoTrack | func | no |  | Called when a video track has been disabled.  @param {{participant, track}}
onParticipantEnabledAudioTrack | func | no |  | Called when an audio track has been enabled.  @param {{participant, track}}
onParticipantDisabledAudioTrack | func | no |  | Called when an audio track has been disabled.  @param {{participant, track}}
onStatsReceived | func | no |  | Callback that is called when stats are received (after calling getStats)
onNetworkQualityLevelsChanged | func | no |  | Callback that is called when network quality levels are changed (only if enableNetworkQualityReporting in connect is set to true)
onDominantSpeakerDidChange | func | no |  | Called when dominant speaker changes @param {{ participant, room }} dominant participant and room
-----

**src/TwilioVideo.ios.js**


### 1. TwilioVideo




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
screenShare | bool | no |  | Flag that enables screen sharing RCTRootView instead of camera capture
onRoomDidConnect | func | no |  | Called when the room has connected  @param {{roomName, participants, localParticipant}}
onRoomDidDisconnect | func | no |  | Called when the room has disconnected  @param {{roomName, error}}
onRoomDidFailToConnect | func | no |  | Called when connection with room failed  @param {{roomName, error}}
onRoomParticipantDidConnect | func | no |  | Called when a new participant has connected  @param {{roomName, participant}}
onRoomParticipantDidDisconnect | func | no |  | Called when a participant has disconnected  @param {{roomName, participant}}
onParticipantAddedVideoTrack | func | no |  | Called when a new video track has been added  @param {{participant, track, enabled}}
onParticipantRemovedVideoTrack | func | no |  | Called when a video track has been removed  @param {{participant, track}}
onParticipantAddedDataTrack | func | no |  | Called when a new data track has been added  @param {{participant, track}}
onParticipantRemovedDataTrack | func | no |  | Called when a data track has been removed  @param {{participant, track}}
onParticipantAddedAudioTrack | func | no |  | Called when a new audio track has been added  @param {{participant, track}}
onParticipantRemovedAudioTrack | func | no |  | Called when a audio track has been removed  @param {{participant, track}}
onParticipantEnabledVideoTrack | func | no |  | Called when a video track has been enabled.  @param {{participant, track}}
onParticipantDisabledVideoTrack | func | no |  | Called when a video track has been disabled.  @param {{participant, track}}
onParticipantEnabledAudioTrack | func | no |  | Called when an audio track has been enabled.  @param {{participant, track}}
onParticipantDisabledAudioTrack | func | no |  | Called when an audio track has been disabled.  @param {{participant, track}}
onDataTrackMessageReceived | func | no |  | Called when an dataTrack receives a message  @param {{message, trackSid}}
onCameraDidStart | func | no |  | Called when the camera has started
onCameraWasInterrupted | func | no |  | Called when the camera has been interrupted
onCameraInterruptionEnded | func | no |  | Called when the camera interruption has ended
onCameraDidStopRunning | func | no |  | Called when the camera has stopped runing with an error  @param {{error}} The error message description
onStatsReceived | func | no |  | Called when stats are received (after calling getStats)
onNetworkQualityLevelsChanged | func | no |  | Called when the network quality levels of a participant have changed (only if enableNetworkQualityReporting is set to True when connecting)
onDominantSpeakerDidChange | func | no |  | Called when dominant speaker changes @param {{ participant, room }} dominant participant
onLocalParticipantSupportedCodecs | func | no |  | Always called on android with @param {{ supportedCodecs }} after connecting to the room

-----

**src/TwilioVideoLocalView.android.js**


### 1. TwilioVideoPreview




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
scaleType | enum(&#x27;fit&#x27;,&#x27;fill&#x27;,) | no |  | How the video stream should be scaled to fit its container.
-----

**src/TwilioVideoLocalView.ios.js**


### 1. TwilioVideoLocalView




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
enabled | bool | YES |  | Indicate if video feed is enabled.
scaleType | enum(&#x27;fit&#x27;,&#x27;fill&#x27;,) | no |  | How the video stream should be scaled to fit its container.
-----

**src/TwilioVideoParticipantView.android.js**


### 1. TwilioRemotePreview




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
trackIdentifier | shape(,) | no |  | &nbsp;
onFrameDimensionsChanged | func | no |  | &nbsp;
trackSid | string | no |  | &nbsp;
renderToHardwareTextureAndroid | string | no |  | &nbsp;
onLayout | string | no |  | &nbsp;
accessibilityLiveRegion | string | no |  | &nbsp;
accessibilityComponentType | string | no |  | &nbsp;
importantForAccessibility | string | no |  | &nbsp;
accessibilityLabel | string | no |  | &nbsp;
nativeID | string | no |  | &nbsp;
testID | string | no |  | &nbsp;
-----

**src/TwilioVideoParticipantView.ios.js**


### 1. TwilioVideoParticipantView




Property | Type | Required | Default value | Description
:--- | :--- | :--- | :--- | :---
trackIdentifier | shape(,,) | no |  | &nbsp;
-----
