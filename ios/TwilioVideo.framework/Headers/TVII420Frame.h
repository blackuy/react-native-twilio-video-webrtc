//
//  TVII420Frame.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  Specifies the orientation of video content.
 */
typedef NS_ENUM(NSUInteger, TVIVideoOrientation) {
    /**
     *  The video is rotated 0 degrees, oriented with its top side up.
     */
    TVIVideoOrientationUp = 0,
    /**
     *  The video is rotated 90 degrees, oriented with its top side to the left.
     */
    TVIVideoOrientationLeft,
    /**
     *  The video is rotated 180 degrees, oriented with its top side to bottom.
     */
    TVIVideoOrientationDown,
    /**
     *  The video is rotated 270 degrees, oriented with its top side to the right.
     */
    TVIVideoOrientationRight,
};

/**
 *  @brief A helper which constructs an affine transform for any orientation.
 *
 *  @param orientation The orientation of the video you wish to display.
 *
 *  @return A `CGAffineTransform` struct which can be applied to a renderer's view.
 */
static inline CGAffineTransform TVIVideoOrientationMakeTransform(TVIVideoOrientation orientation)
{
    return CGAffineTransformMakeRotation((CGFloat)orientation * M_PI_2);
}

/**
 *  @brief A helper which indicates if the orientation would cause the native dimensions to be flipped.
 *
 *  @param orientation The orientation to check.
 *
 *  @return `YES` if the orientation would cause the dimensions to be flipped, and `NO` otherwise.
 */
static inline BOOL TVIVideoOrientationIsRotated(TVIVideoOrientation orientation)
{
    return (orientation == TVIVideoOrientationLeft ||
            orientation == TVIVideoOrientationRight);
}

/**
 *  Represents a planar YUV video frame in the `I420` pixel format.
 */
@interface TVII420Frame : NSObject

/**
 *  @brief The native width of the frame, not accounting for any `orientation` metadata.
 */
@property(nonatomic, readonly) NSUInteger width;

/**
 *  @brief The native height of the frame, not accounting for any `orientation` metadata.
 */
@property(nonatomic, readonly) NSUInteger height;

/**
 *  @brief The width of the chroma (u,v) planes. Chroma information is horizontally subsampled at every 2nd pixel.
 */
@property(nonatomic, readonly) NSUInteger chromaWidth;

/**
 *  @brief The height of the chroma (u,v) planes. Chroma information is vertically subsampled at every 2nd pixel.
 */
@property(nonatomic, readonly) NSUInteger chromaHeight;

/**
 *  @brief The total size of a chroma plane in bytes, including padding.
 */
@property(nonatomic, readonly) NSUInteger chromaSize;

/**
 *  @brief The orientation of the video frame. See `TVIVideoOrientation` for more details.
 */
@property(nonatomic, readonly) TVIVideoOrientation orientation;

/**
 *  @brief A const pointer to the base of the y-plane (luma).
 */
@property(nonatomic, readonly) const uint8_t *yPlane;

/**
 *  @brief A const pointer to the base of the u-plane (chroma).
 */
@property(nonatomic, readonly) const uint8_t *uPlane;

/**
 *  @brief A const pointer to the base of the v-plane (chroma).
 */
@property(nonatomic, readonly) const uint8_t *vPlane;

/**
 *  @brief The total width of each y-plane row including padding.
 */
@property(nonatomic, readonly) NSInteger yPitch;

/**
 *  @brief The total width of each u-plane row including padding.
 */
@property(nonatomic, readonly) NSInteger uPitch;

/**
 *  @brief The total width of each v-plane row including padding.
 */
@property(nonatomic, readonly) NSInteger vPitch;

@end

