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


@interface RCTTWVideoModule () <TVIParticipantDelegate, TVIRoomDelegate, TVICameraCapturerDelegate>

@property (strong, nonatomic) TVICameraCapturer *camera;
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
}

- (void)removeLocalView:(TVIVideoView *)view {
  [self.localVideoTrack removeRenderer:view];
}

- (void)removeParticipantView:(TVIVideoView *)view identity:(NSString *)identity  trackId:(NSString *)trackId {
  // TODO: Implement this nicely
}

- (void)addParticipantView:(TVIVideoView *)view identity:(NSString *)identity  trackId:(NSString *)trackId {
  // Lookup for the participant in the room
  for (TVIParticipant *participant in self.room.participants) {
    if ([participant.identity isEqualToString:identity]) {

      // Lookup for the given trackId
      for (TVIVideoTrack *videoTrack in participant.videoTracks) {
        [videoTrack addRenderer:view];
      }
    }
  }
}

RCT_EXPORT_METHOD(startLocalVideo) {
  if ([TVICameraCapturer availableSources].count > 0) {
    self.camera = [[TVICameraCapturer alloc] init];
    self.camera.delegate = self;

    self.localVideoTrack = [TVILocalVideoTrack trackWithCapturer:self.camera enabled:YES constraints:[self videoConstraints]];
  }
}

RCT_EXPORT_METHOD(startLocalAudio) {
  self.localAudioTrack = [TVILocalAudioTrack trackWithOptions:nil enabled:YES];
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
  } else {
    [self.camera selectSource:TVICameraCaptureSourceFrontCamera];
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

  for (TVIParticipant *p in room.participants) {
    p.delegate = self;
    [participants addObject:[p toJSON]];
  }

  [self sendEventWithName:roomDidConnect body:@{ @"roomName" : room.name , @"participants" : participants }];
}

- (void)room:(TVIRoom *)room didDisconnectWithError:(nullable NSError *)error {
  self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }

  [self sendEventWithName:roomDidDisconnect body:body];
}

- (void)room:(TVIRoom *)room didFailToConnectWithError:(nonnull NSError *)error{
  self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }

  [self sendEventWithName:roomDidFailToConnect body:body];
}


- (void)room:(TVIRoom *)room participantDidConnect:(TVIParticipant *)participant {
  participant.delegate = self;

  [self sendEventWithName:roomParticipantDidConnect body:@{ @"roomName": room.name, @"participant": [participant toJSON] }];
}

- (void)room:(TVIRoom *)room participantDidDisconnect:(TVIParticipant *)participant {
  [self sendEventWithName:roomParticipantDidDisconnect body:@{ @"roomName": room.name, @"participant": [participant toJSON] }];
}

# pragma mark - TVIParticipantDelegate

- (void)participant:(TVIParticipant *)participant addedVideoTrack:(TVIVideoTrack *)videoTrack {
  [self sendEventWithName:participantAddedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [videoTrack toJSON] }];
}

- (void)participant:(TVIParticipant *)participant removedVideoTrack:(TVIVideoTrack *)videoTrack {
  [self sendEventWithName:participantRemovedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [videoTrack toJSON] }];
}

- (void)participant:(TVIParticipant *)participant addedAudioTrack:(TVIAudioTrack *)audioTrack {
  [self sendEventWithName:participantAddedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [audioTrack toJSON] }];
}

- (void)participant:(TVIParticipant *)participant removedAudioTrack:(TVIAudioTrack *)audioTrack {
  [self sendEventWithName:participantRemovedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [audioTrack toJSON] }];
}

- (void)participant:(TVIParticipant *)participant enabledTrack:(TVITrack *)track {
  [self sendEventWithName:participantEnabledTrack body:@{ @"participant": [participant toJSON], @"track": [track toJSON] }];
}

- (void)participant:(TVIParticipant *)participant disabledTrack:(TVITrack *)track {
  [self sendEventWithName:participantDisabledTrack body:@{ @"participant": [participant toJSON], @"track": [track toJSON] }];
}

@end
