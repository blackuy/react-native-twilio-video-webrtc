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

static NSString* gotStats               = @"gotStats";
static NSString* cameraDidStart               = @"cameraDidStart";
static NSString* cameraWasInterrupted        = @"cameraWasInterrupted";
static NSString* cameraDidStopRunning         = @"cameraDidStopRunning";


@interface RCTTWVideoModule () <TVIParticipantDelegate, TVIRoomDelegate, TVICameraCapturerDelegate>

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
    cameraWasInterrupted,
    gotStats
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
  for (TVIParticipant *participant in self.room.participants) {
    if ([participant.identity isEqualToString:identity]) {

      // Lookup for the given trackId
      for (TVIVideoTrack *videoTrack in participant.videoTracks) {
        [videoTrack addRenderer:view];
      }
    }
  }
}
RCT_EXPORT_METHOD(getStats) {
    NSLog(@"GET STATS");
    [self.room getStatsWithBlock:^(NSArray<TVIStatsReport *> * _Nonnull statsReports) {
        NSNumberFormatter *f = [[NSNumberFormatter alloc] init];
        f.numberStyle = NSNumberFormatterDecimalStyle;


        NSMutableDictionary *condensedStats = [[NSMutableDictionary alloc]initWithCapacity:10];
        NSMutableDictionary *incomingAudioTracks = [[NSMutableDictionary alloc]initWithCapacity:10];
        NSMutableDictionary *incomingVideoTracks = [[NSMutableDictionary alloc]initWithCapacity:10];
        NSMutableDictionary *localVideoTracks = [[NSMutableDictionary alloc]initWithCapacity:10];
        NSMutableDictionary *localAudioTracks = [[NSMutableDictionary alloc]initWithCapacity:10];

               for (id statReport in statsReports) {
                   if([statReport peerConnectionId]){
                   NSLog(@"%@",[statReport peerConnectionId]);
                       [condensedStats setObject:[statReport peerConnectionId] forKey:@"peerConnectionId"];
                   }



                   if([statReport localAudioTrackStats]){
                       NSArray *audioTracks = [statReport localAudioTrackStats];
                       for (id audioTrack in audioTracks) {
                           NSMutableDictionary *outgoingTrack= [[NSMutableDictionary alloc]initWithCapacity:10];
                           NSString *trackId = nil;
                           if([audioTrack roundTripTime]){
                               NSNumber *roundTripTime= [f  numberFromString:[NSString stringWithFormat:@"%llu", [audioTrack roundTripTime]]];
                               [outgoingTrack setObject:roundTripTime forKey:@"roundTripTime"];
                           }
                           if([audioTrack audioLevel]){
                               NSNumber *audioLevel= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[audioTrack audioLevel]]];
                               [outgoingTrack setObject:audioLevel forKey:@"audioLevel"];
                           }
                           if([audioTrack jitter]){
                               NSNumber *jitter= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack jitter]]];
                               [outgoingTrack setObject:jitter forKey:@"jitter"];
                           }
                           if([audioTrack codec]){
                               NSString *codec = [NSString stringWithFormat:@"%@", [audioTrack codec]];
                               [outgoingTrack setObject:codec forKey:@"codec"];
                           }

                           if([audioTrack ssrc]){
                               NSNumber *ssrc= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack jitter]]];
                               [outgoingTrack setObject:ssrc forKey:@"ssrc"];
                           }
                           if([audioTrack trackId]){
                               trackId = [NSString stringWithFormat:@"%@", [audioTrack trackId]];
                               [outgoingTrack setObject:trackId forKey:@"trackId"];
                           }

                           if([audioTrack packetsSent]){
                               NSNumber *packetsSent= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack packetsSent]]];
                               [outgoingTrack setObject:packetsSent forKey:@"packetsSent"];
                           }

                           if([audioTrack packetsLost]){
                               NSNumber *packetsLost= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack packetsLost]]];
                               [outgoingTrack setObject:packetsLost forKey:@"packetsLost"];
                           }
                           if([audioTrack valueForKey:@"timestamp"]){
                               NSNumber *timestamp= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[audioTrack valueForKey:@"timestamp"]]];
                               [outgoingTrack setObject:timestamp forKey:@"timestamp"];
                           }

                           [localAudioTracks setObject:outgoingTrack forKey:trackId];


                       }
                   }






                   if([statReport localVideoTrackStats]){
                       NSArray *videoTracks = [statReport localVideoTrackStats];
                       for (id videoTrack in videoTracks) {

                           NSMutableDictionary *outgoingTrack= [[NSMutableDictionary alloc]initWithCapacity:10];
                           NSString *trackId = nil;



                           if([videoTrack bytesSent]){
                               NSNumber *bytesSent= [f  numberFromString:[NSString stringWithFormat:@"%llu", [videoTrack bytesSent]]];

                               [outgoingTrack setObject:bytesSent forKey:@"bytesSent"];
                           }
                           if([videoTrack roundTripTime]){
                               NSNumber *roundTripTime= [f  numberFromString:[NSString stringWithFormat:@"%llu", [videoTrack roundTripTime]]];
                                [outgoingTrack setObject:roundTripTime forKey:@"roundTripTime"];
                           }
                           if([videoTrack codec]){
                               NSString *codec = [NSString stringWithFormat:@"%@", [videoTrack codec]];
                               [outgoingTrack setObject:codec forKey:@"codec"];
                           }
                           if([videoTrack frameRate]){
                                  NSNumber *frameRate= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack frameRate]]];

                               [outgoingTrack setObject:frameRate forKey:@"frameRate"];
                           }
                           if([videoTrack trackId]){
                               trackId = [NSString stringWithFormat:@"%@", [videoTrack trackId]];
                               [outgoingTrack setObject:trackId forKey:@"trackId"];
                           }

                           if([videoTrack packetsSent]){
                               NSNumber *packetsSent= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack packetsSent]]];
                               [outgoingTrack setObject:packetsSent forKey:@"packetsSent"];
                           }

                           if([videoTrack packetsLost]){
                               NSNumber *packetsLost= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack packetsLost]]];
                               [outgoingTrack setObject:packetsLost forKey:@"packetsLost"];
                           }

                           if([videoTrack valueForKey:@"dimensions"]){
                               CMVideoDimensions dimensions =  [videoTrack dimensions];

                               NSNumber *height= [f  numberFromString:[NSString stringWithFormat:@"%d", dimensions.height]];

                               [outgoingTrack setObject:height forKey:@"height"];

                               NSNumber *width= [f  numberFromString:[NSString stringWithFormat:@"%d", dimensions.width]];

                               [outgoingTrack setObject:width forKey:@"width"];

                           }

                           if([videoTrack valueForKey:@"timestamp"]){

                               NSNumber *timestamp= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack valueForKey:@"timestamp"]]];

                               [outgoingTrack setObject:timestamp forKey:@"timestamp"];
                           }

                           [localVideoTracks setObject:outgoingTrack forKey:trackId];


                       }
                   }


                   if([statReport audioTrackStats]){
                       NSArray *audioTracks = [statReport audioTrackStats];
                       for (id audioTrack in audioTracks) {
                           NSString *trackId = nil;
                           NSMutableDictionary *incomingTrack= [[NSMutableDictionary alloc]initWithCapacity:10];

                           if([audioTrack bytesReceived]){
                               NSNumber *bytesReceived= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[audioTrack bytesReceived]]];

                               [incomingTrack setObject:bytesReceived forKey:@"bytesReceived"];
                           }
                           if([audioTrack audioLevel]){
                               NSNumber *audioLevel= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[audioTrack audioLevel]]];
                               [incomingTrack setObject:audioLevel forKey:@"audioLevel"];
                           }
                           if([audioTrack jitter]){
                               NSNumber *jitter= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack jitter]]];
                               [incomingTrack setObject:jitter forKey:@"jitter"];
                           }
                           if([audioTrack codec]){
                               NSString *codec = [NSString stringWithFormat:@"%@", [audioTrack codec]];
                               [incomingTrack setObject:codec forKey:@"codec"];
                           }

                           if([audioTrack ssrc]){
                               NSNumber *ssrc= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack jitter]]];
                               [incomingTrack setObject:ssrc forKey:@"ssrc"];
                           }
                           if([audioTrack trackId]){
                               trackId = [NSString stringWithFormat:@"%@", [audioTrack trackId]];
                               [incomingTrack setObject:trackId forKey:@"trackId"];
                           }

                           if([audioTrack packetsReceived]){
                               NSNumber *packetsReceived= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack packetsReceived]]];
                               [incomingTrack setObject:packetsReceived forKey:@"packetsSent"];
                           }

                           if([audioTrack packetsLost]){
                               NSNumber *packetsLost= [f  numberFromString:[NSString stringWithFormat:@"%lu", [audioTrack packetsLost]]];
                               [incomingTrack setObject:packetsLost forKey:@"packetsLost"];
                           }
                           if([audioTrack valueForKey:@"timestamp"]){
                               NSNumber *timestamp= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[audioTrack valueForKey:@"timestamp"]]];
                               [incomingTrack setObject:timestamp forKey:@"timestamp"];
                           }






                           [incomingAudioTracks setObject:incomingTrack forKey:trackId];

                       }
                   }



                   if([statReport videoTrackStats]){
                       NSArray *videoStats = [statReport videoTrackStats];
                       for (id videoTrack in videoStats) {
                           NSString *trackId = nil;
                           NSMutableDictionary *incomingTrack= [[NSMutableDictionary alloc]initWithCapacity:10];


                           if([videoTrack bytesReceived]){
                               NSNumber *bytesReceived= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack bytesReceived]]];

                              [incomingTrack setObject:bytesReceived forKey:@"bytesReceived"];
                           }

                           if([videoTrack codec]){
                               NSString *codec = [NSString stringWithFormat:@"%@", [videoTrack codec]];
                               [incomingTrack setObject:codec forKey:@"codec"];
                           }
                           if([videoTrack frameRate]){
                               NSNumber *frameRate= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack frameRate]]];

                               [incomingTrack setObject:frameRate forKey:@"frameRate"];
                           }
                           if([videoTrack trackId]){
                               trackId = [NSString stringWithFormat:@"%@", [videoTrack trackId]];
                               [incomingTrack setObject:trackId forKey:@"trackId"];
                           }

                           if([videoTrack packetsReceived]){
                               NSNumber *packetsReceived= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack packetsReceived]]];
                               [incomingTrack setObject:packetsReceived forKey:@"packetsReceived"];
                           }

                           if([videoTrack packetsLost]){

                               NSNumber *packetsLost= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack packetsLost]]];
                               [incomingTrack setObject:packetsLost forKey:@"packetsLost"];
                           }

                           if([videoTrack valueForKey:@"dimensions"]){
                               CMVideoDimensions dimensions =  [videoTrack dimensions];

                               NSNumber *height= [f  numberFromString:[NSString stringWithFormat:@"%d", dimensions.height]];

                               [incomingTrack setObject:height forKey:@"height"];

                               NSNumber *width= [f  numberFromString:[NSString stringWithFormat:@"%d", dimensions.width]];

                               [incomingTrack setObject:width forKey:@"width"];

                           }

                           if([videoTrack valueForKey:@"timestamp"]){

                               NSNumber *timestamp= [f  numberFromString:[NSString stringWithFormat:@"%lu", (unsigned long)[videoTrack valueForKey:@"timestamp"]]];

                               [incomingTrack setObject:timestamp forKey:@"timestamp"];
                           }




                           [incomingVideoTracks setObject:incomingTrack forKey:trackId];

                       }
                   }


               }
        [condensedStats setObject:incomingVideoTracks forKey:@"remoteVideoTracks"];
        [condensedStats setObject:incomingAudioTracks forKey:@"remoteAudioTracks"];

        [condensedStats setObject:localVideoTracks forKey:@"localVideoTracks"];
        [condensedStats setObject:localAudioTracks forKey:@"localAudioTracks"];


        [self sendEventWithName:gotStats body:condensedStats];

    }];
}

RCT_EXPORT_METHOD(startLocalVideo:(BOOL)screenShare constraints:(NSDictionary *)constraints) {
  if (screenShare) {
    UIViewController *rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;
    self.screen = [[TVIScreenCapturer alloc] initWithView:rootViewController.view];

    self.localVideoTrack = [TVILocalVideoTrack trackWithCapturer:self.screen enabled:YES constraints:[self videoConstraints:constraints]];
  } else if ([TVICameraCapturer availableSources].count > 0) {
    self.camera = [[TVICameraCapturer alloc] init];
    self.camera.delegate = self;

    self.localVideoTrack = [TVILocalVideoTrack trackWithCapturer:self.camera enabled:YES constraints:[self videoConstraints:constraints]];
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



-(TVIVideoConstraints*) videoConstraints:(NSDictionary *)constraints {
  return [TVIVideoConstraints constraintsWithBlock:^(TVIVideoConstraintsBuilder *builder) {
    builder.minSize = TVIVideoConstraintsSize352x288;
    builder.maxSize = TVIVideoConstraintsSize352x288;
    builder.aspectRatio = TVIAspectRatio11x9;
    builder.minFrameRate = TVIVideoConstraintsFrameRate10;
    builder.maxFrameRate = TVIVideoConstraintsFrameRate15;
  }];
}




-(TVIVideoConstraints*) old_videoConstraints:(NSDictionary *)constraints {
  return [TVIVideoConstraints constraintsWithBlock:^(TVIVideoConstraintsBuilder *builder) {
    NSString *aspectRatio = constraints[@"aspectRatio"];
    int32_t minWidth = [constraints[@"minWidth"] intValue];
    int32_t maxWidth = [constraints[@"maxWidth"] intValue];
    NSUInteger minFrameRate = [constraints[@"minFrameRate"] intValue];
    NSUInteger maxFrameRate = [constraints[@"maxFrameRate"] intValue];

    CMVideoDimensions minDimensions;
    minDimensions.width = minWidth;
    minDimensions.height = [aspectRatio isEqualToString:@"4:3"]
      ? minWidth * (3 / 4)
      : minWidth * (9 / 16);

    CMVideoDimensions maxDimensions;
    maxDimensions.width = maxWidth;
    maxDimensions.height = [aspectRatio isEqualToString:@"4:3"]
      ? maxWidth * (3 / 4)
      : maxWidth * (9 / 16);

    builder.minSize = minDimensions;
    builder.maxSize = maxDimensions;
    builder.aspectRatio = [aspectRatio isEqualToString:@"4:3"]
      ? TVIAspectRatio4x3
      : TVIAspectRatio16x9;
    builder.minFrameRate = minFrameRate == 0
      ? TVIVideoConstraintsFrameRateNone
      : minFrameRate;
    builder.maxFrameRate = maxFrameRate == 0
      ? TVIVideoConstraintsFrameRateNone
      : maxFrameRate;
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
