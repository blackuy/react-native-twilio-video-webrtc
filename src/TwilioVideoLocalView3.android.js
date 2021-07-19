/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import { requireNativeComponent } from 'react-native'
import PropTypes from 'prop-types'
import React from 'react'


class TwilioLocalVideoView extends React.Component {
  static propTypes = {
    trackId: PropTypes.string.isRequired,
    enabled: PropTypes.bool,
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
      <NativeTwilioLocalVideoView
        {...this.props}
        enabled={this.props ?? false}
        {...this.buildNativeEventWrappers()}
      />
    )
  }
}

const NativeTwilioLocalVideoView = requireNativeComponent(
  'RNTwilioLocalVideoView',
  TwilioLocalVideoView
)

module.exports = TwilioLocalVideoView
