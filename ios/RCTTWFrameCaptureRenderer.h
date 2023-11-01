//
//  RCTTWFrameCaptureRenderer.h
//
//  Created by Christopher Fields II on 6/6/23.
//

#import <Foundation/Foundation.h>
#import <TwilioVideo/TVIVideoRenderer.h>

@interface RCTTWFrameCaptureRenderer : NSObject<TVIVideoRenderer>

@property (nonatomic) BOOL captureThisFrame;
@property (nonatomic) NSString *filename;

- (void) captureFrame:(NSString *)filename;

@end
