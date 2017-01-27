//
//  TVIVideoTrack.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import "TVITrack.h"

#import <UIKit/UIKit.h>

#import "TVIVideoCapturer.h"
#import "TVIVideoRenderer.h"

@class TVIVideoTrack;

/**
 *  `TVIVideoTrackDelegate` allows you to respond to changes in `TVIVideoTrack`.
 *
 *  @discussion This delegate is useful when paired with the `attach:` APIs.
 */
@protocol TVIVideoTrackDelegate <NSObject>

@optional

/**
 *  @brief Fired every time the track's dimensions change.
 *
 *  @discussion If you have attached views, and wish to resize your containers now would be a good time.
 *
 *  @param track      The track which was updated.
 *  @param dimensions The new dimensions of the video track.
 */
- (void)videoTrack:(nonnull TVIVideoTrack *)track dimensionsDidChange:(CMVideoDimensions)dimensions;

@end

/**
 *  `TVIVideoTrack` represents video, and provides an interface to render frames from the track.
 */
@interface TVIVideoTrack : TVITrack

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Tracks cannot be created explicitly.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Tracks cannot be created explicitly.")));

/**
 *  @brief The video track's delegate. Set the delegate to receive updates about the track.
 */
@property (nonatomic, weak, nullable) id<TVIVideoTrackDelegate> delegate;

/**
 *  @brief An array of views that are currently attached to the video track.
 *
 *  @note Use the 'attach:' and 'detach:' methods to manipulate this collection.
 */
@property (nonatomic, strong, readonly, nonnull) NSArray<UIView *> *attachedViews;

/**
 *  @brief An array of renderers that are currently attached to the video track.
 *
 *  @note Use the 'addRenderer:' and 'removeRenderer:' methods to manipulate this collection.
 */
@property (nonatomic, strong, readonly, nonnull) NSArray<id<TVIVideoRenderer>> *renderers;

/**
 *  @brief The dimensions of the track's video. Use this to layout attached views.
 */
@property (nonatomic, assign, readonly) CMVideoDimensions videoDimensions;

/**
 *  @brief Attaches a view to the video track. The track's contents will be drawn into the attached view.
 *
 *  @discussion The `attach:` API is the simplest way to display video. For more control see `addRenderer:`.
 *
 *  @param view The view to attach.
 */
- (void)attach:(nonnull UIView *)view;

/**
 *  @brief Detaches a view from the video track. The track's contents will no longer be drawn into the attached view.
 *
 *  @param view The view to detach.
 */
- (void)detach:(nonnull UIView *)view;

/**
 *  @brief Adds a renderer to the video track. Renderers provide fine control over how video is displayed.
 *
 *  @discussion Use this method instead of `attach` to add your own renderer to `TVIVideoTrack`.
 *
 *  @param renderer An object or swift struct that implements the `TVIVideoRenderer` protocol.
 */
- (void)addRenderer:(nonnull id<TVIVideoRenderer>)renderer;

/**
 *  @brief Removes a renderer from the video track.
 *
 *  @param renderer An object or swift struct that implements the `TVIVideoRenderer` protocol.
 */
- (void)removeRenderer:(nonnull id<TVIVideoRenderer>)renderer;

@end

@class TVIVideoConstraints;

/**
 * `TVILocalVideoTrack` represents local video produced by a `TVIVideoCapturer`.
 */
@interface TVILocalVideoTrack : TVIVideoTrack

/**
 *  @brief Indicates if track is enabled.
 */
@property (nonatomic, assign, getter = isEnabled) BOOL enabled;

/**
 *  @brief The capturer that is associated with this track.
 */
@property (nonatomic, strong, readonly, nonnull) id<TVIVideoCapturer> capturer;

/**
 *  @brief The video constraints.
 */
@property (nonatomic, strong, readonly, nonnull) TVIVideoConstraints *constraints;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Tracks cannot be created explicitly
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Tracks cannot be created explicitly.")));

@end
