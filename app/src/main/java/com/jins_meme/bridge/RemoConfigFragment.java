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
import android.net.wifi.SupplicantState;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jins_meme.bridge.RemoController.OnDevicesListener;
import com.jins_meme.bridge.RemoController.OnMessagesListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.jmdns.ServiceEvent;


public class RemoConfigFragment extends ConfigFragmentBase {
  public String TAG = "RemoConfigFragment";
  private RemoController remoController;

  private LinearLayout llScan;
  private LinearLayout llReceiveSignal1;
  private LinearLayout llReceiveSignal2;
  private LinearLayout llReceiveSignal3;
  private LinearLayout llReceiveSignal4;
  private LinearLayout llReceiveSignal5;
  private LinearLayout llSendSignal1;
  private LinearLayout llSendSignal2;
  private LinearLayout llSendSignal3;
  private LinearLayout llSendSignal4;
  private LinearLayout llSendSignal5;
  private LinearLayout llSignalLabel1;
  private LinearLayout llSignalLabel2;
  private LinearLayout llSignalLabel3;
  private LinearLayout llSignalLabel4;
  private LinearLayout llSignalLabel5;

  private Button bScan;
  private Button bReceiveSignal1;
  private Button bReceiveSignal2;
  private Button bReceiveSignal3;
  private Button bReceiveSignal4;
  private Button bReceiveSignal5;
  private Button bSendSignal1;
  private Button bSendSignal2;
  private Button bSendSignal3;
  private Button bSendSignal4;
  private Button bSendSignal5;
  private Button bSignalLabel1;
  private Button bSignalLabel2;
  private Button bSignalLabel3;
  private Button bSignalLabel4;
  private Button bSignalLabel5;

  private TextView tvSignalLabel1;
  private TextView tvSignalLabel2;
  private TextView tvSignalLabel3;
  private TextView tvSignalLabel4;
  private TextView tvSignalLabel5;
  private TextView tvDeviceInfo;

  private ProgressDialog receiveDialog;
  private ProgressDialog scanDialog;
  private AlertDialog selectDialog;

  private int slotIndex;

  private MainActivity mainActivity;


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
    WIFI_ERROR,
    IDLE,
    SCAN,
    RECEIVEING,
    SENDING
  }
  private State state;

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
    ((MainActivity)getActivity()).updateActionBar(getResources().getString(R.string.remo_conf_title), false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    state = State.IDLE;
    mainActivity = ((MainActivity) getActivity());

    tvDeviceInfo = (TextView) view.findViewById(R.id.remo_info);

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

            if (deviceMap.size() == 0) {
              selectDialog.dismiss();
              showScanDialog();
            }
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

            if (deviceMap.size() == 0) {
              scanDialog.dismiss();
              showSelectDialog();
            }

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
                  receivedMessages(messages, "REMO_SIGNAL_1", tvSignalLabel1, "REMO_SIGNAL_1_NAME");
                  break;
                case 2:
                  receivedMessages(messages, "REMO_SIGNAL_2", tvSignalLabel2, "REMO_SIGNAL_2_NAME");
                  break;
                case 3:
                  receivedMessages(messages, "REMO_SIGNAL_3", tvSignalLabel3, "REMO_SIGNAL_3_NAME");
                  break;
                case 4:
                  receivedMessages(messages, "REMO_SIGNAL_4", tvSignalLabel4, "REMO_SIGNAL_4_NAME");
                  break;
                case 5:
                  receivedMessages(messages, "REMO_SIGNAL_5", tvSignalLabel5, "REMO_SIGNAL_5_NAME");
                  break;
              }
              return;
            }
          });
        } else {
          setExist(false);

          receiveDialog.dismiss();
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
        startScan();
      }
    });

    bReceiveSignal1 = (Button) view.findViewById(R.id.remo_signal_1_receive);
    bReceiveSignal1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(1);
      }
    });
    bReceiveSignal2 = (Button) view.findViewById(R.id.remo_signal_2_receive);
    bReceiveSignal2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(2);
      }
    });
    bReceiveSignal3 = (Button) view.findViewById(R.id.remo_signal_3_receive);
    bReceiveSignal3.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(3);
      }
    });
    bReceiveSignal4 = (Button) view.findViewById(R.id.remo_signal_4_receive);
    bReceiveSignal4.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(4);
      }
    });
    bReceiveSignal5 = (Button) view.findViewById(R.id.remo_signal_5_receive);
    bReceiveSignal5.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        receiveMessages(5);
      }
    });

    bSendSignal1 = (Button) view.findViewById(R.id.remo_signal_1_send);
    bSendSignal1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_1");
      }
    });
    bSendSignal2 = (Button) view.findViewById(R.id.remo_signal_2_send);
    bSendSignal2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_2");
      }
    });
    bSendSignal3 = (Button) view.findViewById(R.id.remo_signal_3_send);
    bSendSignal3.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_3");
      }
    });
    bSendSignal4 = (Button) view.findViewById(R.id.remo_signal_4_send);
    bSendSignal4.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_4");
      }
    });
    bSendSignal5 = (Button) view.findViewById(R.id.remo_signal_5_send);
    bSendSignal5.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendMessage("REMO_SIGNAL_5");
      }
    });

    tvSignalLabel1 = (TextView) view.findViewById(R.id.remo_signal_1_name);
    tvSignalLabel2 = (TextView) view.findViewById(R.id.remo_signal_2_name);
    tvSignalLabel3 = (TextView) view.findViewById(R.id.remo_signal_3_name);
    tvSignalLabel4 = (TextView) view.findViewById(R.id.remo_signal_4_name);
    tvSignalLabel5 = (TextView) view.findViewById(R.id.remo_signal_5_name);

    tvSignalLabel1.setText(mainActivity.getSavedValue("REMO_SIGNAL_1_NAME"));
    tvSignalLabel2.setText(mainActivity.getSavedValue("REMO_SIGNAL_2_NAME"));
    tvSignalLabel3.setText(mainActivity.getSavedValue("REMO_SIGNAL_3_NAME"));
    tvSignalLabel4.setText(mainActivity.getSavedValue("REMO_SIGNAL_4_NAME"));
    tvSignalLabel5.setText(mainActivity.getSavedValue("REMO_SIGNAL_5_NAME"));

    bSignalLabel1 = (Button) view.findViewById(R.id.remo_signal_1_set_name);
    bSignalLabel1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showSetSignalLabelDialog(tvSignalLabel1, "REMO_SIGNAL_1_NAME");
      }
    });
    bSignalLabel2 = (Button) view.findViewById(R.id.remo_signal_2_set_name);
    bSignalLabel2.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showSetSignalLabelDialog(tvSignalLabel2, "REMO_SIGNAL_2_NAME");
      }
    });
    bSignalLabel3 = (Button) view.findViewById(R.id.remo_signal_3_set_name);
    bSignalLabel3.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showSetSignalLabelDialog(tvSignalLabel3, "REMO_SIGNAL_3_NAME");
      }
    });
    bSignalLabel4 = (Button) view.findViewById(R.id.remo_signal_4_set_name);
    bSignalLabel4.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showSetSignalLabelDialog(tvSignalLabel1, "REMO_SIGNAL_4_NAME");
      }
    });
    bSignalLabel5 = (Button) view.findViewById(R.id.remo_signal_5_set_name);
    bSignalLabel5.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        showSetSignalLabelDialog(tvSignalLabel5, "REMO_SIGNAL_5_NAME");
      }
    });

    final String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
    final String name = mainActivity.getSavedValue("REMO_DEVICE_NAME");

    SupplicantState wifiState = remoController.getWifiState();
    Log.d(TAG, "onViewCreated: " + wifiState);

    if (wifiState == SupplicantState.COMPLETED) {
      state = State.IDLE;

      if (address != null) {
        tvDeviceInfo.setText(name + " " + address);

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
      }
    } else {
      state = State.WIFI_ERROR;
      showWifiErrorDialog();
    }
  }
  private void changeState(State newState) {
    Log.d(TAG, "changeState: " + newState);
    state = newState;
  }
  private void setExist(boolean isExist) {
    Log.d(TAG, "setExist: " + isExist);
    if (isExist) {
      tvDeviceInfo.setTextColor(Color.GREEN);
      checkState = CheckState.EXIST;
    } else {
      tvDeviceInfo.setTextColor(Color.RED);
      checkState = CheckState.LOST;
    }
  }


  private void startScan() {
    SupplicantState wifiState = remoController.getWifiState();
    Log.d(TAG, "startScan: " + wifiState);

    if (wifiState != SupplicantState.COMPLETED) {
      changeState(State.WIFI_ERROR);
      showWifiErrorDialog();
      return;
    } else {
      changeState(State.SCAN);
      remoController.startDiscovery();
      deviceMap = new HashMap<String, String>();
      showScanDialog();
    }
  }

  private void showScanDialog() {
    scanDialog = new ProgressDialog(mainActivity);
    scanDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        scanDialog.cancel();
      }
    });
    scanDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        Log.d(TAG, "scan dialog onCancel: ");
        remoController.stopDiscovery();
        changeState(State.IDLE);

        scanDialogHandler.removeCallbacksAndMessages(null);
      }
    });
    scanDialog.setOnDismissListener(new OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialogInterface) {
        Log.d(TAG, "scan dialog onDismiss: ");
        scanDialogHandler.removeCallbacksAndMessages(null);
      }
    });
    scanDialog.setMessage(getString(R.string.remo_scan_dialog));
    scanDialog.show();
    scanDialogHandler = new Handler();

    scanDialogHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "state: " + state + " deviceMap.size: " + deviceMap.size());
        if (state == State.SCAN && deviceMap.size() == 0) {
          scanDialog.cancel();
          showDeviceSettingDialog();
        }
      }
    }, scanTimeoutDuration);
  }

  private void showSelectDialog() {
    adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1,
        new ArrayList<String>());
    final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);


    builder.setTitle(getString(R.string.remo_select_dialog));

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
        Log.d(TAG, "select dialog onCancel: ");
        remoController.stopDiscovery();
        changeState(State.IDLE);
      }
    });

    selectDialog = builder.create();
    selectDialog.show();
  }
  private void showSetSignalLabelDialog(final TextView labelTextView, final String saveId) {

    AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
    final EditText editText = new EditText(mainActivity);

    dialog.setTitle(getString(R.string.remo_set_label_dialog_title));
    dialog.setView(editText);

    dialog.setPositiveButton(getString(R.string.remo_set_label_dialog_ok), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        mainActivity.autoSaveValue(saveId, editText.getText().toString());
        labelTextView.setText(editText.getText().toString());
      }
    });

    dialog.show();
  }

  private void showWifiErrorDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

    builder.setTitle(getString(R.string.not_connected_network_title));
    builder.setMessage(getString(R.string.not_connected_network_explain));
    builder.setPositiveButton(getString(R.string.ok), null);
    builder.show();
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
    irkit.setText(getString(R.string.remo_open_app_irkit));
    irkit.setPadding((int)density*24,(int)density*24,(int)density*24,(int)density*24);

    TextView remo = new TextView(mainActivity);
    remo.setClickable(true);

    remo.setText(getString(R.string.remo_open_app_remo));
    remo.setPadding((int)density*24,(int)density*24,(int)density*24,(int)density*24);

    layout.addView(irkit);
    layout.addView(remo);

    builder.setTitle(getString(R.string.remo_wifi_setting_dialog_title));
    builder.setView(layout);

    dialog = builder.create();
    dialog.show();

    irkit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String packageName = "com.getirkit.irkitsimpleremote";

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
        String packageName = "global.nature.remo";

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

    tvDeviceInfo.setText(name + " " + address);
  }

  private void receiveMessages(int i) {
    if (checkState == CheckState.EXIST) {
      changeState(State.RECEIVEING);

      slotIndex = i;
      String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
      remoController.recevieMessages(address);
      receiveDialog = new ProgressDialog(mainActivity);
      receiveDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          receiveDialog.cancel();
        }
      });
      receiveDialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
          Log.d(TAG, "onCancel: ");
          changeState(State.IDLE);
          remoController.cancelReceiveMessages();
        }
      });
      receiveDialog.setMessage(getString(R.string.remo_receive_dialog));
      receiveDialog.show();
    }
  }
  private void receivedMessages(String messages, String key, TextView labelTextView, String labelKey) {
    mainActivity.autoSaveValue(key, messages);
    changeState(State.IDLE);
    receiveDialog.dismiss();
    showSetSignalLabelDialog(labelTextView, labelKey);

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
