//
//  A component to play stereo sounds
//
//  Created by Umar Nizamani on 29/07/20.
//
//

import { NativeModules } from 'react-native'

const { TwilioStereoTonePlayer } = NativeModules;

export default class {

  constructor() {
    // Max audio files we can play
    TwilioStereoTonePlayer.initialize(5);
  }

  preload(filename) {
    return TwilioStereoTonePlayer.preload(filename);
  }

  play(filename, isLooping, volume, playbackSpeed) {
    return TwilioStereoTonePlayer.play(filename, isLooping, volume, playbackSpeed);
  }

  pause() {
    TwilioStereoTonePlayer.pause();
  }

  setVolume(volume) {
    TwilioStereoTonePlayer.setVolume(volume);
  }

  setPlaybackSpeed(speed) {
    TwilioStereoTonePlayer.setPlaybackSpeed(speed);
  }

  release(filename) {
    TwilioStereoTonePlayer.release(filename);
  }

  terminate() {
    TwilioStereoTonePlayer.terminate();
  }
}