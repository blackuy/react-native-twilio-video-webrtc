#import "TWVideoModule.h"

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
static NSString* cameraWasInterrumpted        = @"cameraWasInterrumpted";
static NSString* cameraDidStopRunning         = @"cameraDidStopRunning";

@interface TWVideoModule () <TVIParticipantDelegate, TVIRoomDelegate, TVIVideoTrackDelegate, TVICameraCapturerDelegate>

@end

@implementation TWVideoModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[roomDidConnect,
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
           cameraWasInterrumpted];
}


- (instancetype)init
{
  self = [super init];
  if (self) {

    UIView* remoteMediaView = [[UIView alloc] init];
    //remoteMediaView.backgroundColor = [UIColor blueColor];

    //remoteMediaView.translatesAutoresizingMaskIntoConstraints = NO;
    self.remoteMediaView = remoteMediaView;


    UIView* previewView = [[UIView alloc] init];
    //previewView.backgroundColor = [UIColor yellowColor];

    //previewView.translatesAutoresizingMaskIntoConstraints = NO;
    self.previewView = previewView;


    self.localMedia = [[TVILocalMedia alloc] init];
    self.camera = [[TVICameraCapturer alloc] init];
    self.camera.delegate = self;

    self.localVideoTrack = [self.localMedia addVideoTrack:YES
                                                 capturer:self.camera
                                              constraints:[self videoConstraints]
                                                    error:nil];

    self.localAudioTrack = [self.localMedia addAudioTrack:YES];

    if (!self.localVideoTrack) {
      NSLog(@"Failed to add video track");
    } else {
      // Attach view to video track for local preview
      [self.localVideoTrack attach:previewView];
    }


  }
  return self;
}

- (void)dealloc
{
  [self.remoteMediaView removeFromSuperview];
  self.remoteMediaView = nil;

  [self.previewView removeFromSuperview];
  self.previewView = nil;

  self.participant = nil;
  self.localMedia = nil;
  self.camera = nil;
  self.localVideoTrack = nil;
  self.videoClient = nil;
  self.room = nil;
}

RCT_EXPORT_METHOD(flipCamera) {
  if (self.camera.source == TVIVideoCaptureSourceFrontCamera) {
    [self.camera selectSource:TVIVideoCaptureSourceBackCameraWide];
  } else {
    [self.camera selectSource:TVIVideoCaptureSourceFrontCamera];
  }
}

RCT_EXPORT_METHOD(detachVideoTrack) {
    [self.localMedia removeVideoTrack: self.localVideoTrack];
}

RCT_EXPORT_METHOD(attachVideoTrack) {
    self.localVideoTrack = [self.localMedia addVideoTrack:YES
                                                 capturer:self.camera
                                              constraints:[self videoConstraints]
                                                    error:nil];
}

RCT_EXPORT_METHOD(detachAudioTrack) {
    [self.localMedia removeAudioTrack: self.localAudioTrack];
}

RCT_EXPORT_METHOD(attachAudioTrack) {
    [self.localMedia addAudioTrack:true];
}

RCT_EXPORT_METHOD(startCallWithAccessToken:(NSString*)_accessToken
                  roomName:(NSString*)_roomName) {
  [self internalStartCallWithAccessToken:_accessToken roomName:_roomName];
}

RCT_EXPORT_METHOD(disconnect) {
  [self.room disconnect];
}


-(void)internalStartCallWithAccessToken:(NSString*)_accessToken roomName:(NSString*)_roomName{

  // Create a Video Client and connect to Twilio's backend.
  self.videoClient = [TVIVideoClient clientWithToken:_accessToken];
  //self.videoClient.delegate = self;

  // Join a room
  TVIConnectOptions *connectOptions = [TVIConnectOptions optionsWithBlock:^(TVIConnectOptionsBuilder * _Nonnull builder) {

    builder.localMedia = self.localMedia;
    builder.name = _roomName;

  }];

  self.room  = [self.videoClient connectWithOptions:connectOptions delegate:self];

}

- (void)cleanupRemoteParticipant {
  if (self.participant) {
    if ([self.participant.media.videoTracks count] > 0) {
      [self.participant.media.videoTracks[0] detach:self.remoteMediaView];
    }
    self.participant = nil;
  }
}


#pragma mark - Camera Delegate
-(void)cameraCapturerWasInterrupted:(TVICameraCapturer *)capturer{
  [self sendEventWithName:cameraWasInterrumpted body:nil];
}

-(void)cameraCapturerPreviewDidStart:(TVICameraCapturer *)capturer{
  [self sendEventWithName:cameraDidStart body:nil];
}

-(void)cameraCapturer:(TVICameraCapturer *)capturer didStopRunningWithError:(NSError *)error{
  [self sendEventWithName:cameraDidStopRunning body:@{@"error": error.localizedDescription ?: @""}];
}


#pragma mark - TVIRoomDelegate

- (void)didConnectToRoom:(TVIRoom *)room {
  // At the moment, this example only supports rendering one Participant at a time.

  NSLog(@"Connected to room %@ as %@", room.name, room.localParticipant.identity);

  if (room.participants.count > 0) {
    self.participant = room.participants[0];
    self.participant.delegate = self;
  }

  NSMutableArray* participantNames = [[NSMutableArray alloc] init];
  [room.participants enumerateObjectsUsingBlock:^(TVIParticipant * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
    [participantNames addObject:obj.identity ?: @""];
  }];

  [self sendEventWithName:roomDidConnect body:@{@"roomName": room.name ?: @"",
                                                @"participantsNames": participantNames}];
}

- (void)room:(TVIRoom *)room didDisconnectWithError:(nullable NSError *)error {
  NSLog(@"Disconncted from room %@, error = %@", room.name, error);

  [self cleanupRemoteParticipant];
  self.room = nil;

  [self sendEventWithName:roomDidDisconnect body:@{
                                                   @"roomName": room.name ?: @"",
                                                   @"error": error.localizedDescription ?: @""
                                                   }];
}

- (void)room:(TVIRoom *)room didFailToConnectWithError:(nonnull NSError *)error{
  NSLog(@"Failed to connect to room, error = %@", error);

  self.room = nil;

  [self sendEventWithName:roomDidFailToConnect body:@{
                                                   @"roomName": room.name ?: @"",
                                                   @"error": error.localizedDescription ?: @""
                                                   }];
}


// First, we set a Participant Delegate when a Participant first connects:
- (void)room:(TVIRoom *)room participantDidConnect:(TVIParticipant *)participant {
  NSLog(@"Participant did connect:%@",participant.identity);

  if (!self.participant) {
    self.participant = participant;
    self.participant.delegate = self;
  }

  [self sendEventWithName:roomParticipantDidConnect body:@{
                                                       @"roomName": room.name ?: @"",
                                                       @"participantName": participant.identity ?: @""
                                                       }];


}

-(void)room:(TVIRoom *)room participantDidDisconnect:(TVIParticipant *)participant{

  if (self.participant == participant) {
    [self cleanupRemoteParticipant];
  }


  [self sendEventWithName:roomParticipantDidDisconnect body:@{
                                                          @"roomName": room.name ?: @"",
                                                          @"participantName": participant.identity ?: @""
                                                       }];

}


/* In the Participant Delegate, we can respond when the Participant adds a Video
 Track by rendering it on screen:*/

- (void)participant:(TVIParticipant *)participant addedVideoTrack:(TVIVideoTrack *)videoTrack {
  NSLog(@"Participant %@ added video track.", participant.identity);

  if (self.participant == participant) {
    [videoTrack attach:self.remoteMediaView];
  }

  [self sendEventWithName:participantAddedVideoTrack body:@{
                                                            @"participantName": participant.identity ?: @""
                                                            }];
}

- (void)participant:(TVIParticipant *)participant removedVideoTrack:(TVIVideoTrack *)videoTrack {
  NSLog(@"Participant %@ removed video track.", participant.identity);

  if (self.participant == participant) {
    [videoTrack detach:self.remoteMediaView];
  }

  [self sendEventWithName:participantRemovedAudioTrack body:@{
                                                            @"participantName": participant.identity ?: @""
                                                            }];
}

- (void)participant:(TVIParticipant *)participant addedAudioTrack:(TVIAudioTrack *)audioTrack {
  NSLog(@"Participant %@ added audio track.", participant.identity);

  [self sendEventWithName:participantAddedAudioTrack body:@{
                                                            @"participantName": participant.identity ?: @""
                                                            }];
}

- (void)participant:(TVIParticipant *)participant removedAudioTrack:(TVIAudioTrack *)audioTrack {
  NSLog(@"Participant %@ removed audio track.", participant.identity);

  [self sendEventWithName:participantRemovedAudioTrack body:@{
                                                            @"participantName": participant.identity ?: @""
                                                            }];
}

- (void)participant:(TVIParticipant *)participant enabledTrack:(TVITrack *)track {
  NSString *type = @"";
  if ([track isKindOfClass:[TVIAudioTrack class]]) {
    type = @"audio";
  } else {
    type = @"video";
  }
  NSLog(@"Participant %@ enabled %@ track.", participant.identity, type);

  [self sendEventWithName:participantEnabledTrack body:@{
                                                         @"participantName": participant.identity ?: @""
                                                         }];
}

- (void)participant:(TVIParticipant *)participant disabledTrack:(TVITrack *)track {
  NSString *type = @"";
  if ([track isKindOfClass:[TVIAudioTrack class]]) {
    type = @"audio";
  } else {
    type = @"video";
  }
  NSLog(@"Participant %@ disabled %@ track.", participant.identity, type);

  [self sendEventWithName:participantDisabledTrack body:@{
                                                            @"participantName": participant.identity ?: @""
                                                            }];
}

-(TVIVideoConstraints*) videoConstraints {

  return [TVIVideoConstraints constraintsWithBlock:^(TVIVideoConstraintsBuilder * _Nonnull builder) {
    builder.minSize = TVIVideoConstraintsSize960x540;
    builder.maxSize = TVIVideoConstraintsSize1280x720;
    builder.aspectRatio = TVIAspectRatio16x9;
    builder.minFrameRate = TVIVideoConstraintsFrameRateNone;
    builder.maxFrameRate = TVIVideoConstraintsFrameRateNone;
  }];
}

@end
