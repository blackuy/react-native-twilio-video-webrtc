//
//  TVIMediaTrack.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//


/**
 *  `TVITrackState` is an enum defines the possible states of a track.
 */
typedef NS_ENUM(NSUInteger, TVITrackState) {
    /**
     *  Track ended.
     */
    TVITrackStateEnded = 0,
    /**
     *  Track live.
     */
    TVITrackStateLive,
};

/**
 *  `TVITrack` is the base class from which all other Track types are dervied.
 */
@interface TVITrack : NSObject

/**
 *  @brief A unique identifier for the Track.
 */
@property (nonatomic, copy, readonly, nonnull) NSString *trackId;

/**
 *  @brief Indicates if the track is enabled or not.
 */
@property (nonatomic, assign, readonly, getter=isEnabled) BOOL enabled;

/**
 *  @brief The current state of the Track.
 */
@property (nonatomic, assign, readonly) TVITrackState state;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion `TVITrack` cannot be created explicitly.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("TVITrack cannot be created explicitly.")));

@end
