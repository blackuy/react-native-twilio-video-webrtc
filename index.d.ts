declare module "react-native-twilio-video-webrtc"{
  import { ViewProps } from "react-native";
  import React from "react";

  interface TwilioVideoLocalViewProps extends ViewProps {
    enabled: boolean;
    ref?: React.Ref<any>;
  }

  interface TwilioVideoParticipantViewProps extends ViewProps {
    trackIdentifier: {
      participantSid: string;
      videoTrackSid: string;
    };
    ref?: React.Ref<any>;
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

  interface RoomEventCommonArgs {
    roomName: string;
    roomSid: string;
  }

  type RoomErrorEventArgs = RoomEventCommonArgs & {
    error: any;
  };

  type RoomEventArgs = RoomEventCommonArgs & {
    participants: Participant[];
  };

  type ParticipantEventArgs = RoomEventCommonArgs & {
    participant: Participant;
  };

  export type RoomEventCb = (p: RoomEventArgs) => void;
  export type RoomErrorEventCb = (t: RoomErrorEventArgs) => void;

  export type ParticipantEventCb = (p: ParticipantEventArgs) => void;

  type TwilioVideoProps = ViewProps & {
    onCameraDidStart?: () => void;
    onCameraDidStopRunning?: (err: any) => void;
    onCameraWasInterrupted?: () => void;
    onParticipantAddedAudioTrack?: TrackEventCb;
    onParticipantAddedVideoTrack?: TrackEventCb;
    onParticipantDisabledVideoTrack?: TrackEventCb;
    onParticipantDisabledAudioTrack?: TrackEventCb;
    onParticipantEnabledVideoTrack?: TrackEventCb;
    onParticipantEnabledAudioTrack?: TrackEventCb;
    onParticipantRemovedAudioTrack?: TrackEventCb;
    onParticipantRemovedVideoTrack?: TrackEventCb;
    onRoomDidConnect?: RoomEventCb;
    onRoomDidDisconnect?: RoomErrorEventCb;
    onRoomDidFailToConnect?: RoomErrorEventCb;
    onRoomParticipantDidConnect?: ParticipantEventCb;
    onRoomParticipantDidDisconnect?: ParticipantEventCb;
    ref?: React.Ref<any>;
  };

  type iOSConnectParams = {
    accessToken: string;
    roomName?: string;
    enableAudio?: boolean;
    enableVideo?: boolean;
    encodingParameters?: {
      enableH264Codec?: boolean;
      // if audioBitrate OR videoBitrate is provided, you must provide both
      audioBitrate?: number;
      videoBitrate?: number;
    }
  }

  type androidConnectParams = {
    roomName?: string;
    accessToken: string;
    enableAudio?: boolean;
    enableVideo?: boolean;
    enableRemoteAudio?: boolean;
  }

  class TwilioVideo extends React.Component<TwilioVideoProps> {
    setLocalVideoEnabled: (enabled: boolean) => Promise<boolean>;
    setLocalAudioEnabled: (enabled: boolean) => Promise<boolean>;
    setBluetoothHeadsetConnected: (enabled: boolean) => Promise<boolean>;
    connect: (options: iOSConnectParams | androidConnectParams) => void;
    disconnect: () => void;
    flipCamera: () => void;
    toggleSoundSetup: (speaker: boolean) => void;
  }

  class TwilioVideoLocalView extends React.Component<TwilioVideoLocalViewProps> {}

  class TwilioVideoParticipantView extends React.Component<TwilioVideoParticipantViewProps> {}

  export { TwilioVideoLocalView, TwilioVideoParticipantView, TwilioVideo };
}
