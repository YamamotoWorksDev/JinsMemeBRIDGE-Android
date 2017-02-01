package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Copylight (C) 2017, Shunichi Yamamoto, tkrworks.net
 *
 * This file is part of MemeBRIDGE.
 *
 **/

public class MainActivity extends AppCompatActivity implements MemeConnectListener {
  private static final String VERSION = "0.5.3";

  private static final String APP_ID = "907977722622109";
  private static final String APP_SECRET = "ka53fgrcct043wq3d6tm9gi8a2hetrxz";

  private static final int MENU_SCAN = 0;
  private static final int MENU_EXIT = 10;

  private MemeLib memeLib;
  private MenuFragment menuFragment;
  private Handler handler;

  private List<String> scannedMemeList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bridge_menu);

    handler = new Handler();
    menuFragment = (MenuFragment)getFragmentManager().findFragmentById(R.id.fragment);

    if(Build.VERSION.SDK_INT >= 23) {
      requestGPSPermission();
    }

    Log.d("DEBUG", "flag = " + MemeMIDI.checkUsbMidi(this));
    if(!MemeMIDI.checkUsbMidi(this)) {
      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Warning");
      alert.setMessage("Please change your USB Connection Type to MIDI and restart.");
      alert.setNeutralButton("Exit", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          finish();
        }
      });

      alert.create().show();
    }

    init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.d("DEBUG", "test..." + scannedMemeList.size());

    menu.add(0, MENU_SCAN, 0, "SCAN").setCheckable(true);

    int index = 1;
    if(scannedMemeList.size() > 0) {
      for(String memeId : scannedMemeList) {
        menu.add(0, MENU_SCAN + index, 0, memeId).setCheckable(true);
      }
    }

    menu.add(0, MENU_EXIT, 0, "EXIT");

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    Log.d("DEBUG", "item id = " + item.getItemId());

    switch(item.getItemId()) {
      case MENU_SCAN:
        if(!item.isChecked()) {
          item.setChecked(true);

          if(scannedMemeList != null)
            scannedMemeList.clear();

          startScan();

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              item.setChecked(false);

              stopScan();
            }
          }, 5000);
        }
        else {
          item.setChecked(false);

          handler.removeCallbacks(null);

          stopScan();
        }
        return true;
      case MENU_EXIT:
        finish();
        break;
      default:
        Log.d("DEBUG", "check = " + item.isChecked());

        if(item.isChecked() && memeLib.isConnected()) {
          memeLib.disconnect();
          item.setChecked(false);
        }
        else if(!item.isChecked() && !memeLib.isConnected()) {
          Log.d("CONNECT", "meme ADDRESS: " + item.getTitle().toString());

          memeLib.connect(item.getTitle().toString());
          item.setChecked(true);
        }
        return true;
    }
    return super.onOptionsItemSelected(item);

  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.d("DEBUG", "onResume..." + scannedMemeList.size());
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

  @Override
  public void memeConnectCallback(boolean b) {
    Log.d("CONNECT", "meme connected.");

    memeLib.startDataReport(menuFragment);
  }

  @Override
  public void memeDisconnectCallback() {
    Log.d("CONNECT", "meme disconnected.");
  }

  public void init() {
    MemeLib.setAppClientID(this, APP_ID, APP_SECRET);
    memeLib = MemeLib.getInstance();
    memeLib.setAutoConnect(false);

    handler = new Handler();
  }

  public void startScan() {
    Log.d("SCAN", "start scannig...");

    memeLib.setMemeConnectListener(this);

    MemeStatus status = memeLib.startScan(new MemeScanListener() {
      @Override
      public void memeFoundCallback(String s) {
        Log.d("SCAN", "found: " + s);

        scannedMemeList.add(s);
      }
    });
  }

  public void stopScan() {
    Log.d("SCAN", "stop scannig...");

    if(memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("SCAN", "scan stopped.");

      invalidateOptionsMenu();
    }
  }
}
