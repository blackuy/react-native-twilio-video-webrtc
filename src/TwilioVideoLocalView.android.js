/**
 * Component for Twilio Video participant views.
 *
 * Authors:
 *   Jonathan Chang <slycoder@gmail.com>
 */

import {
  requireNativeComponent,
  UIManager,
  findNodeHandle,
  View,
} from "react-native";
import PropTypes from "prop-types";
import React from "react";

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

  trackId: PropTypes.string.isRequired,
  enabled: PropTypes.bool,
};

class TwilioLocalVideoView extends React.Component {
  _nextRequestId = 1;
  _requestMap = new Map();

  _onDataReturned = (event) => {
    // We grab the relevant data out of our event.
    let { requestId, result, error } = event.nativeEvent;
    // Then we get the promise we saved earlier for the given request ID.
    let promise = this._requestMap[requestId];
    if (result) {
      // If it was successful, we resolve the promise.
      promise.resolve(result);
    } else {
      // Otherwise, we reject it.
      promise.reject(error);
    }
    // Finally, we clean up our request map.
    this._requestMap.delete(requestId);
  };

  async takeSnapshot() {
    // Grab a new request ID and our request map.
    let requestId = this._nextRequestId++;
    let requestMap = this._requestMap;

    // We create a promise here that will be resolved once `_onRequestDone` is
    // called.
    let promise = new Promise(function (resolve, reject) {
      requestMap[requestId] = { resolve: resolve, reject: reject };
    });

    // Now just dispatch the command as before, adding the request ID to the
    // parameters.
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this._ref),
      //@TODO: find out why UIManager.NativeTwilioLocalVideoView.Commands.takeSnapshot doesn't work
      1,
      // UIManager.NativeTwilioLocalVideoView.Commands.takeSnapshot,
      [requestId]
    );

    return promise;
  }

  render() {
    return (
      <NativeTwilioLocalVideoView
        {...this.props}
        enabled={this.props.enabled ?? false}
        ref={(ref) => (this._ref = ref)}
        onDataReturned={this._onDataReturned}
      />
    );
  }
}

const NativeTwilioLocalVideoView = requireNativeComponent(
  "RNTwilioLocalVideoView",
  TwilioLocalVideoView
);

NativeTwilioLocalVideoView.propTypes = propTypes;

module.exports = TwilioLocalVideoView;
