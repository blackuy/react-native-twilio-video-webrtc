/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import { requireNativeComponent } from 'react-native'
import PropTypes from 'prop-types'
import React from 'react'

const NativeTwilioLocalPreview = requireNativeComponent(
  'RNTwilioLocalView',
  TwilioLocalPreview
)

class TwilioLocalPreview extends React.Component {
  static propTypes = {
    cameraId: PropTypes.string.isRequired,
    onFrameDimensionsChanged: PropTypes.func,
    renderToHardwareTextureAndroid: PropTypes.string,
    onLayout: PropTypes.string,
    accessibilityLiveRegion: PropTypes.string,
    accessibilityComponentType: PropTypes.string,
    importantForAccessibility: PropTypes.string,
    accessibilityLabel: PropTypes.string,
    nativeID: PropTypes.string,
    testID: PropTypes.string
  }

  buildNativeEventWrappers() {
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

  render() {
    return (
      <NativeTwilioLocalPreview
        {...this.props}
        {...this.buildNativeEventWrappers()}
      />
    )
  }
}

module.exports = TwilioLocalPreview
