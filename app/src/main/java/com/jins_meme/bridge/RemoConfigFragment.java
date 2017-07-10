/**
 * RemoConfigFragment.java
 *
 * Copylight (C) 2017, Taiki Niimi(Freelance)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.jins_meme.bridge.RemoController.OnDevicesListener;
import com.jins_meme.bridge.RemoController.OnMessagesListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.jmdns.ServiceEvent;
import javax.jmdns.impl.DNSRecord.Text;


public class RemoConfigFragment extends ConfigFragmentBase {
  public String TAG = "RemoConfigFragment";
  private RemoController remoController;
//  private Switch swConnect;
  private Button bScan;
  private Button bSignal1;
  private Button bSignal2;
  private Button bSignal3;
  private Button bSignal4;
  private Button bSignal5;
  private Button btSignal1;
  private Button btSignal2;
  private Button btSignal3;
  private Button btSignal4;
  private Button btSignal5;
  private EditText etSignal1;
  private EditText etSignal2;
  private EditText etSignal3;
  private EditText etSignal4;
  private EditText etSignal5;
  private TextView tvName;
  private TextView tvAddress;
  private ProgressDialog progressDialog;

  private int slotIndex;

  private MainActivity mainActivity;
  private RelativeLayout layout;

  private ArrayAdapter<String> adapter;
  private HashMap<String, String> deviceMap;

  private final int scanTimeoutDuration = 10000;

  private CheckState checkState;
  enum  CheckState {
    EXIST,
    LOST,
    CHECK
  }

  enum  State {
    IDLE,
    SCAN,
    RECEIVEING,
    SENDING
  }
  private State state;
  private State currentState;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_remoconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    if (remoController != null) {
      remoController.removeDevicesListener();
      remoController.removeMessagesListener();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    ((MainActivity)getActivity()).updateActionBar(getResources().getString(R.string.remo_conf_title));
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    state = State.IDLE;
    mainActivity = ((MainActivity) getActivity());
    tvName = (TextView)view.findViewById(R.id.remo_name);
    tvAddress = (TextView)view.findViewById(R.id.remo_address);

    deviceMap = new HashMap<String, String>();

    remoController = new RemoController(getContext());
    final Handler addHandler = new Handler();
    final Handler removeHandler = new Handler();

    final Handler receiveHandler = new Handler();

    remoController.setDevicesListener(new OnDevicesListener() {
      @Override
      public void onExist(boolean result) {
        if (checkState == CheckState.CHECK) {
          Log.d(TAG, "onExist: " + result);
//        swConnect.setChecked(result);
          setExist(result);
        }
      }
      @Override
      public void onServiceAdded(ServiceEvent serviceEvent) {
      }
      @Override
      public void onServiceRemoved(final ServiceEvent serviceEvent) {
        Log.d(TAG, "onServiceRemoved: " + serviceEvent.getName());
        removeHandler.post(new Runnable() {
          @Override
          public void run() {

            String name = serviceEvent.getName();

            deviceMap.remove(name);
            adapter.remove(name);
            adapter.notifyDataSetChanged();
          }
        });
      }
      @Override
      public void onServiceResolved(final ServiceEvent serviceEvent) {
        Log.d(TAG, "onServiceResolved: " + serviceEvent.getName());
        addHandler.post(new Runnable() {
          @Override
          public void run() {
            String name = serviceEvent.getName();
            String address = serviceEvent.getInfo().getHostAddresses()[0];
            deviceMap.put(name, address);
            adapter.add(name);
            adapter.notifyDataSetChanged();
            return;
          }
        });
      }
    });

    remoController.setMessagesListener(new OnMessagesListener() {

      @Override
      public void onReciveMessages(final String messages, boolean isSuccess) {
        Log.d(TAG, "onReciveMessages: " + messages + " " + isSuccess);
        changeState(State.IDLE);
        if (isSuccess) {

            receiveHandler.post(new Runnable() {
              @Override
              public void run() {

                switch (slotIndex) {
                  case 1:
                    receivedMessages(messages, "REMO_SIGNAL_1", etSignal1);
                    break;
                  case 2:
                    receivedMessages(messages, "REMO_SIGNAL_2", etSignal2);
                    break;
                  case 3:
                    receivedMessages(messages, "REMO_SIGNAL_3", etSignal3);
                    break;
                  case 4:
                    receivedMessages(messages, "REMO_SIGNAL_4", etSignal4);
                    break;
                  case 5:
                    receivedMessages(messages, "REMO_SIGNAL_5", etSignal5);
                    break;
                }
                return;
              }
            });
        } else {
//          swConnect.setChecked(false);
          setExist(false);

          progressDialog.dismiss();
        }
      }
      @Override
      public void onSendMessages(String messages, boolean isSuccess) {
        Log.d(TAG, "onSendMessages: " + messages + " " + isSuccess);
        changeState(State.IDLE);
        if (isSuccess) {
          setExist(true);
        } else {
          setExist(false);
        }
      }
    });

    bScan = (Button) view.findViewById(R.id.remo_scan);
    bScan.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
//        if (state != State.CHECK_EXIST) {
          startScan();
          layout.requestFocus();
//        }
      }
    });


    bSignal1 = (Button) view.findViewById(R.id.remo_signal_1_receive);
    bSignal1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(1);
        layout.requestFocus();
      }
    });
    bSignal2 = (Button) view.findViewById(R.id.remo_signal_2_receive);
    bSignal2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(2);
        layout.requestFocus();
      }
    });
    bSignal3 = (Button) view.findViewById(R.id.remo_signal_3_receive);
    bSignal3.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(3);
        layout.requestFocus();
      }
    });
    bSignal4 = (Button) view.findViewById(R.id.remo_signal_4_receive);
    bSignal4.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(4);
        layout.requestFocus();
      }
    });
    bSignal5 = (Button) view.findViewById(R.id.remo_signal_5_receive);
    bSignal5.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(5);
        layout.requestFocus();
      }
    });

    btSignal1 = (Button) view.findViewById(R.id.remo_signal_1_send);
    btSignal1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_1");
        layout.requestFocus();
      }
    });
    btSignal2 = (Button) view.findViewById(R.id.remo_signal_2_send);
    btSignal2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_2");
        layout.requestFocus();
      }
    });
    btSignal3 = (Button) view.findViewById(R.id.remo_signal_3_send);
    btSignal3.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_3");
        layout.requestFocus();
      }
    });
    btSignal4 = (Button) view.findViewById(R.id.remo_signal_4_send);
    btSignal4.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_4");
        layout.requestFocus();
      }
    });
    btSignal5 = (Button) view.findViewById(R.id.remo_signal_5_send);
    btSignal5.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_5");
        layout.requestFocus();
      }
    });

    etSignal1 = (EditText) view.findViewById(R.id.remo_signal_1_name);
    etSignal1.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal1.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (!b) {
          mainActivity.autoSaveValue("REMO_SIGNAL_1_NAME", ((TextView)view).getText().toString());
          InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });

    etSignal2 = (EditText) view.findViewById(R.id.remo_signal_2_name);
    etSignal2.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal2.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (!b) {
          mainActivity.autoSaveValue("REMO_SIGNAL_2_NAME", ((TextView)view).getText().toString());
          InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });

    etSignal3 = (EditText) view.findViewById(R.id.remo_signal_3_name);
    etSignal3.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal3.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (!b) {
          mainActivity.autoSaveValue("REMO_SIGNAL_3_NAME", ((TextView)view).getText().toString());
          InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });

    etSignal4 = (EditText) view.findViewById(R.id.remo_signal_4_name);
    etSignal4.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal4.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (!b) {
          mainActivity.autoSaveValue("REMO_SIGNAL_4_NAME", ((TextView)view).getText().toString());
          InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });

    etSignal5 = (EditText) view.findViewById(R.id.remo_signal_5_name);
    etSignal5.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal5.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (!b) {
          mainActivity.autoSaveValue("REMO_SIGNAL_5_NAME", ((TextView)view).getText().toString());
          InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });

    layout = (RelativeLayout) view.findViewById(R.id.remo_layout);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("DEBUG", "view touch.");
        layout.requestFocus();
        return false;
      }
    });

    etSignal1.setText(mainActivity.getSavedValue("REMO_SIGNAL_1_NAME"));
    etSignal2.setText(mainActivity.getSavedValue("REMO_SIGNAL_2_NAME"));
    etSignal3.setText(mainActivity.getSavedValue("REMO_SIGNAL_3_NAME"));
    etSignal4.setText(mainActivity.getSavedValue("REMO_SIGNAL_4_NAME"));
    etSignal5.setText(mainActivity.getSavedValue("REMO_SIGNAL_5_NAME"));

    final String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
    final String name = mainActivity.getSavedValue("REMO_DEVICE_NAME");

    if (address != null) {
      tvName.setText(name);
      tvAddress.setText(address);

      checkState = CheckState.CHECK;

      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          remoController.checkExist(address);
        }
      }, 1000);
    } else {
      setExist(false);
      tvName.setText("---");
      tvAddress.setText("---");
    }
  }
  private void changeState(State newState) {
    Log.d(TAG, "changeState: " + newState);
    currentState = state;
    state = newState;
  }
  private void setExist(boolean isExist) {
    Log.d(TAG, "setExist: " + isExist);
    if (isExist) {
      tvAddress.setTextColor(Color.GREEN);
      tvName.setTextColor(Color.GREEN);
      checkState = CheckState.EXIST;
//      changeState(State.EXIST);
    } else {
      tvAddress.setTextColor(Color.RED);
      tvName.setTextColor(Color.RED);
      checkState = CheckState.LOST;
//      changeState(State.LOST);
    }
  }


  private void startScan() {
    changeState(State.SCAN);
    remoController.startDiscovery();

    deviceMap = new HashMap<String, String>();
    adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, new ArrayList<String>());
    final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    final AlertDialog dialog;

    builder.setTitle("Select a device");

    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        Log.d(TAG, "onClick: " + adapter.getItem(i) + " " + deviceMap.get(adapter.getItem(i)));
        remoController.stopDiscovery();
        setDevice(adapter.getItem(i), deviceMap.get(adapter.getItem(i)));
        setExist(true);
      }
    });
    builder.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        Log.d(TAG, "onCancel: ");
        remoController.stopDiscovery();
        changeState(State.IDLE);
//        if (state == State.SCAN) {
//          String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
//          if (address != null) {
//            changeState(State.CHECK_EXIST);
//            remoController.checkExist(address);
//          } else {
//            changeState(State.LOST);
//          }
//        }

          scanDialogHandler.removeCallbacksAndMessages(null);

      }
    });

    dialog = builder.create();
    dialog.show();


    scanDialogHandler = new Handler();


    scanDialogHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "state: " + state + " deviceMap.size: " + deviceMap.size());
        if (state == State.SCAN && deviceMap.size() == 0) {
          dialog.cancel();
          showDeviceSettingDialog();
        }
      }
    }, scanTimeoutDuration);
  }
  private Handler scanDialogHandler;

  private void showDeviceSettingDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
    final AlertDialog dialog;
    LinearLayout layout = new LinearLayout(mainActivity);
    layout.setOrientation(LinearLayout.VERTICAL);

    float density = mainActivity.getResources().getDisplayMetrics().density;

    TextView irkit = new TextView(mainActivity);
    irkit.setClickable(true);
    irkit.setText("Open \"IRKit Simple Remort\"");
    irkit.setPadding((int)density*24,(int)density*24,(int)density*24,(int)density*24);

    TextView remo = new TextView(mainActivity);
    remo.setClickable(true);

    remo.setText("Open \"Nature Remo\"");
    remo.setPadding((int)density*24,(int)density*24,(int)density*24,(int)density*24);

    layout.addView(irkit);
    layout.addView(remo);

    builder.setTitle("Did wifi setting ?");
    builder.setView(layout);

    dialog = builder.create();
    dialog.show();

    irkit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String packageName = "com.getirkit.irkitsimpleremote"; //AndroidManifest.xmlのpackageNameに相当

        PackageManager pm = mainActivity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent == null) {
          intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        startActivity(intent);
        dialog.dismiss();
      }
    });


    remo.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String packageName = "global.nature.remo"; //AndroidManifest.xmlのpackageNameに相当

        PackageManager pm = mainActivity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent == null) {
          intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse("market://details?id=" + packageName));
        }
        startActivity(intent);
        dialog.dismiss();
      }
    });
  }


  private void setDevice(String name, String address) {
    Log.d(TAG, "setDevice: " + name + " " + address);
    address = address.replaceAll("/","");
    ((MainActivity) getActivity()).autoSaveValue("REMO_DEVICE_NAME", name);
    ((MainActivity) getActivity()).autoSaveValue("REMO_DEVICE_ADDRESS", address);
    tvName.setText(name);
    tvAddress.setText(address);
  }

  private void receiveMessages(int i) {
    if (checkState == CheckState.EXIST) {
      changeState(State.RECEIVEING);

      slotIndex = i;
      String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
      remoController.recevieMessages(address);
      progressDialog = new ProgressDialog(mainActivity);
      progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          progressDialog.cancel();
        }
      });
      progressDialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
          Log.d(TAG, "onCancel: ");
          changeState(State.IDLE);
          remoController.cancelReceiveMessages();
        }
      });
      progressDialog.setMessage("Aim your remote at IRKit and press a button briefly");
      progressDialog.show();
    }
  }
  private void receivedMessages(String messages, String key, final EditText etSignal) {
    progressDialog.setMessage("Receveing...");
    mainActivity.autoSaveValue(key, messages);
    changeState(State.IDLE);

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        progressDialog.dismiss();
        etSignal.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(etSignal1, InputMethodManager.RESULT_UNCHANGED_SHOWN);
      }
    }, 1000);
  }
  public void sendMessage(String key) {
    if (checkState == CheckState.EXIST) {
      String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
      String messages = mainActivity.getSavedValue(key);
      if (address != null && !address.equals("") && messages != null && !messages.equals("")) {
        changeState(State.SENDING);
        remoController.sendMessages(address, messages);
      }
    }
  }
}
