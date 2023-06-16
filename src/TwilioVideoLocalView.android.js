/**
 * Component for Twilio Video local views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import {
  requireNativeComponent,
  View,
  Platform,
  UIManager,
  findNodeHandle,
} from "react-native";
import React from "react";
import PropTypes from "prop-types";

const propTypes = {
  ...View.propTypes,
  // Whether to apply Z ordering to this view.  Setting this to true will cause
  // this view to appear above other Twilio Video views.
  applyZOrder: PropTypes.bool,
  /**
   * How the video stream should be scaled to fit its
   * container.
   */
  scaleType: PropTypes.oneOf(["fit", "fill"]),
};

const nativeEvents = {
  captureFrame: 1,
};

class TwilioVideoPreview extends React.Component {
  runCommand(event, args = []) {
    switch (Platform.OS) {
      case "android":
        UIManager.dispatchViewManagerCommand(
          findNodeHandle(this.refs.localVideoView),
          event,
          args
        );
        break;
      default:
        break;
    }
  }

  captureFrame() {
    this.runCommand(nativeEvents.captureFrame);
  }

  render() {
    return <NativeTwilioVideoPreview ref="localVideoView" {...this.props} />;
  }
}

TwilioVideoPreview.propTypes = propTypes;

const NativeTwilioVideoPreview = requireNativeComponent(
  "RNTwilioVideoPreview",
  TwilioVideoPreview
);

module.exports = TwilioVideoPreview;
