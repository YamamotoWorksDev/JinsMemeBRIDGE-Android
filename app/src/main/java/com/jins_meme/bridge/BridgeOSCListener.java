package com.jins_meme.bridge;

import android.util.Log;

import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;

/**
 * Created by shun on 2017/01/31.
 */

public class BridgeOSCListener implements MemeRealtimeListener {
  private MemeOSC memeOSC;

  public void init() {
    memeOSC = new MemeOSC();
    memeOSC.setRemoteIP("192.168.1.255");
    memeOSC.setRemotePort(10316);
    memeOSC.setHostPort(11316);
    memeOSC.initSocket();
  }

  @Override
  public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
    int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
    int eyeBlinkSpeed    = memeRealtimeData.getBlinkSpeed();
    int eyeUp    = memeRealtimeData.getEyeMoveUp();
    int eyeDown  = memeRealtimeData.getEyeMoveDown();
    int eyeLeft  = memeRealtimeData.getEyeMoveLeft();
    int eyeRight = memeRealtimeData.getEyeMoveRight();

    if(eyeBlinkStrength > 0 || eyeBlinkSpeed > 0 || eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
      Log.d("EYE", String.format("meme: BLINK = %d/%d, UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeBlinkStrength, eyeBlinkSpeed, eyeUp, eyeDown, eyeLeft, eyeRight));

      memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.EYE_BLINK);
      memeOSC.setTypeTag("ii");
      memeOSC.addArgument(eyeBlinkStrength);
      memeOSC.addArgument(eyeBlinkSpeed);
      memeOSC.flushMessage();
    }
  }
}
