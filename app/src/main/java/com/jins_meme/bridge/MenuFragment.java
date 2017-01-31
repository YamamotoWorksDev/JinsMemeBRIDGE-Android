package com.jins_meme.bridge;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.jins_meme.bridge.BridgeUIView.*;

/**
 * Created by nariakiiwatani on 2017/01/27.
 */

public class MenuFragment extends Fragment implements IResultListener {
    private BridgeUIView mView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new BridgeUIView(getContext());
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mView.setAdapter(new MyAdapter(getContext(), this));
    }


    @Override
    public void onBridgeMenuFinished(int id) {
        Log.d("RESULT", getResources().getString(id));
        mView.reset();
    }

    public class MyAdapter extends Adapter<MyAdapter.MyCardHolder> {
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
            public MyCardHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.card_text);
            }
        }
    }
}

