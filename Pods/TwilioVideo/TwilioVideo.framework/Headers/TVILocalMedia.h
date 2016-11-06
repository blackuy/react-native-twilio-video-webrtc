//
//  TVILocalMedia.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TVIAudioConstraints;
@class TVILocalAudioTrack;
@class TVILocalVideoTrack;
@class TVIVideoConstraints;
@protocol TVIVideoCapturer;

/**
 *  `TVILocalMedia` is a collection of audio and video tracks which can be shared in a `TVIRoom`.
 *
 *  @discussion The lifecycle of TVILocalMedia is independent of TVIRoom. The same media can be shared in zero, one, or 
 *  many Rooms.
 */
@interface TVILocalMedia : NSObject

/**
 *  @brief A collection of local audio tracks.
 */
@property (nonatomic, strong, readonly, nonnull) NSArray<TVILocalAudioTrack *> *audioTracks;

/**
 * @brief A collection of local video tracks.
 */
@property (nonatomic, strong, readonly, nonnull) NSArray<TVILocalVideoTrack *> *videoTracks;

/**
 *  @brief Adds a local audio track to `TVILocalMedia`.
 *
 *  @param enabled Specifies if `TVILocalAudioTrack` should be enabled or disabled initially.
 *
 *  @return An instance of `TVILocalAudioTrack` if successful, or `nil` if not.
 */
- (nullable TVILocalAudioTrack *)addAudioTrack:(BOOL)enabled;

/**
 *  @brief Adds a local video track to `TVILocalMedia`.
 *
 *  @param enabled Specifies if `TVILocalAudioTrack` should be enabled or disabled initially.
 *  @param constraints The audio constraints.
 *  @param error An `NSError` which will be filled out if the operation fails, or set to nil on success.
 *
 *  @return An instance of TVILocalAudioTrack if successful, or `nil` if not.
 */
- (nullable TVILocalAudioTrack *)addAudioTrack:(BOOL)enabled
                                   constraints:(nullable TVIAudioConstraints *)constraints
                                         error:(NSError * _Nullable * _Nullable)error;

/**
 *  @brief Removes a local audio track from `TVILocalMedia`
 *
 *  @param track The `TVILocalAudioTrack` to attempt to remove from `TVILocalMedia`.
 *
 *  @return YES if successful, or NO if the removal fails.
 */
- (BOOL)removeAudioTrack:(nonnull TVILocalAudioTrack *)track;

/**
 *  @brief Removes a local audio track from `TVILocalMedia`
 *
 *  @param track The `TVILocalAudioTrack` to attempt to remove from `TVILocalMedia`.
 *  @param error An NSError which will be filled out if the operation fails, or set to nil on success.
 *
 *  @return YES if successful, or NO if the removal fails.
 */
- (BOOL)removeAudioTrack:(nonnull TVILocalAudioTrack *)track
                   error:(NSError * _Nullable * _Nullable)error;

/**
 *  @brief Adds a local video track to `TVILocalMedia`.
 *
 *  @param enabled Specifies if `TVILocalVideoTrack` should be enabled or disabled initially.
 *  @param capturer A video capturer which conforms to `TVIVideoCapturer`. Note that only `TVICameraCapturer` and
 *  `TVIScreenCapturer` classes are supported for now.
 *
 *  @return An instance of `TVILocalVideoTrack` if successful, or `nil` if not.
 */
- (nullable TVILocalVideoTrack *)addVideoTrack:(BOOL)enabled
                                      capturer:(nonnull id<TVIVideoCapturer>)capturer;

/**
 *  @brief Adds a local video track to `TVILocalMedia`.
 *
 *  @param enabled Specifies if `TVILocalVideoTrack` should be enabled or disabled initially.
 *  @param capturer A video capturer which conforms to `TVIVideoCapturer`. Note that only `TVICameraCapturer` and
 *  `TVIScreenCapturer` classes are supported for now.
 *  @param constraints The `TVIVideoConstraints` to use. If nil is passed then the default constraints are used instead. 
 *  Default values are determined based upon your device model. For 64-bit devices the default is 640x480x30. If you are
 *  using an older A5 device then 480x360x15 is returned, and for A5x, and A6 devices 480x360x20 is used instead.
 *  @param error An NSError which will be filled out if the operation fails, or set to nil on success.
 *
 *  @return An instance of `TVILocalVideoTrack` if successful, or `nil` if not.
 */
- (nullable TVILocalVideoTrack *)addVideoTrack:(BOOL)enabled
                                      capturer:(nonnull id<TVIVideoCapturer>)capturer
                                   constraints:(nullable TVIVideoConstraints *)constraints
                                         error:(NSError * _Nullable * _Nullable)error;

/**
 *  @brief Removes a local video track from `TVILocalMedia`
 *
 *  @param track The `TVILocalVideoTrack` to attempt to remove from `TVILocalMedia`.
 *
 *  @return YES if success, NO if failure.
 */
- (BOOL)removeVideoTrack:(nonnull TVILocalVideoTrack *)track;

/**
 *  @brief To remove local video track from `TVILocalMedia`.
 *
 *  @param track The `TVILocalVideoTrack` to attempt to remove from `TVILocalMedia`.
 *  @param error An NSError which will be filled out if the operation fails, or set to nil on success.
 */
- (BOOL)removeVideoTrack:(nonnull TVILocalVideoTrack *)track
                   error:(NSError * _Nullable * _Nullable)error;

@end
