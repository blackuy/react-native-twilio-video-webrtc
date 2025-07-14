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

// Multiple track support - replace single track properties
@property (strong, nonatomic) NSMutableDictionary<NSString*, TVILocalVideoTrack*> *localVideoTracks;
@property (strong, nonatomic) NSMutableDictionary<NSString*, TVILocalAudioTrack*> *localAudioTracks;
@property (strong, nonatomic) NSMutableDictionary<NSString*, TVICameraSource*> *cameraSources;

@property (strong, nonatomic) TVILocalDataTrack* localDataTrack;
@property (strong, nonatomic) TVIAppScreenSource *screen;
@property (strong, nonatomic) TVILocalParticipant* localParticipant;
@property (strong, nonatomic) TVIRoom *room;
@property (nonatomic) BOOL listening;

// Map used to map remote data tracks to remote participants
@property (strong, nonatomic) NSMutableDictionary<TVIRemoteDataTrack*, TVIRemoteParticipant*> *dataTrackRemoteParticipantMap;

@end

@implementation RCTTWVideoModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (instancetype)init {
    if (self = [super init]) {
        // Initialize multiple track collections
        self.localVideoTracks = [[NSMutableDictionary alloc] init];
        self.localAudioTracks = [[NSMutableDictionary alloc] init];
        self.cameraSources = [[NSMutableDictionary alloc] init];
        self.dataTrackRemoteParticipantMap = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)dealloc {
  [self clearAllCameraInstances];
}

- (void)clearAllCameraInstances {
  // Stop all camera sources
  for (TVICameraSource *cameraSource in self.cameraSources.allValues) {
    [cameraSource stopCapture];
  }
  [self.cameraSources removeAllObjects];
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

// New methods for multiple track support
RCT_EXPORT_METHOD(createLocalAudioTrack:(NSDictionary *)config
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *trackName = config[@"trackName"];
    BOOL enabled = config[@"enabled"] ? [config[@"enabled"] boolValue] : YES;
    
    if (!trackName) {
        reject(@"INVALID_PARAMS", @"trackName is required", nil);
        return;
    }
    
    if (self.localAudioTracks[trackName]) {
        reject(@"TRACK_EXISTS", @"Audio track with this name already exists", nil);
        return;
    }
    
    TVILocalAudioTrack *audioTrack = [TVILocalAudioTrack trackWithOptions:nil enabled:enabled name:trackName];
    if (audioTrack) {
        self.localAudioTracks[trackName] = audioTrack;
        resolve(trackName);
    } else {
        reject(@"CREATION_FAILED", @"Failed to create audio track", nil);
    }
}

RCT_EXPORT_METHOD(createLocalVideoTrack:(NSDictionary *)config
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *trackName = config[@"trackName"];
    BOOL enabled = config[@"enabled"] ? [config[@"enabled"] boolValue] : YES;
    NSString *cameraType = config[@"cameraType"] ? config[@"cameraType"] : @"front";
    
    if (!trackName) {
        reject(@"INVALID_PARAMS", @"trackName is required", nil);
        return;
    }
    
    if (self.localVideoTracks[trackName]) {
        reject(@"TRACK_EXISTS", @"Video track with this name already exists", nil);
        return;
    }
    
    TVICameraSourceOptions *options = [TVICameraSourceOptions optionsWithBlock:^(TVICameraSourceOptionsBuilder * _Nonnull builder) {
        
    }];
    TVICameraSource *cameraSource = [[TVICameraSource alloc] initWithOptions:options delegate:self];
    
    if (cameraSource == nil) {
        reject(@"CAMERA_UNAVAILABLE", @"Camera source unavailable", nil);
        return;
    }
    
    TVILocalVideoTrack *videoTrack = [TVILocalVideoTrack trackWithSource:cameraSource enabled:enabled name:trackName];
    if (videoTrack) {
        self.localVideoTracks[trackName] = videoTrack;
        self.cameraSources[trackName] = cameraSource;
        
        // Start camera capture
        [self startCameraCaptureForTrack:trackName cameraType:cameraType];
        resolve(trackName);
    } else {
        reject(@"CREATION_FAILED", @"Failed to create video track", nil);
    }
}

RCT_EXPORT_METHOD(publishLocalAudioTrack:(NSString *)trackName
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalAudioTrack *audioTrack = self.localAudioTracks[trackName];
    if (!audioTrack) {
        reject(@"TRACK_NOT_FOUND", @"Audio track not found", nil);
        return;
    }
    
    if (self.localParticipant) {
        [self.localParticipant publishAudioTrack:audioTrack];
        resolve(@(YES));
    } else {
        reject(@"NOT_CONNECTED", @"Not connected to room", nil);
    }
}

RCT_EXPORT_METHOD(publishLocalVideoTrack:(NSString *)trackName
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
    if (!videoTrack) {
        reject(@"TRACK_NOT_FOUND", @"Video track not found", nil);
        return;
    }
    
    if (self.localParticipant) {
        [self.localParticipant publishVideoTrack:videoTrack];
        resolve(@(YES));
    } else {
        reject(@"NOT_CONNECTED", @"Not connected to room", nil);
    }
}

RCT_EXPORT_METHOD(unpublishLocalAudioTrack:(NSString *)trackName
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalAudioTrack *audioTrack = self.localAudioTracks[trackName];
    if (!audioTrack) {
        reject(@"TRACK_NOT_FOUND", @"Audio track not found", nil);
        return;
    }
    
    if (self.localParticipant) {
        [self.localParticipant unpublishAudioTrack:audioTrack];
        resolve(@(YES));
    } else {
        reject(@"NOT_CONNECTED", @"Not connected to room", nil);
    }
}

RCT_EXPORT_METHOD(unpublishLocalVideoTrack:(NSString *)trackName
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
    if (!videoTrack) {
        reject(@"TRACK_NOT_FOUND", @"Video track not found", nil);
        return;
    }
    
    if (self.localParticipant) {
        [self.localParticipant unpublishVideoTrack:videoTrack];
        resolve(@(YES));
    } else {
        reject(@"NOT_CONNECTED", @"Not connected to room", nil);
    }
}

RCT_EXPORT_METHOD(destroyLocalTrack:(NSString *)trackName
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
    TVILocalAudioTrack *audioTrack = self.localAudioTracks[trackName];
    TVICameraSource *cameraSource = self.cameraSources[trackName];
    
    if (videoTrack) {
        // Unpublish if published
        if (self.localParticipant) {
            [self.localParticipant unpublishVideoTrack:videoTrack];
        }
        
        // Stop camera capture
        if (cameraSource) {
            [cameraSource stopCapture];
            [self.cameraSources removeObjectForKey:trackName];
        }
        
        [self.localVideoTracks removeObjectForKey:trackName];
        resolve(@(YES));
    } else if (audioTrack) {
        // Unpublish if published
        if (self.localParticipant) {
            [self.localParticipant unpublishAudioTrack:audioTrack];
        }
        
        [self.localAudioTracks removeObjectForKey:trackName];
        resolve(@(YES));
    } else {
        reject(@"TRACK_NOT_FOUND", @"Track not found", nil);
    }
}

RCT_EXPORT_METHOD(enableLocalTrack:(NSString *)trackName
                 enabled:(BOOL)enabled
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
    TVILocalAudioTrack *audioTrack = self.localAudioTracks[trackName];
    
    if (videoTrack) {
        [videoTrack setEnabled:enabled];
        resolve(@(YES));
    } else if (audioTrack) {
        [audioTrack setEnabled:enabled];
        resolve(@(YES));
    } else {
        reject(@"TRACK_NOT_FOUND", @"Track not found", nil);
    }
}

RCT_EXPORT_METHOD(getLocalTracks:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    NSMutableArray *tracks = [[NSMutableArray alloc] init];
    
    // Add video tracks
    for (NSString *trackName in self.localVideoTracks) {
        TVILocalVideoTrack *track = self.localVideoTracks[trackName];
        [tracks addObject:@{
            @"trackName": track.name,
            @"enabled": @(track.enabled),
            @"type": @"video"
        }];
    }
    
    // Add audio tracks
    for (NSString *trackName in self.localAudioTracks) {
        TVILocalAudioTrack *track = self.localAudioTracks[trackName];
        [tracks addObject:@{
            @"trackName": track.name,
            @"enabled": @(track.enabled),
            @"type": @"audio"
        }];
    }
    
    resolve(tracks);
}

// Helper method for camera capture
- (void)startCameraCaptureForTrack:(NSString *)trackName cameraType:(NSString *)cameraType {
    TVICameraSource *cameraSource = self.cameraSources[trackName];
    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
    
    if (!cameraSource || !videoTrack) {
        return;
    }
    
    AVCaptureDevice *camera;
    if ([cameraType isEqualToString:@"back"]) {
        camera = [TVICameraSource captureDeviceForPosition:AVCaptureDevicePositionBack];
    } else {
        camera = [TVICameraSource captureDeviceForPosition:AVCaptureDevicePositionFront];
    }
    
    [cameraSource startCaptureWithDevice:camera completion:^(AVCaptureDevice *device,
            TVIVideoFormat *startFormat,
            NSError *error) {
        if (!error) {
            for (TVIVideoView *renderer in videoTrack.renderers) {
                [self updateLocalViewMirroring:renderer forTrack:trackName];
            }
            [self sendEventCheckingListenerWithName:cameraDidStart body:nil];
        }
    }];
}

- (void)updateLocalViewMirroring:(TVIVideoView *)view forTrack:(NSString *)trackName {
    TVICameraSource *cameraSource = self.cameraSources[trackName];
    if (cameraSource && cameraSource.device.position == AVCaptureDevicePositionFront) {
        view.mirror = true;
    }
}

// Updated methods to support track names
- (void)addLocalView:(TVIVideoView *)view {
    [self addLocalView:view trackName:nil];
}

- (void)addLocalView:(TVIVideoView *)view trackName:(NSString *)trackName {
    // If trackName is specified, use that track; otherwise use first available track
    if (trackName) {
        TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
        if (videoTrack) {
            [videoTrack addRenderer:view];
            [self updateLocalViewMirroring:view forTrack:trackName];
        }
    } else {
        // Use first available video track
        TVILocalVideoTrack *firstTrack = [self.localVideoTracks.allValues firstObject];
        if (firstTrack) {
            [firstTrack addRenderer:view];
            NSString *firstTrackName = [[self.localVideoTracks allKeysForObject:firstTrack] firstObject];
            [self updateLocalViewMirroring:view forTrack:firstTrackName];
        }
    }
}

- (void)removeLocalView:(TVIVideoView *)view {
  // Remove view from all local video tracks
  for (TVILocalVideoTrack *videoTrack in self.localVideoTracks.allValues) {
    [videoTrack removeRenderer:view];
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

RCT_EXPORT_METHOD(flipCamera) {
    // Flip camera for all camera sources
    for (TVICameraSource *cameraSource in self.cameraSources.allValues) {
        AVCaptureDevicePosition position = cameraSource.device.position;
        AVCaptureDevicePosition nextPosition = position == AVCaptureDevicePositionFront ? AVCaptureDevicePositionBack : AVCaptureDevicePositionFront;
        BOOL mirror = nextPosition == AVCaptureDevicePositionFront;

        AVCaptureDevice *captureDevice = [TVICameraSource captureDeviceForPosition:nextPosition];
        [cameraSource selectCaptureDevice:captureDevice completion:^(AVCaptureDevice *device,
                TVIVideoFormat *startFormat,
                NSError *error) {
            if (!error) {
                // Update mirroring for all renderers of video tracks using this camera source
                for (NSString *trackName in self.localVideoTracks) {
                    TVILocalVideoTrack *videoTrack = self.localVideoTracks[trackName];
                    TVICameraSource *trackCameraSource = self.cameraSources[trackName];
                    if (trackCameraSource == cameraSource) {
                        for (TVIVideoView *renderer in videoTrack.renderers) {
                            renderer.mirror = mirror;
                        }
                    }
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
       
       // Create a screen share video track
       TVILocalVideoTrack *screenTrack = [TVILocalVideoTrack trackWithSource:self.screen enabled:YES name:@"screen"];
       if (screenTrack) {
           self.localVideoTracks[@"screen"] = screenTrack;
           if (self.localParticipant) {
               [self.localParticipant publishVideoTrack:screenTrack];
           }
       }
       [self.screen startCapture];    
    } else {
        TVILocalVideoTrack *screenTrack = self.localVideoTracks[@"screen"];
        if (screenTrack && self.localParticipant) {
            [self.localParticipant unpublishVideoTrack:screenTrack];
        }
        [self.localVideoTracks removeObjectForKey:@"screen"];
        [self.screen stopCapture];
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

RCT_EXPORT_METHOD(connect:(NSString *)accessToken roomName:(NSString *)roomName enableAudio:(BOOL)enableAudio enableVideo:(BOOL)enableVideo encodingParameters:(NSDictionary *)encodingParameters enableNetworkQualityReporting:(BOOL)enableNetworkQualityReporting dominantSpeakerEnabled:(BOOL)dominantSpeakerEnabled cameraType:(NSString *)cameraType) {
  
  // No automatic track creation - users must create tracks explicitly
  
  TVIConnectOptions *connectOptions = [TVIConnectOptions optionsWithToken:accessToken block:^(TVIConnectOptionsBuilder * _Nonnull builder) {
    // Add all video tracks
    NSArray<TVILocalVideoTrack *> *videoTracks = [self.localVideoTracks allValues];
    if (videoTracks.count > 0) {
      builder.videoTracks = videoTracks;
    }

    // Add all audio tracks
    NSArray<TVILocalAudioTrack *> *audioTracks = [self.localAudioTracks allValues];
    if (audioTracks.count > 0) {
      builder.audioTracks = audioTracks;
    }

    self.localDataTrack = [TVILocalDataTrack track];

    if (self.localDataTrack) {
      builder.dataTracks = @[self.localDataTrack];
    }
      
    builder.dominantSpeakerEnabled = dominantSpeakerEnabled;

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
  [self clearAllCameraInstances];
  [self.room disconnect];
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

