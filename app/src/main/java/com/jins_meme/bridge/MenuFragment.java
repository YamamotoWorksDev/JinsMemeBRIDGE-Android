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
        memeOSC.setRemoteIP("192.168.1.255");
        memeOSC.setRemotePort(10316);
        memeOSC.setHostPort(11316);
        memeOSC.initSocket();
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("FRAGMENT", "onStop...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        memeMIDI.closePort();
        memeMIDI = null;

        memeOSC.closeSocket();
        memeOSC = null;

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
    public void onBridgeMenuFinished(int id) {
        Log.d("RESULT", getResources().getString(id));
        mView.reset();

        switch(getResources().getString(id)) {
            case "midi on":
                Log.d("DEBUG", "note on 30");
                memeMIDI.sendNote(1, 30, 127);
                break;
            case "midi off":
                Log.d("DEBUG", "note off 30");
                memeMIDI.sendNote(1, 30, 0);
                break;
            case "osc on":
                Log.d("DEBUG", "note on 31");
                memeMIDI.sendNote(1, 31, 127);
                break;
            case "osc off":
                Log.d("DEBUG", "note off 31");
                memeMIDI.sendNote(1, 31, 0);
                break;
        }
    }

    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
        int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
        int eyeBlinkSpeed    = memeRealtimeData.getBlinkSpeed();
        int eyeUp    = memeRealtimeData.getEyeMoveUp();
        int eyeDown  = memeRealtimeData.getEyeMoveDown();
        int eyeLeft  = memeRealtimeData.getEyeMoveLeft();
        int eyeRight = memeRealtimeData.getEyeMoveRight();

        float yaw   = memeRealtimeData.getYaw();
        float pitch = memeRealtimeData.getPitch();
        float roll  = memeRealtimeData.getRoll();

        Log.d("DEBUG", "rotation  = " + yaw + ", " + pitch + ", " + roll);

        if(memeOSC.initializedSocket()) {
            memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.ANGLE);
            memeOSC.setTypeTag("fff");
            memeOSC.addArgument(yaw);
            memeOSC.addArgument(pitch);
            memeOSC.addArgument(roll);
            memeOSC.flushMessage();
        }

        int iyaw   = (int)((yaw / 90.0) * 127.0);
        int ipitch = (int)((pitch / 90.0) * 127.0);
        int iroll  = (int)((roll / 90.0) * 127.0);

        Log.d("DEBUG", "irotation = " + iyaw + ", " + ipitch + ", " + iroll);

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

        if(eyeBlinkStrength > 0 || eyeBlinkSpeed > 0 || eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
            Log.d("EYE", String.format("meme: BLINK = %d/%d, UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeBlinkStrength, eyeBlinkSpeed, eyeUp, eyeDown, eyeLeft, eyeRight));

            if(eyeBlinkStrength > 10 || eyeUp == 3) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.enter();
                    }
                });
            }
            else if(eyeLeft > 0) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.move(-1);
                    }
                });
            }
            else if(eyeRight > 0) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.move(1);
                    }
                });
            }

            /*
            if(memeMIDI.isInitializedMidi()) {
                memeMIDI.sendControlChange(1, MemeMIDI.EYE_UP, eyeUp);
                memeMIDI.sendControlChange(1, MemeMIDI.EYE_DOWN, eyeDown);
            }
            */
        }
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
                    switch (position) {
                        case 0: id = R.string.back; break;
                        case 1: id = R.string.midi_on; break;
                        case 2: id = R.string.midi_off; break;
                    }
                    break;
                case R.string.osc:
                    switch (position) {
                        case 0: id = R.string.back; break;
                        case 1: id = R.string.osc_on; break;
                        case 2: id = R.string.osc_off; break;
                    }
                    break;
            }
            return id;
        }
        @Override
        public int getChildCardCount(int parent_id) {
            switch(parent_id) {
                case R.string.midi: return 3;
                case R.string.osc: return 3;
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

