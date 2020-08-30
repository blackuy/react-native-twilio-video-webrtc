/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import { requireNativeComponent } from 'react-native'
import PropTypes from 'prop-types'
import React from 'react'

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
    testID: PropTypes.string,
    // Whether to apply Z ordering to this view.  Setting this to true will cause
    // this view to appear above other Twilio Video views.
    applyZOrder: PropTypes.bool
  }

  buildNativeEventWrappers () {
    return [
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
