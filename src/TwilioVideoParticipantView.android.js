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
      videoTrackId: PropTypes.string.isRequired
    }),
    trackId: PropTypes.string,
    renderToHardwareTextureAndroid: PropTypes.string,
    onLayout: PropTypes.string,
    accessibilityLiveRegion: PropTypes.string,
    accessibilityComponentType: PropTypes.string,
    importantForAccessibility: PropTypes.string,
    accessibilityLabel: PropTypes.string,
    nativeID: PropTypes.string,
    testID: PropTypes.string
  }

  render () {
    const { trackIdentifier } = this.props
    return <NativeTwilioRemotePreview trackId={trackIdentifier && trackIdentifier.videoTrackId} {...this.props} />
  }
}

const NativeTwilioRemotePreview = requireNativeComponent(
  'RNTwilioRemotePreview',
  TwilioRemotePreview
)

module.exports = TwilioRemotePreview
