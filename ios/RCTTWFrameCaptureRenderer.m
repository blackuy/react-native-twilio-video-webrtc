//
//  RCTTWFrameCaptureRenderer.m
//  react-native-twilio-video-webrtc
//
//  Created by Christopher Fields II on 6/6/23.
//

#import <Foundation/Foundation.h>
#import "RCTTWFrameCaptureRenderer.h"

// https://github.com/twilio/twilio-video-ios/issues/184#issuecomment-828655941 - overall idea inspiration
// https://github.com/twilio/video-quickstart-ios/pull/286/files - more in depth implementation?

@implementation RCTTWFrameCaptureRenderer

- (void)captureFrame{
    NSLog(@"[rntwilio] capturing the frame");
    self.captureThisFrame = TRUE;
}

- (void)renderFrame:(nonnull TVIVideoFrame *)frame {
    if (!self.captureThisFrame) {
        return;
    }

    self.captureThisFrame = FALSE; // make sure to revert the flag

    // do frame image conversion and save to file system
    CIImage *ciImg = [CIImage imageWithCVImageBuffer:frame.imageBuffer];
    UIImage *img = [UIImage imageWithCIImage:ciImg];
    NSData *imageData = UIImagePNGRepresentation(img);
    NSURL *url = [NSFileManager.defaultManager URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask].firstObject;
    NSUUID *uuid = [NSUUID UUID];
    NSString *fileId = [uuid UUIDString];
    NSString *filename = [NSString stringWithFormat:@"rntframe-%@.jpeg", fileId];
    url = [url URLByAppendingPathComponent:filename];
    BOOL didSave = [imageData writeToFile:url.path atomically:TRUE];
    NSLog(@"[rntwilio] saving frame at path %@ was success: %@", url.path, didSave ? @"YES" : @"NO");

    // emit onFrameCaptured event to notification center (video module will then emit event to JS)
    NSNotificationCenter *notiCenter = NSNotificationCenter.defaultCenter;
    [notiCenter postNotificationName:@"onFrameCaptured" object:self userInfo:@{@"filename": filename}];
}

- (void)updateVideoSize:(CMVideoDimensions)videoSize orientation:(TVIVideoOrientation)orientation {
}

@end
