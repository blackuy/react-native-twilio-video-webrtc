//
//  TVILocalParticipant.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TVILocalMedia;

/**
 *  `TVILocalParticipant` represents your Client in a Room which you are connected to.
 */
@interface TVILocalParticipant : NSObject

/**
 *  @brief The identity of the `TVILocalParticipant`.
 */
@property (nonatomic, readonly, copy, nonnull) NSString *identity;

/**
 *  @brief The `TVILocalMedia` which is shared with other Participants.
 *
 *  @discussion: Use `TVIConnectOptions` to provide Media when connecting to a Room. If you don't provide your own media
 *  then an empty instance will be created for you, and set here.
 */
@property (nonatomic, readonly, strong, nonnull) TVILocalMedia *media;

/**
 *  @brief The LocalParticipant's server identifier. This value uniquely identifies your Client in a Room and is often
 *  useful for debugging purposes.
 */
@property (nonatomic, readonly, copy, nonnull) NSString *sid;

/**
 *  @brief Developers shouldn't initialize this class directly.
 *
 *  @discussion Use `TVIVideoClient` connectWith* methods to join a `TVIRoom` and query its `localParticipant` property.
 */
- (null_unspecified instancetype)init __attribute__((unavailable("Use TVIVideoClient connectWith* methods to join a TVIRoom and query its `localParticipant` property.")));

@end
