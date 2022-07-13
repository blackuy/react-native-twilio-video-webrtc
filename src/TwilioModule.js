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

  static streamAudioFile = async (path) =>
    await RNTwilioModule.streamAudioFile(path);

  static streamAudioFile = async (path) =>
    await RNTwilioModule.streamDefaultMic(path);
}

export default TwilioModule;
