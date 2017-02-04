package com.jins_meme.bridge;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;

import static com.jins_meme.bridge.BridgeUIView.CardHolder;
import static com.jins_meme.bridge.BridgeUIView.IResultListener;

/**
 *
 * MenuFragment.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class MenuFragment extends Fragment implements IResultListener, MemeRealtimeListener {
    private BridgeUIView mView = null;
    private MyAdapter myAdapter;

    private Handler handler = new Handler();

    private MemeMIDI memeMIDI;
    private MemeOSC memeOSC;
    private MemeBTSPP memeBTSPP;

    private MemeRealtimeDataFilter memeFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new BridgeUIView(getContext());
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myAdapter = new MyAdapter(getContext(), this);
        mView.setAdapter(myAdapter);

        // Initialize MIDI
        memeMIDI = new MemeMIDI(getContext());
        memeMIDI.initPort();

        // Initialize OSC
        memeOSC = new MemeOSC();
        //memeOSC.setRemoteIP("192.168.1.255");
        memeOSC.setRemoteIP("172.24.133.255");// JINS Hackason Guest Wi-Fi Broadcast
        memeOSC.setRemotePort(10316);
        memeOSC.setHostPort(11316);
        memeOSC.initSocket();

        // Initialize BTSPP
        memeBTSPP = new MemeBTSPP();

        memeFilter = new MemeRealtimeDataFilter();
//        memeFilter.setMoveType(MemeRealtimeDataFilter.MoveType.HEAD);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("FRAGMENT", "onStop...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(memeMIDI != null) {
            memeMIDI.closePort();
            memeMIDI = null;
        }

        if(memeOSC != null) {
            memeOSC.closeSocket();
            memeOSC = null;
        }

        Log.d("FRAGMENT", "onDestroy...");
    }

    @Override
    public void onEnterCard(int id) {
        Log.d("ENTER", getResources().getString(id));
    }
    @Override
    public void onExitCard(int id) {
        Log.d("EXIT", getResources().getString(id));
    }
    @Override
    public void onEndCardSelected(int id) {
        Log.d("RESULT", getResources().getString(id));
        {
            // MIDI?
            int note = 60;
            switch (id) {
                case R.string.noteon_67: ++note;
                case R.string.noteon_66: ++note;
                case R.string.noteon_65: ++note;
                case R.string.noteon_64: ++note;
                case R.string.noteon_63: ++note;
                case R.string.noteon_62: ++note;
                case R.string.noteon_61: ++note;
                case R.string.noteon_60:
                    final int finalNote = note;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("DEBUG", "note on " + finalNote);
                            memeMIDI.sendNote(1, finalNote, 127);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                Log.d("DEBUG", "note off " + finalNote);
                                memeMIDI.sendNote(1, finalNote, 0);
                            }
                        }
                    }).start();
                    break;
            }
        }
        {
            // OSC?
            switch(id) {
                case R.string.osc_220hz:
                    Log.d("DEBUG", "set freq 220");
                    memeOSC.setAddress(MemeOSC.PREFIX,"/frequency");
                    memeOSC.setTypeTag("i");
                    memeOSC.addArgument(220);
                    memeOSC.flushMessage();
                    break;
                case R.string.osc_440hz:
                    Log.d("DEBUG", "set freq 440");
                    memeOSC.setAddress(MemeOSC.PREFIX,"/frequency");
                    memeOSC.setTypeTag("i");
                    memeOSC.addArgument(440);
                    memeOSC.flushMessage();
                    break;
                case R.string.osc_mute_on:
                    Log.d("DEBUG", "mute on");
                    memeOSC.setAddress(MemeOSC.PREFIX,"/volume");
                    memeOSC.setTypeTag("f");
                    memeOSC.addArgument(0);
                    memeOSC.flushMessage();
                    break;
                case R.string.osc_mute_off:
                    Log.d("DEBUG", "mute off");
                    memeOSC.setAddress(MemeOSC.PREFIX,"/volume");
                    memeOSC.setTypeTag("f");
                    memeOSC.addArgument(1);
                    memeOSC.flushMessage();
                    break;
            }
        }
    }

    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
        float accelX = memeRealtimeData.getAccX();
        float accelY = memeRealtimeData.getAccY();
        float accelZ = memeRealtimeData.getAccZ();
        int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
        int eyeBlinkSpeed    = memeRealtimeData.getBlinkSpeed();
        int eyeUp    = memeRealtimeData.getEyeMoveUp();
        int eyeDown  = memeRealtimeData.getEyeMoveDown();
        int eyeLeft  = memeRealtimeData.getEyeMoveLeft();
        int eyeRight = memeRealtimeData.getEyeMoveRight();

        float yaw   = memeRealtimeData.getYaw();
        float pitch = memeRealtimeData.getPitch();
        float roll  = memeRealtimeData.getRoll();

        memeBTSPP.sendAngle(yaw, pitch, roll);

        //debug Log.d("DEBUG", "accel  = " + accelX + ", " + accelY + ", " + accelZ);
        //debug Log.d("DEBUG", "rotation  = " + yaw + ", " + pitch + ", " + roll);

        if(memeOSC.initializedSocket()) {
            //debug Log.d("DEBUG", "osc bundle.");

            memeOSC.createBundle();

            memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.ACCEL);
            memeOSC.setTypeTag("fff");
            memeOSC.addArgument(accelX);
            memeOSC.addArgument(accelY);
            memeOSC.addArgument(accelZ);
            memeOSC.setMessageSizeToBundle();
            memeOSC.addMessageToBundle();

            memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.ANGLE);
            memeOSC.setTypeTag("fff");
            memeOSC.addArgument(yaw);
            memeOSC.addArgument(pitch);
            memeOSC.addArgument(roll);
            memeOSC.setMessageSizeToBundle();
            memeOSC.addMessageToBundle();

            memeOSC.flushBundle();
        }

        int iyaw   = (int)((yaw / 90.0) * 127.0);
        int ipitch = (int)((pitch / 90.0) * 127.0);
        int iroll  = (int)((roll / 90.0) * 127.0);

        //debug Log.d("DEBUG", "irotation = " + iyaw + ", " + ipitch + ", " + iroll);

        if(iyaw >= 0)
            memeMIDI.sendControlChange(1, 30, iyaw);
        else
            memeMIDI.sendControlChange(1, 31, -iyaw);

        if(ipitch >= 0)
            memeMIDI.sendControlChange(1, 32, ipitch);
        else
            memeMIDI.sendControlChange(1, 33, -ipitch);

        if(iroll >= 0)
            memeMIDI.sendControlChange(1, 34, iroll);
        else
            memeMIDI.sendControlChange(1, 35, -iroll);

        memeFilter.update(memeRealtimeData);
        if(memeFilter.isBlink()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mView.enter();
                }
            });
        }
        if(memeFilter.isLeft()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mView.move(-1);
                }
            });
        }
        if(memeFilter.isRight()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mView.move(1);
                }
            });
        }
        if(eyeBlinkStrength > 0 || eyeBlinkSpeed > 0) {
            Log.d("EYE", String.format("meme: BLINK = %d/%d", eyeBlinkStrength, eyeBlinkSpeed));

            memeBTSPP.sendEyeBlink(eyeBlinkStrength, eyeBlinkSpeed);
        }

        if(eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
            Log.d("EYE", String.format("meme: UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeUp, eyeDown, eyeLeft, eyeRight));

            memeBTSPP.sendEyeMove(eyeUp, eyeDown, eyeLeft, eyeRight);
        }
    }

    public void btConnect(String name) {
        memeBTSPP.connect(name);
    }

    public void btDisconnect() {
        memeBTSPP.disconnect();
    }

    public String[] getBtPairedDeviceName() {
        return memeBTSPP.getPairedDeviceName();
    }

    public String getBtConnectedDeviceName() {
        return memeBTSPP.getConnectedDeviceName();
    }

    public class MyAdapter extends BridgeUIView.Adapter<MyAdapter.MyCardHolder> {
        Context mContext;
        LayoutInflater mInflater;
        MyAdapter(Context context, IResultListener listener) {
            super(listener);
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public MyCardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
            return new MyCardHolder(mInflater.inflate(R.layout.card_sample, parent, false));
        }

        @Override
        public CardFunction getCardFunction(int id) {
            switch(id) {
                case R.string.back:
                    return CardFunction.BACK;
                case R.string.midi:
                case R.string.osc:
                    return CardFunction.ENTER_MENU;
            }
            return CardFunction.END;
        }

        @Override
        public void onBindCardHolder(MyCardHolder cardHolder, int id) {
            cardHolder.mTextView.setText(getResources().getString(id));
        }
        @Override
        public int getCardId(int parent_id, int position) {
            int id = NO_ID;
            switch (parent_id) {
                case NO_ID:
                    switch(position) {
                        case 0: id = R.string.midi; break;
                        case 1: id = R.string.osc; break;
                    }
                    break;
                case R.string.midi:
                    if(position < 8) {
                        id = getResources().getIdentifier("noteon_6"+position, "string", mContext.getPackageName());
                    }
                    else {
                        id = R.string.back;
                    }
                    break;
                case R.string.osc:
                    switch (position) {
                        case 0: id = R.string.osc_220hz; break;
                        case 1: id = R.string.osc_440hz; break;
                        case 2: id = R.string.osc_mute_on; break;
                        case 3: id = R.string.osc_mute_off; break;
                        case 4: id = R.string.back; break;
                    }
                    break;
            }
            return id;
        }
        @Override
        public int getChildCardCount(int parent_id) {
            switch(parent_id) {
                case R.string.midi: return 9;
                case R.string.osc: return 5;
                case NO_ID: return 2;
            }
            return 0;
        }

        class MyCardHolder extends CardHolder {
            TextView mTextView;
            MyCardHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.card_text);
            }
        }
    }
}

