declare module "react-native-twilio-video-webrtc" {
  import { ViewProps } from "react-native";
  import React from "react";

  interface TwilioVideoLocalViewProps extends ViewProps {
    enabled: boolean;
    ref?: React.Ref<any>;
  }

  interface TwilioVideoParticipantViewProps extends ViewProps {
    trackIdentifier: {
      participantIdentity: string;
      videoTrackId: string;
    };
    ref?: React.Ref<any>;
  }

  export interface TwilioVideoTrackCbArgs {
    participant: any;
    track: any;
  }

  type TwilioVideoTrackEventCb = (t: TwilioVideoTrackCbArgs) => void;

  export interface TwilioVideoRoomErrorEventArgs {
    roomName: string;
    error: any;
  }
  type RoomErrorEventCb = (t: TwilioVideoRoomErrorEventArgs) => void;

  type TwilioVideoParticipantEventCb = (roomName: string, participant: any) => void;

  type TwilioVideoProps = ViewProps & {
    onCameraDidStart?: () => void;
    onCameraDidStopRunning?: (err: any) => void;
    onCameraWasInterrupted?: () => void;
    onParticipantAddedAudioTrack?: TwilioVideoTrackEventCb;
    onParticipantAddedVideoTrack?: (participant: any, track: any, enabled: boolean) => void;
    onParticipantDisabledVideoTrack?: TwilioVideoTrackEventCb;
    onParticipantDisabledAudioTrack?: TwilioVideoTrackEventCb;
    onParticipantEnabledVideoTrack?: TwilioVideoTrackEventCb;
    onParticipantEnabledAudioTrack?: TwilioVideoTrackEventCb;
    onParticipantRemovedAudioTrack?: TwilioVideoTrackEventCb;
    onParticipantRemovedVideoTrack?: TwilioVideoTrackEventCb;
    onRoomDidConnect?: (roomName: string, participants: any[]) => void;
    onRoomDidDisconnect?: RoomErrorEventCb;
    onRoomDidFailToConnect?: RoomErrorEventCb;
    onRoomParticipantDidConnect?: TwilioVideoParticipantEventCb;
    onRoomParticipantDidDisconnect?: TwilioVideoParticipantEventCb;

    ref?: React.Ref<any>;
  };

  class TwilioVideo extends React.Component<TwilioVideoProps> {
    setLocalVideoEnabled: (enabled: boolean) => Promise<boolean>;
    setLocalAudioEnabled: (enabled: boolean) => Promise<boolean>;
    connect: (t: { roomName: string; accessToken: string }) => void;
    disconnect: () => void;
    flipCamera: () => void;
  }

  class TwilioVideoLocalView extends React.Component<TwilioVideoLocalViewProps> {}

  class TwilioVideoParticipantView extends React.Component<TwilioVideoParticipantViewProps> {}

  export { TwilioVideoLocalView, TwilioVideoParticipantView, TwilioVideo };
}
