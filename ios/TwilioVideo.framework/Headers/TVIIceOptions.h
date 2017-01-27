//
//  TVIIceOptions.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  `TVIIceTransportPolicy` specifies which ICE transports to allow.
 */
typedef NS_ENUM(NSUInteger, TVIIceTransportPolicy) {
    /**
     *  All transports will be used.
     */
    TVIIceTransportPolicyAll = 0,
    /**
     *  Only TURN relay transports will be used.
     */
    TVIIceTransportPolicyRelay = 1
};

/**
 *  `TVIIceServer` is a single STUN or TURN server.
 */
@interface TVIIceServer : NSObject

/**
 *  @brief The URL for the ICE server.
 *
 *  @discussion Your server URL must begin with either the stun: or turn: scheme.
 *  For example, a STUN server could be defined as <stun:stun.company.com:port>.
 */
@property (nonnull, nonatomic, readonly, copy) NSString *url;

/**
 *  @brief The user name. Required for TURN servers.
 */
@property (nullable, nonatomic, readonly, copy) NSString *username;

/**
 *  @brief The password. Required for TURN servers.
 */
@property (nullable, nonatomic, readonly, copy) NSString *password;

/**
 *  Creates a `TVIIceServer`.
 *
 *  @param serverUrl The URL for your STUN or TURN server.
 *
 *  @return A `TVIIceServer` object.
 */
- (_Null_unspecified instancetype)initWithURL:(nonnull NSString *)serverUrl;

/**
 *  Creates a `TVIIceServer`.
 *
 *  @param serverUrl The URL for your STUN or TURN server.
 *  @param username  The username credential for your server.
 *  @param password  The password credential for your server.
 *
 *  @return A `TVIIceServer` object.
 */
- (_Null_unspecified instancetype)initWithURL:(nonnull NSString *)serverUrl
                                     username:(nullable NSString *)username
                                     password:(nullable NSString *)password;

@end

/**
 *  `TVIIceOptionsBuilder` constructs `TVIIceOptions`
 *
 *  @discussion If you provide a transportPolicy but do not provide any ICE Servers, `TVIVideoClient` will fetch the
 *  default servers based upon your configuration profile. However, the `transportPolicy` provided by you will be respected.
 */
@interface TVIIceOptionsBuilder : NSObject

/**
 *  @brief An array of `TVIIceServer` objects to be used during connection establishment.
 */
@property (nonatomic, strong, nullable) NSArray<TVIIceServer *> *servers;

/**
 *  @brief The transport policy to use. Defaults to `TVIIceTransportPolicyAll`.
 */
@property (nonatomic, assign) TVIIceTransportPolicy transportPolicy;

@end

/**
 *  `TVIIceOptionsBuilderBlock` is a block to configure ICE options.
 *
 *  @param builder The builder
 */
typedef void (^TVIIceOptionsBuilderBlock)(TVIIceOptionsBuilder * _Nonnull builder);

/**
 *  `TVIIceOptions` specifies custom media connectivity configurations.
 *
 *  @discussion Media connections are established using the ICE (Interactive Connectivity Establishment) protocol.
 *  These options allow you to customize how data flows to and from participants, and which protocols to use.
 *  You may also provide your own ICE servers, overriding the defaults.
 *  https://www.twilio.com/stun-turn
 */
@interface TVIIceOptions : NSObject

/**
 *  An array of `TVIIceServer` objects to be used during connection establishment.
 */
@property (nonatomic, copy, nonnull, readonly) NSArray<TVIIceServer *> *servers;

/**
 *  The transport policy to use. Defaults to `TVIIceTransportPolicyAll`.
 */
@property (nonatomic, assign, readonly) TVIIceTransportPolicy transportPolicy;

/**
 *  @brief Creates a default `TVIIceOptions` instance.
 *
 *  @return An instance of `TVIIceOptions`.
 */
+ (_Null_unspecified instancetype)options;

/**
 *  @brief Creates a `TVIIceOptions` instance using a builder block that you specify.
 *
 *  @param block A `TVIIceOptionsBuilderBlock` which sets specific options on the builder.
 *
 *  @return A `TVIIceOptions` instance created using options set in the builder block.
 */
+ (nonnull instancetype)optionsWithBlock:(nonnull TVIIceOptionsBuilderBlock)block;

@end
