"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const config_plugins_1 = require("expo/config-plugins");
const expo_build_properties_1 = require("expo-build-properties");
const { withPermissions: withAndroidPermissions } = config_plugins_1.AndroidConfig.Permissions;
const withAndroidTwilioVideoWebrtc = (config) => {
    // 1. Permissions
    config = withAndroidPermissions(config, [
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.MODIFY_AUDIO_SETTINGS",
    ]);
    // 2. Java 1.8 compatibility
    config = (0, config_plugins_1.withAppBuildGradle)(config, (conf) => {
        if (conf.modResults.contents.includes(`compileOptions`)) {
            return conf;
        }
        conf.modResults.contents = conf.modResults.contents.replace("android {", `
      android {
        compileOptions {
          sourceCompatibility 1.8
          targetCompatibility 1.8
        }
      `);
        return conf;
    });
    // 3. Proguard rules
    config = (0, expo_build_properties_1.withBuildProperties)(config, {
        android: {
            extraProguardRules: `
        -keep class com.twilio.** { *; }
        -keep class tvi.webrtc.** { *; }
      `,
        },
    });
    return config;
};
exports.default = withAndroidTwilioVideoWebrtc;
