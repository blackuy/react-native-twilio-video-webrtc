package com.twiliorn.library;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.NativeViewHierarchyManager;

public class TwilioVideoModule extends ReactContextBaseJavaModule {
  private static final String TAG = "TwilioVideoModule";

  private static ReactApplicationContext mReactContext;

  public CameraModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNTwilioVideoModule";
  }

  @ReactMethod
  public void isRecording(final int viewTag, final Promise promise) {
    final ReactApplicationContext context = getReactApplicationContext();
    UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);

    uiManager.addUIBlock(new UIBlock() {
      @Override
      public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
        final CustomTwilioVideoView videoView;

        try {
          videoView = (CustomTwilioVideoView) nativeViewHierarchyManager.resolveView(viewTag);

          if(!videoView.isActive()){
            promise.reject("E_VIEW_UNAVAILABLE", "isRecording: Video is not running");
            return
          }
          promise.resolve(videoView.isRecording());
        } catch (Exception e) {
          promise.reject("E_VIEW_NOT_FOUND", "isRecording: Expected a CustomTwilioVideoView component");
        }
      }
    });
  }
}