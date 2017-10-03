/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import {
  requireNativeComponent,
  View
} from 'react-native'
import React from 'react'

const propTypes = {
  ...View.propTypes
}

class TwilioRemotePreview extends React.Component {
  render () {
    return (
      <NativeTwilioRemotePreview {...this.props} />
    )
  }
}

TwilioRemotePreview.propTypes = propTypes

const NativeTwilioRemotePreview = requireNativeComponent(
  'RNTwilioRemotePreview',
  TwilioRemotePreview
)

module.exports = TwilioRemotePreview
