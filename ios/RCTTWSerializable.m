//
//  RCTTWSerialization.m
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

#import "RCTTWSerializable.h"

@implementation TVIRemoteParticipant(RCTTWSerializable)

- (id)toJSON {
  return @{ @"identity": self.identity };
}

@end

@implementation TVILocalAudioTrack(RCTTWSerializable)

- (id)toJSON {
  return @{ @"trackId": self.name };
}
@end

@implementation TVILocalVideoTrack(RCTTWSerializable)

- (id)toJSON {
  return @{ @"trackId": self.name };
}

@end

@implementation TVIRemoteAudioTrack(RCTTWSerializable)

- (id)toJSON {
  return @{ @"trackId": self.name };
}
@end

@implementation TVIRemoteVideoTrack(RCTTWSerializable)

- (id)toJSON {
  return @{ @"trackId": self.name };
}

@end

@implementation TVIRemoteVideoTrackPublication(RCTTWSerializable)

- (id)toJSON {
  return @{ @"remoteTrackId": self.remoteTrack.name };
}

@end
