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
  return @{
    @"sid": self.sid,
    @"identity": self.identity
  };
}

@end

@implementation TVITrackPublication(RCTTWSerializable)

- (id)toJSON {
  return @{
    @"trackSid": self.trackSid,
    @"trackName": self.trackName,
    @"enabled": @(self.trackEnabled)
  };
}

@end
