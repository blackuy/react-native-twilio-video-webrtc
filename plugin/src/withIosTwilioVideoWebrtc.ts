import { ConfigPlugin, withInfoPlist } from "expo/config-plugins";
import { withBuildProperties } from "expo-build-properties";

import { WithTwilioVideoWebRtcProps } from "./withTwilioVideoWebrtc";

const addPermission = (conf, permission, message) => {
  if (!conf.modResults[permission] && !!message) {
    conf.modResults[permission] = message;
  }
};

const withIosTwilioVideoWebrtc: ConfigPlugin<WithTwilioVideoWebRtcProps> = (
  config,
  { cameraPermission, microphonePermission }
) => {
  // 1. Deployment target (iOS 11.0+)
  config = withBuildProperties(config, {
    ios: {
      deploymentTarget: "11.0",
    },
  });

  // 2. Camera / Microphone Permission on Info.plist
  config = withInfoPlist(config, (conf) => {
    addPermission(conf, "NSCameraUsageDescription", cameraPermission);
    addPermission(conf, "NSMicrophoneUsageDescription", microphonePermission);

    return conf;
  });

  return config;
};

export default withIosTwilioVideoWebrtc;
