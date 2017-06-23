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
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.example.android.camera2basic.Camera2BasicFragment;
import com.jins_meme.bridge.BridgeUIView.Adapter;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraMenuFragment extends MenuFragmentBase implements MemeRealtimeDataFilter.MemeFilteredDataCallback, IResultListener, Camera2BasicFragment.IListener {

    private static final String[] REQUIED_PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Camera2BasicFragment mCamera;
    private OnFragmentInteractionListener mListener;
    Handler mHandler = new Handler();

    public CameraMenuFragment() {
        // Required empty public constructor
    }

    @Override
    protected Adapter createAdapter() {
        return new CardAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mCamera = Camera2BasicFragment.newInstance();
        mCamera.setListener(this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.camera, mCamera).commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCamera = null;
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isPermissionRequested) {
            if(checkIfAllRequiedPermissionGranted()) {
                mCamera.reopenCamera();
            }
            else {
                mHandler.post(new Runnable() {
                    public void run() {
                        mListener.backToPreviousMenu();
                    }
                });
            }
        }
        isPermissionRequested = false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) {
            mCamera.closeCamera();
        }
        else {
            if (checkIfAllRequiedPermissionGranted()) {
                mCamera.reopenCamera();
            }
            else {
                requestCameraPermission(getPermissionsNotGrantedYet());
            }
        }
    }
    public void shoot() {
        if(!isCameraReady()) {
            return;
        }
        mCamera.takePicture();
    }

    public void toggleFrontRear() {
        if(!isCameraReady()) {
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

    private boolean isCameraOpened = false;
    @Override
    public void onCameraOpened() {
        isCameraOpened = true;
    }

    @Override
    public void onCameraClosed() {
        isCameraOpened = false;
    }

    boolean isCameraReady() {
        return mCamera != null && isCameraOpened && !mCamera.isCameraProcessing();
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

    private void requestCameraPermission(String[] permissions) {
//        if (shouldShowRequestAnyPermissionRationale(permissions)) {
//            new ConfirmationDialog(permissions).show(getChildFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            requestPermissions(permissions, getResources().getInteger(R.integer.PERMISSION_REQUEST_CODE_CAMERA));
//        }
        isPermissionRequested = true;
        requestPermissions(permissions, getResources().getInteger(R.integer.PERMISSION_REQUEST_CODE_CAMERA));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode == getResources().getInteger(R.integer.PERMISSION_REQUEST_CODE_CAMERA)) {
            for(int i = 0; i < grantResults.length; ++i) {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkIfAllRequiedPermissionGranted() {
        for(String permission : REQUIED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private String[] getPermissionsNotGrantedYet() {
        ArrayList<String> ret = new ArrayList<String>();
        for(String permission : REQUIED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                ret.add(permission);
            }
        }
        return ret.toArray(new String[]{});
    }
    private boolean shouldShowRequestAnyPermissionRationale(String[] permissions) {
        for(String permission : permissions) {
            if(shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPermissionRequested = false;


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
