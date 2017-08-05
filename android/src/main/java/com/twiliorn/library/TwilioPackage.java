/**
 * Twilio Video for React Native.
 *
 * Authors:
 *   Ralph Pina <ralph.pina@gmail.com>
 *   Jonathan Chang <slycoder@gmail.com>
 */

package com.twiliorn.library;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwilioPackage implements ReactPackage {
	private static CustomTwilioVideoViewManager customTwilioVideoViewManager = null;

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
		customTwilioVideoViewManager = new CustomTwilioVideoViewManager();
        return Arrays.<ViewManager>asList(
            customTwilioVideoViewManager,
            new TwilioRemotePreviewManager(),
            new TwilioVideoPreviewManager()
        );
    }

	public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if(customTwilioVideoViewManager != null) {
			CustomTwilioVideoView customTwilioVideoView = customTwilioVideoViewManager.getCustomTwilioVideoViewInstance();
			if(customTwilioVideoView != null) {
				customTwilioVideoView.onRequestPermissionsResult(requestCode, permissions, grantResults);
			}
		}
	}
}
