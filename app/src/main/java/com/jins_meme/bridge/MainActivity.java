package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
  // please write your APP_ID and APPSSECRET
  private static final String APP_ID = "";
  private static final String APP_SECRET = "";

  private Handler handler;
  private MemeLib memeLib;
  private List<String> scannedMemeList = new ArrayList<>();
  private MenuFragment menuFragment;
  private BasicConfigFragment basicConfigFragment;
  private AboutFragment aboutFragment;

  /*
   * MODIFY YOURSELF
   * Add your implemented function's configuration
   *
   */
  private OSCConfigFragment oscConfigFragment;
  private MIDIConfigFragment midiConfigFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bridge_menu);

    handler = new Handler();
    menuFragment = new MenuFragment();
    basicConfigFragment = new BasicConfigFragment();
    oscConfigFragment = new OSCConfigFragment();
    midiConfigFragment = new MIDIConfigFragment();
    aboutFragment = new AboutFragment();

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.replace(R.id.container, menuFragment);
    transaction.addToBackStack("MAIN");
    transaction.commit();

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
    int index = 0;
    /*
    for(String pairedDeviceName : menuFragment.getBtPairedDeviceName()) {
      if(pairedDeviceName.equals(menuFragment.getBtConnectedDeviceName())) {
        menu.add(0, index, 0, pairedDeviceName).setCheckable(true).setChecked(true);
      }
      else {
        menu.add(0, index, 0, pairedDeviceName).setCheckable(true).setChecked(false);
      }
      index++;
    }
    */

    menu.add(0, index, 0, R.string.scan).setCheckable(true);
    index++;

    if(scannedMemeList.size() > 0 && scannedMemeList.size() < 9) {
      for(String memeId : scannedMemeList) {
        menu.add(0, index, 0, memeId).setCheckable(true);
        index++;
      }
    }

    menu.add(0, index++, 0, R.string.basic_conf);

    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    menu.add(0, index++, 0, R.string.osc_conf);
    menu.add(0, index++, 0, R.string.midi_conf);

    menu.add(0, index++, 0, R.string.about);
    menu.add(0, index,   0, R.string.exit);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    String barTitle = getSupportActionBar().getTitle().toString();

    if(!barTitle.contains(getString(R.string.app_name))) {
      for(int i = 0; i < menu.size(); i++) {
        MenuItem item = menu.getItem(i);
        String title = item.getTitle().toString();
        if(barTitle.contains(title)) {
          item.setVisible(false);
        }
        else {
          item.setVisible(true);
        }
      }
    }
    else {
      for(int i = 0; i < menu.size(); i++) {
        menu.getItem(i).setVisible(true);
      }
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    CharSequence cs = item.getTitle();

    if(cs == null) {
      Log.d("DEBUG", "press actionbar back!");

      transitToMain(0);

      return true;
    }

    String itemTitle = item.getTitle().toString();

    Log.d("DEBUG", "item id = " + item.getItemId() + " " + itemTitle);

    if(itemTitle.equals(getString(R.string.scan))) {
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
    else if(itemTitle.equals(getString(R.string.basic_conf))) {
      Log.d("DEBUG", "tap basic setting");

      transitToConfig(basicConfigFragment);

      return true;
    }
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    else if(itemTitle.equals(getString(R.string.osc_conf))) {
      Log.d("DEBUG", "tap osc setting");

      transitToConfig(oscConfigFragment);

      return true;
    }
    else if(itemTitle.equals(getString(R.string.midi_conf))) {
      Log.d("DEBUG", "tap midi setting");

      transitToConfig(midiConfigFragment);

      return true;
    }
    else if(itemTitle.equals(getString(R.string.about))) {
      Log.d("DEBUG", "tap about");

      transitToConfig(aboutFragment);

      return true;
    }
    else if(itemTitle.equals(getString(R.string.exit))) {
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

  @Override
  public void onBackPressed() {
    Log.d("DEBUG", "press back!");

    transitToMain(1);
  }

  void setActionBarTitle(@NonNull String title) {
    getSupportActionBar().setTitle(title);
  }

  void setActionBarBack(boolean flag) {
    getSupportActionBar().setDisplayHomeAsUpEnabled(flag);
  }

  void transitToConfig(Fragment fragment) {
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();

    transaction.setCustomAnimations(R.anim.config_in, android.R.anim.fade_out);
    transaction.replace(R.id.container, fragment);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  void transitToMain(int direction) {
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();

    switch(direction) {
      case 0:
        transaction.setCustomAnimations(android.R.anim.fade_in, R.anim.config_out);
        break;
      case 1:
        transaction.setCustomAnimations(android.R.anim.fade_in, R.anim.config_out2);
        break;
    }
    transaction.replace(R.id.container, menuFragment);
    transaction.addToBackStack(null);
    transaction.commit();

    setActionBarTitle(getString(R.string.app_name));
    setActionBarBack(false);
  }
}
