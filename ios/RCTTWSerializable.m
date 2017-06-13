//
//  RCTTWSerialization.m
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import "RCTTWSerializable.h"

@implementation TVIParticipant(RCTTWSerializable)

- (id)toJSON {
  return @{ @"identity": self.identity };
}

@end

@implementation TVITrack(RCTTWSerializable)

- (id)toJSON {
  return @{ @"trackId": self.trackId };
}

@end
