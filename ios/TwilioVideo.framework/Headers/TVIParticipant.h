//
//  TVIParticipant.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TVIParticipant;
@class TVIVideoTrack;
@class TVIAudioTrack;
@class TVITrack;
@class TVIMedia;
@protocol TVITrackStatisticsDelegate;
@protocol TVIParticipantDelegate;

/**
 *  `TVIParticipant` represents a remote Participant in a Room which you are connected to.
 */
@interface TVIParticipant : NSObject

/**
 *  @brief Indicates if the Participant is connected to the Room.
 */
@property (nonatomic, assign, readonly, getter=isConnected) BOOL connected;

/**
 *  @brief The Participant's delegate. Set this property to be notified about Participant events such as tracks being 
 *  added or removed.
 */
@property (atomic, weak, nullable) id<TVIParticipantDelegate> delegate;

/**
 *  @brief The Participant's stats delegate. Set this property to be notified about track statistics.
 */
@property (nonatomic, weak, nullable) id<TVITrackStatisticsDelegate> statsDelegate;

/**
 *  @brief The Participant's identity.
 */
@property (nonatomic, readonly, copy, nonnull) NSString *identity;

/**
 *  @brief The Participant's media. You can query `TVIMedia` in order to interact with the Participant's audio and video
 *  tracks.
 */
@property (nonatomic, readonly, strong, nonnull) TVIMedia *media;

/**
 *  @sid The Participant's server identifier. This value uniquely identifies the Participant in a Room and is often 
 *  useful for debugging purposes.
 */
@property (nonatomic, readonly, copy, nullable) NSString *sid;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Use `TVIVideoClient` connectWith* methods to join a `TVIRoom` with `TVIParticipant` instances.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Use TVIVideoClient connectWith* methods to join a TVIRoom with TVIParticipant instances.")));

@end

/**
 *  `TVIParticipantDelegate` provides callbacks when important changes to a `TVIParticipant` occur.
 */
@protocol TVIParticipantDelegate <NSObject>

@optional
/**
 *  @brief Delegate method called when the Participant adds a video track.
 *
 *  @param participant The Participant who added the video.
 *  @param videoTrack  The added video track. You can use `TVIVideoTrack`'s attach API or add a renderer to it
 *  to display the Participant's video feed.
 */
- (void)participant:(nonnull TVIParticipant *)participant addedVideoTrack:(nonnull TVIVideoTrack *)videoTrack;

/**
 *  @brief Delegate method called when the Participant removes a video track.
 *
 *  @param participant The Participant.
 *  @param videoTrack  The removed video track.
 */
- (void)participant:(nonnull TVIParticipant *)participant removedVideoTrack:(nonnull TVIVideoTrack *)videoTrack;

/**
 *  @brief Delegate method called when the Participant adds an audio track.
 *
 *  @param participant The Participant.
 *  @param audioTrack  The added audio track.
 */
- (void)participant:(nonnull TVIParticipant *)participant addedAudioTrack:(nonnull TVIAudioTrack *)audioTrack;

/**
 *  @brief Delegate method called when the Participant removes an audio track.
 *
 *  @param participant The Participant.
 *  @param audioTrack  The removed audio track.
 */
- (void)participant:(nonnull TVIParticipant *)participant removedAudioTrack:(nonnull TVIAudioTrack *)audioTrack;

/**
 *  @brief Delegate method called when the Participant enables a track.
 *
 *  @param participant The Participant.
 *  @param track       The track.
 */
- (void)participant:(nonnull TVIParticipant *)participant enabledTrack:(nonnull TVITrack *)track;

/**
 *  @brief Delegate method called when the Participant disables a track.
 *
 *  @param participant The Participant.
 *  @param track       The track.
 */
- (void)participant:(nonnull TVIParticipant *)participant disabledTrack:(nonnull TVITrack *)track;

@end

