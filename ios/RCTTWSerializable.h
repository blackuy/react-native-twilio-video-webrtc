//
//  RCTTWSerialization.h
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import <Foundation/Foundation.h>
#import <TwilioVideo/TwilioVideo.h>

@protocol RCTTWSerializable <NSObject>

- (id)toJSON;

@end

@interface TVIParticipant(RCTTWSerializable)<RCTTWSerializable>
@end

@interface TVITrackPublication(RCTTWSerializable)<RCTTWSerializable>
@end
