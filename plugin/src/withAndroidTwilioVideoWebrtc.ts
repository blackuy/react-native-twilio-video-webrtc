import {
  AndroidConfig,
  ConfigPlugin,
  ExportedConfigWithProps,
  withAppBuildGradle,
} from "expo/config-plugins";
import { withBuildProperties } from "expo-build-properties";

const { withPermissions: withAndroidPermissions } = AndroidConfig.Permissions;

const withAndroidTwilioVideoWebrtc: ConfigPlugin = (config) => {
  // 1. Permissions
  config = withAndroidPermissions(config, [
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.MODIFY_AUDIO_SETTINGS",
  ]);

  // 2. Java 1.8 compatibility
  config = withAppBuildGradle(config, (conf: ExportedConfigWithProps) => {
    if (conf.modResults.contents.includes(`compileOptions`)) {
      return conf;
    }

    conf.modResults.contents = conf.modResults.contents.replace(
      "android {",
      `
      android {
        compileOptions {
          sourceCompatibility 1.8
          targetCompatibility 1.8
        }
      `
    );

    return conf;
  });

  // 3. Proguard rules
  config = withBuildProperties(config, {
    android: {
      extraProguardRules: `
        -keep class com.twilio.** { *; }
        -keep class tvi.webrtc.** { *; }
      `,
    },
  });

  return config;
};

export default withAndroidTwilioVideoWebrtc;
