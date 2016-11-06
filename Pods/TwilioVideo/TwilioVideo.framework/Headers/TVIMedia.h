//
//  TVIMedia.h
//  TwilioVideo
//
//  Created by Piyush Tank on 8/16/16.
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TVITrack;
@class TVIAudioTrack;
@class TVIVideoTrack;

/**
 *  `TVIMedia` is a collection of audio and video tracks shared by a remote Participant.
 */
@interface TVIMedia : NSObject

/**
 *  @brief A collection of shared audio tracks.
 */
@property (nonatomic, copy, readonly, nonnull) NSArray<TVIAudioTrack *> *audioTracks;

/**
 *  @brief A collection of shared video tracks.
 */
@property (nonatomic, copy, readonly, nonnull) NSArray<TVIVideoTrack *> *videoTracks;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Use the media property on `TVIParticipant`.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Use the media property on TVIParticipant.")));

/**
 *  @brief A utility method which gets a `TVITrack` by its id.
 *
 *  @param trackId The track id.
 *
 *  @return An instance of `TVITrack` if successful, or `nil` if not.
 */
- (nullable TVITrack *)getTrack:(nonnull NSString *)trackId;

/**
 *  @brief A utility method which gets a `TVIAudioTrack` by its id.
 *
 *  @param trackId The track id.
 *
 *  @return An instance of `TVIAudioTrack` if successful, or `nil` if not.
 */
- (nullable TVIAudioTrack *)getAudioTrack:(nonnull NSString *)trackId;

/**
 *  @brief A utility method which gets a `TVIVideoTrack` by its id.
 *
 *  @param trackId The track id.
 *
 *  @return An instance of `TVIVideoTrack` if successful, or `nil` if not.
 */
- (nullable TVIVideoTrack *)getVideoTrack:(nonnull NSString *)trackId;

@end
