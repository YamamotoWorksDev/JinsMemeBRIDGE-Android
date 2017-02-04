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
 * MainActivity.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class MainActivity extends AppCompatActivity implements MemeConnectListener {
  private static final String VERSION = "0.5.9";

  private static final String APP_ID = "907977722622109";
  private static final String APP_SECRET = "ka53fgrcct043wq3d6tm9gi8a2hetrxz";

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
      alert.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Quit App...");

          finish();
        }
      });
      alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Close Alert Dialog...");
        }
      });

      alert.create().show();
    }

    init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //Log.d("DEBUG", "test..." + scannedMemeList.size() + " connectedDeviceName : " + memeBTSPP.getConnectedDeviceName());

    int index = 0;
    for(String pairedDeviceName : menuFragment.getBtPairedDeviceName()) {
      if(pairedDeviceName.equals(menuFragment.getBtConnectedDeviceName())) {
        menu.add(0, index, 0, pairedDeviceName).setCheckable(true).setChecked(true);
      }
      else {
        menu.add(0, index, 0, pairedDeviceName).setCheckable(true).setChecked(false);
      }
      index++;
    }

    menu.add(0, index, 0, "SCAN").setCheckable(true);
    index++;

    if(scannedMemeList.size() > 0 && scannedMemeList.size() < 9) {
      for(String memeId : scannedMemeList) {
        menu.add(0, index, 0, memeId).setCheckable(true);
        index++;
      }
    }

    menu.add(0, index++, 0, "EXIT");
    menu.add(0, index++, 0, "Ver." + VERSION);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    String itemTitle = item.getTitle().toString();

    Log.d("DEBUG", "item id = " + item.getItemId() + " " + itemTitle);

    if(itemTitle.equals("SCAN")) {
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
    }
    else if(itemTitle.equals("EXIT")) {
      finish();
    }
    else if(scannedMemeList.contains(itemTitle)) {
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
    else {
      if(item.isChecked()) {
        Log.d("DEBUG", "disconnect....");
        menuFragment.btDisconnect();
        item.setChecked(false);
      }
      else {
        Log.d("DEBUG", "connect....");
        menuFragment.btConnect(itemTitle);
        item.setChecked(true);
      }
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.d("DEBUG", "onResume..." + scannedMemeList.size());
  }

  @Override
  protected void onStop() {
    super.onStop();

    Log.d("DEBUG", "onStop...");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if(memeLib.isConnected()) {
      memeLib.disconnect();
      memeLib = null;
    }

    menuFragment = null;

    handler = null;

    Log.d("DEBUG", "onDestroy...");
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

    //Log.d("DEBUG", "devs : " + memeBTSPP.getPairedDeviceName());
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
