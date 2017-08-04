//
//  TwilioVideoLocalView.js
//  Black
//
//  Created by Martín Fernández on 6/13/17.
//
//

import React, { Component } from 'react'
import { requireNativeComponent } from 'react-native'
import PropTypes from 'prop-types';

class TwilioVideoLocalView extends Component {
  static propTypes = {
    /**
     * Indicate if video feed is enabled.
     */
    enabled: PropTypes.bool.isRequired
  }

  render() {
    return <RCTTWLocalVideoView {...this.props}>{this.props.children}</RCTTWLocalVideoView>
  }
}

const RCTTWLocalVideoView = requireNativeComponent('RCTTWLocalVideoView', TwilioVideoLocalView)

module.exports = TwilioVideoLocalView
