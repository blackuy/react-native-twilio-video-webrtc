import { ConfigPlugin, createRunOncePlugin } from "expo/config-plugins";

import withAndroidTwilioVideoWebrtc from "./withAndroidTwilioVideoWebrtc";
import withIosTwilioVideoWebrtc from "./withIosTwilioVideoWebrtc";

const pkg = require("react-native-twilio-video-webrtc/package.json");

export type WithTwilioVideoWebRtcProps = {
  cameraPermission?: string;
  microphonePermission?: string;
};

const withTwilioVideoWebrtc: ConfigPlugin<WithTwilioVideoWebRtcProps> = (
  config,
  props
) => {
  config = withAndroidTwilioVideoWebrtc(config);
  config = withIosTwilioVideoWebrtc(config, props);

  return config;
};

export default createRunOncePlugin(
  withTwilioVideoWebrtc,
  pkg.name,
  pkg.version
);
