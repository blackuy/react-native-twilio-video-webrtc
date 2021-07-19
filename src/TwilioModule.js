import { NativeModules } from "react-native";

const RNTwilioModule = NativeModules.RNTwilioModule

class TwilioModule {
  static getPreloadTracks = async () => {
    try {
      const devices = await RNTwilioModule.getAvailableLocalTracks();
      return devices;

    } catch (error) {
      console.error(error);
      return Promise.resolve([]);
    }
  }
}

export default TwilioModule;