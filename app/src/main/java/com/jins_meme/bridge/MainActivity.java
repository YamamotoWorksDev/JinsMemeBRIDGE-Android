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
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeFitStatus;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemeConnectListener,
    MemeRealtimeListener,
    RootMenuFragment.OnFragmentInteractionListener, MIDIMenuFragment.OnFragmentInteractionListener,
    OSCMenuFragment.OnFragmentInteractionListener, HueMenuFragment.OnFragmentInteractionListener,
    CameraFragment.OnFragmentInteractionListener {

  private String appID = null;
  private String appSecret = null;

  private String lastConnectedMemeID;

  private int blinkThreshold = 90;
  private int upDownThreshold = 0;
  private int leftRightThreshold = 0;
  private int rollThreshold = 15;
  private int batteryStatus = 0;

  private SharedPreferences preferences;
  private SharedPreferences.Editor editor;

  private Handler handler;
  private FrameLayout mainLayout;

  private MemeLib memeLib = null;
  private List<String> scannedMemeList = new ArrayList<>();
  private MemeRealtimeDataFilter mMemeDataFilter = new MemeRealtimeDataFilter();
  private static final int PAUSE_MAX = 50;
  private static final int REFRACTORY_PERIOD_MAX = 20;
  private boolean cancelFlag = false;
  private int pauseCount = 0;
  private boolean pauseFlag = false;
  private int refractoryPeriod = 0;

  private static final int BATTERY_CHECK_INTERVAL = 2000;
  private int batteryCheckCount = BATTERY_CHECK_INTERVAL;

  private RootMenuFragment rootMenu;
  private MIDIMenuFragment midiMenu;
  private OSCMenuFragment oscMenu;
  private HueMenuFragment hueMenu;
  private ArrayList<MenuFragmentBase> menus = new ArrayList<MenuFragmentBase>();

  private BasicConfigFragment basicConfigFragment;
  private AboutFragment aboutFragment;

  private OSCConfigFragment oscConfigFragment;
  private MIDIConfigFragment midiConfigFragment;
  private HueConfigFragment hueConfigFragment;
  /*
   * MODIFY YOURSELF
   * Add your implemented function's configuration
   *
   */
  // private ***ConfigFragment ***ConfigFragment;

  public int getBlinkThreshold() {
    return blinkThreshold;
  }

  public int getUpDownThreshold() {
    return upDownThreshold;
  }

  public int getLeftRightThreshold() {
    return leftRightThreshold;
  }

  public int getRollThreshold() {
    return rollThreshold;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_bridge_menu);

    setActionBarTitle(R.string.actionbar_title);

    handler = new Handler();
    mainLayout = (FrameLayout) findViewById(R.id.container);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = preferences.edit();

    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));

    rootMenu = new RootMenuFragment();
    midiMenu = new MIDIMenuFragment();
    oscMenu = new OSCMenuFragment();
    hueMenu = new HueMenuFragment();
    menus.add(rootMenu);
    menus.add(midiMenu);
    menus.add(oscMenu);
    menus.add(hueMenu);

    cancelFlag = false;
    pauseCount = 0;
    pauseFlag = false;
    refractoryPeriod = 0;

    basicConfigFragment = new BasicConfigFragment();
    oscConfigFragment = new OSCConfigFragment();
    midiConfigFragment = new MIDIConfigFragment();
    hueConfigFragment = new HueConfigFragment();
    aboutFragment = new AboutFragment();
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    // ***ConfigFragment = new ***ConfigFragment

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    for(Fragment m : menus) {
      transaction.add(R.id.container, m);
      transaction.hide(m);
    }
    transaction.show(rootMenu);
    transaction.commit();

    checkBluetoothEnable();

    if (Build.VERSION.SDK_INT >= 23) {
      requestGPSPermission();
    }

    lastConnectedMemeID = preferences.getString("LAST_CONNECTED_MEME_ID", null);
    if (lastConnectedMemeID != null) {
      Log.d("MAIN", "SCAN Start");
      Toast.makeText(this, getString(R.string.meme_scanning), Toast.LENGTH_SHORT).show();

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

                final String s2 = lastConnectedMemeID;
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(MainActivity.this, getString(R.string.meme_connect, s2),
                        Toast.LENGTH_SHORT).show();
                  }
                });
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

    menu.add(0, index++, 0, R.string.battery);
    menu.add(0, index++, 0, R.string.basic_conf);

    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    menu.add(0, index++, 0, R.string.osc_conf);
    menu.add(0, index++, 0, R.string.midi_conf);
    menu.add(0, index++, 0, R.string.hue_conf);

    menu.add(0, index++, 0, R.string.about);
    menu.add(0, index, 0, R.string.exit_app);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    String barTitle = getSupportActionBar().getTitle().toString();

    Log.d("DEBUG", "Title = " + barTitle);

    if (barTitle.contains(getString(R.string.app_name))) {
      Log.d("DEBUG", "true");

      switch (batteryStatus) {
        case 1:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_alert_white_24dp);
          break;
        case 2:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_30_white_24dp);
          break;
        case 3:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_50_white_24dp);
          break;
        case 4:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_80_white_24dp);
          break;
        case 5:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_full_white_24dp);
          break;
        default:
          menu.getItem(0).setIcon(R.mipmap.ic_battery_unknown_white_24dp);
          break;
      }
      menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      menu.getItem(0).setVisible(true);
    } else {
      Log.d("DEBUG", "false");

      menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      menu.getItem(0).setVisible(false);
    }

    if (!barTitle.contains(getString(R.string.app_name))) {
      for (int i = 1; i < menu.size(); i++) {
        MenuItem item = menu.getItem(i);
        String title = item.getTitle().toString();
        if (barTitle.contains(title)) {
          item.setVisible(false);
        } else {
          item.setVisible(true);
        }
      }
    } else {
      for (int i = 1; i < menu.size(); i++) {
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
      return true;
    }

    String itemTitle = item.getTitle().toString();

    Log.d("DEBUG", "item id = " + item.getItemId() + " " + itemTitle);

    if (itemTitle.equals(getString(R.string.basic_conf))) {
      Log.d("DEBUG", "tap basic setting");

      transitToFragment(basicConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.osc_conf))) {
      Log.d("DEBUG", "tap osc setting");

      transitToFragment(oscConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.midi_conf))) {
      Log.d("DEBUG", "tap midi setting");

      transitToFragment(midiConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.hue_conf))) {
      Log.d("DEBUG", "tap hue setting");

      transitToFragment(hueConfigFragment);

      return true;
    }
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    /*
    else if (itemTitle.equals(getString(R.string.***_config) {
      Log.d("DEBUG", "tap *** setting");

      transittToConfig(***ConfigFragment);

      return true;
    }
     */
    else if (itemTitle.equals(getString(R.string.about))) {
      Log.d("DEBUG", "tap about");

      transitToFragment(aboutFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.exit_app))) {
      finishAndRemoveTask();
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

    rootMenu = null;
    midiMenu = null;
    oscMenu = null;
    hueMenu = null;

    basicConfigFragment = null;
    aboutFragment = null;

    oscConfigFragment = null;
    midiConfigFragment = null;
    hueConfigFragment = null;
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
    if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, 0);
    } else if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
      Log.d("MAIN", "Initialize MEME LIB");
      initMemeLib();
    }
  }

  @Override
  public void openNextMenu(int card_id) {
    switch(card_id) {
      case R.string.midi:
        transitToMenu(midiMenu);
        break;
      case R.string.osc:
        transitToMenu(oscMenu);
        break;
      case R.string.hue:
        transitToMenu(hueMenu);
        break;
      case R.string.camera:
        transitToFragment(new CameraFragment());
        break;
    }
  }

  @Override
  public void backToPreviousMenu() {
    super.onBackPressed();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        Log.d("MAIN", "Bluetooth ON");
      } else {
        finishAndRemoveTask();
      }
    } else {
      if (resultCode == RESULT_OK) {
        Log.d("MAIN", "Auth OK");

        basicConfigFragment.unlockAppIDandSecret();
      } else {
        Log.d("MAIN", "Auth NG");

        basicConfigFragment.lockAppIDandSecret();
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
        Toast.makeText(MainActivity.this, getString(R.string.succeeded), Toast.LENGTH_SHORT)
            .show();
      } else {
        Log.d("PERMISSION", "Failed");
        Toast.makeText(MainActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT)
            .show();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  @Override
  public void memeConnectCallback(boolean b) {
    Log.d("MAIN", "meme connected. " + b + " " + lastConnectedMemeID);

    if (b) {
      autoSaveValue("LAST_CONNECTED_MEME_ID", lastConnectedMemeID);
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        getSupportActionBar()
            .setBackgroundDrawable(new ColorDrawable(Color.rgb(0x3F, 0x51, 0xB5)));

        Toast.makeText(MainActivity.this, getString(R.string.meme_connected),
            Toast.LENGTH_SHORT).show();
      }
    });
    invalidateOptionsMenu();

    memeLib.startDataReport(this);
  }

  @Override
  public void memeDisconnectCallback() {
    Log.d("MAIN", "meme disconnected.");

    handler.post(new Runnable() {
      @Override
      public void run() {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));

        invalidateOptionsMenu();
        basicConfigFragment.setSwConnect(false);

        Toast.makeText(MainActivity.this, getString(R.string.meme_disconnected), Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  public void initMemeLib() {
    appID = preferences.getString("APP_ID", getString(R.string.meme_app_id));
    appSecret = preferences.getString("APP_SECRET", getString(R.string.meme_app_secret));

    Log.d("MAIN", "APP_ID: " + appID + " APP_SECRET: " + appSecret);

    if (appID != null && appID.length() > 0 && appSecret != null && appSecret.length() > 0) {
      Log.d("MAIN", "Initialized MemeLib with " + appID + " and " + appSecret);

      MemeLib.setAppClientID(this, appID, appSecret);
      memeLib = MemeLib.getInstance();
      memeLib.setAutoConnect(false);
    }

    //Log.d("DEBUG", "devs : " + memeBTSPP.getPairedDeviceName());
  }

  public void clearScannedMemeList() {
    if (scannedMemeList != null) {
      scannedMemeList.clear();
    }
  }

  public void startScan() {
    Log.d("MAIN", "start scannig...");

    if (scannedMemeList != null) {
      scannedMemeList.clear();
    }

    if (memeLib == null) {
      Log.d("MAIN", "memeLib is null!");
    }

    if (memeLib != null) {
      memeLib.setMemeConnectListener(this);

      MemeStatus status = memeLib.startScan(new MemeScanListener() {
        @Override
        public void memeFoundCallback(String s) {
          Log.d("MAIN", getString(R.string.meme_found, s));

          /*
          final String s2 = s;
          handler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(MainActivity.this, getString(R.string.meme_found, s2), Toast.LENGTH_SHORT).show();
            }
          });
          */

          if (getScannedMemeSize() > 0 && scannedMemeList.contains(lastConnectedMemeID)) {
            handler.removeCallbacksAndMessages(null);

            connectToMeme(lastConnectedMemeID);

            /*
            handler.post(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(MainActivity.this, getString(R.string.meme_connect, s2), Toast.LENGTH_SHORT).show();
              }
            });
            */
          }

          scannedMemeList.add(s);
        }
      });

      Log.d("MAIN", "MemeStatus = " + status);

      switch (status) {
        case MEME_ERROR_SDK_AUTH:
        case MEME_ERROR_APP_AUTH:
          showAppIDandSecretWarning();
          break;
      }
    } else {

    }
  }

  public void stopScan() {
    Log.d("MAIN", "stop scannig...");

    if (memeLib != null && memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("MAIN", "scan stopped.");
    }
  }

  @Override
  public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
    if (++batteryCheckCount > BATTERY_CHECK_INTERVAL) {
      Log.d("DEBUG", "battery status = " + memeRealtimeData.getPowerLeft());
      renewBatteryState(memeRealtimeData.getPowerLeft());

      batteryCheckCount = 0;
    }
    float accelX = memeRealtimeData.getAccX();
    float accelY = memeRealtimeData.getAccY();
    float accelZ = memeRealtimeData.getAccZ();

    int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
    int eyeBlinkSpeed = memeRealtimeData.getBlinkSpeed();

    int eyeUp = memeRealtimeData.getEyeMoveUp();
    int eyeDown = memeRealtimeData.getEyeMoveDown();
    int eyeLeft = memeRealtimeData.getEyeMoveLeft();
    int eyeRight = memeRealtimeData.getEyeMoveRight();

    float yaw = memeRealtimeData.getYaw();
    float pitch = memeRealtimeData.getPitch();
    float roll = memeRealtimeData.getRoll();
    if (memeRealtimeData.getFitError() == MemeFitStatus.MEME_FIT_OK) {
      if (Math.abs(roll) > getRollThreshold()) {
        cancelFlag = true;
        //Log.d("DEBUG", "menu = " + getResources().getString(currentEnteredMenu) + " / item = " + getResources().getString(currentSelectedItem));

        if (!pauseFlag) {
          if (++pauseCount >= PAUSE_MAX) {
            pauseFlag = true;
            handler.post(new Runnable() {
              @Override
              public void run() {
                findViewById(R.id.pauseView).setVisibility(View.VISIBLE);
              }
            });
            Log.d("=========PAUSE=========", "pause");
          }
        }
      } else if (Math.abs(roll) <= getRollThreshold()) {
        final MenuFragmentBase active = getVisibleMenuFragment();
        if (!pauseFlag) {
          if (cancelFlag && pauseCount < PAUSE_MAX) {
            if(transitToRootMenu()) {
              refractoryPeriod = REFRACTORY_PERIOD_MAX;
              Log.d("=========PAUSE=========", "cancel");
            }
          }
          cancelFlag = false;
          pauseCount = 0;
        }
        if (refractoryPeriod > 0) {
          refractoryPeriod--;
        } else {
          mMemeDataFilter.update(memeRealtimeData, getBlinkThreshold(), getUpDownThreshold(), getLeftRightThreshold());
          if(pauseFlag) {
            if(mMemeDataFilter.isBlink()) {
              pauseFlag = false;
              handler.post(new Runnable() {
                @Override
                public void run() {
                  findViewById(R.id.pauseView).setVisibility(View.GONE);
                }
              });
              Log.d("=========PAUSE=========", "pause clear");
            }
          }
          else if(active instanceof MemeRealtimeDataFilter.MemeFilteredDataCallback) {
            final MemeRealtimeDataFilter.MemeFilteredDataCallback accepter = (MemeRealtimeDataFilter.MemeFilteredDataCallback)active;
            if (mMemeDataFilter.isBlink()) {
              Log.d("EYE", "blink = " + eyeBlinkStrength + " " + eyeBlinkSpeed);
              handler.post(new Runnable() {
                @Override
                public void run() {
                  accepter.onMemeBlinked();
                }
              });
            } else if (mMemeDataFilter.isLeft()) {
              Log.d("EYE", "left = " + eyeLeft);
              handler.post(new Runnable() {
                @Override
                public void run() {
                  accepter.onMemeMoveLeft();
                }
              });
            } else if (mMemeDataFilter.isRight()) {
              Log.d("EYE", "right = " + eyeRight);
              handler.post(new Runnable() {
                @Override
                public void run() {
                  accepter.onMemeMoveRight();
                }
              });
            }
          }
        }

      }
    }
  }

  public List<String> getScannedMemeList() {
    return scannedMemeList;
  }

  public int getScannedMemeSize() {
    return scannedMemeList.size();
  }

  public boolean isMemeConnected() {
    return memeLib != null && memeLib.isConnected();
  }

  public void connectToMeme(String id) {
    lastConnectedMemeID = id;

    memeLib.connect(id);
  }

  public void disconnectToMeme() {
    if (memeLib.isConnected()) {
      memeLib.disconnect();
    }
  }
  @Override
  public void onBackPressed() {
    Log.d("MAIN", "press back!");

    FragmentManager manager = getSupportFragmentManager();
    MenuFragmentBase active = getVisibleMenuFragment();

    if(active == null || !active.menuBack()) {
      super.onBackPressed();
    }
    if(manager.getBackStackEntryCount() == 0) {
      setActionBarTitle(R.string.actionbar_title);
      setActionBarBack(false);
      invalidateOptionsMenu();
    }
  }

  void setActionBarTitle(int resId) {
    switch (resId) {
      case R.string.actionbar_title:
      case R.string.about:
        getSupportActionBar().setTitle(getString(resId));
        break;
      default:
        getSupportActionBar().setTitle(getString(resId) + " SETTING");
        break;
    }
  }

  void setActionBarBack(boolean flag) {
    getSupportActionBar().setDisplayHomeAsUpEnabled(flag);
  }

  void transitToMenu(MenuFragmentBase next) {
    InputMethodManager imm = (InputMethodManager) getSystemService(
        Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in, R.anim.config_out2);
    hideVisibleMenuFragments(transaction);
    transaction.show(next);
    transaction.addToBackStack(null);
    transaction.commit();

    setActionBarTitle(R.string.actionbar_title);
    setActionBarBack(false);
    invalidateOptionsMenu();
  }
  boolean transitToRootMenu() {
    FragmentManager manager = getSupportFragmentManager();
    if(manager.getBackStackEntryCount() > 0) {
      InputMethodManager imm = (InputMethodManager) getSystemService(
          Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(mainLayout.getWindowToken(),
          InputMethodManager.HIDE_NOT_ALWAYS);

      BackStackEntry entry = manager.getBackStackEntryAt(0);
      manager.popBackStack(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
      manager.executePendingTransactions();

      FragmentTransaction transaction = manager.beginTransaction();
      transaction
          .setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in,
              R.anim.config_out2);
      hideVisibleMenuFragments(transaction);
      transaction.show(rootMenu);
      //    transaction.addToBackStack(null);
      transaction.commit();

      setActionBarTitle(R.string.actionbar_title);
      setActionBarBack(false);
      invalidateOptionsMenu();
      return true;
    }
    return false;
  }
  void transitToFragment(Fragment next) {
    InputMethodManager imm = (InputMethodManager) getSystemService(
        Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in, R.anim.config_out2);
    hideVisibleMenuFragments(transaction);
    transaction.add(R.id.container, next);
    transaction.addToBackStack(null);
    transaction.commit();

    setActionBarTitle(R.string.actionbar_title);
    setActionBarBack(false);
    invalidateOptionsMenu();
  }

  private MenuFragmentBase getVisibleMenuFragment() {
    for(MenuFragmentBase m : menus) {
      if(m.isVisible()) return m;
    }
    return null;
  }
  private void hideVisibleMenuFragments(FragmentTransaction transaction) {
    for(Fragment m : menus) {
      if(m.isVisible()) transaction.hide(m);
    }
  }
  String getSavedValue(String key) {
    return preferences.getString(key, null);
  }

  String getSavedValue(String key, String initValue) {
    return preferences.getString(key, initValue);
  }

  int getSavedValue(String key, int initValue) {
    return preferences.getInt(key, initValue);
  }

  void autoSaveValue(String key, String text) {
    editor.putString(key, text);
    editor.apply();
  }

  void autoSaveValue(String key, int value) {
    switch (key) {
      case "BLINK_TH":
        blinkThreshold = value;
        break;
      case "UD_TH":
        upDownThreshold = value;
        break;
      case "LR_TH":
        leftRightThreshold = value;
        break;
      case "ROLL_TH":
        rollThreshold = value;
    }

    editor.putInt(key, value);
    editor.apply();
  }

  void showAuthScreen() {
    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(
        Context.KEYGUARD_SERVICE);

    if (!keyguardManager.isKeyguardSecure()) {
      basicConfigFragment.unlockAppIDandSecret();
      return;
    }

    Intent intent = keyguardManager
        .createConfirmDeviceCredentialIntent(getString(R.string.unlock_auth_title),
            getString(R.string.unlock_auth_explain));

    if (intent != null) {
      startActivityForResult(intent, 1);
    }
  }

  void showAppIDandSecretWarning() {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle(getString(R.string.incorrect_app_id_secret_title));
    alert.setMessage(getString(R.string.incorrect_app_id_secret_explain));
    alert.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        Log.d("DEBUG", "Quit App...");

        finishAndRemoveTask();
      }
    });
    alert.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        Log.d("DEBUG", "Close Alert Dialog...");

        transitToFragment(basicConfigFragment);
      }
    });
    alert.setCancelable(false);
    alert.create().show();
  }

  boolean checkAppIDandSecret() {
    return (appID != null && appID.length() > 0 && appSecret != null
        && appSecret.length() > 0);
  }

  void restart() {
    PendingIntent pendingIntent = PendingIntent
        .getActivity(this, 0, getIntent(), PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);

    finishAndRemoveTask();
  }

  void renewBatteryState(int status) {
    batteryStatus = status;
    invalidateOptionsMenu();
  }

}