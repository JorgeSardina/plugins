// Copyright 2019 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.share;

import java.io.IOException;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * Handles the method calls for the plugin.
 */
class MethodCallHandler implements MethodChannel.MethodCallHandler {

  private Share share;

  MethodCallHandler(Share share) {
    this.share = share;
  }


  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    if (call.method.equals("share")) {
      if (!(call.arguments instanceof Map)) {
        throw new IllegalArgumentException("Map argument expected");
      }
      // Android does not support showing the share sheet at a particular point on screen.
      share.share((String) call.argument("text"), (String) call.argument("subject"));
      result.success(null);
    } else if (call.method.equals("shareFile")) {
      expectMapArguments(call);
      // Android does not support showing the share sheet at a particular point on screen.
      try {
        share.shareFile(
                (String) call.argument("path"),
                (String) call.argument("mimeType"),
                (String) call.argument("subject"),
                (String) call.argument("text"));
        result.success(null);
      } catch (IOException e) {
        result.error(e.getMessage(), null, null);
      }
    } else {
      result.notImplemented();
    }

  }

  private void expectMapArguments(MethodCall call) throws IllegalArgumentException {
    if (!(call.arguments instanceof Map)) {
      throw new IllegalArgumentException("Map argument expected");
    }
  }


}

