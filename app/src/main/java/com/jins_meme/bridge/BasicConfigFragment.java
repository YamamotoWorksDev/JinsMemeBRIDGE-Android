/**
 * BasicConfigFragment.java
 *
 * Copyright (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnScrollChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class BasicConfigFragment extends ConfigFragmentBase {

  private Handler handler;

  private ScrollView scrollView;
  private LinearLayout layout;
  private Switch swScan;
  private Switch swConnect;
  private Spinner spMemeList;
  private EditText etAppId;
  private EditText etAppSecret;
  private TextView tvBlinkTitle;
  private TextView tvRollTitle;
  private TextView tvPauseTimeTitle;
  private SeekBar sbBlinkThreshold;
  private SeekBar sbLeftRightThreshold;
  private SeekBar sbRollThreshold;
  private SeekBar sbPauseTime;
  private Switch swDirection;
  private ImageButton ibRestart;
  private ImageButton ibLock;

  private Switch cameraEnableSwitch;
  private Switch spotifyEnableSwitch;
  private Switch remoEnableSwitch;
  private Switch hueEnableSwitch;
  private Switch eyevdjEnableSwitch;
  private Switch darkEnableSwitch;

  ProgressDialogFragment memeScanProgressDialog;

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

    layout = null;
    swScan = null;
    swConnect = null;
    spMemeList = null;
    etAppId = null;
    etAppSecret = null;
    tvBlinkTitle = null;
    tvRollTitle = null;
    tvPauseTimeTitle = null;
    sbBlinkThreshold = null;
    sbLeftRightThreshold = null;
    sbRollThreshold = null;
    sbPauseTime = null;
    swDirection = null;
    ibRestart = null;
    ibLock = null;
    cameraEnableSwitch = null;
    spotifyEnableSwitch = null;
    remoEnableSwitch = null;
    hueEnableSwitch = null;
    eyevdjEnableSwitch = null;
    darkEnableSwitch = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    ((MainActivity) getActivity())
        .updateActionBar(getResources().getString(R.string.basic_conf_title), false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("BASIC", "onViewCreated");

    scrollView = (ScrollView) view.findViewById(R.id.scroll_view);
    scrollView.setOnScrollChangeListener(new OnScrollChangeListener() {
      @Override
      public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        Log.d("DEBUG",
            "view scroll " + scrollX + " " + scrollY + " " + oldScrollX + " " + oldScrollY);

        if (scrollY - oldScrollY > 3000) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              scrollView.setScrollY(0);
            }
          });
        }
      }
    });

    layout = (LinearLayout) view.findViewById(R.id.basic_layout);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("DEBUG", "view touch. " + scrollView.getScrollY());

        layout.requestFocus();

        return false;
      }
    });

    swScan = (Switch) view.findViewById(R.id.scan);
    swScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          if (((MainActivity) getActivity()).checkAppIDandSecret()) {
            Log.d("BASIC", "SCAN Start");
            //Toast.makeText(getActivity(), "SCANNING...", Toast.LENGTH_SHORT).show();

            memeScanProgressDialog = ProgressDialogFragment.newInstance("meme_scan");
            //memeConnectProgressDialog.setDialogListener(this);
            memeScanProgressDialog.setCancelable(false);
            memeScanProgressDialog.show(getFragmentManager(), "dialog");

            ((MainActivity) getActivity()).startScan(false);

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
          //Toast.makeText(getActivity(), "SCAN STOPPED.", Toast.LENGTH_SHORT).show();
          if (memeScanProgressDialog != null) {
            memeScanProgressDialog.dismiss();
          }

          ((MainActivity) getActivity()).stopScan();
        }
        layout.requestFocus();
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
        layout.requestFocus();
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
    etAppId.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager) getActivity()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
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
    etAppSecret.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager) getActivity()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
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
        getString(R.string.meme_config_blink,
            ((MainActivity) getActivity()).getSavedValue("BLINK_TH", 90)));

    sbBlinkThreshold = (SeekBar) view.findViewById(R.id.blink_threshold);
    sbBlinkThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("BLINK_TH", 90) - 20);
    sbBlinkThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int value = seekBar.getProgress() + 20;
        tvBlinkTitle.setText(getString(R.string.meme_config_blink, value));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        layout.requestFocus();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress() + 20;
        ((MainActivity) getActivity()).autoSaveValue("BLINK_TH", value);
        Log.d("BASIC", "blink th. = " + value);
        Toast.makeText(getActivity(), "BLINK THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
        tvBlinkTitle.setText(getString(R.string.meme_config_blink, value));
      }
    });

    /*
    sbUpDownThreshold = (SeekBar) view.findViewById(R.id.updown_threshold);
    sbUpDownThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("UD_TH", 0));
    sbUpDownThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        layout.requestFocus();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress();
        ((MainActivity) getActivity()).autoSaveValue("UD_TH", value);
        Log.d("BASIC", "up/down th. = " + value);
        Toast.makeText(getActivity(), "UP/DOWN THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
      }
    });
    */

    sbLeftRightThreshold = (SeekBar) view.findViewById(R.id.leftright_threshold);
    sbLeftRightThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("LR_TH", 0));
    sbLeftRightThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        layout.requestFocus();
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
    tvRollTitle.setText(getString(R.string.meme_config_angle,
        ((MainActivity) getActivity()).getSavedValue("ROLL_TH", 15)));

    sbRollThreshold = (SeekBar) view.findViewById(R.id.roll_threshold);
    sbRollThreshold.setProgress(((MainActivity) getActivity()).getSavedValue("ROLL_TH", 15) - 10);
    sbRollThreshold.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int value = seekBar.getProgress() + 10;
        tvRollTitle.setText(getString(R.string.meme_config_angle, value));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        layout.requestFocus();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int value = seekBar.getProgress() + 10;
        ((MainActivity) getActivity()).autoSaveValue("ROLL_TH", value);
        Log.d("BASIC", "roll th. = " + value);
        Toast.makeText(getActivity(), "ROLL THRESHOLD: " + value, Toast.LENGTH_SHORT).show();
        tvRollTitle.setText(getString(R.string.meme_config_angle, value));
      }
    });

    tvPauseTimeTitle = (TextView) view.findViewById(R.id.pause_time_title);
    tvPauseTimeTitle.setText(getString(R.string.meme_config_pause_time,
        ((MainActivity) getActivity()).getSavedValue("PAUSE_TIME", 2.5f)));

    sbPauseTime = (SeekBar) view.findViewById(R.id.pause_time);
    sbPauseTime.setProgress(
        (int) (((MainActivity) getActivity()).getSavedValue("PAUSE_TIME", 2.5f) * 10) - 10);
    sbPauseTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        float value = (seekBar.getProgress() + 10) / 10.0f;
        tvPauseTimeTitle.setText(getString(R.string.meme_config_pause_time, value));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        layout.requestFocus();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        float value = (seekBar.getProgress() + 10) / 10.0f;
        ((MainActivity) getActivity()).autoSaveValue("PAUSE_TIME", value);
        Log.d("BASIC", "pause time = " + value);
        Toast.makeText(getActivity(), "PAUSE TIME: " + value, Toast.LENGTH_SHORT).show();
        tvPauseTimeTitle.setText(getString(R.string.meme_config_pause_time, value));
      }
    });

    /*
    ibRestart = (ImageButton) view.findViewById(R.id.restart);
    ibRestart.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        ((MainActivity) getActivity()).restart();
      }
    });
    setEnableRestart(false);
    */

    swDirection = (Switch) view.findViewById(R.id.card_slide_direction);
    swDirection
        .setChecked(((MainActivity) getActivity()).getSavedValue("MENU_SLIDE_DIRECTION", false));
    swDirection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((MainActivity) getActivity()).autoSaveValue("MENU_SLIDE_DIRECTION", isChecked);
      }
    });

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

    cameraEnableSwitch = (Switch) view.findViewById(R.id.enable_camera);
    cameraEnableSwitch
        .setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_CAMERA", true));
    cameraEnableSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((MainActivity) getActivity()).autoSaveValue("ENABLE_CAMERA", isChecked);

        setSwitchOnAtLeastOne();
      }
    });

    spotifyEnableSwitch = (Switch) view.findViewById(R.id.enable_spotify);
    spotifyEnableSwitch
        .setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_SPOTIFY", true));
    spotifyEnableSwitch.setOnCheckedChangeListener(new

                                                       OnCheckedChangeListener() {
                                                         @Override
                                                         public void onCheckedChanged(
                                                             CompoundButton buttonView,
                                                             boolean isChecked) {
                                                           ((MainActivity) getActivity())
                                                               .autoSaveValue("ENABLE_SPOTIFY",
                                                                   isChecked);

                                                           setSwitchOnAtLeastOne();
                                                         }
                                                       });

    remoEnableSwitch = (Switch) view.findViewById(R.id.enable_remo);
    remoEnableSwitch.setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_REMO", true));
    remoEnableSwitch.setOnCheckedChangeListener(new

                                                    OnCheckedChangeListener() {
                                                      @Override
                                                      public void onCheckedChanged(
                                                          CompoundButton buttonView,
                                                          boolean isChecked) {
                                                        ((MainActivity) getActivity())
                                                            .autoSaveValue("ENABLE_REMO",
                                                                isChecked);

                                                        setSwitchOnAtLeastOne();
                                                      }
                                                    });

    hueEnableSwitch = (Switch) view.findViewById(R.id.enable_hue);
    hueEnableSwitch.setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_HUE", true));
    hueEnableSwitch.setOnCheckedChangeListener(new

                                                   OnCheckedChangeListener() {
                                                     @Override
                                                     public void onCheckedChanged(
                                                         CompoundButton buttonView,
                                                         boolean isChecked) {
                                                       ((MainActivity) getActivity())
                                                           .autoSaveValue("ENABLE_HUE", isChecked);

                                                       setSwitchOnAtLeastOne();
                                                     }
                                                   });

    eyevdjEnableSwitch = (Switch) view.findViewById(R.id.enable_eyevdj);
    eyevdjEnableSwitch
        .setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_EYEVDJ", true));
    eyevdjEnableSwitch.setOnCheckedChangeListener(new

                                                      OnCheckedChangeListener() {
                                                        @Override
                                                        public void onCheckedChanged(
                                                            CompoundButton buttonView,
                                                            boolean isChecked) {
                                                          ((MainActivity) getActivity())
                                                              .autoSaveValue("ENABLE_EYEVDJ",
                                                                  isChecked);

                                                          setSwitchOnAtLeastOne();
                                                        }
                                                      });

    darkEnableSwitch = (Switch) view.findViewById(R.id.enable_dark_mode);
    darkEnableSwitch.setChecked(((MainActivity) getActivity()).getSavedValue("ENABLE_DARK", true));
    darkEnableSwitch.setOnCheckedChangeListener(new

                                                    OnCheckedChangeListener() {
                                                      @Override
                                                      public void onCheckedChanged(
                                                          CompoundButton buttonView,
                                                          boolean isChecked) {
                                                        ((MainActivity) getActivity())
                                                            .autoSaveValue("ENABLE_DARK",
                                                                isChecked);
                                                      }
                                                    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
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

    if (swConnect != null) {
      swConnect.setChecked(b);
    }
    /*
    handler.post(new Runnable() {
      @Override
      public void run() {
        swConnect.setChecked(b);
      }
    });
    */
  }

  void setSwitchOnAtLeastOne() {
    if (((MainActivity) getActivity()).getEnabledCardNum() == 1) {
      if (cameraEnableSwitch.isChecked()) {
        cameraEnableSwitch.setEnabled(false);
      } else if (spotifyEnableSwitch.isChecked()) {
        spotifyEnableSwitch.setEnabled(false);
      } else if (remoEnableSwitch.isChecked()) {
        remoEnableSwitch.setEnabled(false);
      } else if (hueEnableSwitch.isChecked()) {
        hueEnableSwitch.setEnabled(false);
      } else if (eyevdjEnableSwitch.isChecked()) {
        eyevdjEnableSwitch.setEnabled(false);
      }
    } else {
      cameraEnableSwitch.setEnabled(true);
      spotifyEnableSwitch.setEnabled(true);
      remoEnableSwitch.setEnabled(true);
      hueEnableSwitch.setEnabled(true);
      eyevdjEnableSwitch.setEnabled(true);
    }
  }
}
