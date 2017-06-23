/**
 * MainActivity.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeFitStatus;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemeConnectListener,
    MemeRealtimeListener, RootMenuFragment.OnFragmentInteractionListener,
    CameraMenuFragment.OnFragmentInteractionListener,
    SpotifyMenuFragment.OnFragmentInteractionListener,
    HueMenuFragment.OnFragmentInteractionListener, VDJMenuFragment.OnFragmentInteractionListener,
    RemoMenuFragment.OnFragmentInteractionListener, DialogListener {

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

  private ProgressDialog memeConnectProgressDialog;
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
  private SpotifyMenuFragment spotifyMenu;
  private HueMenuFragment hueMenu;
  private RemoMenuFragment remoMenu;
  private CameraMenuFragment cameraMenu;

  private VDJMenuFragment vdjMenu;
  /*
   * MODIFY YOURSELF
   * Add your implemented function's configuration
   *
   */
  // private ***Fragment ***Fragment;

  private ArrayList<MenuFragmentBase> menus = new ArrayList<>();

  private BasicConfigFragment basicConfigFragment;
  private AboutFragment aboutFragment;

  private OSCConfigFragment oscConfigFragment;
  private MIDIConfigFragment midiConfigFragment;
  private SpotifyConfigFragment spotifyConfigFragment;
  private HueConfigFragment hueConfigFragment;
  private RemoConfigFragment remoConfigFragment;
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
    Fabric.with(this, new Crashlytics());

    setContentView(R.layout.activity_bridge_menu);

    setActionBarTitle(R.string.actionbar_title);

    handler = new Handler();
    mainLayout = (FrameLayout) findViewById(R.id.container);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = preferences.edit();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));
    }

    rootMenu = new RootMenuFragment();
    spotifyMenu = new SpotifyMenuFragment();
    hueMenu = new HueMenuFragment();
    vdjMenu = new VDJMenuFragment();
    remoMenu = new RemoMenuFragment();
    cameraMenu = new CameraMenuFragment();

    menus.add(rootMenu);
    menus.add(spotifyMenu);
    menus.add(hueMenu);
    menus.add(vdjMenu);
    menus.add(remoMenu);
    menus.add(cameraMenu);

    cancelFlag = false;
    pauseCount = 0;
    pauseFlag = false;
    refractoryPeriod = 0;

    basicConfigFragment = new BasicConfigFragment();
    oscConfigFragment = new OSCConfigFragment();
    midiConfigFragment = new MIDIConfigFragment();
    spotifyConfigFragment = new SpotifyConfigFragment();
    hueConfigFragment = new HueConfigFragment();
    remoConfigFragment = new RemoConfigFragment();
    aboutFragment = new AboutFragment();
    /*
     * MODIFY YOURSELF
     * Add your implemented function's configuration
     *
     */
    // ***ConfigFragment = new ***ConfigFragment

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    for (Fragment m : menus) {
      String fullClassName = m.getClass().getName();
      String className = fullClassName
          .substring(getPackageName().length() + 1, fullClassName.length());
      transaction.add(R.id.container, m, className);
      transaction.hide(m);
    }
    transaction.show(rootMenu);
    transaction.commit();

    if (Build.VERSION.SDK_INT >= 23) {
      requestGPSPermission();
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
    menu.add(0, index++, 0, R.string.spotify_conf);
    menu.add(0, index++, 0, R.string.remo_conf);
    menu.add(0, index++, 0, R.string.hue_conf);
    menu.add(0, index++, 0, getString(R.string.osc_conf) + " (for Eye VDJ)");
    menu.add(0, index++, 0, getString(R.string.midi_conf) + " (for Eye VDJ)");

    menu.add(0, index++, 0, R.string.about);
    menu.add(0, index, 0, R.string.exit_app);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    String barTitle;
    if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
      barTitle = getSupportActionBar().getTitle().toString();

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
    } else if (itemTitle.equals(getString(R.string.spotify_conf))) {
      Log.d("DEBUG", "tap spotify setting");

      transitToFragment(spotifyConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.hue_conf))) {
      Log.d("DEBUG", "tap hue setting");

      transitToFragment(hueConfigFragment);

      return true;
    } else if (itemTitle.equals(getString(R.string.remo_conf))) {
      Log.d("DEBUG", "tap remo setting");

      transitToFragment(remoConfigFragment);

      return true;
    } else if (itemTitle.contains(getString(R.string.osc_conf))) {
      Log.d("DEBUG", "tap osc setting");

      transitToFragment(oscConfigFragment);

      return true;
    } else if (itemTitle.contains(getString(R.string.midi_conf))) {
      Log.d("DEBUG", "tap midi setting");

      transitToFragment(midiConfigFragment);

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
    spotifyMenu = null;
    remoMenu = null;
    hueMenu = null;
    vdjMenu = null;

    basicConfigFragment = null;
    aboutFragment = null;

    spotifyConfigFragment = null;
    remoConfigFragment = null;
    hueConfigFragment = null;
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

  private void checkAirplaneMode() {
    Log.d("DEBUG", "AIRPLANE = " + Settings.System
        .getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0));
    if (Settings.System.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1) {
      Log.d("DEBUG", "show airplane warning.");

      AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("airplane");
      alertDialogFragment.setCancelable(false);
      alertDialogFragment.setDialogListener(this);
      alertDialogFragment.show(getSupportFragmentManager(), "dialog");
    } else {
      checkNetworkEnable();
    }
  }

  private void checkNetworkEnable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
        CONNECTIVITY_SERVICE);

    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if (networkInfo == null || !networkInfo.isConnected()) {
      AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("network");
      alertDialogFragment.setCancelable(false);
      alertDialogFragment.setDialogListener(this);
      alertDialogFragment.show(getSupportFragmentManager(), "dialog");
    } else {
      checkBluetoothEnable();
    }
  }

  private void checkBluetoothEnable() {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, 0);
    } else if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
      Log.d("MAIN", "Initialize MEME LIB");
      initMemeLib();

      scanAndConnectToLastConnectedMeme();
    }
  }

  private void scanAndConnectToLastConnectedMeme() {
    lastConnectedMemeID = preferences.getString("LAST_CONNECTED_MEME_ID", null);
    if (lastConnectedMemeID != null) {
      Log.d("MAIN", "SCAN Start");
      //Toast.makeText(this, getString(R.string.meme_scanning), Toast.LENGTH_SHORT).show();

      memeConnectProgressDialog = new ProgressDialog(MainActivity.this);
      memeConnectProgressDialog.setMax(100);
      memeConnectProgressDialog.setMessage("Scannig...");
      memeConnectProgressDialog.setTitle("SCAN & CONNECT");
      memeConnectProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      memeConnectProgressDialog.setCancelable(false);
      memeConnectProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
          new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              stopScan();

              handler.removeCallbacksAndMessages(null);
            }
          });
      memeConnectProgressDialog.show();

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
              } else {
                memeConnectProgressDialog.dismiss();

                showNotFoundMeme();
              }
            }
          }, 30000);
        }
      }, 1000);
    }
  }

  @Override
  public void openNextMenu(int card_id) {
    switch (card_id) {
      case R.string.vdj:
        transitToMenu(vdjMenu);
        break;
      case R.string.spotify:
        transitToMenu(spotifyMenu);
        break;
      case R.string.hue:
        transitToMenu(hueMenu);
        break;
      case R.string.camera:
        transitToMenu(cameraMenu);
        break;
      case R.string.remo:
        transitToMenu(remoMenu);
        break;
    }
  }

  @Override
  public void backToPreviousMenu() {
    if (hasBackStackEntryCount()) {
      FragmentManager manager = getSupportFragmentManager();
      Fragment active = manager.findFragmentById(R.id.container);
      if (active instanceof MenuFragmentBase) {
        ((MenuFragmentBase) active).menuReset();
      }
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("DEBUG", "MAIN:: onActivityResult -> " + requestCode);

    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        Log.d("MAIN", "Bluetooth ON");
      } else {
        finishAndRemoveTask();
      }
    } else if (requestCode == 1337) {
      spotifyMenu.processRequestToken(requestCode, resultCode, data);

      spotifyMenu.setAuthenticated(true);
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
      requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
          getResources().getInteger(R.integer.PERMISSION_REQUEST_CODE_GPS));
    } else {
      Log.d("DEBUG", "check airplane...");
      checkAirplaneMode();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    if (requestCode == getResources().getInteger(R.integer.PERMISSION_REQUEST_CODE_GPS)) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("PERMISSION", "Succeeded");
        Toast.makeText(MainActivity.this, getString(R.string.succeeded), Toast.LENGTH_SHORT)
            .show();

        checkAirplaneMode();
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

    memeConnectProgressDialog.dismiss();

    if (b) {
      autoSaveValue("LAST_CONNECTED_MEME_ID", lastConnectedMemeID);
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        if (getSupportActionBar() != null) {
          getSupportActionBar()
              .setBackgroundDrawable(new ColorDrawable(Color.rgb(0x3F, 0x51, 0xB5)));
        }

        Toast.makeText(MainActivity.this, getString(R.string.meme_connected),
            Toast.LENGTH_SHORT).show();
      }
    });
    invalidateOptionsMenu();

    memeLib.setAutoConnect(true);
    memeLib.startDataReport(this);
  }

  @Override
  public void memeDisconnectCallback() {
    Log.d("MAIN", "meme disconnected.");

    handler.post(new Runnable() {
      @Override
      public void run() {
        if (getSupportActionBar() != null) {
          getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(127, 127, 127)));
        }

        invalidateOptionsMenu();
        if (basicConfigFragment != null) {
          basicConfigFragment.setSwConnect(false);
        }

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
      Log.d("MAIN", "Initialized MemeLib with " + appID + " / and " + appSecret);

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

          memeConnectProgressDialog.setMessage("Found: " + s);

          /*
          final String s2 = s;
          handler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(MainActivity.this, getString(R.string.meme_found, s2), Toast.LENGTH_SHORT).show();
            }
          });
          */

          scannedMemeList.add(s);

          if (getScannedMemeSize() > 0 && scannedMemeList.contains(lastConnectedMemeID)) {
            stopScan();

            handler.removeCallbacksAndMessages(null);

            Log.d("MAIN", "connect and stop callbacks");
            memeConnectProgressDialog.setMessage("Connect to: " + s);

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
        }
      });

      Log.d("MAIN", "MemeStatus = " + status);

      switch (status) {
        case MEME_ERROR_SDK_AUTH:
        case MEME_ERROR_APP_AUTH:
          showAppIDandSecretWarning();
          break;
      }
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

    int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
    int eyeBlinkSpeed = memeRealtimeData.getBlinkSpeed();

    //int eyeUp = memeRealtimeData.getEyeMoveUp();
    //int eyeDown = memeRealtimeData.getEyeMoveDown();
    int eyeLeft = memeRealtimeData.getEyeMoveLeft();
    int eyeRight = memeRealtimeData.getEyeMoveRight();

    //float accelX = memeRealtimeData.getAccX();
    //float accelY = memeRealtimeData.getAccY();
    //float accelZ = memeRealtimeData.getAccZ();

    //float yaw = memeRealtimeData.getYaw();
    //float pitch = memeRealtimeData.getPitch();
    float roll = memeRealtimeData.getRoll();

    if (memeRealtimeData.getFitError() == MemeFitStatus.MEME_FIT_OK) {
      if (Math.abs(roll) > getRollThreshold()) {
        cancelFlag = true;
        //Log.d("DEBUG", "menu = " + getResources().getString(currentEnteredMenu) + " / item = " + getResources().getString(currentSelectedItem));

        //if (++pauseCount >= PAUSE_MAX) {
        if (pauseCount < PAUSE_MAX) {
          pauseCount++;

          if (pauseCount == PAUSE_MAX) {

            //pauseFlag = true;
            pauseFlag = !pauseFlag;

            if (pauseFlag) {
              handler.post(new Runnable() {
                @Override
                public void run() {
                  findViewById(R.id.pauseView).setVisibility(View.VISIBLE);
                }
              });
              Log.d("=========PAUSE=========", "pause");
            } else {
              handler.post(new Runnable() {
                @Override
                public void run() {
                  findViewById(R.id.pauseView).setVisibility(View.GONE);
                }
              });
              Log.d("=========PAUSE=========", "pause clear");
            }
          }
        }
      } else if (Math.abs(roll) <= getRollThreshold()) {
        final MenuFragmentBase active = getVisibleMenuFragment();
        if (!pauseFlag) {
          if (cancelFlag && pauseCount < PAUSE_MAX) {
            if (cancel(false)) {
              refractoryPeriod = REFRACTORY_PERIOD_MAX;
              Log.d("=========PAUSE=========", "cancel");
            }
          } else {
            if (refractoryPeriod > 0) {
              refractoryPeriod--;
            } else {
              mMemeDataFilter.update(memeRealtimeData, getBlinkThreshold(), getUpDownThreshold(),
                  getLeftRightThreshold());
              if (active instanceof MemeRealtimeDataFilter.MemeFilteredDataCallback) {
                final MemeRealtimeDataFilter.MemeFilteredDataCallback accepter = (MemeRealtimeDataFilter.MemeFilteredDataCallback) active;
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

        cancelFlag = false;
        pauseCount = 0;
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
    cancel(true);
  }

  public boolean cancel(boolean allow_finish) {
    FragmentManager manager = getSupportFragmentManager();
    Fragment active = manager.findFragmentById(R.id.container);

    boolean processed = false;
    if (active instanceof MenuFragmentBase) {
      processed = ((MenuFragmentBase) active).menuBack();
    }
    if (!processed) {
      if (allow_finish || hasBackStackEntryCount()) {
        super.onBackPressed();
        processed = true;
      }
    }
    if (!hasBackStackEntryCount()) {
      setActionBarTitle(R.string.actionbar_title);
      setActionBarBack(false);
      invalidateOptionsMenu();
    }
    return processed;
  }

  void setActionBarTitle(int resId) {
    if (getSupportActionBar() != null) {
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
  }

  void setActionBarBack(boolean flag) {
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(flag);
    }
  }

  void transitToMenu(MenuFragmentBase next) {
    InputMethodManager imm = (InputMethodManager) getSystemService(
        Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction
        .setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in,
            R.anim.config_out2);
    hideVisibleMenuFragments(transaction);
    transaction.show(next);
    transaction.addToBackStack(null);
    transaction.commit();

    setActionBarTitle(R.string.actionbar_title);
    setActionBarBack(false);
    invalidateOptionsMenu();
  }

  boolean transitToRootMenu() {
    if (hasBackStackEntryCount()) {
      FragmentManager manager = getSupportFragmentManager();
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
    transaction
        .setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in,
            R.anim.config_out2);
    hideVisibleMenuFragments(transaction);
    transaction.add(R.id.container, next);
    transaction.addToBackStack(null);
    transaction.commit();

    setActionBarTitle(R.string.actionbar_title);
    setActionBarBack(false);
    invalidateOptionsMenu();
  }

  private boolean hasBackStackEntryCount() {
    return getSupportFragmentManager().getBackStackEntryCount() > 0;
  }

  private MenuFragmentBase getVisibleMenuFragment() {
    for (MenuFragmentBase m : menus) {
      if (m.isVisible()) {
        return m;
      }
    }
    return null;
  }

  private void hideVisibleMenuFragments(FragmentTransaction transaction) {
    for (Fragment m : menus) {
      if (m.isVisible()) {
        transaction.hide(m);
      }
    }
  }

  String getSavedValue(String key) {
    return preferences.getString(key, null);
  }

  String getSavedValue(String key, String initValue) {
    return preferences.getString(key, initValue);
  }

  boolean getSavedValue(String key, boolean flag) {
    return preferences.getBoolean(key, flag);
  }

  int getSavedValue(String key, int initValue) {
    return preferences.getInt(key, initValue);
  }

  void autoSaveValue(String key, String text) {
    editor.putString(key, text);
    editor.apply();
  }

  void autoSaveValue(String key, boolean flag) {
    editor.putBoolean(key, flag);
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

  void showNotFoundMeme() {
    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("meme");
    alertDialogFragment.setCancelable(false);
    alertDialogFragment.setDialogListener(this);
    alertDialogFragment.show(getSupportFragmentManager(), "dialog");
  }

  void showAppIDandSecretWarning() {
    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("app_id_secret");
    alertDialogFragment.setCancelable(false);
    alertDialogFragment.setDialogListener(this);
    alertDialogFragment.show(getSupportFragmentManager(), "dialog");
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

  @Override
  public void doPositiveClick(String type) {
    switch (type) {
      case "network":
        checkBluetoothEnable();
        break;
      case "meme":
        transitToFragment(basicConfigFragment);
        break;
    }
  }

  @Override
  public void doNegativeClick(String type) {
    handler.removeCallbacksAndMessages(null);
    finishAndRemoveTask();
  }
}