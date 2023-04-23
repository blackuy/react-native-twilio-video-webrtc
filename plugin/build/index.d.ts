import { ConfigPlugin } from "expo/config-plugins";
export type WithTwilioVideoWebRtcProps = {
    cameraPermission?: string;
    microphonePermission?: string;
};
declare const _default: ConfigPlugin<WithTwilioVideoWebRtcProps>;
export default _default;
