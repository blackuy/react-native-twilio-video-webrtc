declare module "react-native-twilio-video-webrtc" {
  import { ViewProps } from "react-native";
  import React from "react";

  export interface TrackIdentifier {
    participantSid: string;
    videoTrackSid: string;
  }

  // New interfaces for multiple track support
  export interface LocalTrackConfig {
    trackName: string;
    enabled?: boolean;
  }

  export interface LocalVideoTrackConfig extends LocalTrackConfig {
    cameraType?: cameraType;
  }

  export interface LocalAudioTrackConfig extends LocalTrackConfig {
    // Audio-specific configs can be added here
  }

  // Updated TrackPublication interface to match native implementation
  export interface TrackPublication {
    trackName: string;
    enabled: boolean;
    type: "audio" | "video";
  }

  // Interface for remote tracks (these have trackSid)
  export interface RemoteTrackPublication {
    trackSid: string;
    trackName: string;
    enabled: boolean;
    type: "audio" | "video";
  }

  type scaleType = "fit" | "fill";
  type cameraType = "front" | "back";

  interface TwilioVideoParticipantViewProps extends ViewProps {
    trackIdentifier: TrackIdentifier;
    ref?: React.Ref<any>;
    scaleType?: scaleType;
    /**
     * Whether to apply Z ordering to this view.  Setting this to true will cause
     * this view to appear above other Twilio Video views. 
     */
     applyZOrder?: boolean | undefined;
  }

  interface TwilioVideoLocalViewProps extends ViewProps {
    enabled: boolean;
    trackName?: string; // Optional track name for multiple track support
    ref?: React.Ref<any>;
    scaleType?: scaleType;
    /**
     * Whether to apply Z ordering to this view.  Setting this to true will cause
     * this view to appear above other Twilio Video views. 
     */
    applyZOrder?: boolean | undefined;
  }

  interface Participant {
    sid: string;
    identity: string;
  }

  interface Track {
    enabled: boolean;
    trackName: string;
    trackSid: string;
  }

  export interface TrackEventCbArgs {
    participant: Participant;
    track: Track;
  }

  export type TrackEventCb = (t: TrackEventCbArgs) => void;

  export interface DataTrackEventCbArgs {
    message: string;
    trackSid: string;
  }

  export type DataTrackEventCb = (t: DataTrackEventCbArgs) => void;

  interface RoomEventCommonArgs {
    roomName: string;
    roomSid: string;
  }

  export type RoomErrorEventArgs = RoomEventCommonArgs & {
    error: any;
  };

  type RoomEventArgs = RoomEventCommonArgs & {
    participants: Participant[];
    localParticipant: Participant;
  };

  type ParticipantEventArgs = RoomEventCommonArgs & {
    participant: Participant;
  };

  type NetworkLevelChangeEventArgs = {
    participant: Participant;
    isLocalUser: boolean;
    quality: number;
  };

  export type RoomEventCb = (p: RoomEventArgs) => void;
  export type RoomErrorEventCb = (t: RoomErrorEventArgs) => void;

  export type ParticipantEventCb = (p: ParticipantEventArgs) => void;
  
  export type NetworkLevelChangeEventCb = (p: NetworkLevelChangeEventArgs) => void;

  export type DominantSpeakerChangedEventArgs = RoomEventCommonArgs & {
    participant: Participant;
  }
  
  export type DominantSpeakerChangedCb = (d: DominantSpeakerChangedEventArgs) => void;

  export type LocalParticipantSupportedCodecsCbEventArgs = {
    supportedCodecs: Array<string>;
  }

  export type LocalParticipantSupportedCodecsCb = (d: LocalParticipantSupportedCodecsCbEventArgs) => void;

  // New event types for multiple track support
  export interface TrackCreatedEventArgs {
    trackName: string;
    trackSid?: string; // Optional since local tracks might not have sid until published
    enabled: boolean;
  }

  export interface TrackErrorEventArgs {
    error: string;
  }

  export type TrackCreatedEventCb = (t: TrackCreatedEventArgs) => void;
  export type TrackErrorEventCb = (t: TrackErrorEventArgs) => void;

  export type TwilioVideoProps = ViewProps & {
    onCameraDidStart?: () => void;
    onCameraDidStopRunning?: (err: any) => void;
    onCameraWasInterrupted?: () => void;
    onDominantSpeakerDidChange?: DominantSpeakerChangedCb;
    onParticipantAddedAudioTrack?: TrackEventCb;
    onParticipantAddedVideoTrack?: TrackEventCb;
    onParticipantDisabledVideoTrack?: TrackEventCb;
    onParticipantDisabledAudioTrack?: TrackEventCb;
    onParticipantEnabledVideoTrack?: TrackEventCb;
    onParticipantEnabledAudioTrack?: TrackEventCb;
    onParticipantRemovedAudioTrack?: TrackEventCb;
    onParticipantRemovedVideoTrack?: TrackEventCb;
    onParticipantAddedDataTrack?: TrackEventCb;
    onParticipantRemovedDataTrack?: TrackEventCb;
    onRoomDidConnect?: RoomEventCb;
    onRoomDidDisconnect?: RoomErrorEventCb;
    onRoomDidFailToConnect?: RoomErrorEventCb;
    onRoomParticipantDidConnect?: ParticipantEventCb;
    onRoomParticipantDidDisconnect?: ParticipantEventCb;
    onNetworkQualityLevelsChanged?: NetworkLevelChangeEventCb;
    onLocalParticipantSupportedCodecs?: LocalParticipantSupportedCodecsCb;

    onStatsReceived?: (data: any) => void;
    onDataTrackMessageReceived?: DataTrackEventCb;
    
    // New events for multiple track support
    onAudioTrackCreated?: TrackCreatedEventCb;
    onVideoTrackCreated?: TrackCreatedEventCb;
    onTrackCreationError?: TrackErrorEventCb;
    onTrackPublishError?: TrackErrorEventCb;
    onTrackUnpublishError?: TrackErrorEventCb;
    
    // iOS only
    autoInitializeCamera?: boolean;    
    ref?: React.Ref<any>;
  };

  type iOSConnectParams = {
    roomName?: string;
    accessToken: string;
    cameraType?: cameraType;
    dominantSpeakerEnabled?: boolean;
    enableAudio?: boolean;
    enableVideo?: boolean;
    encodingParameters?: {
      enableH264Codec?: boolean;
      // if audioBitrate OR videoBitrate is provided, you must provide both
      audioBitrate?: number;
      videoBitrate?: number;
    };
    enableNetworkQualityReporting?: boolean;
  };

  type androidConnectParams = {
    roomName?: string;
    accessToken: string;
    cameraType?: cameraType;
    dominantSpeakerEnabled?: boolean;
    enableAudio?: boolean;
    enableVideo?: boolean;
    enableRemoteAudio?: boolean;
    encodingParameters?: {
      enableH264Codec?: boolean;
    };
    enableNetworkQualityReporting?: boolean;
    maintainVideoTrackInBackground?: boolean;
  };

  class TwilioVideo extends React.Component<TwilioVideoProps> {
    setRemoteAudioEnabled: (enabled: boolean) => Promise<boolean>;
    setBluetoothHeadsetConnected: (enabled: boolean) => Promise<boolean>;
    connect: (options: iOSConnectParams | androidConnectParams) => void;
    disconnect: () => void;
    flipCamera: () => void;
    toggleSoundSetup: (speaker: boolean) => void;
    getStats: () => void;
    
    // Multiple track methods
    createLocalAudioTrack: (config: LocalAudioTrackConfig) => Promise<string>;
    createLocalVideoTrack: (config: LocalVideoTrackConfig) => Promise<string>;
    publishLocalAudioTrack: (trackName: string) => Promise<boolean>;
    publishLocalVideoTrack: (trackName: string) => Promise<boolean>;
    unpublishLocalAudioTrack: (trackName: string) => Promise<boolean>;
    unpublishLocalVideoTrack: (trackName: string) => Promise<boolean>;
    destroyLocalTrack: (trackName: string) => Promise<boolean>;
    enableLocalTrack: (trackName: string, enabled: boolean) => Promise<boolean>;
    getLocalTracks: () => Promise<TrackPublication[]>;
    
    sendString: (message: string) => void;
  }

  class TwilioVideoLocalView extends React.Component<
    TwilioVideoLocalViewProps
  > {}

  class TwilioVideoParticipantView extends React.Component<
    TwilioVideoParticipantViewProps
  > {}

  export { TwilioVideoLocalView, TwilioVideoParticipantView, TwilioVideo };
}
