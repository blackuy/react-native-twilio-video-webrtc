//
//  RCTTWVideoModule.h
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#import <TwilioVideo/TwilioVideo.h>

@interface RCTTWVideoModule : RCTEventEmitter <RCTBridgeModule>

- (void)addLocalView:(TVIVideoView *)view;
- (void)removeLocalView:(TVIVideoView *)view;
- (void)addParticipantView:(TVIVideoView *)view sid:(NSString *)sid trackSid:(NSString *)trackSid;

@end
