import {
  ConfigPlugin,
  ExportedConfigWithProps,
  withInfoPlist,
} from "expo/config-plugins";
import { withBuildProperties } from "expo-build-properties";

import { WithTwilioVideoWebRtcProps } from ".";

const addPermission = (
  conf: ExportedConfigWithProps,
  permission: string,
  message?: string
) => {
  if (!conf.modResults[permission] && !!message) {
    conf.modResults[permission] = message;
  }
};

const withIosTwilioVideoWebrtc: ConfigPlugin<WithTwilioVideoWebRtcProps> = (
  config,
  { cameraPermission, microphonePermission }
) => {
  // 1. Deployment target (iOS 11.0+)
  // config = withBuildProperties(config, {
  //  ios: {
  //    deploymentTarget: "11.0",
  //  },
  // });

  // 2. Camera / Microphone Permission on Info.plist
  config = withInfoPlist(config, (conf: ExportedConfigWithProps) => {
    addPermission(conf, "NSCameraUsageDescription", cameraPermission);
    addPermission(conf, "NSMicrophoneUsageDescription", microphonePermission);

    return conf;
  });

  return config;
};

export default withIosTwilioVideoWebrtc;
