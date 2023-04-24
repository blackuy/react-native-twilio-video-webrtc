"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const config_plugins_1 = require("expo/config-plugins");
const addPermission = (conf, permission, message) => {
    if (!conf.modResults[permission] && !!message) {
        conf.modResults[permission] = message;
    }
};
const withIosTwilioVideoWebrtc = (config, { cameraPermission, microphonePermission }) => {
    // 1. Deployment target (iOS 11.0+)
    // config = withBuildProperties(config, {
    //  ios: {
    //    deploymentTarget: "11.0",
    //  },
    // });
    // 2. Camera / Microphone Permission on Info.plist
    config = (0, config_plugins_1.withInfoPlist)(config, (conf) => {
        addPermission(conf, "NSCameraUsageDescription", cameraPermission);
        addPermission(conf, "NSMicrophoneUsageDescription", microphonePermission);
        return conf;
    });
    return config;
};
exports.default = withIosTwilioVideoWebrtc;
