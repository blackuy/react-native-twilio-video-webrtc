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

@property (strong) NSString *participantSid;
@property (strong) NSString *videoTrackSid;

@end

@implementation RCTTWVideoTrackIdentifier

@end

@interface RCTConvert(RCTTWVideoTrackIdentifier)

+ (RCTTWVideoTrackIdentifier *)RCTTWVideoTrackIdentifier:(id)json;

@end

@implementation RCTConvert(RCTTWVideoTrackIdentifier)

+ (RCTTWVideoTrackIdentifier *)RCTTWVideoTrackIdentifier:(id)json {
  RCTTWVideoTrackIdentifier *trackIdentifier = [[RCTTWVideoTrackIdentifier alloc] init];
  trackIdentifier.participantSid = json[@"participantSid"];
  trackIdentifier.videoTrackSid = json[@"videoTrackSid"];

  return trackIdentifier;
}

@end

@interface RCTTWRemoteVideoViewManager()
@end

@implementation RCTTWRemoteVideoViewManager

RCT_EXPORT_MODULE()

RCT_CUSTOM_VIEW_PROPERTY(scalesType, NSInteger, TVIVideoView) {
  view.subviews[0].contentMode = [RCTConvert NSInteger:json];
}

- (UIView *)view {
  UIView *container = [[UIView alloc] init];
  TVIVideoView *inner = [[TVIVideoView alloc] init];
  inner.autoresizingMask = (UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth);
  [container addSubview:inner];
  return container;
}

RCT_CUSTOM_VIEW_PROPERTY(trackIdentifier, RCTTWVideoTrackIdentifier, TVIVideoView) {
  if (json) {
    RCTTWVideoModule *videoModule = [self.bridge moduleForName:@"TWVideoModule"];
    RCTTWVideoTrackIdentifier *id = [RCTConvert RCTTWVideoTrackIdentifier:json];

    [videoModule addParticipantView:view.subviews[0] sid:id.participantSid trackSid:id.videoTrackSid];
  }
}


@end
