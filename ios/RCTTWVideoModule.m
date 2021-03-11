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


TVIVideoFormat *RCTTWVideoModuleCameraSourceSelectVideoFormatBySize(AVCaptureDevice *device, CMVideoDimensions targetSize, NSUInteger targetFps) {
    TVIVideoFormat *selectedFormat = nil;
    // Ordered from smallest to largest.
    NSOrderedSet<TVIVideoFormat *> *formats = [TVICameraSource supportedFormatsForDevice:device];

    for (TVIVideoFormat *format in formats) {
        if (format.pixelFormat != TVIPixelFormatYUV420BiPlanarFullRange) {
            continue;
        }
        
        selectedFormat = format;
        
        // ^ Select whatever is available until we find one we like and break the loop
        CMVideoDimensions dimensions = format.dimensions;
        NSUInteger fps = format.frameRate;

        if (dimensions.width >= targetSize.width && dimensions.height >= targetSize.height && fps >= targetFps) {
            break;
        }
    }
    return selectedFormat;
}


@interface RCTTWVideoModule () <TVIRemoteDataTrackDelegate, TVIRemoteParticipantDelegate, TVIRoomDelegate, TVICameraSourceDelegate, TVILocalParticipantDelegate>

@property (strong, nonatomic) TVICameraSource *camera;
@property (strong, nonatomic) TVILocalVideoTrack* localVideoTrack;
@property (strong, nonatomic) TVILocalAudioTrack* localAudioTrack;
@property (strong, nonatomic) TVILocalDataTrack* localDataTrack;
@property (strong, nonatomic) TVILocalParticipant* localParticipant;
@property (strong, nonatomic) TVIRoom *room;
@property (nonatomic) BOOL listening;

@end

@implementation RCTTWVideoModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (void)dealloc {
  [self clearCameraInstance];
  [self stopLocalAudio];
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

- (void)startCameraCapture {
  if (self.camera == nil) {
    return;
  }
  AVCaptureDevice *camera = [TVICameraSource captureDeviceForPosition:AVCaptureDevicePositionFront];
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

- (bool)_setLocalVideoEnabled:(bool)enabled {
  if (self.localVideoTrack != nil) {
      [self.localVideoTrack setEnabled:enabled];
      if (self.camera) {
          if (enabled) {
            [self startCameraCapture];
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

RCT_EXPORT_METHOD(setTrackPriority:(NSString *)trackSid trackPriority:(NSString *)trackPriority) {
    for (TVIRemoteParticipant *participant in [self.room remoteParticipants]) {
        if (participant) {
            for (TVIRemoteVideoTrackPublication *publication in participant.remoteVideoTracks) {
              if ([publication.trackSid isEqualToString:trackSid]) {
                  TVITrackPriority priority = [self parsePriorityString:trackPriority];
                  [publication.remoteTrack setPriority:priority];
              }
            }
        }
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

-(TVITrackPriority)parsePriorityString:(NSString *)priority {
    if (priority == nil) {
        return nil;
    }
    
    if ([[priority uppercaseString] isEqualToString:@"LOW"]) {
        return TVITrackPriorityLow;
    } else if ([[priority uppercaseString] isEqualToString:@"STANDARD"]) {
        return TVITrackPriorityStandard;
    } else if ([[priority uppercaseString] isEqualToString:@"HIGH"]) {
        return TVITrackPriorityHigh;
    } else if ([[priority uppercaseString] isEqualToString:@"NULL"]) {
        return nil;
    }
    return nil;
}

-(TVIVideoDimensions*)parseDimensionString:(NSString *)dimension {
    if (dimension == nil) {
        return nil;
    }
    
    NSArray* dimensionArray = [dimension componentsSeparatedByString:@"x"];
    if ([dimensionArray count] != 2) {
        NSLog(@"Malformed dimension. Ignoring: %@", dimension);
        return nil;
    }
    
    unsigned int width = [[dimensionArray objectAtIndex:0] unsignedIntValue];
    unsigned int height = [[dimensionArray objectAtIndex:1] unsignedIntValue];
    
    return [TVIVideoDimensions dimensionsWithWidth:width height:height];
}

-(TVIVideoBandwidthProfileOptions*)prepareBandwidthProfile:(NSDictionary *)bandwidthProfileOptions {
    return [TVIVideoBandwidthProfileOptions optionsWithBlock:^(TVIVideoBandwidthProfileOptionsBuilder * _Nonnull builder) {
    
        if (bandwidthProfileOptions[@"mode"]) {
            TVIBandwidthProfileMode mode;
            
            NSString *comparisonString = [(NSString *)[bandwidthProfileOptions objectForKey:@"mode"] uppercaseString];
            
            if ([comparisonString isEqualToString:@"GRID"]) {
                mode = TVIBandwidthProfileModeGrid;
            } else if ([comparisonString isEqualToString:@"COLLABORATION"]) {
                mode = TVIBandwidthProfileModeCollaboration;
            } else if ([comparisonString isEqualToString:@"PRESENTATION"]) {
                mode = TVIBandwidthProfileModePresentation;
            }
            
            NSLog(@"BandwidthProfile - mode: %@", mode);
            builder.mode = mode;
        }
        
        if (bandwidthProfileOptions[@"trackSwitchOffMode"]) {
            TVITrackSwitchOffMode mode;
            
            NSString *comparisonString = [(NSString *)[bandwidthProfileOptions objectForKey:@"trackSwitchOffMode"] uppercaseString];
            
            if ([comparisonString isEqualToString:@"DISABLED"]) {
                mode = TVITrackSwitchOffModeDisabled;
            } else if ([comparisonString isEqualToString:@"PREDICTED"]) {
                mode = TVITrackSwitchOffModePredicted;
            } else if ([comparisonString isEqualToString:@"DETECTED"]) {
                mode = TVITrackSwitchOffModeDetected;
            }
            
            builder.trackSwitchOffMode = mode;
            NSLog(@"BandwidthProfile - trackSwitchOffMode: %@", mode);
        }
        
        if (bandwidthProfileOptions[@"maxTracks"]) {
            NSNumber *numberValue = @([bandwidthProfileOptions[@"maxTracks"] integerValue]);
            
            if (numberValue > 0) {
                builder.maxTracks = numberValue;
                NSLog(@"BandwidthProfile - maxTracks: %@", numberValue);
            } else {
                NSLog(@"maxTracks cant be less than 1. Ignoring.");
            }
        }
        
        if (bandwidthProfileOptions[@"maxSubscriptionBitrate"]) {
            NSNumber *numberValue = @([bandwidthProfileOptions[@"maxSubscriptionBitrate"] integerValue]);
            
            if (numberValue > 0) {
                builder.maxSubscriptionBitrate = numberValue;
                NSLog(@"BandwidthProfile - maxSubscriptionBitrate: %@", numberValue);
            } else {
                NSLog(@"maxSubscriptionBitrate cant be less than 1. Ignoring.");
            }
        }
        
        if (bandwidthProfileOptions[@"dominantSpeakerPriority"]) {
            builder.dominantSpeakerPriority = [self parsePriorityString:(NSString *)[bandwidthProfileOptions objectForKey:@"dominantSpeakerPriority"]];
            NSLog(@"BandwidthProfile - dominantSpeakerPriority: %@", builder.dominantSpeakerPriority);
        }
        
        if (bandwidthProfileOptions[@"renderDimensions"]) {
            NSDictionary *renderDimensionsDict = [bandwidthProfileOptions objectForKey:@"renderDimensions"];
            
            TVIVideoRenderDimensions *dimensions = [TVIVideoRenderDimensions alloc];
            
            if (renderDimensionsDict[@"low"]) {
                dimensions.low = [self parseDimensionString:(NSString *)[renderDimensionsDict objectForKey:@"low"]];
                NSLog(@"BandwidthProfile - renderDimensions - low: %lux%lu", (unsigned long)dimensions.low.width, (unsigned long)dimensions.low.height);
            }
            
            if (renderDimensionsDict[@"standard"]) {
                dimensions.standard = [self parseDimensionString:(NSString *)[renderDimensionsDict objectForKey:@"standard"]];
                NSLog(@"BandwidthProfile - renderDimensions - standard: %lux%lu", (unsigned long)dimensions.standard.width, (unsigned long)dimensions.standard.height);
            }
            
            if (renderDimensionsDict[@"high"]) {
                dimensions.high = [self parseDimensionString:(NSString *)[renderDimensionsDict objectForKey:@"high"]];
                NSLog(@"BandwidthProfile - renderDimensions - high: %lux%lu", (unsigned long)dimensions.high.width, (unsigned long)dimensions.high.height);
            }
            
            builder.renderDimensions = dimensions;
        }
    }];
}

RCT_EXPORT_METHOD(connect:(NSString *)accessToken roomName:(NSString *)roomName enableAudio:(BOOL *)enableAudio enableVideo:(BOOL *)enableVideo encodingParameters:(NSDictionary *)encodingParameters enableNetworkQualityReporting:(BOOL *)enableNetworkQualityReporting dominantSpeakerEnabled:(BOOL *)dominantSpeakerEnabled bandwidthProfileOptions:(NSDictionary *)bandwidthProfileOptions) {

  [self _setLocalVideoEnabled:enableVideo];
    
  TVIVideoBandwidthProfileOptions* videoBandwidthProfile = [self prepareBandwidthProfile:bandwidthProfileOptions];

  TVIConnectOptions *connectOptions = [TVIConnectOptions optionsWithToken:accessToken block:^(TVIConnectOptionsBuilder * _Nonnull builder) {
    if (self.localVideoTrack) {
      builder.videoTracks = @[self.localVideoTrack];
    }

    if (self.localAudioTrack) {
      [self.localAudioTrack setEnabled:enableAudio];
      builder.audioTracks = @[self.localAudioTrack];
    }

    self.localDataTrack = [TVILocalDataTrack track];

    if (self.localDataTrack) {
      builder.dataTracks = @[self.localDataTrack];
    }
      
    builder.dominantSpeakerEnabled = dominantSpeakerEnabled ? YES : NO;

    builder.roomName = roomName;

    if(encodingParameters[@"enableH264Codec"]){
        if ([encodingParameters[@"enableH264Codec"] boolValue] == true) {
            builder.preferredVideoCodecs = @[ [TVIH264Codec new] ];
            NSLog(@"Preferring H264 Codec");
        }
    }

    if (encodingParameters[@"audioBitrate"] && encodingParameters[@"videoBitrate"]) {
      NSInteger audioBitrate = [encodingParameters[@"audioBitrate"] integerValue];
      NSInteger videoBitrate = [encodingParameters[@"videoBitrate"] integerValue];
        
        if (audioBitrate >= 0 && videoBitrate >= 0) {
            builder.encodingParameters = [[TVIEncodingParameters alloc] initWithAudioBitrate:audioBitrate videoBitrate:videoBitrate];
            NSLog(@"Audio encoding bitrate %li - Video encoding bitrate %li", (long)audioBitrate, (long)videoBitrate);
        } else {
            // If we have specified only 1 of the bit rate values
            if ((audioBitrate >= 0) || (videoBitrate >= 0)) {
                // Log a warning
                NSLog(@"Either audio bitrate: %li or video bitrate: %li has an incorrect value. Ignoring both values.", (long)audioBitrate, (long)videoBitrate);
            }
        }
    } else if (encodingParameters[@"audioBitrate"] || encodingParameters[@"videoBitrate"]) {
        NSLog(@"Either audio or video bit rate is not specified. Ignoring both values");
    }

    if (enableNetworkQualityReporting) {
      builder.networkQualityEnabled = true;
      builder.networkQualityConfiguration = [ [TVINetworkQualityConfiguration alloc] initWithLocalVerbosity:TVINetworkQualityVerbosityMinimal remoteVerbosity:TVINetworkQualityVerbosityMinimal];
    }
      
    builder.bandwidthProfileOptions = [[TVIBandwidthProfileOptions alloc] initWithVideoOptions:videoBandwidthProfile];
      
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
  [self stopLocalAudio];
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
  [self sendEventCheckingListenerWithName:roomDidConnect body:@{ @"roomName" : room.name , @"roomSid": room.sid, @"participants" : participants }];

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
    [self sendEventCheckingListenerWithName:dataTrackMessageReceived body:@{ @"message": message }];
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
