package com.example;

import com.facebook.react.ReactActivity;
import com.twiliorn.library.TwilioPackage;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "Example";
    }

	@Override
  	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      	TwilioPackage.onRequestPermissionsResult(requestCode, permissions, grantResults); // very important event callback
      	super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  	}
}
