package com.jins_meme.bridge;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.android.camera2basic.Camera2BasicFragment;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Camera2BasicFragment implements MemeRealtimeListener {

    private MemeRealtimeDataFilter mMemeFilter;

    public CameraFragment() {
        // Required empty public constructor
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCameraProcessing()) {
                    return;
                }
                switch(v.getId()) {
                    case R.id.back:
                        endFragment();
                        break;
                    case R.id.take:
                        takePicture();
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        fragment.mMemeFilter = new MemeRealtimeDataFilter();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    // JinsMemeからのデータ処理部
    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
        mMemeFilter.update(memeRealtimeData, 30, 0);
        if(isCameraProcessing()) {
            return;
        }
        if(mMemeFilter.isBlink()) {
            takePicture();
        }
        else if(mMemeFilter.isLeft()) {
            endFragment();
        }
        else if(mMemeFilter.isRight()) {
            toggleCamera();
        }
    }

    private void toggleCamera() {
        Integer lens = getCurrentLensFacing();
        switch(lens) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                openCamera(CameraCharacteristics.LENS_FACING_BACK);
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                openCamera(CameraCharacteristics.LENS_FACING_FRONT);
                break;
        }
    }
    private void endFragment() {
        //getFragmentManager().popBackStack();
        ((MainActivity) getActivity()).transitToMain(-1);
    }
}
