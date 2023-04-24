"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const config_plugins_1 = require("expo/config-plugins");
const withAndroidTwilioVideoWebrtc_1 = __importDefault(require("./withAndroidTwilioVideoWebrtc"));
const withIosTwilioVideoWebrtc_1 = __importDefault(require("./withIosTwilioVideoWebrtc"));
const pkg = require("react-native-twilio-video-webrtc/package.json");
const withTwilioVideoWebrtc = (config, props) => {
    config = (0, withAndroidTwilioVideoWebrtc_1.default)(config);
    config = (0, withIosTwilioVideoWebrtc_1.default)(config, props);
    return config;
};
exports.default = (0, config_plugins_1.createRunOncePlugin)(withTwilioVideoWebrtc, pkg.name, pkg.version);
