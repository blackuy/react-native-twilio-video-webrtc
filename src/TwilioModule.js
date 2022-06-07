import { NativeModules } from "react-native";

const RNTwilioModule = NativeModules.RNTwilioModule;

class TwilioModule {
  static getPreloadTracks = async () => {
    try {
      const devices = await RNTwilioModule.getAvailableLocalTracks();
      return devices;
    } catch (error) {
      console.error(error);
      return Promise.resolve([]);
    }
  };

  static getAvailableCameras = async () => {
    try {
      const cameraIds = await RNTwilioModule.getAvailableCameras();
      return cameraIds;
    } catch (error) {
      console.error(error);
      return Promise.resolve([]);
    }
  };

  static startStethoscope = async () => {
    const path = await RNTwilioModule.startStethoscope();
    return path;
  };

  static stopStethoscope = async () => {
    await RNTwilioModule.stopStethoscope();
  };

  static stethoscopeRecordToFile = async (path, timeout) => {
    const path = await RNTwilioModule.stethoscopeRecordToFile(path, timeout);
    return path;
  };
}

export default TwilioModule;
