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
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class BasicConfigFragment extends ConfigFragmentBase {

  private Handler handler;

  private Switch swScan;
  private Switch swConnect;
  private Spinner spMemeList;
  private EditText etAppId;
  private EditText etAppSecret;
  private TextView tvBlinkTitle;
  private TextView tvRollTitle;
  private SeekBar sbBlinkThreshold;
  private SeekBar sbUpDownThreshold;
  private SeekBar sbLeftRightThreshold;
  private SeekBar sbRollThreshold;
  private ImageButton ibRestart;
  private ImageButton ibLock;

  private ArrayAdapter<String> adapter;

  private String selectedMemeID;
  private boolean isLockOn = true;

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

    handler = null;

    swScan = null;
    swConnect = null;
    spMemeList = null;
    etAppId = null;
    etAppSecret = null;
    tvBlinkTitle = null;
    tvRollTitle = null;
    sbBlinkThreshold = null;
    sbUpDownThreshold = null;
    sbLeftRightThreshold = null;
    sbRollThreshold = null;
    ibRestart = null;
    ibLock = null;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("BASIC", "onViewCreated");

    ((MainActivity) getActivity()).setActionBarTitle(R.string.basic_conf);
    ((MainActivity) getActivity()).setActionBarBack(true);
    getActivity().invalidateOptionsMenu();

    swScan = (Switch) view.findViewById(R.id.scan);
    swScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          if (((MainActivity) getActivity()).checkAppIDandSecret()) {
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
            swScan.setChecked(false);

            ((MainActivity) getActivity()).showAppIDandSecretWarning();
          }
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
            Toast.makeText(getActivity(), "CONNECTING TO " + selectedMemeID,
                Toast.LENGTH_SHORT).show();

            ((MainActivity) getActivity()).connectToMeme(selectedMemeID);
          }
        } else {
          Log.d("BASIC", "CONNECT Stop");
          Toast.makeText(getActivity(), "DISCONNECTING...", Toast.LENGTH_SHORT).show();

          ((MainActivity) getActivity()).disconnectToMeme();

          ((MainActivity) getActivity()).clearScannedMemeList();
          adapter.clear();
          swConnect.setEnabled(false);
        }
      }
    });
    if (((MainActivity) getActivity()).isMemeConnected()
        || ((MainActivity) getActivity()).getScannedMemeSize() > 0) {
      swConnect.setEnabled(true);
    } else {
      swConnect.setEnabled(false);
    }
    if (((MainActivity) getActivity()).isMemeConnected() && !swConnect.isChecked()) {
      swConnect.setChecked(true);
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
    etAppId.setEnabled(false);
    etAppId.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ((MainActivity) getActivity()).autoSaveValue("APP_ID", charSequence.toString());
        Log.d("BASIC", "APP_ID: " + charSequence.toString());

        setEnableRestart(true);
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    etAppId.setText(
        ((MainActivity) getActivity()).getSavedValue("APP_ID", getString(R.string.meme_app_id)),
        TextView.BufferType.EDITABLE);

    etAppSecret = (EditText) view.findViewById(R.id.app_secret);
    etAppSecret.setEnabled(false);
    etAppSecret.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        ((MainActivity) getActivity()).autoSaveValue("APP_SECRET", charSequence.toString());
        Log.d("BASIC", "APP_SECRET: " + charSequence.toString());

        setEnableRestart(true);
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });
    etAppSecret.setText(
        ((MainActivity) getActivity())
            .getSavedValue("APP_SECRET", getString(R.string.meme_app_secret)),
        TextView.BufferType.EDITABLE);

    Log.d("BASIC", "adapter count = " + adapter.getCount());

    tvBlinkTitle = (TextView) view.findViewById(R.id.blink_title);
    tvBlinkTitle.setText(
        String.format("BLINK (%d)", ((MainActivity) getActivity()).getSavedValue("BLINK_TH", 90)));

    sbBlinkThreshold = (SeekBar) view.findViewById(R.id.blink_threshold);
    sbBlinkThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("BLINK_TH", 90) - 50);
    sbBlinkThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int value = seekBar.getProgress() + 50;
        tvBlinkTitle.setText(String.format("BLINK (%d)", value));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress() + 50;
        ((MainActivity) getActivity()).autoSaveValue("BLINK_TH", value);
        Log.d("BASIC", "blink th. = " + value);
        Toast.makeText(getActivity(), "BLINK THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
        tvBlinkTitle.setText(String.format("BLINK (%d)", value));
      }
    });

    sbUpDownThreshold = (SeekBar) view.findViewById(R.id.updown_threshold);
    sbUpDownThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("UD_TH", 0));
    sbUpDownThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress();
        ((MainActivity) getActivity()).autoSaveValue("UD_TH", value);
        Log.d("BASIC", "up/down th. = " + value);
        Toast.makeText(getActivity(), "UP/DOWN THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
      }
    });

    sbLeftRightThreshold = (SeekBar) view.findViewById(R.id.leftright_threshold);
    sbLeftRightThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("LR_TH", 0));
    sbLeftRightThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress();
        ((MainActivity) getActivity()).autoSaveValue("LR_TH", value);
        Log.d("BASIC", "left/right th. = " + value);
        Toast.makeText(getActivity(), "LEFT/RIGHT THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
      }
    });

    tvRollTitle = (TextView) view.findViewById(R.id.roll_title);
    tvRollTitle.setText(
        String.format("CANCEL/PAUSE ANGLE(ROLL: %d)", ((MainActivity) getActivity()).getSavedValue("ROLL_TH", 15)));

    sbRollThreshold = (SeekBar) view.findViewById(R.id.roll_threshold);
    sbRollThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("ROLL_TH", 15) - 10);
    sbRollThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int value = seekBar.getProgress() + 10;
        tvRollTitle.setText(String.format("CANCEL/PAUSE ANGLE(ROLL: %d)", value));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress() + 10;
        ((MainActivity) getActivity()).autoSaveValue("ROLL_TH", value);
        Log.d("BASIC", "roll th. = " + value);
        Toast.makeText(getActivity(), "ROLL THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
        tvRollTitle.setText(String.format("CANCEL/PAUSE ANGLE(ROLL: %d)", value));
      }
    });

    ibRestart = (ImageButton) view.findViewById(R.id.restart);
    ibRestart.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).restart();
      }
    });
    setEnableRestart(false);

    ibLock = (ImageButton) view.findViewById(R.id.lock);
    ibLock.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (isLockOn) {
          ((MainActivity) getActivity()).showAuthScreen();
        } else {
          lockAppIDandSecret();
        }
      }
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    handler = null;

    swScan = null;
    swConnect = null;
    spMemeList = null;
    etAppId = null;
    etAppSecret = null;
    tvBlinkTitle = null;
    sbBlinkThreshold = null;
    sbUpDownThreshold = null;
    sbLeftRightThreshold = null;
    ibRestart = null;
    ibLock = null;
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

  void lockAppIDandSecret() {
    ibLock.setImageResource(R.mipmap.ic_lock_black_24dp);
    etAppId.setEnabled(false);
    etAppSecret.setEnabled(false);

    isLockOn = true;
  }

  void unlockAppIDandSecret() {
    ibLock.setImageResource(R.mipmap.ic_lock_open_black_24dp);
    etAppId.setEnabled(true);
    etAppSecret.setEnabled(true);

    isLockOn = false;
  }

  private void setEnableRestart(boolean b) {
    if (ibRestart != null) {
      ibRestart.setEnabled(b);
      if (b) {
        ibRestart.setColorFilter(Color.BLACK, Mode.SRC_IN);
      } else {
        ibRestart.setColorFilter(Color.GRAY, Mode.SRC_IN);
      }
    }
  }

  void setSwConnect(final boolean b) {
    Log.d("BASIC", "state " + b);

    swConnect.setChecked(b);
    /*
    handler.post(new Runnable() {
      @Override
      public void run() {
        swConnect.setChecked(b);
      }
    });
    */
  }
}
