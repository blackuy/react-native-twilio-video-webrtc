
class TwilioVideoMock extends Component {
  connect = () => {}

  disconnect = () => {}

  setLocalAudioEnabled = () => {}

  setLocalVideoEnabled = () => {}

  toggleSoundSetup = () => {}

  flipCamera = () => {}

  setRemoteAudioPlayback = () => {}

  getStats = () => {}

  render() {
      return (<div />);
  }
}

class TwilioVideoLocalViewMock extends Component {
  render() {
      return (<div />);
  }
}

class TwilioVideoParticipantViewMock extends Component {
  render() {
      return (<div />);
  }
}

const asMock = {
  TwilioVideo: TwilioVideoMock,
  TwilioVideoLocalView: TwilioVideoLocalViewMock,
  TwilioVideoParticipantView: TwilioVideoParticipantViewMock,
};

export default asMock;
