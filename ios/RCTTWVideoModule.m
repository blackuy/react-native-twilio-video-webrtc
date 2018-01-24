//
//  RCTTWVideoModule.h
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import "RCTTWVideoModule.h"

#import "RCTTWSerializable.h"

static NSString* roomDidConnect               = @"roomDidConnect";
static NSString* roomDidDisconnect            = @"roomDidDisconnect";
static NSString* roomDidFailToConnect         = @"roomDidFailToConnect";
static NSString* roomParticipantDidConnect    = @"roomParticipantDidConnect";
static NSString* roomParticipantDidDisconnect = @"roomParticipantDidDisconnect";

static NSString* participantAddedVideoTrack   = @"participantAddedVideoTrack";
static NSString* participantRemovedVideoTrack = @"participantRemovedVideoTrack";
static NSString* participantAddedAudioTrack   = @"participantAddedAudioTrack";
static NSString* participantRemovedAudioTrack = @"participantRemovedAudioTrack";
static NSString* participantEnabledTrack      = @"participantEnabledTrack";
static NSString* participantDisabledTrack     = @"participantDisabledTrack";

static NSString* cameraDidStart               = @"cameraDidStart";
static NSString* cameraWasInterrupted        = @"cameraWasInterrupted";
static NSString* cameraDidStopRunning         = @"cameraDidStopRunning";


@interface RCTTWVideoModule () <TVIRemoteParticipantDelegate, TVIRoomDelegate, TVICameraCapturerDelegate>

@property (strong, nonatomic) TVICameraCapturer *camera;
@property (strong, nonatomic) TVIScreenCapturer *screen;
@property (strong, nonatomic) TVILocalVideoTrack* localVideoTrack;
@property (strong, nonatomic) TVILocalAudioTrack* localAudioTrack;
@property (strong, nonatomic) TVIRoom *room;

@end

@implementation RCTTWVideoModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    roomDidConnect,
    roomDidDisconnect,
    roomDidFailToConnect,
    roomParticipantDidConnect,
    roomParticipantDidDisconnect,
    participantAddedVideoTrack,
    participantRemovedVideoTrack,
    participantAddedAudioTrack,
    participantRemovedAudioTrack,
    participantEnabledTrack,
    participantDisabledTrack,
    cameraDidStopRunning,
    cameraDidStart,
    cameraWasInterrupted
  ];
}

- (void)addLocalView:(TVIVideoView *)view {
  [self.localVideoTrack addRenderer:view];
  if (self.camera && self.camera.source == TVICameraCaptureSourceBackCameraWide) {
    view.mirror = NO;
  } else {
    view.mirror = YES;
  }
}

- (void)removeLocalView:(TVIVideoView *)view {
  [self.localVideoTrack removeRenderer:view];
}

- (void)removeParticipantView:(TVIVideoView *)view identity:(NSString *)identity  trackId:(NSString *)trackId {
  // TODO: Implement this nicely
}

- (void)addParticipantView:(TVIVideoView *)view identity:(NSString *)identity  trackId:(NSString *)trackId {
  // Lookup for the participant in the room
  for (TVIRemoteParticipant *participant in self.room.remoteParticipants) {
    if ([participant.identity isEqualToString:identity]) {
      // Lookup for the given trackId
      for (TVIRemoteVideoTrackPublication *videoTrackPublication in participant.videoTracks) {
        [videoTrackPublication.videoTrack addRenderer:view];
      }
    }
  }
}

- (void)logMessage:(NSString *)msg {
    NSLog(@"%@", msg);
//    self.messageLabel.text = msg;
}

RCT_EXPORT_METHOD(startLocalVideo:(BOOL)screenShare) {
  if (screenShare) {
    UIViewController *rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    self.screen = [[TVIScreenCapturer alloc] initWithView:rootViewController.view];

    self.localVideoTrack = [TVILocalVideoTrack trackWithCapturer:self.screen enabled:YES constraints:[self videoConstraints] name:@"Screen"];
  } else if ([TVICameraCapturer availableSources].count > 0) {
    self.camera = [[TVICameraCapturer alloc] init];
    self.camera.delegate = self;

    self.localVideoTrack = [TVILocalVideoTrack trackWithCapturer:self.camera enabled:YES constraints:[self videoConstraints] name:@"Camera"];
  }
}

RCT_EXPORT_METHOD(startLocalAudio) {
  self.localAudioTrack = [TVILocalAudioTrack trackWithOptions:nil enabled:YES name:@"Microphone"];
}

RCT_EXPORT_METHOD(stopLocalVideo) {
  self.localVideoTrack = nil;
  self.camera = nil;
}

RCT_EXPORT_METHOD(stopLocalAudio) {
  self.localAudioTrack = nil;
}

RCT_REMAP_METHOD(setLocalAudioEnabled, enabled:(BOOL)enabled setLocalAudioEnabledWithResolver:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject) {
  [self.localAudioTrack setEnabled:enabled];

  resolve(@(enabled));
}

RCT_REMAP_METHOD(setLocalVideoEnabled, enabled:(BOOL)enabled setLocalVideoEnabledWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  [self.localVideoTrack setEnabled:enabled];

  resolve(@(enabled));
}


RCT_EXPORT_METHOD(flipCamera) {
  if (self.camera.source == TVICameraCaptureSourceFrontCamera) {
    [self.camera selectSource:TVICameraCaptureSourceBackCameraWide];
    if (self.localVideoTrack) {
      for (TVIVideoView *r in self.localVideoTrack.renderers) {
        r.mirror = NO;
      }
    }
  } else {
    [self.camera selectSource:TVICameraCaptureSourceFrontCamera];
    if (self.localVideoTrack) {
      for (TVIVideoView *r in self.localVideoTrack.renderers) {
        r.mirror = YES;
      }
    }
  }
}

RCT_EXPORT_METHOD(connect:(NSString *)accessToken roomName:(NSString *)roomName) {
  TVIConnectOptions *connectOptions = [TVIConnectOptions optionsWithToken:accessToken block:^(TVIConnectOptionsBuilder * _Nonnull builder) {
    if (self.localVideoTrack) {
      builder.videoTracks = @[self.localVideoTrack];
    }

    if (self.localAudioTrack) {
      builder.audioTracks = @[self.localAudioTrack];
    }

    builder.roomName = roomName;
  }];

  self.room = [TwilioVideo connectWithOptions:connectOptions delegate:self];
}

RCT_EXPORT_METHOD(disconnect) {
  [self.room disconnect];
}

-(TVIVideoConstraints*) videoConstraints {
  return [TVIVideoConstraints constraintsWithBlock:^(TVIVideoConstraintsBuilder *builder) {
    builder.minSize = TVIVideoConstraintsSize960x540;
    builder.maxSize = TVIVideoConstraintsSize1280x720;
    builder.aspectRatio = TVIAspectRatio16x9;
    builder.minFrameRate = TVIVideoConstraintsFrameRateNone;
    builder.maxFrameRate = TVIVideoConstraintsFrameRateNone;
  }];
}

# pragma mark - TVICameraCapturerDelegate

-(void)cameraCapturerWasInterrupted:(TVICameraCapturer *)capturer {
  [self sendEventWithName:cameraWasInterrupted body:nil];
}

-(void)cameraCapturerPreviewDidStart:(TVICameraCapturer *)capturer {
  [self sendEventWithName:cameraDidStart body:nil];
}

-(void)cameraCapturer:(TVICameraCapturer *)capturer didStopRunningWithError:(NSError *)error {
  [self sendEventWithName:cameraDidStopRunning body:@{ @"error" : error.localizedDescription }];
}

# pragma mark - TVIRoomDelegate

- (void)didConnectToRoom:(TVIRoom *)room {
  NSMutableArray *participants = [NSMutableArray array];
  [self logMessage:[NSString stringWithFormat:@"Connected to room %@ as %@", room.name, room.localParticipant.identity]];

  for (TVIRemoteParticipant *p in room.remoteParticipants) {
    p.delegate = self;
    [participants addObject:[p toJSON]];
  }

  [self sendEventWithName:roomDidConnect body:@{ @"roomName" : room.name , @"participants" : participants }];
}

- (void)room:(TVIRoom *)room didDisconnectWithError:(nullable NSError *)error {
  [self logMessage:[NSString stringWithFormat:@"Disconncted from room %@, error = %@", room.name, error]];
  self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }

  [self sendEventWithName:roomDidDisconnect body:body];
}

- (void)room:(TVIRoom *)room didFailToConnectWithError:(nonnull NSError *)error{
    [self logMessage:[NSString stringWithFormat:@"Failed to connect to room, error = %@", error]];
    self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }

  [self sendEventWithName:roomDidFailToConnect body:body];
}


- (void)room:(TVIRoom *)room participantDidConnect:(TVIRemoteParticipant *)participant {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ connected with %lu audio and %lu video tracks",
                      participant.identity,
                      (unsigned long)[participant.audioTracks count],
                      (unsigned long)[participant.videoTracks count]]];

  participant.delegate = self;

  [self sendEventWithName:roomParticipantDidConnect body:@{ @"roomName": room.name, @"participant": [participant toJSON] }];
}

- (void)room:(TVIRoom *)room participantDidDisconnect:(TVIRemoteParticipant *)participant {
    [self logMessage:[NSString stringWithFormat:@"Room %@ participant %@ disconnected", room.name, participant.identity]];
  [self sendEventWithName:roomParticipantDidDisconnect body:@{ @"roomName": room.name, @"participant": [participant toJSON] }];
}

# pragma mark - TVIRemoteParticipantDelegate

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
      publishedVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
    
    // Remote Participant has offered to share the video Track.
    
    [self logMessage:[NSString stringWithFormat:@"Participant %@ published video track.", participant.identity]];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
    unpublishedVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
    
    // Remote Participant has stopped sharing the video Track.
    
    [self logMessage:[NSString stringWithFormat:@"Participant %@ unpublished video track.", participant.identity]];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
      publishedAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    
    // Remote Participant has offered to share the audio Track.
    
    [self logMessage:[NSString stringWithFormat:@"Participant %@ published audio track.", participant.identity]];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
    unpublishedAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    
    // Remote Participant has stopped sharing the audio Track.
    
    [self logMessage:[NSString stringWithFormat:@"Participant %@ unpublished audio track.", participant.identity]];
}

- (void)subscribedToVideoTrack:(TVIRemoteVideoTrack *)videoTrack
                   publication:(TVIRemoteVideoTrackPublication *)publication
                forParticipant:(TVIRemoteParticipant *)participant {
    
    // We are subscribed to the remote Participant's audio Track. We will start receiving the
    // remote Participant's video frames now.
    
    [self logMessage:[NSString stringWithFormat:@"Subscribed to video track for Participant %@", participant.identity]];
    
      [self sendEventWithName:participantAddedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [videoTrack toJSON] }];
}

- (void)unsubscribedFromVideoTrack:(TVIRemoteVideoTrack *)videoTrack
                       publication:(TVIRemoteVideoTrackPublication *)publication
                    forParticipant:(TVIRemoteParticipant *)participant {
    
    // We are unsubscribed from the remote Participant's video Track. We will no longer receive the
    // remote Participant's video.
    
    [self logMessage:[NSString stringWithFormat:@"Unsubscribed from video track for Participant %@", participant.identity]];
    
    [self sendEventWithName:participantRemovedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [videoTrack toJSON] }];
}

- (void)subscribedToAudioTrack:(TVIRemoteAudioTrack *)audioTrack
                   publication:(TVIRemoteAudioTrackPublication *)publication
                forParticipant:(TVIRemoteParticipant *)participant {
    
    // We are subscribed to the remote Participant's audio Track. We will start receiving the
    // remote Participant's audio now.
    
    [self logMessage:[NSString stringWithFormat:@"Subscribed to audio track for Participant %@", participant.identity]];
    [self sendEventWithName:participantAddedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [audioTrack toJSON] }];
}

- (void)unsubscribedFromAudioTrack:(TVIRemoteAudioTrack *)audioTrack
                       publication:(TVIRemoteAudioTrackPublication *)publication
                    forParticipant:(TVIRemoteParticipant *)participant {
    
    // We are unsubscribed from the remote Participant's audio Track. We will no longer receive the
    // remote Participant's audio.
    
    [self logMessage:[NSString stringWithFormat:@"Unsubscribed from audio track for Participant %@", participant.identity]];
    [self sendEventWithName:participantRemovedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [audioTrack toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
        enabledVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ enabled video track.", participant.identity]];
//TBA      [self sendEventWithName:participantEnabledTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
       disabledVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ disabled video track.", participant.identity]];
//TBA    [self sendEventWithName:participantDisabledTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
        enabledAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ enabled audio track.", participant.identity]];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant
       disabledAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ disabled audio track.", participant.identity]];
}

- (void)failedToSubscribeToAudioTrack:(TVIRemoteAudioTrackPublication *)publication
                                error:(NSError *)error
                       forParticipant:(TVIRemoteParticipant *)participant {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ failed to subscribe to %@ audio track.",
                      participant.identity, publication.trackName]];
}

- (void)failedToSubscribeToVideoTrack:(TVIRemoteVideoTrackPublication *)publication
                                error:(NSError *)error
                       forParticipant:(TVIRemoteParticipant *)participant {
    [self logMessage:[NSString stringWithFormat:@"Participant %@ failed to subscribe to %@ video track.",
                      participant.identity, publication.trackName]];
}

@end
