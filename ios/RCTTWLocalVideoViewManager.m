//
//  RCTTWLocalVideoViewManager.m
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import "RCTTWLocalVideoViewManager.h"

#import "RCTTWVideoModule.h"

@interface RCTTWLocalVideoViewManager()
@end

@implementation RCTTWLocalVideoViewManager

RCT_EXPORT_MODULE()

- (UIView *)view {
  return [[TVIVideoView alloc] init];
}

RCT_CUSTOM_VIEW_PROPERTY(enabled, BOOL, TVIVideoView) {
  if (json) {
    RCTTWVideoModule *videoModule = [self.bridge moduleForName:@"TWVideoModule"];
    BOOL isEnabled = [RCTConvert BOOL:json];

    if (isEnabled) {
      [videoModule addLocalView:view];
    } else {
      [videoModule removeLocalView:view];
    }
  }
}

@end
