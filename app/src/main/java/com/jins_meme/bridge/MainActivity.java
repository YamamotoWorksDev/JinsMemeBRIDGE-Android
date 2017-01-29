package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String VERSION = "0.3";

  private static final String APP_ID = "907977722622109";
  private static final String APP_SECRET = "ka53fgrcct043wq3d6tm9gi8a2hetrxz";

  private Handler handler;
  private MemeLib memeLib;
  private MemeOSC memeOSC;

  private List<String> scannedMemeList = new ArrayList<>();
  private ArrayAdapter<String> memeAdapter;

  // Test UI
  private ToggleButton btnScan;
  private ToggleButton btnConnect;
  private Spinner spnrScanResult;

  private MemeConnectListener memeConnectListener = new MemeConnectListener() {
    @Override
    public void memeConnectCallback(boolean b) {
      Log.d("CONNECT", "meme connected.");

      handler.post(new Runnable() {
        @Override
        public void run() {
          btnScan.setEnabled(false);
          spnrScanResult.setEnabled(false);
        }
      });

      memeLib.startDataReport(memeRealtimeListener);
    }

    @Override
    public void memeDisconnectCallback() {
      Log.d("CONNECT", "meme disconnected.");

      handler.post(new Runnable() {
        @Override
        public void run() {
          btnScan.setEnabled(true);
          spnrScanResult.setEnabled(true);
        }
      });
    }
  };

  private MemeRealtimeListener memeRealtimeListener = new MemeRealtimeListener() {
    @Override
    public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
      int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
      int eyeBlinkSpeed    = memeRealtimeData.getBlinkSpeed();
      int eyeUp    = memeRealtimeData.getEyeMoveUp();
      int eyeDown  = memeRealtimeData.getEyeMoveDown();
      int eyeLeft  = memeRealtimeData.getEyeMoveLeft();
      int eyeRight = memeRealtimeData.getEyeMoveRight();

      if(eyeBlinkStrength > 0 || eyeBlinkSpeed > 0 || eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
        Log.d("EYE", String.format("meme: BLINK = %d/%d, UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeBlinkStrength, eyeBlinkSpeed, eyeUp, eyeDown, eyeLeft, eyeRight));

        memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.EYE_BLINK);
        memeOSC.setTypeTag("ii");
        memeOSC.addArgument(eyeBlinkStrength);
        memeOSC.addArgument(eyeBlinkSpeed);
        memeOSC.flushMessage();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if(Build.VERSION.SDK_INT >= 23) {
      requestGPSPermission();
    }

    init();
  }

  @TargetApi(23)
  private void requestGPSPermission() {
    if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if(requestCode == 1) {
      if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("PERMISSION", "Succeeded");
        Toast.makeText(MainActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
      }
      else {
        Log.d("PERMISSION", "Failed");
        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
      }
    }
    else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  private void init() {
    MemeLib.setAppClientID(getApplicationContext(), APP_ID, APP_SECRET);
    memeLib = MemeLib.getInstance();
    memeLib.setAutoConnect(false);

    memeOSC = new MemeOSC();
    memeOSC.setRemoteIP("192.168.1.255");
    memeOSC.setRemotePort(10316);
    memeOSC.setHostPort(11316);
    memeOSC.initSocket();

    handler = new Handler();

    btnScan = (ToggleButton)findViewById(R.id.scan);
    btnScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(btnScan.isChecked()) {
          if(memeAdapter != null)
            memeAdapter.clear();

          if(scannedMemeList != null)
            scannedMemeList.clear();

          startScan();

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              btnScan.setChecked(false);
            }
          }, 5000);
        }
        else {
          handler.removeCallbacks(null);

          stopScan();
        }
      }
    });

    btnConnect = (ToggleButton)findViewById(R.id.connect);
    btnConnect.setEnabled(false);
    btnConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(memeLib.isConnected()) {
          memeLib.disconnect();
        }
        else {
          Log.d("CONNECT", "meme ADDRESS: " + spnrScanResult.getSelectedItem().toString());
          memeLib.connect(spnrScanResult.getSelectedItem().toString());
        }
      }
    });

    spnrScanResult = (Spinner)findViewById(R.id.scan_result);
    spnrScanResult.setEnabled(false);

  }

  private void startScan() {
    Log.d("SCAN", "start scannig...");

    memeLib.setMemeConnectListener(memeConnectListener);

    MemeStatus status = memeLib.startScan(new MemeScanListener() {
      @Override
      public void memeFoundCallback(String s) {
        Log.d("SCAN", "found: " + s);

        scannedMemeList.add(s);
      }
    });
  }

  private void stopScan() {
    Log.d("SCAN", "stop scannig...");

    if(memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("SCAN", "scan stopped.");

      List<String> list = new ArrayList<>(new HashSet<>(scannedMemeList));
      memeAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, list);
      memeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spnrScanResult.setAdapter(memeAdapter);

      if(scannedMemeList.size() > 0) {
        spnrScanResult.setEnabled(true);
        btnConnect.setEnabled(true);
      }
    }
  }
}
