//
//  TVIAudioTrack.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TVITrack.h"

/**
 *  `TVIAudioTrack` represents a local or remote audio track.
 */
@interface TVIAudioTrack : TVITrack
/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Tracks cannot be created explicitly.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Tracks cannot be created explicitly.")));

@end

@class TVIAudioConstraints;

/**
 *  `TVILocalAudioTrack` represents an audio track where the content is captured from your device's audio subsystem.
 */
@interface TVILocalAudioTrack : TVIAudioTrack

/**
 *  @brief The `TVIAudioConstraints` that were provided when the track was added to `TVILocalMedia`.
 */
@property (nonatomic, strong, readonly, nonnull) TVIAudioConstraints *constraints;

/**
 *  @brief Indicates if the track content is enabled.
 *
 *  @discussion It is possible to enable and disable local tracks. The results of this operation are signaled to other 
 *  Participants in the same Room. When an audio track is disabled, silence is sent in place of normal audio.
 */
@property (nonatomic, assign, getter=isEnabled) BOOL enabled;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Tracks cannot be created explicitly.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Tracks cannot be created explicitly.")));

@end
