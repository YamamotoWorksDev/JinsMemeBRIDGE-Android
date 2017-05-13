/**
 * MainActivity.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemeConnectListener {

  private String appID = null;
  private String appSecret = null;

  private String lastConnectedMemeID;

  private SharedPreferences preferences;
  private SharedPreferences.Editor editor;

  private Handler handler;
  private FrameLayout mainLayout;

  private MemeLib memeLib;
  private List<String> scannedMemeList = new ArrayList<>();
  private MenuFragment menuFragment;
  private BasicConfigFragment basicConfigFragment;
  private AboutFragment aboutFragment;

  private OSCConfigFragment oscConfigFragment;
  private MIDIConfigFragment midiConfigFragment;
  /*
   * MODIFY YOURSELF
   * Add your implemented function's configuration
   *
   */
  // private ***ConfigFragment ***ConfigFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bridge_menu);

    handler = new Handler();
    mainLayout = (FrameLayout) findViewById(R.id.container);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = preferences.edit();

    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));

    menuFragment = new MenuFragment();
    basicConfigFragment = new BasicConfigFragment();
    oscConfigFragment = new OSCConfigFragment();
    midiConfigFragment = new MIDIConfigFragment();
    aboutFragment = new AboutFragment();
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    // ***ConfigFragment = new ***ConfigFragment

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.replace(R.id.container, menuFragment);
    transaction.addToBackStack("MAIN");
    transaction.commit();

    checkBluetoothEnable();

    if (Build.VERSION.SDK_INT >= 23) {
      requestGPSPermission();
    }

    lastConnectedMemeID = preferences.getString("LAST_CONNECTED_MEME_ID", null);
    initMemeLib();

    if(lastConnectedMemeID != null) {
      Log.d("MAIN", "SCAN Start");
      Toast.makeText(this, "SCANNING...", Toast.LENGTH_SHORT).show();

      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          startScan();

          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              stopScan();

              if (getScannedMemeSize() > 0 && scannedMemeList.contains(lastConnectedMemeID)) {
                connectToMeme(lastConnectedMemeID);

                //Toast.makeText(, "SCANNING...", Toast.LENGTH_SHORT).show();
              }
            }
          }, 3000);
        }
      }, 10000);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int index = 0;

    menu.add(0, index++, 0, R.string.basic_conf);

    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    menu.add(0, index++, 0, R.string.osc_conf);
    menu.add(0, index++, 0, R.string.midi_conf);

    menu.add(0, index++, 0, R.string.about);
    menu.add(0, index, 0, R.string.exit);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    String barTitle = getSupportActionBar().getTitle().toString();

    if (!barTitle.contains(getString(R.string.app_name))) {
      for (int i = 0; i < menu.size(); i++) {
        MenuItem item = menu.getItem(i);
        String title = item.getTitle().toString();
        if (barTitle.contains(title)) {
          item.setVisible(false);
        } else {
          item.setVisible(true);
        }
      }
    } else {
      for (int i = 0; i < menu.size(); i++) {
        menu.getItem(i).setVisible(true);
      }
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    CharSequence cs = item.getTitle();

    if (cs == null) {
      Log.d("DEBUG", "press actionbar back!");

      transitToMain(0);

      return true;
    }

    String itemTitle = item.getTitle().toString();

    Log.d("DEBUG", "item id = " + item.getItemId() + " " + itemTitle);

    if (itemTitle.equals(getString(R.string.basic_conf))) {
      Log.d("DEBUG", "tap basic setting");

      transitToConfig(basicConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.osc_conf))) {
      Log.d("DEBUG", "tap osc setting");

      transitToConfig(oscConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.midi_conf))) {
      Log.d("DEBUG", "tap midi setting");

      transitToConfig(midiConfigFragment);

      return true;
    }
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    /*
    else if(itemTitle.equals(getString(R.string.***_config) {
      Log.d("DEBUG", "tap *** setting");

      transittToConfig(***ConfigFragment);

      return true;
    }
     */
    else if (itemTitle.equals(getString(R.string.about))) {
      Log.d("DEBUG", "tap about");

      transitToConfig(aboutFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.exit))) {
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.d("MAIN", "onResume..." + scannedMemeList.size());
  }

  @Override
  protected void onStop() {
    super.onStop();

    Log.d("MAIN", "onStop...");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (memeLib != null && memeLib.isConnected()) {
      memeLib.disconnect();
      memeLib = null;
    }

    menuFragment = null;
    basicConfigFragment = null;
    aboutFragment = null;

    oscConfigFragment = null;
    midiConfigFragment = null;
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    // ***ConfigFragment = null;

    Log.d("MAIN", "onDestroy...");
  }

  private void checkBluetoothEnable() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (!bluetoothAdapter.isEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, 0);
      /*
      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Warning");
      alert.setMessage("Please change your USB Connection Type to MIDI and restart.");
      alert.setPositiveButton("NO", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Quit App...");

          finish();
        }
      });
      alert.setNegativeButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Close Alert Dialog...");


        }
      });

      alert.create().show();
      */
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 0) {
      if (resultCode == Activity.RESULT_OK) {
        Log.d("MAIN", "Bluetooth ON");
      }
      else {
        finish();
      }
    }
  }

  @TargetApi(23)
  private void requestGPSPermission() {
    if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    if (requestCode == 1) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("PERMISSION", "Succeeded");
        Toast.makeText(MainActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
      } else {
        Log.d("PERMISSION", "Failed");
        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  public void memeConnectCallback(boolean b) {
    Log.d("MAIN", "meme connected. " + b + " " + lastConnectedMemeID);

    if (b) {
      autoSaveText("LAST_CONNECTED_MEME_ID", lastConnectedMemeID);
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(0x3F, 0x51, 0xB5)));

        Toast.makeText(MainActivity.this, "JINS MEME CONNECTED", Toast.LENGTH_SHORT).show();
      }
    });

    memeLib.startDataReport(menuFragment);
  }

  @Override
  public void memeDisconnectCallback() {
    Log.d("MAIN", "meme disconnected.");

    handler.post(new Runnable() {
      @Override
      public void run() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));

        Toast.makeText(MainActivity.this, "JINS MEME DISCONNECTED", Toast.LENGTH_SHORT).show();
      }
    });
  }

  public void initMemeLib() {
    appID = preferences.getString("APP_ID", getString(R.string.meme_app_id));
    appSecret = preferences.getString("APP_SECRET", getString(R.string.meme_app_secret));

    if (appID != null && appSecret != null) {
      Log.d("MAIN", "Initialized MemeLib with " + appID + " and " + appSecret);

      MemeLib.setAppClientID(this, appID, appSecret);
      memeLib = MemeLib.getInstance();
      memeLib.setAutoConnect(false);
    }

    //Log.d("DEBUG", "devs : " + memeBTSPP.getPairedDeviceName());
  }

  public void startScan() {
    Log.d("MAIN", "start scannig...");

    if (scannedMemeList != null) {
      scannedMemeList.clear();
    }

    memeLib.setMemeConnectListener(this);

    MemeStatus status = memeLib.startScan(new MemeScanListener() {
      @Override
      public void memeFoundCallback(String s) {
        Log.d("MAIN", "found: " + s);

        scannedMemeList.add(s);
      }
    });
  }

  public void stopScan() {
    Log.d("MAIN", "stop scannig...");

    if (memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("MAIN", "scan stopped.");

      invalidateOptionsMenu();
    }
  }

  public List<String> getScannedMemeList() {
    return scannedMemeList;
  }

  public int getScannedMemeSize() {
    return scannedMemeList.size();
  }

  public boolean isMemeConnected() {
    return memeLib.isConnected();
  }

  public void connectToMeme(String id) {
    lastConnectedMemeID = id;

    memeLib.connect(id);
  }

  public void disconnectToMeme() {
    memeLib.disconnect();
  }

  @Override
  public void onBackPressed() {
    Log.d("MAIN", "press back!");

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

  void transitToMain(final int direction) {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (direction) {
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
    }, 50);
  }

  String getSavedText(String key) {
    return preferences.getString(key, null);
  }

  void autoSaveText(String key, String text) {
    editor.putString(key, text);
    editor.apply();
  }
}
