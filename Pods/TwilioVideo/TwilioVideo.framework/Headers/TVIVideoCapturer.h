//
//  TVIVideoCapturer.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TVILocalVideoTrack;

/**
 *  `TVIVideoCapturer` is a video capturer protocol.
 *
 *  @discussion In future betas you will be able to provide your own capturers by implementing this protocol.
 */
@protocol TVIVideoCapturer <NSObject>

/**
 *  @brief Indicates if the TVIVideoCapturer is actively capturing video.
 */
@property (atomic, assign, readonly, getter = isCapturing) BOOL capturing;

/**
 *  @brief The video track associated with capturer. 
 *
 *  @note The video track will be `nil` until video track is added to `TVILocalMedia`. 
 *  See `TVILocalMedia`'s addVideoTrack APIs for more information.
 */
@property (nonatomic, weak, nullable) TVILocalVideoTrack *videoTrack;

@end
