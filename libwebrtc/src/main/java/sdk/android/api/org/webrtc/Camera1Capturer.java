/*
 *  Copyright 2016 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import android.content.Context;


public class Camera1Capturer extends CameraCapturer {
  private final boolean captureToTexture;

  public Camera1Capturer(
      String cameraName, CameraEventsHandler eventsHandler, boolean captureToTexture) {
    super(cameraName, eventsHandler, new Camera1Enumerator(captureToTexture));

    this.captureToTexture = captureToTexture;
  }

  @Override
  protected void createCameraSession(CameraSession.CreateSessionCallback createSessionCallback,
      CameraSession.Events events, Context applicationContext,
      SurfaceTextureHelper surfaceTextureHelper, String cameraName, int width, int height,
      int framerate, byte[] byteFrame) {

    //todo
    Camera1Session.create(createSessionCallback, events, captureToTexture, applicationContext,
        surfaceTextureHelper, 1, width, height,
        framerate);
  }

  @Override
  protected void createCameraSession1(CameraSession.CreateSessionCallback createSessionCallback, CameraSession.Events events, Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, String cameraName, int width, int height, int framerate, byte[] byteFrame) {

//    Camera1Session.create1(createSessionCallback, events, captureToTexture, applicationContext,
//            surfaceTextureHelper, Camera1Enumerator.getCameraIndex(cameraName), width, height,
//            framerate);

  }
}
