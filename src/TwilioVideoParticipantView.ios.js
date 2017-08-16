//
//  TwilioVideoParticipantView.js
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { requireNativeComponent } from 'react-native'

class TwilioVideoParticipantView extends Component {
  static propTypes = {
    trackIdentifier: PropTypes.shape({
      /**
       * The participant identifier.
       */
      participantIdentity: PropTypes.string.isRequired,
      /**
       * The participant's video track you want to render in the view.
       */
      videoTrackId: PropTypes.string.isRequired
    })
  }

  render () {
    return <RCTTWRemoteVideoView {...this.props}>{this.props.children}</RCTTWRemoteVideoView>
  }
}

const RCTTWRemoteVideoView = requireNativeComponent('RCTTWRemoteVideoView', TwilioVideoParticipantView)

module.exports = TwilioVideoParticipantView
