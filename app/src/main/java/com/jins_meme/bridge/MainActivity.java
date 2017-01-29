package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

public class MainActivity extends AppCompatActivity {
  private static final String APP_ID = "907977722622109";
  private static final String APP_SECRET = "ka53fgrcct043wq3d6tm9gi8a2hetrxz";

  private MemeLib memeLib;

  // Test UI
  private Button btnScan;
  private Button btnConnect;

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

    btnScan = (Button)findViewById(R.id.scan);
    btnScan.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(btnScan.getText().toString().equals("Scan")) {
          btnScan.setText("Stop");

          startScan();
        }
        else if(btnScan.getText().toString().equals("Stop")) {
          btnScan.setText("Scan");

          stopScan();
        }
      }
    });

    btnConnect = (Button)findViewById(R.id.connect);
    btnConnect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(memeLib.isConnected()) {
          memeLib.disconnect();
        }
        else {
          //Intent intent = new Intent(MainActivity.this, )
        }
      }
    });
  }

  private void startScan() {
    Log.d("SCAN", "start scannig...");

    MemeStatus status = memeLib.startScan(new MemeScanListener() {
      @Override
      public void memeFoundCallback(String s) {
        Log.d("SCAN", "found: " + s);
      }
    });
  }

  private void stopScan() {
    Log.d("SCAN", "stop scannig...");

    if(memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("SCAN", "scan stopped.");
    }
  }
}
