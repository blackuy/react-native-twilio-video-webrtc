/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import { requireNativeComponent, Platform, UIManager, findNodeHandle } from 'react-native'
import PropTypes from 'prop-types'
import React from 'react'

const nativeEvents = {
  takeSnapshot: 10001,
}

class TwilioRemotePreview extends React.Component {
  static propTypes = {
    trackIdentifier: PropTypes.shape({
      /**
       * The participant's video track you want to render in the view.
       */
      videoTrackSid: PropTypes.string.isRequired
    }),
    onFrameDimensionsChanged: PropTypes.func,
    trackSid: PropTypes.string,
    renderToHardwareTextureAndroid: PropTypes.string,
    onLayout: PropTypes.string,
    accessibilityLiveRegion: PropTypes.string,
    accessibilityComponentType: PropTypes.string,
    importantForAccessibility: PropTypes.string,
    accessibilityLabel: PropTypes.string,
    nativeID: PropTypes.string,
    testID: PropTypes.string
  }

  takeSnapshot() {
    this.runCommand(nativeEvents.takeSnapshot)
  }

  runCommand(event, args) {
    switch (Platform.OS) {
      case 'android':
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this.refs.remoteParticipantView),
          event,
          args
        )
        break
      default:
        break
    }
  }

  buildNativeEventWrappers () {
    return [
      'onSnapshot',
      'onFrameDimensionsChanged'
    ].reduce((wrappedEvents, eventName) => {
      if (this.props[eventName]) {
        return {
          ...wrappedEvents,
          [eventName]: data => this.props[eventName](data.nativeEvent)
        }
      }
      return wrappedEvents
    }, {})
  }

  render () {
    const { trackIdentifier } = this.props
    return (
      <NativeTwilioRemotePreview
        ref="remoteParticipantView"
        trackSid={trackIdentifier && trackIdentifier.videoTrackSid}
        {...this.props}
        {...this.buildNativeEventWrappers()}
      />
    )
  }
}

const NativeTwilioRemotePreview = requireNativeComponent(
  'RNTwilioRemotePreview',
  TwilioRemotePreview
)

module.exports = TwilioRemotePreview
