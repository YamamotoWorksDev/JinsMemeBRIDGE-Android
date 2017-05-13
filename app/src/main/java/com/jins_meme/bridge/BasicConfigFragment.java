/**
 * SettingFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class BasicConfigFragment extends Fragment {

  private Handler handler;

  private Switch swScan;
  private Switch swConnect;
  private Spinner spMemeList;
  private EditText etAppId;
  private EditText etAppSecret;

  private ArrayAdapter<String> adapter;

  private String selectedMemeID;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handler = new Handler();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.d("BASIC", "onCreateView");

    return inflater.inflate(R.layout.fragment_basicconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d("BASIC", "onDetach");

    swScan = null;
    swConnect = null;
    spMemeList = null;
    etAppId = null;
    etAppSecret = null;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("BASIC", "onViewCreated");

    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.basic_conf) + " SETTING");
    ((MainActivity) getActivity()).setActionBarBack(true);

    swScan = (Switch) view.findViewById(R.id.scan);
    swScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          Log.d("BASIC", "SCAN Start");
          Toast.makeText(getActivity(), "SCANNING...", Toast.LENGTH_SHORT).show();

          ((MainActivity) getActivity()).startScan();

          adapter.clear();
          //spMemeList.setAdapter(adapter);

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              swScan.setChecked(false);

              ((MainActivity) getActivity()).stopScan();

              if (((MainActivity) getActivity()).getScannedMemeSize() > 0) {
                adapter.addAll(((MainActivity) getActivity()).getScannedMemeList());
                swConnect.setEnabled(true);
                selectedMemeID = (String) spMemeList.getSelectedItem();
              }
            }
          }, 5000);
        } else {
          Log.d("BASIC", "SCAN Stop");
          Toast.makeText(getActivity(), "SCAN STOPPED.", Toast.LENGTH_SHORT).show();

          ((MainActivity) getActivity()).stopScan();
        }
      }
    });

    swConnect = (Switch) view.findViewById(R.id.connect);
    swConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          if (!((MainActivity) getActivity()).isMemeConnected()) {
            selectedMemeID = (String) spMemeList.getSelectedItem();
            Log.d("BASIC", "CONNECT Start " + selectedMemeID);
            Toast.makeText(getActivity(), "CONNECTING TO " + selectedMemeID + "...",
                Toast.LENGTH_SHORT).show();

            ((MainActivity) getActivity()).connectToMeme(selectedMemeID);
          }
        } else {
          Log.d("BASIC", "CONNECT Stop");
          Toast.makeText(getActivity(), "DISCONNECTING...", Toast.LENGTH_SHORT).show();

          ((MainActivity) getActivity()).disconnectToMeme();
        }
      }
    });
    if (((MainActivity) getActivity()).isMemeConnected()
        || ((MainActivity) getActivity()).getScannedMemeSize() > 0) {
      swConnect.setEnabled(true);
    } else {
      swConnect.setEnabled(false);
    }

    Log.d("BASIC", "spMemeList new");

    spMemeList = (Spinner) view.findViewById(R.id.meme_list);
    adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
    if (((MainActivity) getActivity()).getScannedMemeSize() > 0) {
      adapter.addAll(((MainActivity) getActivity()).getScannedMemeList());
      setSelection(selectedMemeID);
    }
    spMemeList.setAdapter(adapter);

    etAppId = (EditText) view.findViewById(R.id.app_id);
    etAppId.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ((MainActivity) getActivity()).autoSaveText("APP_ID", charSequence.toString());
        Log.d("BASIC", "APP_ID: " + charSequence.toString());
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    etAppId.setText(((MainActivity) getActivity()).getSavedText("APP_ID"),
        TextView.BufferType.EDITABLE);

    etAppSecret = (EditText) view.findViewById(R.id.app_secret);
    etAppSecret.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ((MainActivity) getActivity()).autoSaveText("APP_SECRET", charSequence.toString());
        Log.d("BASIC", "APP_SECRET: " + charSequence.toString());
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    etAppSecret.setText(((MainActivity) getActivity()).getSavedText("APP_SECRET"),
        TextView.BufferType.EDITABLE);

    Log.d("BASIC", "adapter count = " + adapter.getCount());

  }

  private void setSelection(@NonNull String item) {
    int index = 0;
    for (int i = 0; i < adapter.getCount(); i++) {
      if (adapter.getItem(i).equals(item)) {
        index = i;
        break;
      }
    }
    spMemeList.setSelection(index);
  }
}
