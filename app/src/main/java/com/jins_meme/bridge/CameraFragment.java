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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.camera2basic.Camera2BasicFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment implements MemeRealtimeDataFilter.MemeFilteredDataCallback {

    Camera2BasicFragment mCamera;
    private OnFragmentInteractionListener mListener;

    public CameraFragment() {
        // Required empty public constructor
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera.isCameraProcessing()) {
                    return;
                }
                switch(v.getId()) {
                    case R.id.back:
                        mListener.backToPreviousMenu();
                        break;
                    case R.id.take:
                        mCamera.takePicture();
                        break;
                    case R.id.toggle:
                        toggleCamera();
                        break;
                }
            }
        };
        view.findViewById(R.id.back).setOnClickListener(listener);
        view.findViewById(R.id.take).setOnClickListener(listener);
        view.findViewById(R.id.toggle).setOnClickListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
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

    @Override
    public void onMemeBlinked() {
        if(mCamera.isCameraProcessing()) {
            return;
        }
        mCamera.takePicture();
    }

    @Override
    public void onMemeMoveLeft() {
        if(mCamera.isCameraProcessing()) {
            return;
        }
        mListener.backToPreviousMenu();
    }

    @Override
    public void onMemeMoveRight() {
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

    public interface OnFragmentInteractionListener {
        void backToPreviousMenu();
    }
}
