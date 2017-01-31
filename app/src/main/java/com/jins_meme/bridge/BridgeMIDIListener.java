package com.jins_meme.bridge;

import android.content.Context;
import android.util.Log;

import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;

/**
 *
 * Copylight (C) 2017, Shunichi Yamamoto, tkrworks.net
 *
 * This file is part of MemeBRIDGE.
 *
 **/

public class BridgeMIDIListener implements MemeRealtimeListener {
  MemeMIDI memeMIDI;

  public void init(Context context) {
    memeMIDI = new MemeMIDI(context);
    memeMIDI.initPort();
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

      if(memeMIDI.isInitializedMidi()) {
        memeMIDI.sendControlChange(1, MemeMIDI.EYE_UP, eyeUp);
        memeMIDI.sendControlChange(1, MemeMIDI.EYE_DOWN, eyeDown);
      }
    }
  }
}
