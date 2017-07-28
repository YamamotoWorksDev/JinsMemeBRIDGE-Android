/**
 * HueConfigFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Locale;

public class HueConfigFragment extends ConfigFragmentBase {

  private Handler handler;
  private Switch swConnect;
  private Spinner spLightPresetList;
  private SeekBar sbRed;
  private SeekBar sbGreen;
  private SeekBar sbBlue;
  private SeekBar sbBrightness;
  private SeekBar sbTransactionTime;
  private TextView tvRed;
  private TextView tvGreen;
  private TextView tvBlue;
  private TextView tvBrightness;
  private TextView tvTransactionTime;
  private Button bTest;

  private HueController hueController;

  private boolean isConnectionCheck = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_hueconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    isConnectionCheck = false;

    handler = null;

    if (hueController != null) {
      hueController.turnOff();
      hueController = null;
    }

    swConnect = null;
    spLightPresetList = null;
    sbRed = null;
    sbGreen = null;
    sbBlue = null;
    sbBrightness = null;
    sbTransactionTime = null;
    tvRed = null;
    tvGreen = null;
    tvBlue = null;
    tvBrightness = null;
    tvTransactionTime = null;
    bTest = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    ((MainActivity)getActivity()).updateActionBar(getResources().getString(R.string.hue_conf_title), false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("BASIC", "onViewCreated");

    handler = new Handler();

    hueController = new HueController(getContext(), getFragmentManager());

    swConnect = (Switch) view.findViewById(R.id.hue_connect);
    swConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.d("HUE", "swConnect = " + b);

        if (b) {
          isConnectionCheck = true;
          ConnectCheckThread cct = new ConnectCheckThread();
          cct.start();

          if (HueController.getConnectionState() != 4) {
            /*
            isConnectionCheck = true;
            ConnectCheckThread cct = new ConnectCheckThread();
            cct.start();
            */

            hueController.connect();
            //connect();
          }
        } else {
          hueController.disconnect();
          //disconnect();
        }
      }
    });
    Log.d("HUE", "connectionState = " + HueController.getConnectionState());
    if (HueController.getConnectionState() == 4) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          swConnect.setChecked(true);
        }
      });
    }

    spLightPresetList = (Spinner) view.findViewById(R.id.hue_presets);
    spLightPresetList.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("HUE", "index = " + i);

        int[] val = new int[5];
        switch (i) {
          case 0:
            val[0] = ((MainActivity) getActivity()).getSavedValue("HUE_L1_R", 255);
            val[1] = ((MainActivity) getActivity()).getSavedValue("HUE_L1_G", 0);
            val[2] = ((MainActivity) getActivity()).getSavedValue("HUE_L1_B", 0);
            val[3] = ((MainActivity) getActivity()).getSavedValue("HUE_L1_BRI", 127);
            val[4] = ((MainActivity) getActivity()).getSavedValue("HUE_L1_TTIME", 10);
            break;
          case 1:
            val[0] = ((MainActivity) getActivity()).getSavedValue("HUE_L2_R", 0);
            val[1] = ((MainActivity) getActivity()).getSavedValue("HUE_L2_G", 255);
            val[2] = ((MainActivity) getActivity()).getSavedValue("HUE_L2_B", 0);
            val[3] = ((MainActivity) getActivity()).getSavedValue("HUE_L2_BRI", 127);
            val[4] = ((MainActivity) getActivity()).getSavedValue("HUE_L2_TTIME", 10);
            break;
          case 2:
            val[0] = ((MainActivity) getActivity()).getSavedValue("HUE_L3_R", 0);
            val[1] = ((MainActivity) getActivity()).getSavedValue("HUE_L3_G", 0);
            val[2] = ((MainActivity) getActivity()).getSavedValue("HUE_L3_B", 255);
            val[3] = ((MainActivity) getActivity()).getSavedValue("HUE_L3_BRI", 127);
            val[4] = ((MainActivity) getActivity()).getSavedValue("HUE_L3_TTIME", 10);
            break;
          case 3:
            val[0] = ((MainActivity) getActivity()).getSavedValue("HUE_L4_R", 255);
            val[1] = ((MainActivity) getActivity()).getSavedValue("HUE_L4_G", 255);
            val[2] = ((MainActivity) getActivity()).getSavedValue("HUE_L4_B", 255);
            val[3] = ((MainActivity) getActivity()).getSavedValue("HUE_L4_BRI", 127);
            val[4] = ((MainActivity) getActivity()).getSavedValue("HUE_L4_TTIME", 10);
            break;
        }
        //sbRed.setProgress(val[0]);
        //sbGreen.setProgress(val[1]);
        //sbBlue.setProgress(val[2]);
        //sbBrightness.setProgress(val[3]);
        //sbTransactionTime.setProgress(val[4]);

        ObjectAnimator animRed = ObjectAnimator.ofInt(sbRed, "progress", val[0]);
        animRed.setDuration(300);
        animRed.setInterpolator(new DecelerateInterpolator());
        animRed.start();

        ObjectAnimator animGreen = ObjectAnimator.ofInt(sbGreen, "progress", val[1]);
        animGreen.setDuration(300);
        animGreen.setInterpolator(new DecelerateInterpolator());
        animGreen.start();

        ObjectAnimator animBlue = ObjectAnimator.ofInt(sbBlue, "progress", val[2]);
        animBlue.setDuration(300);
        animBlue.setInterpolator(new DecelerateInterpolator());
        animBlue.start();

        ObjectAnimator animBri = ObjectAnimator.ofInt(sbBrightness, "progress", val[3]);
        animBri.setDuration(300);
        animBri.setInterpolator(new DecelerateInterpolator());
        animBri.start();

        ObjectAnimator animTTime = ObjectAnimator.ofInt(sbTransactionTime, "progress", val[4]);
        animTTime.setDuration(300);
        animTTime.setInterpolator(new DecelerateInterpolator());
        animTTime.start();

        //tvRed.setText(String.valueOf(val[0]));
        //tvGreen.setText(String.valueOf(val[1]));
        //tvBlue.setText(String.valueOf(val[2]));
        //tvBrightness.setText(String.format(Locale.JAPAN, "%d(%%)", (int) ((val[3] / 254.0) * 100.0)));
        //tvBlue.setText(String.format(Locale.JAPAN, "%d(msec)", val[4]));
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    sbRed = (SeekBar) view.findViewById(R.id.hue_r_bar);
    sbRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvRed.setText(String.valueOf(i));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        //Log.d("HUE", "onStopTrackingTouch: " + spLightPresetList.getSelectedItemPosition());

        ((MainActivity) getActivity()).autoSaveValue(String
                .format(Locale.JAPAN, "HUE_L%d_R", spLightPresetList.getSelectedItemPosition() + 1),
            seekBar.getProgress());
      }
    });

    sbGreen = (SeekBar) view.findViewById(R.id.hue_g_bar);
    sbGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvGreen.setText(String.valueOf(i));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        ((MainActivity) getActivity()).autoSaveValue(String
                .format(Locale.JAPAN, "HUE_L%d_G", spLightPresetList.getSelectedItemPosition() + 1),
            seekBar.getProgress());
      }
    });

    sbBlue = (SeekBar) view.findViewById(R.id.hue_b_bar);
    sbBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvBlue.setText(String.valueOf(i));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        ((MainActivity) getActivity()).autoSaveValue(String
                .format(Locale.JAPAN, "HUE_L%d_B", spLightPresetList.getSelectedItemPosition() + 1),
            seekBar.getProgress());
      }
    });

    sbBrightness = (SeekBar) view.findViewById(R.id.hue_bri_bar);
    sbBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvBrightness.setText(String.format(Locale.JAPAN, "%d(%%)", (int) ((i / 254.0) * 100.0)));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        ((MainActivity) getActivity()).autoSaveValue(String
                .format(Locale.JAPAN, "HUE_L%d_BRI", spLightPresetList.getSelectedItemPosition() + 1),
            seekBar.getProgress());
      }
    });

    sbTransactionTime = (SeekBar) view.findViewById(R.id.hue_ttime_bar);
    sbTransactionTime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvTransactionTime.setText(String.format(Locale.JAPAN, "%d(msec)", i));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        ((MainActivity) getActivity()).autoSaveValue(String
                .format(Locale.JAPAN, "HUE_L%d_TTIME", spLightPresetList.getSelectedItemPosition() + 1),
            seekBar.getProgress());
      }
    });

    bTest = (Button) view.findViewById(R.id.hue_test);
    bTest.setEnabled(false);
    bTest.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d("HUE", "change color...");

        hueController.changeColor(sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress(), sbBrightness.getProgress(), sbTransactionTime.getProgress());
        //changeColor(sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress(), sbBrightness.getProgress(), sbTransactionTime.getProgress());
      }
    });

    tvRed = (TextView) view.findViewById(R.id.hue_r_val);
    tvRed.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("HUE_L1_R", 255)));

    tvGreen = (TextView) view.findViewById(R.id.hue_g_val);
    tvGreen.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("HUE_L1_G", 0)));

    tvBlue = (TextView) view.findViewById(R.id.hue_b_val);
    tvBlue.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("HUE_L1_B", 0)));

    tvBrightness = (TextView) view.findViewById(R.id.hue_bri_val);
    tvBrightness.setText(String.format(Locale.JAPAN, "%d(%%)",
        (int) ((((MainActivity) getActivity()).getSavedValue("HUE_L1_BRI", 127) / 254.0) * 100.0)));

    tvTransactionTime = (TextView) view.findViewById(R.id.hue_ttime_val);
    tvTransactionTime.setText(String.format(Locale.JAPAN, "%d(msec)",
        ((MainActivity) getActivity()).getSavedValue("HUE_L1_TTIME", 1000)));
  }

  private class ConnectCheckThread extends Thread {

    @Override
    public void run() {
      while (isConnectionCheck) {
        Log.d("HUE", "connection state = " + HueController.getConnectionState());

        if (HueController.getConnectionState() == -1 || HueController.getConnectionState() == -2) {
          isConnectionCheck = false;

          hueController.closeProgressDialog();
          if (swConnect.isChecked()) {
            hueController.showConnectionAlertDialog(getFragmentManager());

            handler.post(new Runnable() {
              @Override
              public void run() {
                swConnect.setChecked(false);
              }
            });
          }
        } else if (HueController.getConnectionState() == 4) {
          isConnectionCheck = false;

          handler.post(new Runnable() {
            @Override
            public void run() {
              if (!swConnect.isChecked()) {
                swConnect.setChecked(true);
              }
              bTest.setEnabled(true);
            }
          });
        }

        try {
          sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        //debug Log.d("HUE", "ConnectCheckThread = " + isConnectionCheck);
      }

      Log.d("HUE", "ConnectCheckThread finish...");
    }
  }
}
