//
//  RCTTWFrameCaptureRenderer.m
//  react-native-twilio-video-webrtc
//
//  Created by Christopher Fields II on 6/6/23.
//

#import <Foundation/Foundation.h>
#import "RCTTWFrameCaptureRenderer.h"
#import "RCTTWImageUtils.h"
#include <math.h>

// https://github.com/twilio/twilio-video-ios/issues/184#issuecomment-828655941 - overall idea inspiration
// https://github.com/twilio/video-quickstart-ios/pull/286/files - more in depth implementation?

@implementation RCTTWFrameCaptureRenderer

- (void)captureFrame:(NSString *)filename {
    NSLog(@"[RNTwilioVideo] capturing the frame with filename %@", filename);
    self.filename = filename;
    self.captureThisFrame = TRUE;
}

- (void)renderFrame:(nonnull TVIVideoFrame *)frame {
    if (!self.captureThisFrame) {
        return;
    }

    self.captureThisFrame = FALSE; // make sure to revert the flag

    NSLog(@"[RNTwilioVideo] capturing the rendered frame: w x h = %zu x %zu, orientation = %lu", frame.width, frame.height, (unsigned long)frame.orientation);
    
    // save frame on background thread
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self saveFrame:frame filename:self.filename];
    });
}

- (void)updateVideoSize:(CMVideoDimensions)videoSize orientation:(TVIVideoOrientation)orientation {
}

- (int)getDegrees:(TVIVideoOrientation) orientation {
    switch (orientation) {
        case TVIVideoOrientationUp:
            return 0;
        case TVIVideoOrientationLeft:
            return 90;
        case TVIVideoOrientationDown:
            return 180;
        case TVIVideoOrientationRight:
            return 270;
    }
}

- (void)saveFrame:(TVIVideoFrame *)frame filename:(NSString *)filename {
    // do frame image conversion and save to file system
    CIImage *ciImg = [CIImage imageWithCVImageBuffer:frame.imageBuffer];
    UIImage *rawImg = [UIImage imageWithCIImage:ciImg];
    UIImage *img = [rawImg rotate: [self getDegrees:frame.orientation]]; // rotate by desired frame orientation
    NSData *imageData = UIImageJPEGRepresentation(img, 90);
    NSURL *url = [NSFileManager.defaultManager URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask].firstObject;
    NSString *filePath = [NSString stringWithFormat:@"%@.jpeg", filename];
    url = [url URLByAppendingPathComponent:filePath];
    BOOL didSave = [imageData writeToFile:url.path atomically:TRUE];
    NSLog(@"[RNTwilioVideo] saving frame at path %@ was success: %@", url.path, didSave ? @"YES" : @"NO");

    // emit onFrameCaptured event to notification center (video module will then emit event to JS)
    NSNotificationCenter *notiCenter = NSNotificationCenter.defaultCenter;
    [notiCenter postNotificationName:@"onFrameCaptured" object:self userInfo:@{@"filename": filePath}];
}


@end

