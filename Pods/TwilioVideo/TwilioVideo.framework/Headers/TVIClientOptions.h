//
//  TVIClientOptions.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>


@class TVICodecOptions;
@class TVIIceOptions;

/**
 *  `TVIClientOptionsBuilder` constructs `TVIClientOptions`.
 */
@interface TVIClientOptionsBuilder : NSObject

/**
 *  @brief CodecOptions to use for connection attempts (if not overridden by `TVIConnectOptions`).
 *
 *  @discussion Codec selection is not yet implemented, but will be in future beta releases.
 */
@property (nonatomic, copy, nullable) TVICodecOptions *codecOptions;

/**
 *  @brief The queue where TwilioVideo classes will invoke delegate methods.
 *
 *  @discussion All delegate methods except for `TVIVideoViewRendererDelegate` and `TVICameraCaptureDelegate`
 *  are performed on this queue. The `TVIVideoClient`, and any `TVIRoom` instances which are created
 *  from it will maintain strong references to this queue until they are destroyed.
 */
@property (nonatomic, strong, nullable) dispatch_queue_t delegateQueue;

@end

/**
 *  `TVIClientOptionsBuilderBlock` is a block to configure client options.
 *
 *  @param builder The builder
 */
typedef void (^TVIClientOptionsBuilderBlock)(TVIClientOptionsBuilder * _Nonnull builder);

/**
 *  `TVIClientOptions` represents configuration for your `TVIVideoClient`.
 *
 *  @discussion `TVIRoom` instances created from `TVIVideoClient` will inherit these options.
 */
@interface TVIClientOptions : NSObject

/**
 *  @brief CodecOptions to use for connection attempts (if not overridden by `TVIConnectOptions`).
 *
 *  @discussion Codec selection is not yet implemented, but will be in future beta releases.
 */
@property (nonatomic, copy, readonly, nullable) TVICodecOptions *codecOptions;

/**
 *  @brief The delegate queue provided when this object was created.
 *
 *  @discussion All delegate methods except for `TVIVideoViewRendererDelegate` and `TVICameraCaptureDelegate`
 *  are performed on this queue. The `TVIVideoClient`, and any `TVIRoom` instances which are created
 *  from it will maintain strong references to this queue until they are destroyed.
 */
@property (nonatomic, strong, readonly, nullable) dispatch_queue_t delegateQueue;

/**
 *  @brief Creates the default `TVIClientOptions`.
 *  
 *  @discussion The main dispatch queue and default ice options will be used.
 *
 *  @return An instance of `TVIClientOptions`.
 */
+ (nonnull instancetype)options;

/**
 *  @brief Creates `TVIClientOptions` with a user provided delegate queue, `TVIIceOptions` and `TVICodecOptions`.
 *
 *  @param block The `TVIClientOptionsBuilderBlock` to use when constructing this instance.
 *
 *  @return An instance of `TVIClientOptions`.
 */
+ (nonnull instancetype)optionsWithBlock:(nonnull TVIClientOptionsBuilderBlock)block;

@end
