/**
 * OSCConfigFragment.java
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
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OSCConfigFragment extends ConfigFragmentBase {

  private Handler handler;
  private LinearLayout layout;
  private EditText etRemoteIP;
  private EditText etRemotePort;
  private EditText etHostIP;
  private EditText etHostPort;
  private Button btnTest;
  private static Toast toast;

  private MemeOSC testOSC;
  private boolean isShown = true;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_oscconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    handler = null;
    layout = null;
    etRemoteIP = null;
    etRemotePort = null;
    etHostIP = null;
    etHostPort = null;
    btnTest = null;

    if(testOSC != null) {
      testOSC.closeSocket();
      testOSC = null;
    }

    isShown = false;
  }

  @Override
  public void onResume() {
    super.onResume();
    ((MainActivity)getActivity()).updateActionBar(getResources().getString(R.string.osc_conf_title));
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    handler = new Handler();

    layout = (LinearLayout) view.findViewById(R.id.osc_layout);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("DEBUG", "view touch.");

        layout.requestFocus();

        return false;
      }
    });

    InputFilter[] filters = new InputFilter[1];
    filters[0] = new InputFilter() {
      @Override
      public CharSequence filter(CharSequence source, int start,
          int end, Spanned dest, int dstart, int dend) {
        if (end > start) {
          String destTxt = dest.toString();
          String resultingTxt = destTxt.substring(0, dstart) +
              source.subSequence(start, end) +
              destTxt.substring(dend);
          if (!resultingTxt.matches ("^\\d{1,3}(\\." +
              "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
            return "";
          } else {
            String[] splits = resultingTxt.split("\\.");
            for (String split : splits) {
              if (Integer.valueOf(split) > 255) {
                return "";
              }
            }
          }
        }
        return null;
      }
    };

    etRemoteIP = (EditText) view.findViewById(R.id.remote_ip);
    String savedRemoteIP = ((MainActivity) getActivity()).getSavedValue("REMOTE_IP", "255.255.255.255");
    if (savedRemoteIP.equals("255.255.255.255")) {
      etRemoteIP.setText(MemeOSC.getRemoteIPv4Address());
    }
    else {
      etRemoteIP.setText(savedRemoteIP);
    }
    etRemoteIP.setFilters(filters);
    etRemoteIP.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etRemoteIP.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        ((MainActivity) getActivity()).autoSaveValue("REMOTE_IP", editable.toString());

        testOSC.setRemoteIP(etRemoteIP.getText().toString());
        testOSC.initSocket();
      }
    });

    etRemotePort = (EditText) view.findViewById(R.id.remote_port);
    etRemotePort.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("REMOTE_PORT", 10316)));
    etRemotePort.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etRemotePort.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        if(editable.toString().length() > 0) {
          ((MainActivity) getActivity())
              .autoSaveValue("REMOTE_PORT", Integer.valueOf(editable.toString()));

          testOSC.setRemotePort(Integer.parseInt(editable.toString()));
          testOSC.initSocket();
        }
      }
    });

    etHostIP = (EditText) view.findViewById(R.id.host_ip);
    etHostIP.setText(MemeOSC.getHostIPv4Address());
    etHostIP.setEnabled(false);

    etHostPort = (EditText) view.findViewById(R.id.host_port);
    etHostPort.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("HOST_PORT", 11316)));
    etHostPort.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etHostPort.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        if(editable.toString().length() > 0) {
          ((MainActivity) getActivity()).autoSaveValue("HOST_PORT", Integer.valueOf(editable.toString()));
          testOSC.setHostPort(Integer.parseInt(editable.toString()));
          testOSC.initSocket();
        }
      }
    });

    btnTest = (Button) view.findViewById(R.id.remote_test);
    btnTest.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        layout.requestFocus();

        testOSC.setAddress("/meme/bridge", "/test");
        testOSC.setTypeTag("si");
        testOSC.addArgument(etRemoteIP.getText().toString());
        testOSC.addArgument(Integer.parseInt(etRemotePort.getText().toString()));
        testOSC.flushMessage();
      }
    });

    testOSC = new MemeOSC();
    testOSC.setRemoteIP(etRemoteIP.getText().toString());
    testOSC.setRemotePort(Integer.parseInt(etRemotePort.getText().toString()));
    testOSC.setHostPort(Integer.parseInt(etHostPort.getText().toString()));
    testOSC.initSocket();

    isShown = true;
    Thread rcvTestThread = new Thread(new ReceiveTestTRunnable());
    rcvTestThread.start();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    Log.d("DEBUG", "destroy view...");

    isShown = false;

    handler = null;
    layout = null;
    etRemoteIP = null;
    etRemotePort = null;
    etHostIP = null;
    etHostPort = null;
    btnTest = null;

    if(testOSC != null) {
      testOSC.closeSocket();
      testOSC = null;
    }
  }

  private class ReceiveTestTRunnable implements Runnable {
    @Override
    public void run() {
      while (isShown) {
        //Log.d("DEBUG", "receive polling...");

        try {
          testOSC.receiveMessage();
          testOSC.extractAddressFromPacket();
          testOSC.extractTypeTagFromPacket();
          testOSC.extractArgumentsFromPacket();

          //Log.d("DEBUG", String.format("%s %s %d", testOSC.getAddress(), testOSC.getTypeTags(), testOSC.getArgumentsLength()));

          String rcvMessage = testOSC.getAddress();
          int argslen = testOSC.getArgumentsLength();
          String typetags = testOSC.getTypeTags();
          for (int i = 0; i < argslen; i++) {
            switch (typetags.substring(i, i + 1)) {
              case "i":
                rcvMessage += " " + testOSC.getIntArgumentAtIndex(i);
                break;
              case "f":
                rcvMessage += " " + testOSC.getFloatArgumentAtIndex(i);
                break;
              case "s":
                rcvMessage += " " + testOSC.getStringArgumentAtIndex(i);
                break;
              default:
                break;
            }
          }
          final String rm = rcvMessage;

          handler.post(new Runnable() {
            @Override
            public void run() {

              if (toast != null) {
                toast.cancel();
              }
              if(rm != null && rm.length() > 0) {
                toast = Toast.makeText(getActivity(), rm, Toast.LENGTH_SHORT);
                toast.show();
              }
            }
          });
        } catch (NullPointerException e) {
          e.printStackTrace();
        }

        try {
          Thread.sleep(100);
        } catch(InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
