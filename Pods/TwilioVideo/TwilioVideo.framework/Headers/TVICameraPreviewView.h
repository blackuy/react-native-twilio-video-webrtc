//
//  TVICameraPreviewView.h
//  TwilioVideo
//
//  Copyright © 2016 Twilio Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

/**
 *  `TVICameraPreviewView` previews video captured by `TVICameraCapturer`.
 *
 *  @discussion This view uses the special preview path offered by `AVCaptureVideoPreviewLayer`. It offers some benefits
 *  which are not provided by `TVIVideoViewRenderer` such as frame accurate mirroring, and support for UIViewContentMode.
 *  The supported content modes are: UIViewContentModeScaleToFill, UIViewContentModeScaleAspectFill, and UIViewContentModeScaleAspectFit.
 */
@interface TVICameraPreviewView : UIView

/**
 *  @brief The current orientation of the view's content.
 */
@property (nonatomic, assign, readonly) UIInterfaceOrientation orientation;

@end
