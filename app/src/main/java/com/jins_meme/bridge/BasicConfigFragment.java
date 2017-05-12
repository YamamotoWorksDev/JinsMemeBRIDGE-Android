package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

/**
 *
 * SettingFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class BasicConfigFragment extends Fragment {
  Handler handler;

  Switch swScan;
  Switch swConnect;
  Spinner spMemeList;

  ArrayAdapter<String> adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handler = new Handler();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_basicconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    swScan = null;
    swConnect = null;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((MainActivity)getActivity()).setActionBarTitle(getString(R.string.basic_conf) + " SETTING");
    ((MainActivity)getActivity()).setActionBarBack(true);

    swScan = (Switch)view.findViewById(R.id.scan);
    swScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
          Log.d("BASIC", "SCAN Start");
          Toast.makeText(getActivity(), "SCANNING...", Toast.LENGTH_SHORT).show();

          ((MainActivity)getActivity()).startScan();

          adapter.clear();
          //adapter.addAll(((MainActivity)getActivity()).getScannedMemeList());
          spMemeList.setAdapter(adapter);

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              swScan.setChecked(false);

              ((MainActivity)getActivity()).stopScan();

              adapter.addAll(((MainActivity)getActivity()).getScannedMemeList());
            }
          }, 5000);
        }
        else {
          Log.d("BASIC", "SCAN Stop");
          Toast.makeText(getActivity(), "SCAN STOPPED.", Toast.LENGTH_SHORT).show();

          ((MainActivity)getActivity()).stopScan();
        }
      }
    });

    swConnect = (Switch)view.findViewById(R.id.connect);
    swConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
          String memeId = (String)spMemeList.getSelectedItem();
          Log.d("BASIC", "CONNECT Start " + memeId);
          Toast.makeText(getActivity(), "CONNECTING TO " + memeId + "...", Toast.LENGTH_SHORT).show();

          ((MainActivity)getActivity()).connectToMeme(memeId);
        }
        else {
          Log.d("BASIC", "CONNECT Stop");
          Toast.makeText(getActivity(), "DISCONNECTING...", Toast.LENGTH_SHORT).show();

          ((MainActivity)getActivity()).disconnectToMeme();
        }
      }
    });

    spMemeList = (Spinner)view.findViewById(R.id.meme_list);
    adapter = new ArrayAdapter<String>(getContext( ), android.R.layout.simple_spinner_item);
    spMemeList.setAdapter(adapter);
  }
}
