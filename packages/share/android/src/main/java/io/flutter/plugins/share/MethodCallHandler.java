// Copyright 2019 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.share;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import java.util.Map;

/** Handles the method calls for the plugin. */
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
          shareFile(
              (String) call.argument("path"),
              (String) call.argument("mimeType"),
              (String) call.argument("subject"),
              (String) call.argument("text"));
          result.success(null);
        } catch (IOException e) {
          result.error(e.getMessage(), null, null);
        }
      else {
        result.notImplemented();
      }
    }
  }

  private void expectMapArguments(MethodCall call) throws IllegalArgumentException {
    if (!(call.arguments instanceof Map)) {
      throw new IllegalArgumentException("Map argument expected");
    }
  }

  private void shareFile(String path, String mimeType, String subject, String text)
      throws IOException {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Non-empty path expected");
    }

    File file = new File(path);
    clearExternalShareFolder();
    if (!fileIsOnExternal(file)) {
      file = copyToExternalShareFolder(file);
    }

    Uri fileUri =
        FileProvider.getUriForFile(
            mRegistrar.context(),
            mRegistrar.context().getPackageName() + ".flutter.share_provider",
            file);

    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
    if (subject != null) shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    if (text != null) shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    shareIntent.setType(mimeType != null ? mimeType : "*/*");
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);
    if (mRegistrar.activity() != null) {
      mRegistrar.activity().startActivity(chooserIntent);
    } else {
      chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mRegistrar.context().startActivity(chooserIntent);
    }
  }

}

