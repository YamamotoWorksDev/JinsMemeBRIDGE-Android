package com.jins_meme.bridge;

import android.util.Log;

import com.jins_jp.meme.MemeRealtimeData;

/**
 *
 * MemeRealtimeDataFilter.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class MemeRealtimeDataFilter {
    private final static int COMMAND_NONE= 0x00;
    private final static int COMMAND_LEFT = 0x01;
    private final static int COMMAND_RIGHT = 0x02;
    private final static int COMMAND_UP = 0x04;
    private final static int COMMAND_DOWN = 0x08;
    private final static int COMMAND_BLINK = 0x10;
    public enum MoveType {
        HEAD,EYE
    }

    private MoveType mMoveType;
    private long mLastTime;
    private int mLastCommand;
    private final int COMMAND_WAIT_TIME = 750;
    MemeRealtimeDataFilter() {
        mMoveType = MoveType.EYE;
        reset();
    }
    public void setMoveType(MoveType type) {
        mMoveType = type;
    }
    public void reset() {
        mLastTime = System.currentTimeMillis();
        mLastCommand = COMMAND_NONE;
    }
    public boolean isLeft() { return isCommand(COMMAND_LEFT); }
    public boolean isRight() { return isCommand(COMMAND_RIGHT); }
    public boolean isUp() { return isCommand(COMMAND_UP); }
    public boolean isDown() { return isCommand(COMMAND_DOWN); }
    public boolean isBlink() { return isCommand(COMMAND_BLINK); }

    public void update(MemeRealtimeData memeRealtimeData, int thresholdBlink, int threshold) {
        update(memeRealtimeData, thresholdBlink, threshold, threshold);
    }

    public void update(MemeRealtimeData memeRealtimeData, int thresholdBlink, int thresholdUD, int thresholdLR) {
        update(memeRealtimeData, thresholdBlink, thresholdUD, thresholdUD, thresholdLR, thresholdLR);
    }
    public void update(MemeRealtimeData memeRealtimeData, int thresholdBlink, int thresholdUp, int thresholdDown, int thresholdLeft, int thresholdRight) {
        if(isWaiting()) {
            mLastCommand = COMMAND_NONE;
            return;
        }
        int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
        if (eyeBlinkStrength > thresholdBlink) {
            setCommand(COMMAND_BLINK);
        }
        switch (mMoveType) {
            case HEAD:
                float accX = memeRealtimeData.getAccX();
                float accY = memeRealtimeData.getAccY();
                if (accX > 5) {
                    setCommand(COMMAND_LEFT);
                }
                if (accX < -3) {
                    setCommand(COMMAND_RIGHT);
                }
                else if(accY < 1) {
                    setCommand(COMMAND_UP);
                }
                else if(accY > 13) {
                    setCommand(COMMAND_DOWN);
                }
                break;
            case EYE:
                if (memeRealtimeData.getEyeMoveLeft() > thresholdLeft) {
                    setCommand(COMMAND_LEFT);
                }
                else if (memeRealtimeData.getEyeMoveRight() > thresholdRight) {
                    setCommand(COMMAND_RIGHT);
                }
                if (memeRealtimeData.getEyeMoveUp() > thresholdUp) {
                    setCommand(COMMAND_UP);
                }
                else if (memeRealtimeData.getEyeMoveDown() > thresholdDown) {
                    setCommand(COMMAND_DOWN);
                }
                break;
        }
        if(mLastCommand != COMMAND_NONE) {
            Log.d("COMMAND", mLastCommand+"");
        }
    }

    private boolean isWaiting() {
        return System.currentTimeMillis() - mLastTime < COMMAND_WAIT_TIME;
    }
    private void setCommand(int command) {
        mLastCommand |= command;
        mLastTime = System.currentTimeMillis();
    }
    private boolean isCommand(int command) {
        return (mLastCommand & command) != 0;
    }
}
