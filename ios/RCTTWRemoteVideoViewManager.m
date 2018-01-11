//
//  RCTTWRemoteVideoViewManager.m
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import "RCTTWRemoteVideoViewManager.h"

#import <React/RCTConvert.h>
#import "RCTTWVideoModule.h"

@interface RCTTWVideoTrackIdentifier : NSObject

@property (strong) NSString *participantIdentity;
@property (strong) NSString *videoTrackId;

@end

@implementation RCTTWVideoTrackIdentifier

@end

@interface RCTConvert(RCTTWVideoTrackIdentifier)

+ (RCTTWVideoTrackIdentifier *)RCTTWVideoTrackIdentifier:(id)json;

@end

@implementation RCTConvert(RCTTWVideoTrackIdentifier)

+ (RCTTWVideoTrackIdentifier *)RCTTWVideoTrackIdentifier:(id)json {
  RCTTWVideoTrackIdentifier *trackIdentifier = [[RCTTWVideoTrackIdentifier alloc] init];
  trackIdentifier.participantIdentity = json[@"participantIdentity"];
  trackIdentifier.videoTrackId = json[@"videoTrackId"];

  return trackIdentifier;
}

@end

@interface RCTTWRemoteVideoViewManager()
@end

@implementation RCTTWRemoteVideoViewManager

RCT_EXPORT_MODULE()

- (UIView *)view {
  return [[TVIVideoView alloc] init];
}

RCT_CUSTOM_VIEW_PROPERTY(trackIdentifier, RCTTWVideoTrackIdentifier, TVIVideoView) {
  if (json) {
    RCTTWVideoModule *videoModule = [self.bridge moduleForName:@"TWVideoModule"];
    RCTTWVideoTrackIdentifier *id = [RCTConvert RCTTWVideoTrackIdentifier:json];

    [videoModule addParticipantView:view identity:id.participantIdentity trackId:id.videoTrackId];
  }
}


@end
