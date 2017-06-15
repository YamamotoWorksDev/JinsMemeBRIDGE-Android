/**
 * MenuFragment.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.example.android.camera2basic.Camera2BasicFragment;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraMenuFragment extends MenuFragmentBase implements MemeRealtimeDataFilter.MemeFilteredDataCallback, IResultListener  {

    Camera2BasicFragment mCamera;
    private OnFragmentInteractionListener mListener;

    public CameraMenuFragment() {
        // Required empty public constructor
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardAdapter cardAdapter = new CardAdapter(getContext(), this);
        mView.setAdapter(cardAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View uiView = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (LinearLayout)inflater.inflate(R.layout.fragment_camera, container, false);
        parent.addView(uiView);
        ((LayoutParams) uiView.getLayoutParams()).height = 0;
        ((LayoutParams) uiView.getLayoutParams()).weight = 1;
        // Inflate the layout for this fragment
        return parent;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RootMenuFragment.OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnFragmentInteractionListener");
        }
        mCamera = new Camera2BasicFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.camera, mCamera).commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCamera = null;
        mListener = null;
    }

    public void shoot() {
        if(mCamera.isCameraProcessing()) {
            return;
        }
        mCamera.takePicture();
    }

    public void toggleFrontRear() {
        if(mCamera.isCameraProcessing()) {
            return;
        }
        toggleCamera();
    }


    private void toggleCamera() {
        Integer lens = mCamera.getCurrentLensFacing();
        switch(lens) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                mCamera.openCamera(CameraCharacteristics.LENS_FACING_BACK);
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                mCamera.openCamera(CameraCharacteristics.LENS_FACING_FRONT);
                break;
        }
    }

    @Override
    public void onEnterCard(int id) {
    }

    @Override
    public void onExitCard(int id) {

    }

    @Override
    public void onEndCardSelected(int id) {
        switch(id) {
            case R.string.photoshoot:
                shoot();
                break;
            case R.string.switch_fr:
                toggleFrontRear();
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void backToPreviousMenu();
    }

    private class CardAdapter extends BridgeUIView.Adapter<BridgeUIView.CardHolder> {

        Context mContext;
        LayoutInflater mInflater;

        CardAdapter(Context context, IResultListener listener) {
            super(listener);
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public CardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
            return new MyCardHolder(mInflater.inflate(R.layout.card_smalltext, parent, false));
        }

        @Override
        public void onBindCardHolder(CardHolder cardHolder, int id) {
            MyCardHolder ch = (MyCardHolder)cardHolder;
            ch.mTitle.setText(getResources().getString(id));
        }


        @Override
        public CardFunction getCardFunction(int id) {
            return CardFunction.END;
        }

        @Override
        public int getCardId(int parent_id, int position) {
            int id = NO_ID;
            switch (position) {
                case 0:
                    id = R.string.photoshoot;
                    break;
                case 1:
                    id = R.string.switch_fr;
                    break;
            }
            return id;
        }

        @Override
        public int getChildCardCount(int parent_id) {
            switch (parent_id) {
                case NO_ID:
                    return 2;
            }
            return 0;
        }

        @Override
        public int getCardType(int id) {
            return R.integer.CARD_TYPE_ONLY_TITLE;
        }

        private class MyCardHolder extends CardHolder {

            TextView mTitle;
            TextView mValue;
            Handler mHandler = new Handler();

            MyCardHolder(View itemView) {
                super(itemView);

                mTitle = (TextView) itemView.findViewById(R.id.card_text);
                mValue = (TextView) itemView.findViewById(R.id.card_select);
            }

            void setText(String text) {
                //mValue.setText(getString(R.string.selected));
                mValue.setText(text);
            }

            void setText(String text, int msec) {
                //mValue.setText(getString(R.string.selected));
                mValue.setText(text);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mValue.setText(" ");
                    }
                }, msec);
            }

            void clearText() {
                mValue.setText(" ");
            }
        }

    }}
