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
    return @{ @"trackId": self.trackId };
}
@end

@implementation TVILocalVideoTrack(RCTTWSerializable)

- (id)toJSON {
    return @{ @"trackId": self.trackId };
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
