package com.jins_meme.bridge;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
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
  private Switch swConnect;
  private Button bSignal1;
  private EditText etSignal1;
  private TextView tvName;
  private TextView tvAddress;
  private ProgressDialog progressDialog;

  private int slotIndex;

  private MainActivity mainActivity;
  private RelativeLayout layout;

  private ArrayAdapter<String> adapter;
  private HashMap<String, String> deviceMap;

  enum  State {
    CHECK_EXIST,
    EXIST,
    LOST,
    FOUNDING,
    RECEIVEING
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
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    state = State.CHECK_EXIST;
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
        Log.d(TAG, "onExist: " + result);
        swConnect.setChecked(result);
        if (result) {
          state = State.EXIST;
        } else {
          state = State.LOST;
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
      public void onReciveMessages(final String messages) {
        if (state == State.RECEIVEING) {
          receiveHandler.post(new Runnable() {
            @Override
            public void run() {
              switch (slotIndex) {
                case 1:
                  progressDialog.dismiss();



                  mainActivity.autoSaveValue("REMO_SIGNAL_1", messages);
                  state = State.EXIST;
                  Handler handler = new Handler();
                  handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                      etSignal1.requestFocus();
                      InputMethodManager inputMethodManager = (InputMethodManager) mainActivity
                          .getSystemService(Context.INPUT_METHOD_SERVICE);
                      inputMethodManager.showSoftInput(etSignal1, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    }
                  }, 100);

                  break;
                case 2:
                  mainActivity.autoSaveValue("REMO_SIGNAL_2", messages);
                  break;
              }
              return;
            }
          });
        }
      }
      @Override
      public void onSendMessages(String messages) {

      }
    });


    swConnect = (Switch) view.findViewById(R.id.remo_connect);
    swConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (state != State.CHECK_EXIST) {
          if (b) {
            remoController.startDiscovery();

            adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, new ArrayList<String>());
            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle("Devices");

            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: " + adapter.getItem(i) + " " + deviceMap.get(adapter.getItem(i)));
                remoController.stopDiscovery();
                setDevice(adapter.getItem(i), deviceMap.get(adapter.getItem(i)));
                state = State.EXIST;
              }
            });
            builder.setOnCancelListener(new OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialogInterface) {
                Log.d(TAG, "onCancel: ");
                remoController.stopDiscovery();
                if (state == State.LOST) {
                  swConnect.setChecked(false);
                }
              }
            });
            builder.show();
          }
        }
      }
    });

    bSignal1 = (Button) view.findViewById(R.id.remo_signal_1_receive);
    bSignal1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (state == State.EXIST) {
          state = State.RECEIVEING;
          slotIndex = 1;
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
              state = State.EXIST;
              remoController.cancelReceiveMessages();
            }
          });
          progressDialog.show();
        }
      }
    });

    etSignal1 = (EditText) view.findViewById(R.id.remo_signal_1_name);
    etSignal1.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        Log.d(TAG, "onEditorAction: " + i);
        if (i == EditorInfo.IME_ACTION_DONE) {
//          mainActivity.autoSaveValue("REMO_SIGNAL_1_NAME", textView.getText().toString());
          layout.requestFocus();
        }
        return false;
      }
    });
    etSignal1.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        Log.d(TAG, "onFocusChange: " + b);
        if (b) {

        } else {
          mainActivity.autoSaveValue("REMO_SIGNAL_1_NAME", ((TextView)view).getText().toString());
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

    String signal1Name = mainActivity.getSavedValue("REMO_SIGNAL_1_NAME");
    etSignal1.setText(signal1Name);

    String address = mainActivity.getSavedValue("REMO_DEVICE_ADDRESS");
    String name = mainActivity.getSavedValue("REMO_DEVICE_NAME");
    tvName.setText(name);
    tvAddress.setText(address);
    if (!address.equals("")) {
      remoController.checkExist(address);
    }
  }


  private void setDevice(String name, String address) {
    ((MainActivity) getActivity()).autoSaveValue("REMO_DEVICE_NAME", name);
    ((MainActivity) getActivity()).autoSaveValue("REMO_DEVICE_ADDRESS", address);
    tvName.setText(name);
    tvAddress.setText(address);
  }

}
