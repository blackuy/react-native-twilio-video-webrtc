@import TwilioVideo;
#import "TWVideoPreviewManager.h"
#import "PureLayout/PureLayout.h"
#import "TWVideoModule.h"

@interface TWVideoPreviewManager()

@end

@implementation TWVideoPreviewManager

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
    [videoModule.previewView removeFromSuperview];
    [view addSubview:videoModule.previewView];
    [videoModule.previewView autoPinEdgesToSuperviewEdges];
      
      
  }
  
  return view;
  
}

@end
