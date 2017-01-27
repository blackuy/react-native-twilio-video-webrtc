//
//  TVIVideoViewRenderer.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "TVIVideoRenderer.h"

@class TVIVideoViewRenderer;

/**
 *  `TVIVideoViewRendererDelegate` allows you to respond to, and customize the behaviour of
 *  `TVIVideoViewRenderer`.
 */
@protocol TVIVideoViewRendererDelegate <NSObject>

@optional
/**
 *  @brief This method is called once, and only once after the first frame is received.
 *  Use it to drive user interface animations.
 *  @note: Querying hasVideoData will return 'YES' within, and after this call.
 *
 *  @param renderer The renderer which became ready.
 */
- (void)rendererDidReceiveVideoData:(nonnull TVIVideoViewRenderer *)renderer;

/**
 *  @brief This method is called every time the video track's dimensions change.
 *
 *  @param renderer   The renderer.
 *  @param dimensions The new dimensions of the video stream.
 */
- (void)renderer:(nonnull TVIVideoViewRenderer *)renderer dimensionsDidChange:(CMVideoDimensions)dimensions;

/**
 *  @brief This method is called every time the video track's orientation changes.
 *
 *  @param renderer     The renderer.
 *  @param orientation  The new orientation of the video stream.
 */
- (void)renderer:(nonnull TVIVideoViewRenderer *)renderer orientationDidChange:(TVIVideoOrientation)orientation;

/**
 *  @brief Specify if the renderer or the application will handle rotated video content.
 *
 *  @discussion Handling rotations at the application level is more complex, but allows you to smoothly animate 
 *  transitions.
 *
 *  @param renderer The renderer.
 *  @return `NO` if you wish to handle rotations in your own layout. Defaults to `YES`.
 */
- (BOOL)rendererShouldRotateContent:(nonnull TVIVideoViewRenderer *)renderer;

@end

/**
 *  Specifies the type of video rendering used.
 */
typedef NS_ENUM(NSUInteger, TVIVideoRenderingType) {
    /**
     *  Metal video rendering is supported on 64-bit devices, not including the simulator.
     */
    TVIVideoRenderingTypeMetal = 0,
    /**
     *  OpenGLES video rendering is supported on all Apple devices, including the simulator.
     */
    TVIVideoRenderingTypeOpenGLES
};

/**
 *  `TVIVideoViewRenderer` displays video inside a `UIView`.
 */
@interface TVIVideoViewRenderer : NSObject <TVIVideoRenderer>

/**
 *  @brief Creates a video renderer with a delegate.
 *
 *  @discussuion The default video rendering type is determined based upon your device model. For 64-bit devices the 
 *  Metal APIs will be used otherwise OpenGL ES video rendering APIs will be used.
 *
 *  @param delegate An object implementing the TVIVideoViewRendererDelegate protocol (often a UIViewController).
 *
 *  @return A renderer which is appropriate for your device and OS combination.
 */
- (nonnull instancetype)initWithDelegate:(nullable id<TVIVideoViewRendererDelegate>)delegate;

/**
 *  @brief Creates a video renderer with a delegate.
 *
 *  @discussuion The default video rendering type is determined based upon your device model. For 64-bit devices the 
 *  Metal APIs will be used otherwise OpenGL ES video rendering APIs will be used.
 *
 *  @param delegate An object implementing the TVIVideoViewRendererDelegate protocol (often a UIViewController).
 *
 *  @return A renderer which is appropriate for your device and OS combination.
 */
+ (nonnull TVIVideoViewRenderer *)rendererWithDelegate:(nullable id<TVIVideoViewRendererDelegate>)delegate;

/**
 *  @brief Creates a video renderer with a rendering type and a delegate.
 *
 *  @param renderingType The rendering type.
 *  @param delegate An object implementing the TVIVideoViewRendererDelegate protocol (often a UIViewController).
 *
 *  @return A renderer which is appropriate for the current device and OS combination. 
 *  Returns `nil` if rendering type is not supported on the current device.
 */
+ (nonnull TVIVideoViewRenderer *)rendererWithRenderingType:(TVIVideoRenderingType)renderingType
                                                   delegate:(nullable id<TVIVideoViewRendererDelegate>)delegate;

/**
 *  @brief A delegate which receives callbacks when important renderer events occur.
 *
 *  @note The delegate is always called on the main thread in order to synchronize with UIKit.
 */
@property (nonatomic, weak, readonly, nullable) id<TVIVideoViewRendererDelegate> delegate;

/**
 *  @brief The dimensions of incoming video frames (without rotations applied). Use this to layout the renderer's view.
 */
@property (nonatomic, assign, readonly) CMVideoDimensions videoFrameDimensions;

/**
 *  @brief The orientation of incoming video frames. Use this to layout the renderer's view.
 */
@property (nonatomic, assign, readonly) TVIVideoOrientation videoFrameOrientation;

/**
 *  @brief Indicates that at least one frame of video data has been received.
 */
@property (atomic, assign, readonly) BOOL hasVideoData;

/**
 *  @brief The renderer's view. Add this to your view hierarchy to display rendered video content.
 */
@property (nonatomic, strong, readonly, nonnull) UIView *view;

@end
