declare module 'react-native-twilio-video-webrtc' {
  import { ViewProps } from 'react-native';
  import React from 'react';

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

  class TwilioVideo extends React.Component<TwilioVideoProps> {
    setLocalVideoEnabled: (enabled: boolean) => Promise<boolean>;
    setLocalAudioEnabled: (enabled: boolean) => Promise<boolean>;
    connect: (t: { roomName: string; accessToken: string; encodingParameters: object }) => void;
    disconnect: () => void;
    flipCamera: () => void;
  }

  class TwilioVideoLocalView extends React.Component<TwilioVideoLocalViewProps> {}

  class TwilioVideoParticipantView extends React.Component<TwilioVideoParticipantViewProps> {}

  export { TwilioVideoLocalView, TwilioVideoParticipantView, TwilioVideo };
}
