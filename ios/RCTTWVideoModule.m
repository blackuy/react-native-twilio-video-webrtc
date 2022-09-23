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
static NSString* dominantSpeakerDidChange     = @"onDominantSpeakerDidChange";

static NSString* participantAddedVideoTrack   = @"participantAddedVideoTrack";
static NSString* participantRemovedVideoTrack = @"participantRemovedVideoTrack";
static NSString* participantAddedDataTrack   = @"participantAddedDataTrack";
static NSString* participantRemovedDataTrack   = @"participantRemovedDataTrack";
static NSString* participantAddedAudioTrack   = @"participantAddedAudioTrack";
static NSString* participantRemovedAudioTrack = @"participantRemovedAudioTrack";
static NSString* participantEnabledVideoTrack      = @"participantEnabledVideoTrack";
static NSString* participantDisabledVideoTrack     = @"participantDisabledVideoTrack";
static NSString* participantEnabledAudioTrack      = @"participantEnabledAudioTrack";
static NSString* participantDisabledAudioTrack     = @"participantDisabledAudioTrack";
static NSString* dataTrackMessageReceived     = @"dataTrackMessageReceived";

static NSString* cameraDidStart               = @"cameraDidStart";
static NSString* cameraWasInterrupted         = @"cameraWasInterrupted";
static NSString* cameraInterruptionEnded      = @"cameraInterruptionEnded";
static NSString* cameraDidStopRunning         = @"cameraDidStopRunning";
static NSString* statsReceived                = @"statsReceived";
static NSString* networkQualityLevelsChanged  = @"networkQualityLevelsChanged";

static const CMVideoDimensions kRCTTWVideoAppCameraSourceDimensions = (CMVideoDimensions){900, 720};

static const int32_t kRCTTWVideoCameraSourceFrameRate = 15;

TVIVideoFormat *RCTTWVideoModuleCameraSourceSelectVideoFormatBySize(AVCaptureDevice *device, CMVideoDimensions targetSize) {
    TVIVideoFormat *selectedFormat = nil;
    // Ordered from smallest to largest.
    NSOrderedSet<TVIVideoFormat *> *formats = [TVICameraSource supportedFormatsForDevice:device];

    for (TVIVideoFormat *format in formats) {
        if (format.pixelFormat != TVIPixelFormatYUV420BiPlanarFullRange) {
            continue;
        }
        selectedFormat = format;
        // ^ Select whatever is available until we find one we like and short-circuit
        CMVideoDimensions dimensions = format.dimensions;

        if (dimensions.width >= targetSize.width && dimensions.height >= targetSize.height) {
            break;
        }
    }
    return selectedFormat;
}


@interface RCTTWVideoModule () <TVIRemoteDataTrackDelegate, TVIRemoteParticipantDelegate, TVIRoomDelegate, TVICameraSourceDelegate, TVILocalParticipantDelegate, TVIAppScreenSourceDelegate>

@property (strong, nonatomic) TVICameraSource *camera;
@property (strong, nonatomic) TVILocalVideoTrack* localVideoTrack;
@property (strong, nonatomic) TVILocalAudioTrack* localAudioTrack;
@property (strong, nonatomic) TVILocalDataTrack* localDataTrack;
@property (strong, nonatomic) TVIAppScreenSource *screen;
@property (strong, nonatomic) TVILocalParticipant* localParticipant;
@property (strong, nonatomic) TVIRoom *room;
@property (nonatomic) BOOL listening;

@end

@implementation RCTTWVideoModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (void)dealloc {
  [self clearCameraInstance];
}

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
    participantAddedDataTrack,
    participantRemovedDataTrack,
    participantAddedAudioTrack,
    participantRemovedAudioTrack,
    participantEnabledVideoTrack,
    participantDisabledVideoTrack,
    participantEnabledAudioTrack,
    participantDisabledAudioTrack,
    dataTrackMessageReceived,
    cameraDidStopRunning,
    cameraDidStart,
    cameraWasInterrupted,
    cameraInterruptionEnded,
    statsReceived,
    networkQualityLevelsChanged,
    dominantSpeakerDidChange
  ];
}

- (void)addLocalView:(TVIVideoView *)view {
  if (self.localVideoTrack != nil) {
    [self.localVideoTrack addRenderer:view];
  }
  [self updateLocalViewMirroring:view];
}

- (void)updateLocalViewMirroring:(TVIVideoView *)view {
  if (self.camera && self.camera.device.position == AVCaptureDevicePositionFront) {
    view.mirror = true;
  }
}

- (void)removeLocalView:(TVIVideoView *)view {
  if (self.localVideoTrack != nil) {
    [self.localVideoTrack removeRenderer:view];
  }
}

- (void)removeParticipantView:(TVIVideoView *)view sid:(NSString *)sid trackSid:(NSString *)trackSid {
  // TODO: Implement this nicely
}

- (void)addParticipantView:(TVIVideoView *)view sid:(NSString *)sid trackSid:(NSString *)trackSid {
  // Lookup for the participant in the room
  TVIRemoteParticipant *participant = [self.room getRemoteParticipantWithSid:sid];
  if (participant) {
     for (TVIRemoteVideoTrackPublication *publication in participant.remoteVideoTracks) {
       if ([publication.trackSid isEqualToString:trackSid]) {
         [publication.videoTrack addRenderer:view];
       }
     }
  }
}

RCT_EXPORT_METHOD(changeListenerStatus:(BOOL)value) {
    self.listening = value;
}

RCT_EXPORT_METHOD(setRemoteAudioPlayback:(NSString *)participantSid enabled:(BOOL)enabled) {
    TVIRemoteParticipant *participant = [self.room getRemoteParticipantWithSid:participantSid];
    if (participant) {
        NSArray<TVIRemoteAudioTrackPublication *> *trackPublications = participant.remoteAudioTracks;
        for(TVIRemoteAudioTrackPublication *remoteAudioTrack in trackPublications) {
            [remoteAudioTrack.remoteTrack setPlaybackEnabled:enabled];
        }
    }
}

RCT_EXPORT_METHOD(startLocalVideo) {
  TVICameraSourceOptions *options = [TVICameraSourceOptions optionsWithBlock:^(TVICameraSourceOptionsBuilder * _Nonnull builder) {

  }];
  self.camera = [[TVICameraSource alloc] initWithOptions:options delegate:self];
  if (self.camera == nil) {
      return;
  }
  self.localVideoTrack = [TVILocalVideoTrack trackWithSource:self.camera enabled:NO name:@"camera"];
}

- (void)startCameraCapture:(NSString *)cameraType {
  if (self.camera == nil) {
    return;
  }
  AVCaptureDevice *camera;
    if ([cameraType isEqualToString:@"back"]) {
    camera = [TVICameraSource captureDeviceForPosition:AVCaptureDevicePositionBack];
  } else {
    camera = [TVICameraSource captureDeviceForPosition:AVCaptureDevicePositionFront];
  }

  [self.camera startCaptureWithDevice:camera completion:^(AVCaptureDevice *device,
          TVIVideoFormat *startFormat,
          NSError *error) {
      if (!error) {
          for (TVIVideoView *renderer in self.localVideoTrack.renderers) {
            [self updateLocalViewMirroring:renderer];
          }
          [self sendEventCheckingListenerWithName:cameraDidStart body:nil];
      }
  }];
}

RCT_EXPORT_METHOD(startLocalAudio) {
    self.localAudioTrack = [TVILocalAudioTrack trackWithOptions:nil enabled:YES name:@"microphone"];
}

RCT_EXPORT_METHOD(stopLocalVideo) {
    [self clearCameraInstance];
}

RCT_EXPORT_METHOD(stopLocalAudio) {
  self.localAudioTrack = nil;
}

RCT_EXPORT_METHOD(publishLocalVideo) {
  if(self.localVideoTrack != nil){
    TVILocalParticipant *localParticipant = self.room.localParticipant;
    [localParticipant publishVideoTrack:self.localVideoTrack];
  }
}

RCT_EXPORT_METHOD(publishLocalAudio) {
  TVILocalParticipant *localParticipant = self.room.localParticipant;
  [localParticipant publishAudioTrack:self.localAudioTrack];
}

RCT_EXPORT_METHOD(unpublishLocalVideo) {
  if(self.localVideoTrack != nil){
    TVILocalParticipant *localParticipant = self.room.localParticipant;
    [localParticipant unpublishVideoTrack:self.localVideoTrack];
  }
}

RCT_EXPORT_METHOD(unpublishLocalAudio) {
  TVILocalParticipant *localParticipant = self.room.localParticipant;
  [localParticipant unpublishAudioTrack:self.localAudioTrack];
}

RCT_REMAP_METHOD(setLocalAudioEnabled, enabled:(BOOL)enabled setLocalAudioEnabledWithResolver:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject) {
  [self.localAudioTrack setEnabled:enabled];

  resolve(@(enabled));
}


// set a default for setting local video enabled
- (bool)_setLocalVideoEnabled:(bool)enabled {
    return [self _setLocalVideoEnabled:enabled cameraType:@"front"];
}

- (bool)_setLocalVideoEnabled:(bool)enabled cameraType:(NSString *)cameraType {
  if (self.localVideoTrack != nil) {
      [self.localVideoTrack setEnabled:enabled];
      if (self.camera) {
          if (enabled) {
            [self startCameraCapture:cameraType];
          } else {
            [self clearCameraInstance];
          }
          return enabled;
      }
  }
  return false;
}

RCT_REMAP_METHOD(setLocalVideoEnabled, enabled:(BOOL)enabled setLocalVideoEnabledWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  bool result = [self _setLocalVideoEnabled:enabled];
  resolve(@(result));
}

RCT_EXPORT_METHOD(flipCamera) {
    if (self.camera) {
        AVCaptureDevicePosition position = self.camera.device.position;
        AVCaptureDevicePosition nextPosition = position == AVCaptureDevicePositionFront ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront;
        BOOL mirror = nextPosition == AVCaptureDevicePositionFront;

        AVCaptureDevice *captureDevice = [TVICameraSource captureDeviceForPosition:nextPosition];
        [self.camera selectCaptureDevice:captureDevice completion:^(AVCaptureDevice *device,
                TVIVideoFormat *startFormat,
                NSError *error) {
            if (!error) {
                for (TVIVideoView *renderer in self.localVideoTrack.renderers) {
                    renderer.mirror = mirror;
                }
            }
        }];
  }
}

RCT_EXPORT_METHOD(toggleScreenSharing: (BOOL) value) {
    if (value) {
       TVIAppScreenSourceOptions *options = [TVIAppScreenSourceOptions optionsWithBlock:^(TVIAppScreenSourceOptionsBuilder * _Nonnull builder) {

       }];
       self.screen = [[TVIAppScreenSource alloc] initWithOptions:options delegate:self];
       if (self.screen == nil) {
           return;
       }
       self.localVideoTrack = [TVILocalVideoTrack trackWithSource:self.screen enabled:YES name:@"screen"];
       if(self.localVideoTrack != nil){
         TVILocalParticipant *localParticipant = self.room.localParticipant;
         [localParticipant publishVideoTrack:self.localVideoTrack];
       }
       [self.screen startCapture];    
  } else {
        [self unpublishLocalVideo];
        [self.screen stopCapture];
        self.localVideoTrack = nil;
       }
}


RCT_EXPORT_METHOD(toggleSoundSetup:(BOOL)speaker) {
  NSError *error = nil;
  kTVIDefaultAVAudioSessionConfigurationBlock();
  AVAudioSession *session = [AVAudioSession sharedInstance];
  AVAudioSessionMode mode = speaker ? AVAudioSessionModeVideoChat : AVAudioSessionModeVoiceChat ;
  // Overwrite the audio route
  if (![session setMode:mode error:&error]) {
    NSLog(@"AVAudiosession setMode %@",error);
  }

  if (![session overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:&error]) {
    NSLog(@"AVAudiosession overrideOutputAudioPort %@",error);
  }
}

-(void)convertBaseTrackStats:(TVIBaseTrackStats *)stats result:(NSMutableDictionary *)result {
  result[@"trackSid"] = stats.trackSid;
  result[@"packetsLost"] = @(stats.packetsLost);
  result[@"codec"] = stats.codec;
  result[@"ssrc"] = stats.ssrc;
  result[@"timestamp"] = @(stats.timestamp);
}

-(void)convertRemoteTrackStats:(TVIRemoteTrackStats *)stats result:(NSMutableDictionary *)result {
  result[@"bytesReceived"] = @(stats.bytesReceived);
  result[@"packetsReceived"] = @(stats.packetsReceived);
}

-(void)convertLocalTrackStats:(TVILocalTrackStats *)stats result:(NSMutableDictionary *)result {
  result[@"bytesSent"] = @(stats.bytesSent);
  result[@"packetsSent"] = @(stats.packetsSent);
  result[@"roundTripTime"] = @(stats.roundTripTime);
}

-(NSMutableDictionary*)convertDimensions:(CMVideoDimensions)dimensions {
  NSMutableDictionary *result = [[NSMutableDictionary alloc] initWithCapacity:2];
  result[@"width"] = @(dimensions.width);
  result[@"height"] = @(dimensions.height);
  return result;
}

-(NSMutableDictionary*)convertRemoteAudioTrackStats:(TVIRemoteAudioTrackStats *)stats {
  NSMutableDictionary *result = [[NSMutableDictionary alloc] initWithCapacity:10];
  [self convertBaseTrackStats:stats result:result];
  [self convertRemoteTrackStats:stats result:result];
  result[@"audioLevel"] = @(stats.audioLevel);
  result[@"jitter"] = @(stats.jitter);
  return result;
}

-(NSMutableDictionary*)convertRemoteVideoTrackStats:(TVIRemoteVideoTrackStats *)stats {
  NSMutableDictionary *result = [[NSMutableDictionary alloc] initWithCapacity:10];
  [self convertBaseTrackStats:stats result:result];
  [self convertRemoteTrackStats:stats result:result];
  result[@"dimensions"] = [self convertDimensions:stats.dimensions];
  result[@"frameRate"] = @(stats.frameRate);
  return result;
}

-(NSMutableDictionary*)convertLocalAudioTrackStats:(TVILocalAudioTrackStats *)stats {
  NSMutableDictionary *result = [[NSMutableDictionary alloc] initWithCapacity:10];
  [self convertBaseTrackStats:stats result:result];
  [self convertLocalTrackStats:stats result:result];
  result[@"audioLevel"] = @(stats.audioLevel);
  result[@"jitter"] = @(stats.jitter);
  return result;
}

-(NSMutableDictionary*)convertLocalVideoTrackStats:(TVILocalVideoTrackStats *)stats {
  NSMutableDictionary *result = [[NSMutableDictionary alloc] initWithCapacity:10];
  [self convertBaseTrackStats:stats result:result];
  [self convertLocalTrackStats:stats result:result];
  result[@"dimensions"] = [self convertDimensions:stats.dimensions];
  result[@"frameRate"] = @(stats.frameRate);
  return result;
}

RCT_EXPORT_METHOD(getStats) {
  if (self.room) {
    [self.room getStatsWithBlock:^(NSArray<TVIStatsReport *> * _Nonnull statsReports) {
      NSMutableDictionary *eventBody = [[NSMutableDictionary alloc] initWithCapacity:10];
      for (TVIStatsReport *statsReport in statsReports) {
        NSMutableArray *audioTrackStats = [[NSMutableArray alloc] initWithCapacity:10];
        NSMutableArray *videoTrackStats = [[NSMutableArray alloc] initWithCapacity:10];
        NSMutableArray *localAudioTrackStats = [[NSMutableArray alloc] initWithCapacity:10];
        NSMutableArray *localVideoTrackStats = [[NSMutableArray alloc] initWithCapacity:10];
        for (TVIRemoteAudioTrackStats *stats in statsReport.remoteAudioTrackStats) {
          [audioTrackStats addObject:[self convertRemoteAudioTrackStats:stats]];
        }
        for (TVIRemoteVideoTrackStats *stats in statsReport.remoteVideoTrackStats) {
          [videoTrackStats addObject:[self convertRemoteVideoTrackStats:stats]];
        }
        for (TVILocalAudioTrackStats *stats in statsReport.localAudioTrackStats) {
          [localAudioTrackStats addObject:[self convertLocalAudioTrackStats:stats]];
        }
        for (TVILocalVideoTrackStats *stats in statsReport.localVideoTrackStats) {
          [localVideoTrackStats addObject:[self convertLocalVideoTrackStats:stats]];
        }
        eventBody[statsReport.peerConnectionId] = @{
          @"remoteAudioTrackStats": audioTrackStats,
          @"remoteVideoTrackStats": videoTrackStats,
          @"localAudioTrackStats": localAudioTrackStats,
          @"localVideoTrackStats": localVideoTrackStats
        };
      }
      [self sendEventCheckingListenerWithName:statsReceived body:eventBody];
    }];
  }
}

RCT_EXPORT_METHOD(connect:(NSString *)accessToken roomName:(NSString *)roomName enableAudio:(BOOL *)enableAudio enableVideo:(BOOL *)enableVideo encodingParameters:(NSDictionary *)encodingParameters enableNetworkQualityReporting:(BOOL *)enableNetworkQualityReporting dominantSpeakerEnabled:(BOOL *)dominantSpeakerEnabled cameraType:(NSString *)cameraType) {
  [self _setLocalVideoEnabled:enableVideo cameraType:cameraType];
  if (self.localAudioTrack) {
    [self.localAudioTrack setEnabled:enableAudio];
  }

  TVIConnectOptions *connectOptions = [TVIConnectOptions optionsWithToken:accessToken block:^(TVIConnectOptionsBuilder * _Nonnull builder) {
    if (self.localVideoTrack) {
      builder.videoTracks = @[self.localVideoTrack];
    }

    if (self.localAudioTrack) {
      builder.audioTracks = @[self.localAudioTrack];
    }

    self.localDataTrack = [TVILocalDataTrack track];

    if (self.localDataTrack) {
      builder.dataTracks = @[self.localDataTrack];
    }
      
    builder.dominantSpeakerEnabled = dominantSpeakerEnabled ? YES : NO;

    builder.roomName = roomName;

    if(encodingParameters[@"enableH264Codec"]){
      builder.preferredVideoCodecs = @[ [TVIH264Codec new] ];
    }

    if(encodingParameters[@"audioBitrate"] || encodingParameters[@"videoBitrate"]){
      NSInteger audioBitrate = [encodingParameters[@"audioBitrate"] integerValue];
      NSInteger videoBitrate = [encodingParameters[@"videoBitrate"] integerValue];
      builder.encodingParameters = [[TVIEncodingParameters alloc] initWithAudioBitrate:(audioBitrate) ? audioBitrate : 40 videoBitrate:(videoBitrate) ? videoBitrate : 1500];
    }

    if (enableNetworkQualityReporting) {
      builder.networkQualityEnabled = true;
      builder.networkQualityConfiguration = [ [TVINetworkQualityConfiguration alloc] initWithLocalVerbosity:TVINetworkQualityVerbosityMinimal remoteVerbosity:TVINetworkQualityVerbosityMinimal];
    }

  }];

  self.room = [TwilioVideoSDK connectWithOptions:connectOptions delegate:self];
}

RCT_EXPORT_METHOD(sendString:(nonnull NSString *)message) {
    [self.localDataTrack sendString:message];
    //NSData *data = [message dataUsingEncoding:NSUTF8StringEncoding];
    //[self.localDataTrack sendString:message];
}

RCT_EXPORT_METHOD(disconnect) {
  [self clearCameraInstance];
  [self.room disconnect];
}

- (void)clearCameraInstance {
    // We are done with camera
    if (self.camera) {
        [self.camera stopCapture];
    }
}

# pragma mark - Common

-(void)sendEventCheckingListenerWithName:(NSString *)event body:(NSDictionary *)body {
    if (_listening) {
        [self sendEventWithName:event body:body];
    }
}

# pragma mark - TVICameraSourceDelegate


- (void)cameraSourceWasInterrupted:(nonnull TVICameraSource *)source reason:(AVCaptureSessionInterruptionReason)reason  API_AVAILABLE(ios(9.0)){
    NSString *reasonStr = @"unknown";
    if (@available(iOS 9.0, *)) {
        if (reason == AVCaptureSessionInterruptionReasonVideoDeviceNotAvailableInBackground) {
            reasonStr = @"video device not available in background";
        } else if (reason == AVCaptureSessionInterruptionReasonAudioDeviceInUseByAnotherClient) {
            reasonStr = @"audio device in use by another client";
        } else if (reason == AVCaptureSessionInterruptionReasonVideoDeviceInUseByAnotherClient) {
            reasonStr = @"video device in use by another client";
        } else if (reason == AVCaptureSessionInterruptionReasonVideoDeviceNotAvailableWithMultipleForegroundApps) {
            reasonStr = @"video device not available with multiple foreground apps";
        }
    }
    if (@available(iOS 11.1, *)) {
        if (reason == AVCaptureSessionInterruptionReasonVideoDeviceNotAvailableDueToSystemPressure) {
            reasonStr = @"video device not available due to system pressure";
        }
    }

    [self sendEventCheckingListenerWithName:cameraWasInterrupted body:@{@"reason" : reasonStr }];
}

- (void)cameraSourceInterruptionEnded:(nonnull TVICameraSource *)source {
    [self sendEventCheckingListenerWithName:cameraInterruptionEnded body:nil];
}

-(void)cameraSource:(nonnull TVICameraSource *)source didFailWithError:(nonnull NSError *)error {
  [self sendEventCheckingListenerWithName:cameraDidStopRunning body:@{ @"error" : error.localizedDescription }];
}

# pragma mark - TVIRoomDelegate

- (void)room:(TVIRoom *)room dominantSpeakerDidChange :(TVIRemoteParticipant *)participant {
    if (participant) {
        [self sendEventCheckingListenerWithName:dominantSpeakerDidChange body:@{ @"participant" : [participant toJSON], @"roomName" : room.name , @"roomSid": room.sid }];
    } else {
        [self sendEventCheckingListenerWithName:dominantSpeakerDidChange body:@{ @"participant" : @"", @"roomName" : room.name , @"roomSid": room.sid, }];
    }
}

- (void)didConnectToRoom:(TVIRoom *)room {
  NSMutableArray *participants = [NSMutableArray array];

  for (TVIRemoteParticipant *p in room.remoteParticipants) {
    p.delegate = self;
    [participants addObject:[p toJSON]];
  }
  self.localParticipant = room.localParticipant;
  self.localParticipant.delegate = self;

  [participants addObject:[self.localParticipant toJSON]];
  [self sendEventCheckingListenerWithName:roomDidConnect body:@{ @"roomName" : room.name , @"roomSid": room.sid, @"participants" : participants, @"localParticipant" : [self.localParticipant toJSON] }];

}

- (void)room:(TVIRoom *)room didDisconnectWithError:(nullable NSError *)error {
  self.localDataTrack = nil;
  self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name, @"roomSid": room.sid } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }
    [self sendEventCheckingListenerWithName:roomDidDisconnect body:body];
}

- (void)room:(TVIRoom *)room didFailToConnectWithError:(nonnull NSError *)error{
  self.localDataTrack = nil;
  self.room = nil;

  NSMutableDictionary *body = [@{ @"roomName": room.name, @"roomSid": room.sid } mutableCopy];

  if (error) {
    [body addEntriesFromDictionary:@{ @"error" : error.localizedDescription }];
  }

  [self sendEventCheckingListenerWithName:roomDidFailToConnect body:body];
}


- (void)room:(TVIRoom *)room participantDidConnect:(TVIRemoteParticipant *)participant {
  participant.delegate = self;

  [self sendEventCheckingListenerWithName:roomParticipantDidConnect body:@{ @"roomName": room.name, @"roomSid": room.sid, @"participant": [participant toJSON] }];
}

- (void)room:(TVIRoom *)room participantDidDisconnect:(TVIRemoteParticipant *)participant {
  [self sendEventCheckingListenerWithName:roomParticipantDidDisconnect body:@{ @"roomName": room.name, @"roomSid": room.sid, @"participant": [participant toJSON] }];
}

# pragma mark - TVIRemoteParticipantDelegate

- (void)didSubscribeToDataTrack:(TVIRemoteDataTrack *)dataTrack publication:(TVIRemoteDataTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    dataTrack.delegate = self;
    [self sendEventCheckingListenerWithName:participantAddedDataTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)didUnsubscribeFromDataTrack:(TVIRemoteVideoTrack *)videoTrack publication:(TVIRemoteVideoTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    [self sendEventCheckingListenerWithName:participantRemovedDataTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)didSubscribeToVideoTrack:(TVIRemoteVideoTrack *)videoTrack publication:(TVIRemoteVideoTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    [self sendEventCheckingListenerWithName:participantAddedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)didUnsubscribeFromVideoTrack:(TVIRemoteVideoTrack *)videoTrack publication:(TVIRemoteVideoTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    [self sendEventCheckingListenerWithName:participantRemovedVideoTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)didSubscribeToAudioTrack:(TVIRemoteAudioTrack *)audioTrack publication:(TVIRemoteAudioTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    [self sendEventCheckingListenerWithName:participantAddedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)didUnsubscribeFromAudioTrack:(TVIRemoteAudioTrack *)audioTrack publication:(TVIRemoteAudioTrackPublication *)publication forParticipant:(TVIRemoteParticipant *)participant {
    [self sendEventCheckingListenerWithName:participantRemovedAudioTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant didEnableVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
  [self sendEventCheckingListenerWithName:participantEnabledVideoTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant didDisableVideoTrack:(TVIRemoteVideoTrackPublication *)publication {
  [self sendEventCheckingListenerWithName:participantDisabledVideoTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant didEnableAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    [self sendEventCheckingListenerWithName:participantEnabledAudioTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(TVIRemoteParticipant *)participant didDisableAudioTrack:(TVIRemoteAudioTrackPublication *)publication {
    [self sendEventCheckingListenerWithName:participantDisabledAudioTrack body:@{ @"participant": [participant toJSON], @"track": [publication toJSON] }];
}

- (void)remoteParticipant:(nonnull TVIRemoteParticipant *)participant networkQualityLevelDidChange:(TVINetworkQualityLevel)networkQualityLevel {
    [self sendEventCheckingListenerWithName:networkQualityLevelsChanged body:@{ @"participant": [participant toJSON], @"isLocalUser": [NSNumber numberWithBool:false], @"quality": [NSNumber numberWithInt:(int)networkQualityLevel]}];
}

# pragma mark - TVIRemoteDataTrackDelegate

- (void)remoteDataTrack:(nonnull TVIRemoteDataTrack *)remoteDataTrack didReceiveString:(nonnull NSString *)message {
    [self sendEventCheckingListenerWithName:dataTrackMessageReceived body:@{ @"message": message, @"trackSid": remoteDataTrack.sid }];
}

- (void)remoteDataTrack:(nonnull TVIRemoteDataTrack *)remoteDataTrack didReceiveData:(nonnull NSData *)message {
    // TODO: Handle didReceiveData
    NSLog(@"DataTrack didReceiveData");
}

# pragma mark - TVILocalParticipantDelegate

- (void)localParticipant:(nonnull TVILocalParticipant *)participant networkQualityLevelDidChange:(TVINetworkQualityLevel)networkQualityLevel {
    [self sendEventCheckingListenerWithName:networkQualityLevelsChanged body:@{ @"participant": [participant toJSON], @"isLocalUser": [NSNumber numberWithBool:true], @"quality": [NSNumber numberWithInt:(int)networkQualityLevel]}];
}

@end

