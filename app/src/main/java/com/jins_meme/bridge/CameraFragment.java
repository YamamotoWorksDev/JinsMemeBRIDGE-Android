package com.jins_meme.bridge;

import android.content.Context;
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
public class CameraFragment extends Fragment implements MemeRealtimeListener {

    private Camera2BasicFragment mCamera;
    private MemeRealtimeDataFilter mMemeFilter;

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraFragment.
     */
    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mCamera = (Camera2BasicFragment)getFragmentManager().findFragmentById(R.id.fragment);
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // JinsMemeからのデータ処理部
    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
        mMemeFilter.update(memeRealtimeData, 30, 0);
        if(mMemeFilter.isBlink()) {
        }
        else if(mMemeFilter.isLeft()) {
        }
        else if(mMemeFilter.isRight()) {
        }
    }

    // CameraFragmentListenerからactivityへの通知イベント関連
    public enum CameraFragmentEvent {
        EXIT_CAMERA {
            @Override
            public void apply(AppCompatActivity activity) {
                activity.getFragmentManager().popBackStack();
            }
        };
        abstract public void apply(AppCompatActivity activity);
    }
    public interface CameraFragmentListener {
        void onCameraFragmentEnd(CameraFragmentEvent event);
    }
    private CameraFragmentListener mListener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CameraFragmentListener) {
            mListener = (CameraFragmentListener)context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement CameraFragmentListener");
        }
        mMemeFilter = new MemeRealtimeDataFilter();
    }
}
