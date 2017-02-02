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
    public enum Command {
        LEFT,RIGHT,UP,DOWN,BLINK,NONE
    }
    public enum MoveType {
        HEAD,EYE
    }

    private MoveType mMoveType;
    private long mLastTime;
    private Command mLastCommand;
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
        mLastCommand = Command.NONE;
    }
    public boolean isLeft() { return isCommand(Command.LEFT); }
    public boolean isRight() { return isCommand(Command.RIGHT); }
    public boolean isUp() { return isCommand(Command.UP); }
    public boolean isDown() { return isCommand(Command.DOWN); }
    public boolean isBlink() { return isCommand(Command.BLINK); }
    public void update(MemeRealtimeData memeRealtimeData) {
        if(isWaiting()) {
            mLastCommand = Command.NONE;
            return;
        }
        int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
        if (eyeBlinkStrength > 30) {
            setCommand(Command.BLINK);
            return;
        }
        switch (mMoveType) {
            case HEAD:
                float accX = memeRealtimeData.getAccX();
                float accY = memeRealtimeData.getAccY();
                if (accX > 5) {
                    setCommand(Command.LEFT);
                }
                else if (accX < -3) {
                    setCommand(Command.RIGHT);
                }
                else if(accY < 1) {
                    setCommand(Command.UP);
                }
                else if(accY > 13) {
                    setCommand(Command.DOWN);
                }
                break;
            case EYE:
                if (memeRealtimeData.getEyeMoveLeft() > 0) {
                    setCommand(Command.LEFT);
                }
                else if (memeRealtimeData.getEyeMoveRight() > 0) {
                    setCommand(Command.RIGHT);
                }
                else if (memeRealtimeData.getEyeMoveUp() > 0) {
                    setCommand(Command.UP);
                }
                else if (memeRealtimeData.getEyeMoveDown() > 0) {
                    setCommand(Command.DOWN);
                }
                break;
        }
        if(mLastCommand != Command.NONE) {
            Log.d("COMMAND", mLastCommand+"");
        }
    }
    private boolean isWaiting() {
        return System.currentTimeMillis() - mLastTime < COMMAND_WAIT_TIME;
    }
    private void setCommand(Command command) {
        mLastCommand = command;
        mLastTime = System.currentTimeMillis();
    }
    private boolean isCommand(Command command) {
        return mLastCommand == command;
    }
}
