#import <React/UIView+React.h>

@import TwilioVideo;
#import "TWRemotePreviewManager.h"
#import "PureLayout/PureLayout.h"
#import "TWVideoModule.h"

@interface TWRemotePreviewManager()

@end

@implementation TWRemotePreviewManager

RCT_EXPORT_MODULE()

-(void)dealloc{

}

-(instancetype)init{
  if(self = [super init]){

  }
  return self;
}

- (UIView *)view

{
  
  UIView* view = [[UIView alloc] init];
  
  TWVideoModule* videoModule = [self.bridge moduleForName:@"TWVideoModule"];
  
  if (videoModule) {
    [videoModule.remoteMediaView removeFromSuperview];
    [view addSubview:videoModule.remoteMediaView];
    [videoModule.remoteMediaView autoPinEdgesToSuperviewEdges];
      
  }
  
  return view;
  
}

@end