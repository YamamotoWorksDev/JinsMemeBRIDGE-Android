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

/**
 *
 * Copylight (C) 2017, Shunichi Yamamoto, tkrworks.net
 *
 * This file is part of MemeBRIDGE.
 *
 **/

public class MainActivity extends AppCompatActivity {
  private static final String VERSION = "0.5.2";

  private static final int MENU_SCAN = 0;
  private static final int MENU_EXIT = 10;

  private MenuFragment menuFragment;
  private Handler handler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_main);
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

    menuFragment.init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.d("DEBUG", "test..." + menuFragment.getFoundMemeNum());

    menu.add(0, MENU_SCAN, 0, "SCAN").setCheckable(true);

    int index = 1;
    if(menuFragment.getFoundMemeNum() > 0) {
      for(String memeId : menuFragment.getScannedMemeList()) {
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

          menuFragment.clearMemeList();

          menuFragment.startScan();

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              item.setChecked(false);

              menuFragment.stopScan();
              invalidateOptionsMenu();
            }
          }, 5000);
        }
        else {
          item.setChecked(false);

          handler.removeCallbacks(null);

          menuFragment.stopScan();
        }
        return true;
      case MENU_EXIT:
        finish();
        break;
      default:
        Log.d("DEBUG", "check = " + item.isChecked());

        if(item.isChecked() && menuFragment.isMemeConnected()) {
          menuFragment.memeDisconnect();
          item.setChecked(false);
        }
        else if(!item.isChecked() && !menuFragment.isMemeConnected()) {
          Log.d("CONNECT", "meme ADDRESS: " + item.getTitle().toString());

          menuFragment.memeConnect(item.getTitle().toString());
          item.setChecked(true);
        }
        return true;
    }
    return super.onOptionsItemSelected(item);

  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.d("DEBUG", "onResume..." + menuFragment.getFoundMemeNum());
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
}
