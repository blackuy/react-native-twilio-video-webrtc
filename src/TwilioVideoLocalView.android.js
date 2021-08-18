/**
 * Component for Twilio Video local views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import { requireNativeComponent, View } from 'react-native'
import React from 'react'
import PropTypes from 'prop-types'

const propTypes = {
  ...View.propTypes,
  /**
   * How the video stream should be scaled to fit its
   * container.
   */
  scaleType: PropTypes.oneOf(['fit', 'fill'])
}

class TwilioVideoPreview extends React.Component {
  render () {
    return <NativeTwilioVideoPreview {...this.props} />
  }
}

TwilioVideoPreview.propTypes = propTypes

const NativeTwilioVideoPreview = requireNativeComponent(
  'RNTwilioVideoPreview',
  TwilioVideoPreview
)

module.exports = TwilioVideoPreview
