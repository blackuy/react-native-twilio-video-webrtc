//
//  TVIAudioConstraints.h
//  TwilioVideo
//
//  Created by Piyush Tank on 8/3/16.
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>


/**
 *  `TVIAudioConstraintsBuilder` constructs `TVIAudioConstraints`.
 */
@interface TVIAudioConstraintsBuilder : NSObject

/**
 *  @brief Automatically applies gain so that voices are easier to understand. Defaults to `NO`.
 */
@property (nonatomic, assign) BOOL autoGainControl;

/**
 *  @brief Reduces background noise levels. Defaults to `NO`.
 */
@property (nonatomic, assign) BOOL noiseReduction;

@end

/**
 *  `TVIAudioConstraintsBuilderBlock` allows you to construct `TVIAudioConstraints` using the builder pattern.
 *
 *  @param builder The builder
 */
typedef void (^TVIAudioConstraintsBuilderBlock)(TVIAudioConstraintsBuilder * _Nonnull builder);

/**
 *  `TVIAudioConstraints` specifies requirements for `TVILocalAudioTrack`.
 */
@interface TVIAudioConstraints : NSObject

/**
 *  Automatically applies gain so that voices are easier to understand. Defaults to `NO`.
 */
@property (nonatomic, assign, readonly) BOOL autoGainControl;

/**
 *  Reduces background noise levels. Defaults to `NO`.
 */
@property (nonatomic, assign, readonly) BOOL noiseReduction;

/**
 *  @brief Creates default constraints.
 *
 *  @return An instance of `TVILocalVideoTrack`.
 */
+ (null_unspecified instancetype)constraints;

/**
 *  @brief Constructs `TVIAudioConstraints` using the builder pattern.
 *
 *  @param block You can pass audio constraints to the builder using this block. The builder will construct a 
 *  `TVIAudioConstraints` object using the options that you passed.
 *
 *  @return An instance of `TVIAudioConstraints`.
 */
+ (null_unspecified instancetype)constraintsWithBlock:(nonnull TVIAudioConstraintsBuilderBlock)block;

@end
