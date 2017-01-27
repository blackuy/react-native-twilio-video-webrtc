//
//  TVIVideoConstraints.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <CoreMedia/CoreMedia.h>

/**
 *  Rational aspect ratio represented as numerator:denominator
 *
 *  @discussion For x:y aspect ratio you can set numerator to x and denominator to y
 */
typedef struct
{
    NSUInteger numerator;
    NSUInteger denominator;
} TVIAspectRatio;

/**
 *  @brief Makes a valid `TVIAspectRatio` with a `numerator` and a `denominator`
 *
 *  @return The aspect ratio
 */
inline static TVIAspectRatio TVIAspectRatioMake(NSUInteger numerator, NSUInteger denominator)
{
    return (TVIAspectRatio){numerator, denominator};
}

/**
 *  The maximum fps allowed in your constraints. Limited to 30 fps at the moment.
 */
extern NSUInteger const TVIVideoConstraintsMaximumFPS;

/**
 *  The minimum fps allowed in your constraints.
 */
extern NSUInteger const TVIVideoConstraintsMinimumFPS;

/**
 *  @brief Represents no video size constraint.
 *
 *  @discussion This constant can be passed as `maxSize` or `minSize` to `TVIVideoConstraintsBuilder` in 
 * `constraintsWithBlock:` when you don't want to constrain by size.
 */
extern CMVideoDimensions const TVIVideoConstraintsSizeNone;

/**
 *  Represents no video frame rate constraint.
 *
 *  @discussion This constant can be passed as `maxFrameRate` or `minFrameRate` to `TVIVideoConstraintsBuilder` in
 *  `constraintsWithBlock:` when you don't want to constrain by frame rate.
 */
extern NSUInteger const TVIVideoConstraintsFrameRateNone;

/**
 *  Represents no aspect ratio constraints.
 *
 *  @discussion This constant can be passed as `aspectRatio` to `TVIVideoConstraintsBuilder` in
 *  `constraintsWithBlock:` when you don't want to constrain by aspect ratio.
 */
extern TVIAspectRatio const TVIVideoConstraintsAspectRatioNone;

/*
 *  Pre-defined aspect ratio 11:9
 */
extern TVIAspectRatio const TVIAspectRatio11x9;

/*
 *  Pre-defined aspect ratio 4:3
 */
extern TVIAspectRatio const TVIAspectRatio4x3;

/*
 * Pre-defined aspect ratio 16:9
 */
extern TVIAspectRatio const TVIAspectRatio16x9;

/**
 *  `TVIVideoConstraintsBuilder` is a video constraints object builder.
 *  You can pass video constraints to the builder using `TVIVideoConstraints`'s class method
 *   `constraintsWithBlock:builderBlock`. The builder will build video constraints object using the constraints you
 *    passed in the builder block.
 */
@interface TVIVideoConstraintsBuilder : NSObject

/**
 *  @brief Specifies the maximum size for your video in native input coordinates (think landscape for 
 *  `TVICameraCapturer`).
 *
 *  @discussion Each dimension must be divisible by 8.
 */
@property (nonatomic, assign) CMVideoDimensions maxSize;

/**
 *  @brief Specifies the minimum size for your video in native input coordinates.
 *
 *  @discussion Defaults to {0,0}, which indicates no minimum video size.
 *  Set this property if you wish to choose a size within the range of `minSize` and `maxSize`.
 */
@property (nonatomic, assign) CMVideoDimensions minSize;

/**
 *  @brief Specifies the maximum frame rate of your video (frames per second).
 *
 *  @discussion Frame rates are capped at `TVIVideoConstraintsMaximumFPS`.
 */
@property (nonatomic, assign) NSUInteger maxFrameRate;

/**
 *  @brief Specifies the minimum frame rate of your video (frames per second).
 *
 *  @discussion Defaults to no minimum value.
 */
@property (nonatomic, assign) NSUInteger minFrameRate;

/**
 *  @brief Specifies the aspect ratio of your video.
 */
@property (nonatomic, assign) TVIAspectRatio aspectRatio;

@end

/**
 *  @brief A Builder block which is fired when you create video constraints.
 *
 *  @param builder The builder on which you can pass video constraints to the builder using this block. The builder will 
 *  build video constraints object using the constraints you passed.
 */
typedef void (^TVIVideoConstraintsBuilderBlock)(TVIVideoConstraintsBuilder * _Nonnull builder);

/**
 *  @brief `TVIVideoConstraints` specifies requirements for local video capture.
 *
 *  @discussion Use this class in conjunction with `TVILocalVideoTrack` to customize video capture for your use case.
 *  See `TVICameraCapturer.h` for size and frame rate presets which pair well with that capturer.
 *  Note that `TVIVideoConstraints` is used to resolve the capture format, but the actual video sent to Participants
 *  may be downscaled temporally or spatially in response to network and device conditions.
 */
@interface TVIVideoConstraints : NSObject

/**
 *  @brief The default video constraints.
 *
 *  @discussion The default video constraints are determined based upon your device model. For 64-bit devices the 
 *  default is 640x480x30. If you are using an older A5 device then 480x360x15 is returned, and for A5x, and A6 devices 
 *  480x360x20 is used instead.
 *  @return Video constraints.
 */
+ (null_unspecified instancetype)constraints;

/**
 *  @brief Construct `TVIVideoConstraints` using the builder pattern.
 *
 *  @param builderBlock You can pass video constraints to the builder using this block. The builder will construct a
 *   `TVIVideoConstraints` instance using the options that you provided.
 *  @return Video Constraints
 */
+ (null_unspecified instancetype)constraintsWithBlock:(_Nonnull TVIVideoConstraintsBuilderBlock)builderBlock;

/**
 *  @brief Specifies the maximum size for your video in native input coordinates (think landscape for 
 *  `TVICameraCapturer`).
 *
 *  @discussion Each dimension must be divisible by 8.
 */
@property (nonatomic, assign, readonly) CMVideoDimensions maxSize;

/**
 *  @brief Specifies the minimum size for your video in native input coordinates.
 *
 *  @discussion Defaults to {0,0}, which indicates no minimum video size. 
 *  Set this property if you wish to choose a size within the range of `minSize` and `maxSize`.
 */
@property (nonatomic, assign, readonly) CMVideoDimensions minSize;

/**
 *  @brief Specifies the maximum frame rate of your video (frames per second).
 *
 *  @discussion Frame rates are capped at `TVIVideoConstraintsMaximumFPS`.
 */
@property (nonatomic, assign, readonly) NSUInteger maxFrameRate;

/**
 *  @brief Specifies the minimum frame rate of your video (frames per second).
 *
 *  @discussion Defaults to no minimum value.
 */
@property (nonatomic, assign, readonly) NSUInteger minFrameRate;

/**
 *  @brief Specifies the aspect ratio of your video.
 */
@property (nonatomic, assign, readonly) TVIAspectRatio aspectRatio;

@end
