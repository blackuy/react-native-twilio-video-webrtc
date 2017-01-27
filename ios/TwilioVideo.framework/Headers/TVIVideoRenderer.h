//
//  TVIVideoRenderer.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <CoreMedia/CoreMedia.h>
#import <Foundation/Foundation.h>

#import "TVII420Frame.h"

/**
 *  TVIVideoRenderers render frames from video tracks.
 *
 *  @note: All the renderers attached to a video track will be called back on the same background thread.
 */
@protocol TVIVideoRenderer <NSObject>

/**
 *  @brief Render an individual frame.
 *
 *  @note You *must* copy or strongly reference (retain) the TVII420Frame object before this call returns.
 *
 *  @param frame The frame to be rendered.
 */
- (void)renderFrame:(nonnull TVII420Frame *)frame;

/**
 *  @brief Informs your renderer that the size and/or orientation of the video stream is about to change.
 *
 *  @note Expect the next delivered frame to have the new orientation.
 *
 *  @param videoSize The new dimensions for the video stream.
 *  @param orientation The new orientation of the video stream.
 *  Always `TVIVideoOrientationUp` unless you opt into orientation support.
 */
- (void)updateVideoSize:(CMVideoDimensions)videoSize orientation:(TVIVideoOrientation)orientation;

@optional
/**
 *  @brief Indicates support for video frame orientation metadata.
 *
 *  @note Supporting orientation allows frames to be delivered to the renderer without an additional copy.
 *
 *  @return Your renderer should return `YES` if it prefers un-rotated metadata tagged frames.
 *  If you would instead prefer pre-rotated frames you should either return `NO` or not implement this method.
 */
- (BOOL)supportsVideoFrameOrientation;

@end
