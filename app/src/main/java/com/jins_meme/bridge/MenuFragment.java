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

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import java.util.ArrayList;
import java.util.List;

import static com.jins_meme.bridge.BridgeUIView.*;

/**
 * Created by nariakiiwatani on 2017/01/27.
 */

public class MenuFragment extends Fragment implements IResultListener, MemeConnectListener {
    private static final String APP_ID = "907977722622109";
    private static final String APP_SECRET = "ka53fgrcct043wq3d6tm9gi8a2hetrxz";

    private BridgeUIView mView = null;
    private MyAdapter myAdapter;

    private Handler handler;
    private MemeLib memeLib;
    private MemeMIDI memeMIDI;

    private List<String> scannedMemeList = new ArrayList<>();

    public int getFoundMemeNum() {
        return scannedMemeList.size();
    }
    public List<String> getScannedMemeList() {
        return scannedMemeList;
    }
    public void clearMemeList() {
        if(scannedMemeList != null)
            scannedMemeList.clear();
    }

    @Override
    public void memeConnectCallback(boolean b) {
        Log.d("CONNECT", "meme connected.");

        memeLib.startDataReport(myAdapter);

        memeMIDI = new MemeMIDI(getContext());
        memeMIDI.initPort();
    }

    @Override
    public void memeDisconnectCallback() {
        Log.d("CONNECT", "meme disconnected.");
    }

    public void init() {
        MemeLib.setAppClientID(getContext(), APP_ID, APP_SECRET);
        memeLib = MemeLib.getInstance();
        memeLib.setAutoConnect(false);

        handler = new Handler();
    }

    public void startScan() {
        Log.d("SCAN", "start scannig...");

        memeLib.setMemeConnectListener(this);

        MemeStatus status = memeLib.startScan(new MemeScanListener() {
            @Override
            public void memeFoundCallback(String s) {
                Log.d("SCAN", "found: " + s);

                scannedMemeList.add(s);
            }
        });
    }

    public void stopScan() {
        Log.d("SCAN", "stop scannig...");

        if(memeLib.isScanning()) {
            memeLib.stopScan();

            Log.d("SCAN", "scan stopped.");

            //invalidateOptionsMenu();
        }
    }

    public boolean isMemeConnected() {
        return memeLib.isConnected();
    }
    public void memeConnect(String id) {
        memeLib.connect(id);
    }
    public void memeDisconnect() {
        memeLib.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new BridgeUIView(getContext());
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myAdapter = new MyAdapter(getContext(), this);
        //mView.setAdapter(new MyAdapter(getContext(), this));
        mView.setAdapter(myAdapter);
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

    public class MyAdapter extends BridgeUIView.Adapter<MyAdapter.MyCardHolder> implements MemeRealtimeListener {
        Context mContext;
        LayoutInflater mInflater;
        MyAdapter(Context context, IResultListener listener) {
            super(listener);
            mContext = context;
            mInflater = LayoutInflater.from(context);

            init();
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

            //Log.d("DEBUG", "rotation = " + yaw + ", " + pitch + ", " + roll);

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

        class MyCardHolder extends CardHolder {
            TextView mTextView;
            public MyCardHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.card_text);
            }
        }
    }
}

