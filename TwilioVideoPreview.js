import React, { Component } from 'react'
import { requireNativeComponent } from 'react-native'

// requireNativeComponent automatically resolves this to "RCTMapManager"

class TwilioVideoPreview extends Component {

  // constructor(props, context) {
  //   super(props)
  //   // this._onConnect = this._onConnect.bind(this)
  // }

  // _onConnect(event: Event) {
  //   if (!this.props.onConnect) {
  //     return
  //   }
  //   this.props.onConnect(event.nativeEvent.connect)
  // }

  render() {
    return <TWVideoPreview {...this.props}>{this.props.children}</TWVideoPreview> // onConnect={this._onConnect}
  }
}

TwilioVideoPreview.propTypes = {

  // onConnect: React.PropTypes.func,

  /**
   * When this property is set to `true` and a valid camera is associated
   * with the map, the camera’s pitch angle is used to tilt the plane
   * of the map. When this property is set to `false`, the camera’s pitch
   * angle is ignored and the map is always displayed as if the user
   * is looking straight down onto it.
   */
  //pitchEnabled: React.PropTypes.bool,
}


var TWVideoPreview = requireNativeComponent('TWVideoPreview', TwilioVideoPreview)

module.exports = TwilioVideoPreview
