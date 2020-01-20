/**
 * MainActivity.java
 *
 * Copyright (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.jins_jp.meme.MemeConnectListener;
import com.jins_jp.meme.MemeFitStatus;
import com.jins_jp.meme.MemeLib;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import com.jins_jp.meme.MemeScanListener;
import com.jins_jp.meme.MemeStatus;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationRequest.Builder;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.AuthenticationResponse.Type;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Player.OperationCallback;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jins_meme.bridge.BridgeUIView.Adapter.NO_ID;

public class MainActivity extends AppCompatActivity implements MemeConnectListener,
    MemeRealtimeListener, RootMenuFragment.OnFragmentInteractionListener,
    CameraMenuFragment.OnFragmentInteractionListener,
    SpotifyMenuFragment.OnFragmentInteractionListener,
    HueMenuFragment.OnFragmentInteractionListener, VDJMenuFragment.OnFragmentInteractionListener,
    RemoMenuFragment.OnFragmentInteractionListener, DialogListener,
    SpotifyPlayer.NotificationCallback,
    ConnectionStateCallback, SimpleTimer.OnResultListener,
    PauseActionDetector.OnPauseActionListener, MidiReceiveListener {

  private static final int REQUEST_CODE = 1337;

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

  private ProgressDialogFragment memeConnectProgressDialog;
  private Handler handler;
  private FrameLayout mainLayout;

  private MemeMIDI memeMIDI;
  private MemeOSC memeOSC;

  private MemeLib memeLib = null;
  private List<String> scannedMemeList = new ArrayList<>();
  private MemeRealtimeDataFilter mMemeDataFilter = new MemeRealtimeDataFilter();

  private static final int TIMER_ID_UI_DISABLE = 0;
  private static final int TIMER_ID_UI_CANCEL = 1;
  private static final int TIMER_ID_UI_PAUSE = 2;
  private SimpleTimer interactionDisableTimer = new SimpleTimer(TIMER_ID_UI_DISABLE);
  private SimpleTimer cancelTimer = new SimpleTimer(TIMER_ID_UI_CANCEL);
  private SimpleTimer pauseTimer = new SimpleTimer(TIMER_ID_UI_PAUSE);
  private boolean memeInteractionFlagPrepareCancel = false;
  private float pauseWaitTime = 2.5f;
  private float cancelWaitTime = 2.5f;
  private float CANCEL_REFRACTORY_TIME = 1.f;

  private static final int BATTERY_CHECK_INTERVAL = 2000;
  private int batteryCheckCount = BATTERY_CHECK_INTERVAL;

  private static final Map<String, Integer> ROOT_CARD_IDS;
  static {
    Map<String, Integer> map = new LinkedHashMap<>();
    map.put("ENABLE_CAMERA", R.string.camera);
    map.put("ENABLE_SPOTIFY", R.string.spotify);
    map.put("ENABLE_REMO", R.string.remo);
    map.put("ENABLE_HUE", R.string.hue);
    map.put("ENABLE_EYEVDJ", R.string.vdj);
    ROOT_CARD_IDS = Collections.unmodifiableMap(map);
  }
  private boolean isNetworkEnable = false;

  private Player mPlayer;
  private static boolean isAuthenticated = false;
  private BroadcastReceiver mNetworkStateReceiver;

  private boolean isCameraMenuFragment = false;

  void setIsCameraMenuFragment(boolean flag) {
    isCameraMenuFragment = flag;
  }

  boolean isCameraMenuFragment() {
    return isCameraMenuFragment;
  }

  private final Player.OperationCallback mOperationCallback = new OperationCallback() {
    @Override
    public void onSuccess() {
      Log.d("DEBUG", "SPOTIFY:: OperationCallback -> onSuccess");
    }

    @Override
    public void onError(Error error) {
      Log.d("DEBUG", "SPOTIFY:: OperationCallback -> onError:" + error);
    }
  };

  private boolean isForeground = true;

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


  public InputStream openLocalorAssets(String fileName) {
    InputStream is = null;

    Log.d("DEBUG", "external dir. = " + getExternalFilesDir(null).toString());

    File file = new File(getExternalFilesDir(null), fileName);
    if (file.exists()) {
      try {
        is = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      try {
        is = getResources().getAssets().open(fileName);
        FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir(null), fileName));
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();

        is = getResources().getAssets().open(fileName);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return is;
  }

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

  public void setAuthenticated(boolean authenticated) {
    isAuthenticated = authenticated;
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public boolean isNetworkEnabled() {
    return isNetworkEnable;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());

    setContentView(R.layout.activity_bridge_menu);

    handler = new Handler();
    mainLayout = (FrameLayout) findViewById(R.id.container);

    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    editor = preferences.edit();

    if (getSupportActionBar() != null) {
      //updateActionBar(getResources().getString(R.string.actionbar_title));
      updateActionBarLogo(isCameraMenuFragment);
    }

    // Only use for Eye VDJ
    /*
    View decor = this.getWindow().getDecorView();
    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE);
     */

    rootMenu = new RootMenuFragment();
    spotifyMenu = new SpotifyMenuFragment();
    hueMenu = new HueMenuFragment();
    vdjMenu = new VDJMenuFragment();
    remoMenu = new RemoMenuFragment();
    cameraMenu = new CameraMenuFragment();

    interactionDisableTimer.setListener(this);
    cancelTimer.setListener(this);
    pauseTimer.setListener(this);
    ((PauseActionDetector) findViewById(R.id.interceptor)).setListaner(this);

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
    transaction.add(R.id.container, rootMenu);
    transaction.commit();

    if (preferences.getBoolean("SHOW_WELCOME", true)) {
      showWelcom();
    } else {
      if (Build.VERSION.SDK_INT >= 23) {
        requestGPSPermission();
      }
    }

    mNetworkStateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (mPlayer != null) {
          Connectivity connectivity = getNetworkConnectivity(context);
          mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
        }
      }
    };

    // Initialize MIDI
    memeMIDI = new MemeMIDI(this);
    memeMIDI.initPort();
    memeMIDI.setListener(this);

    // Initialize OSC
    memeOSC = new MemeOSC();
    //memeOSC.setRemoteIP(((MainActivity) getActivity()).getSavedValue("REMOTE_IP", MemeOSC.getRemoteIPv4Address()));
    memeOSC.setRemoteIP(getSavedValue("REMOTE_IP", MemeOSC.getRemoteIPv4Address()));
    memeOSC.setRemotePort(getSavedValue("REMOTE_PORT_2", 20316));
    memeOSC.initSocket();
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

    //debug Log.d("DEBUG", "onPrepareOptionsMenu");

    String barTitle;
    if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
      barTitle = getSupportActionBar().getTitle().toString();

      Log.d("DEBUG", "Title = " + barTitle);

      if (barTitle.length() > 1) {
        for (int i = 0; i < menu.size(); i++) {
          MenuItem item = menu.getItem(i);
          String title = item.getTitle().toString();

          if (title.contains(" (for Eye VDJ)")) {
            title = title.substring(0, title.indexOf(" (for Eye VDJ)"));
          }

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

    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(mNetworkStateReceiver, filter);

    isForeground = true;

    //if (Build.VERSION.SDK_INT >= 23) {
    //  requestGPSPermission();
    //}

    Log.d("DEBUG", "onResume..." + scannedMemeList.size());
  }

  @Override
  protected void onPause() {
    super.onPause();

    unregisterReceiver(mNetworkStateReceiver);

    isForeground = false;

    Log.d("DEBUG", "MAIN:: onPause..." + scannedMemeList.size());
  }

  @Override
  protected void onStop() {
    super.onStop();

    Log.d("DEBUG", "MAIN:: onStop... " + isCameraMenuFragment);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (memeMIDI != null) {
      memeMIDI.removeListener();
      memeMIDI.closePort();
      memeMIDI = null;
    }

    if ((memeOSC != null)) {
      memeOSC.closeSocket();
      memeOSC = null;
    }

    if (memeLib != null && memeLib.isConnected()) {
      memeLib.disconnect();
      memeLib = null;
    }

    if (mPlayer != null) {
      mPlayer.logout();
      mPlayer.destroy();
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

    Log.d("DEBUG", "MAIN:: onDestroy...");
  }

  private void showWelcom() {
    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("welcome");
    alertDialogFragment.setCancelable(false);
    alertDialogFragment.setDialogListener(this);
    alertDialogFragment.show(getSupportFragmentManager(), "dialog");
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
      isNetworkEnable = false;
      AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("network");
      alertDialogFragment.setCancelable(false);
      alertDialogFragment.setDialogListener(this);
      alertDialogFragment.show(getSupportFragmentManager(), "dialog");
    } else {
      isNetworkEnable = true;
      checkBluetoothEnable();
    }
  }

  private void checkBluetoothEnable() {
    if (!isMemeConnected()) {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 0);
      } else if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
        Log.d("DEBUG", "MAIN:: Initialize MEME LIB");
        initMemeLib();

        scanAndConnectToLastConnectedMeme();
      }
    }
  }

  private void scanAndConnectToLastConnectedMeme() {
    lastConnectedMemeID = preferences.getString("LAST_CONNECTED_MEME_ID", null);

    Log.d("DEBUG", "MAIN:: last connected meme ID = " + lastConnectedMemeID);

    if (lastConnectedMemeID != null) {
      Log.d("DEBUG", "MAIN:: SCAN Start");

      memeConnectProgressDialog = ProgressDialogFragment.newInstance("meme_connect");
      memeConnectProgressDialog.setDialogListener(this);
      memeConnectProgressDialog.setCancelable(false);
      memeConnectProgressDialog.show(getSupportFragmentManager(), "dialog");

      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          startScan(true);

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
        transitToFragment(vdjMenu);
        break;
      case R.string.spotify:
        transitToFragment(spotifyMenu);
        break;
      case R.string.hue:
        transitToFragment(hueMenu);
        break;
      case R.string.camera:
        transitToFragment(cameraMenu);
        break;
      case R.string.remo:
        transitToFragment(remoMenu);
        break;
    }
  }

  @Override
  public void backToPreviousMenu() {
    if (hasBackStackEntryCount()) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("DEBUG", "MAIN:: onActivityResult -> " + requestCode);

    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        Log.d("DEBUG", "MAIN:: Bluetooth ON");
      } else {
        finishAndRemoveTask();
      }
    } else if (requestCode == 1337) {
      processRequestToken(requestCode, resultCode, data);
    } else {
      if (resultCode == RESULT_OK) {
        Log.d("DEBUG", "MAIN:: Auth OK");

        basicConfigFragment.unlockAppIDandSecret();
      } else {
        Log.d("DEBUG", "MAIN:: Auth NG");

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
    Log.d("DEBUG", "MAIN:: meme connected. " + b + " " + lastConnectedMemeID);

    if (memeConnectProgressDialog != null) {
      memeConnectProgressDialog.dismiss();
    }

    if (b) {
      autoSaveValue("LAST_CONNECTED_MEME_ID", lastConnectedMemeID);
    }

    if (handler != null) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(MainActivity.this, getString(R.string.meme_connected),
              Toast.LENGTH_SHORT).show();
        }
      });
    }
    batteryStatus = 5;

    Log.d("DEBUG", "MAIN:: memeConnectCallback:: " + getSupportActionBar().getTitle().length());
    if (getSupportActionBar().getTitle().length() <= 2) {
      updateActionBarLogo(isCameraMenuFragment);
    }
    //invalidateOptionsMenu();

    memeLib.setAutoConnect(true);
    memeLib.startDataReport(this);
  }

  @Override
  public void memeDisconnectCallback() {
    Log.d("DEBUG", "MAIN:: meme disconnected.");

    handler.post(new Runnable() {
      @Override
      public void run() {
        //invalidateOptionsMenu();
        batteryStatus = 0;
        updateActionBarLogo(isCameraMenuFragment);
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

    Log.d("DEBUG", "MAIN:: APP_ID: " + appID + " APP_SECRET: " + appSecret);

    if (appID != null && appID.length() > 0 && appSecret != null && appSecret.length() > 0) {
      Log.d("DEBUG", "MAIN:: Initialized MemeLib with " + appID + " / and " + appSecret);

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

  public void startScan(final boolean isConnect) {
    Log.d("DEBUG", "MAIN:: start scannig... " + lastConnectedMemeID);

    if (scannedMemeList != null) {
      scannedMemeList.clear();
    }

    if (memeLib == null) {
      Log.d("DEBUG", "MAIN:: memeLib is null!");
    }

    if (memeLib != null) {
      memeLib.setMemeConnectListener(this);

      MemeStatus status = memeLib.startScan(new MemeScanListener() {
        @Override
        public void memeFoundCallback(String s) {
          Log.d("DEBUG", "MAIN:: " + getString(R.string.meme_found, s));

          if (memeConnectProgressDialog != null) {
            memeConnectProgressDialog.setMessage("Found: " + s);
          }

          scannedMemeList.add(s);

          if (getScannedMemeSize() > 0 && scannedMemeList.contains(lastConnectedMemeID)) {
            stopScan();

            if (handler != null) {
              handler.removeCallbacksAndMessages(null);
            }

            if (isConnect) {
              Log.d("DEBUG", "MAIN:: connect and stop callbacks");
              if (memeConnectProgressDialog != null) {
                memeConnectProgressDialog.setMessage("Connect to: " + s);
              }

              connectToMeme(lastConnectedMemeID);
            }
          }
        }
      });

      Log.d("DEBUG", "MAIN:: MemeStatus = " + status);

      switch (status) {
        case MEME_ERROR_SDK_AUTH:
        case MEME_ERROR_APP_AUTH:
          showAppIDandSecretWarning();
          break;
      }
    }
  }

  public void stopScan() {
    Log.d("DEBUG", "MAIN:: stop scannig...");

    if (memeLib != null && memeLib.isScanning()) {
      memeLib.stopScan();

      Log.d("DEBUG", "MAIN:: scan stopped.");
    }
  }

  @Override
  public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
    if (++batteryCheckCount > BATTERY_CHECK_INTERVAL) {
      Log.d("DEBUG", "MAIN:: battery status = " + memeRealtimeData.getPowerLeft());
      renewBatteryState(memeRealtimeData.getPowerLeft());

      batteryCheckCount = 0;
    }

    if (isUIDisabled) {
      return;
    }

    int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
    int eyeBlinkSpeed = memeRealtimeData.getBlinkSpeed();

    int eyeUp = memeRealtimeData.getEyeMoveUp();
    int eyeDown = memeRealtimeData.getEyeMoveDown();
    int eyeLeft = memeRealtimeData.getEyeMoveLeft();
    int eyeRight = memeRealtimeData.getEyeMoveRight();

    //float accelX = memeRealtimeData.getAccX();
    //float accelY = memeRealtimeData.getAccY();
    //float accelZ = memeRealtimeData.getAccZ();

    //float yaw = memeRealtimeData.getYaw();
    //float pitch = memeRealtimeData.getPitch();
    float roll = memeRealtimeData.getRoll();

    if (isForeground && memeRealtimeData.getFitError() == MemeFitStatus.MEME_FIT_OK) {
      if (Math.abs(roll) > getRollThreshold()) {
        if (!memeInteractionFlagPrepareCancel) {
          if (!isUIPaused) {
            cancelTimer.startTimer(cancelWaitTime, true);
          }
          pauseTimer.startTimer(pauseWaitTime, true);
        }
        memeInteractionFlagPrepareCancel = true;
      } else if (Math.abs(roll) <= getRollThreshold()) {
        final Fragment active = getSupportFragmentManager().findFragmentById(R.id.container);
        if (!isUIPaused) {
          pauseTimer.abortTimer();
          cancelTimer.abortTimer();
          if (!isUIDisabled) {
            mMemeDataFilter.update(memeRealtimeData, getBlinkThreshold(), getUpDownThreshold(),
                getLeftRightThreshold());
            if (active instanceof MemeRealtimeDataFilter.MemeFilteredDataCallback) {
              final MemeRealtimeDataFilter.MemeFilteredDataCallback accepter = (MemeRealtimeDataFilter.MemeFilteredDataCallback) active;
              if (mMemeDataFilter.isBlink()) {
                Log.d("EYE", "blink = " + eyeBlinkStrength + " " + eyeBlinkSpeed);
                memeMIDI.sendControlChange(1, 1, 127);

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    accepter.onMemeBlinked();
                  }
                });
              } else if (mMemeDataFilter.isUp()) {
                Log.d("EYE", "up = " + eyeUp);
                memeMIDI.sendControlChange(1, 2, 127);
              } else if (mMemeDataFilter.isDown()) {
                Log.d("EYE", "down = " + eyeDown);
                memeMIDI.sendControlChange(1, 3, 127);
              } else if (mMemeDataFilter.isLeft()) {
                Log.d("EYE", "left = " + eyeLeft);
                memeMIDI.sendControlChange(1, 4, 127);

                if (getSavedValue("MENU_SLIDE_DIRECTION", false)) {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      accepter.onMemeMoveLeft();
                    }
                  });
                } else {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      accepter.onMemeMoveRight();
                    }
                  });
                }
              } else if (mMemeDataFilter.isRight()) {
                Log.d("EYE", "right = " + eyeRight);
                memeMIDI.sendControlChange(1, 5, 127);

                if (getSavedValue("MENU_SLIDE_DIRECTION", false)) {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      accepter.onMemeMoveRight();
                    }
                  });
                } else {
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      accepter.onMemeMoveLeft();
                    }
                  });
                }
              }
            }
          }
        }
        if (!mMemeDataFilter.isDown()) {
          memeInteractionFlagPrepareCancel = false;
        }
      }
    }
  }

  @Override
  public void onTimerStarted(int id) {
    switch (id) {
      case TIMER_ID_UI_DISABLE:
        isUIDisabled = true;
        break;
      case TIMER_ID_UI_CANCEL:
        break;
      case TIMER_ID_UI_PAUSE:
        break;
    }
  }

  @Override
  public void onTimerFinished(int id, boolean completed) {
    switch (id) {
      case TIMER_ID_UI_DISABLE:
        if (completed) {
          isUIDisabled = false;
        }
        break;
      case TIMER_ID_UI_CANCEL:
        if (!completed) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              if (cancel(false)) {
                interactionDisableTimer.startTimer(CANCEL_REFRACTORY_TIME, true);
              }
            }
          });
        }
        break;
      case TIMER_ID_UI_PAUSE:
        if (completed) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              setUIPaused(!isUIPaused);
            }
          });
        }
        break;
    }
  }

  private boolean isUIPaused = false;
  private boolean isUIDisabled = false;

  private void setUIPaused(final boolean pause) {
    Fragment active = getSupportFragmentManager().findFragmentById(R.id.container);
    if (active instanceof MenuFragmentBase) {
      isUIPaused = pause;
      ((MenuFragmentBase) active).setTouchEnabled(!isUIPaused);
      handler.post(new Runnable() {
        @Override
        public void run() {
          findViewById(R.id.pauseView).setVisibility(pause ? View.VISIBLE : View.GONE);
        }
      });
    }
  }

  public void pause() {
    setUIPaused(!isUIPaused);
  }

  @Override
  public void onPauseAction() {
    setUIPaused(!isUIPaused);
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
    Log.d("DEBUG", "MAIN:: connectToMeme");

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
    Log.d("DEBUG", "MAIN:: press back!");
    setUIPaused(false);
    cancel(false);
  }

  public boolean cancel(boolean allow_finish) {
    Fragment active = getSupportFragmentManager().findFragmentById(R.id.container);

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
    return processed;
  }

  void changeMainBackgroud(final int resId) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        mainLayout.setBackgroundResource(resId);
      }
    });
  }

  void changeSettingButton(final boolean isRev) {
    final String overflowDesc = getString(R.string.accessibility_overflow);

    final ViewGroup decor = (ViewGroup) getWindow().getDecorView();

    decor.post(new Runnable() {
      @Override
      public void run() {
        final ArrayList<View> outViews = new ArrayList<>();

        decor.findViewsWithText(outViews, overflowDesc, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

        if (outViews.isEmpty()) {
          return;
        }

        ImageView overflow = (ImageView) outViews.get(0);
        if (isRev) {
          overflow.setImageResource(R.mipmap.ic_setting_rev);
        } else {
          overflow.setImageResource(R.mipmap.ic_setting);
        }
      }
    });
  }

  void updateActionBar(String title, boolean isRev) {
    Log.d("DEBUG", "updateActionBar 0");

    ActionBar target = getSupportActionBar();
    if (target != null) {
      Log.d("DEBUG", "updateActionBar 1");

      target.setTitle(String.format("  %s", title));
      target.setDisplayHomeAsUpEnabled(false);

      target.setDisplayShowHomeEnabled(true);
      target.setDisplayUseLogoEnabled(false);

      if (isRev) {
        target.setBackgroundDrawable(getDrawable(R.color.no0));
      } else {
        target.setBackgroundDrawable(getDrawable(R.color.no4));
      }

      /*
      switch (batteryStatus) {
        case 1:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.connected_caution);
          }
          break;
        case 2:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.connected_30);
          }
          break;
        case 3:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.connected_50);
          }
          break;
        case 4:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.connected_80);
          }
          break;
        case 5:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.connected_full);
          }
          break;
        default:
          if (isRev) {

          } else {
            target.setLogo(R.drawable.not_connected);
          }
          break;
      }
      */
    }
  }

  void updateActionBarLogo(final boolean isRev) {
    final ActionBar target = getSupportActionBar();
    if (target != null) {
      Log.d("DEBUG", "MAIN:: updateActionBarLogo " + isRev);

      handler.post(new Runnable() {
        @Override
        public void run() {
          target.setDisplayHomeAsUpEnabled(false);
          //invalidateOptionsMenu();

          target.setDisplayShowHomeEnabled(true);
          target.setDisplayUseLogoEnabled(true);

          if (isRev) {
            target.setBackgroundDrawable(getDrawable(R.color.no0));
          } else {
            target.setBackgroundDrawable(getDrawable(R.color.no4));
          }

          switch (batteryStatus) {
            case 1:
              if (isRev) {
                target.setLogo(R.mipmap.connected_caution_rev);
              } else {
                target.setLogo(R.mipmap.connected_caution);
              }
              break;
            case 2:
              if (isRev) {
                target.setLogo(R.mipmap.connected_30_rev);
              } else {
                target.setLogo(R.mipmap.connected_30);
              }
              break;
            case 3:
              if (isRev) {
                target.setLogo(R.mipmap.connected_50_rev);
              } else {
                target.setLogo(R.mipmap.connected_50);
              }
              break;
            case 4:
              if (isRev) {
                target.setLogo(R.mipmap.connected_80_rev);
              } else {
                target.setLogo(R.mipmap.connected_80);
              }
              break;
            case 5:
              if (isRev) {
                target.setLogo(R.mipmap.connected_full_rev);
              } else {
                target.setLogo(R.mipmap.connected_full);
              }
              break;
            default:
              if (isRev) {
                target.setLogo(R.mipmap.not_connected_rev);
              } else {
                target.setLogo(R.mipmap.not_connected);
              }
              break;
          }
        }
      });
    }
  }

  void transitToFragment(Fragment next) {
    setUIPaused(false);
    InputMethodManager imm = (InputMethodManager) getSystemService(
        Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mainLayout.getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction
        .setCustomAnimations(R.anim.config_in, android.R.anim.fade_out, android.R.anim.fade_in,
            R.anim.config_out2);
    transaction.replace(R.id.container, next);
    transaction.addToBackStack(null);
    transaction.commit();
  }

  private boolean hasBackStackEntryCount() {
    return getSupportFragmentManager().getBackStackEntryCount() > 0;
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

  float getSavedValue(String key, float initValue) {
    return preferences.getFloat(key, initValue);
  }

  void autoSaveValue(String key, String text) {
    Log.d("DEBUG", "SAVE PARAM:: " + key + " -> " + text);

    editor.putString(key, text);
    editor.apply();
  }

  void autoSaveValue(String key, boolean flag) {
    Log.d("DEBUG", "SAVE PARAM:: " + key + " -> " + flag);

    editor.putBoolean(key, flag);
    editor.apply();
  }

  void autoSaveValue(String key, int value) {
    Log.d("DEBUG", "SAVE PARAM:: " + key + " -> " + value);

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

  void autoSaveValue(String key, float value) {
    Log.d("DEBUG", "SAVE PARAM:: " + key + " -> " + value);

    switch (key) {
      case "PAUSE_TIME":
        pauseWaitTime = value;
        cancelWaitTime = value;
        break;
    }

    editor.putFloat(key, value);
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
    Log.d("DEBUG", "renewBatteryState");

    batteryStatus = status;
    //invalidateOptionsMenu();

    if (getSupportActionBar().getTitle().length() <= 2) {
      updateActionBarLogo(isCameraMenuFragment);
    }
  }

  int getRootCardId(int position) {
    for(String key : ROOT_CARD_IDS.keySet()) {
      if(getSavedValue(key, true)) {
        if(--position<0) {
          return ROOT_CARD_IDS.get(key);
        }
      }
    }
    Log.d("ERROR", "unexpected operation. if new root cards were added, edit ROOT_CARD_IDS as well.");
    return NO_ID;
  }

  int getEnabledCardNum() {
    int num = 0;
    for(String key : ROOT_CARD_IDS.keySet()) {
      if(getSavedValue(key, true)) {
          ++num;
      }
    }
    return num;
  }

  @Override
  public void doPositiveClick(String type) {
    switch (type) {
      case "welcome":
        if (Build.VERSION.SDK_INT >= 23) {
          requestGPSPermission();
        }
        editor.putBoolean("SHOW_WELCOME", false);
        editor.apply();
        break;
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
    switch (type) {
      case "meme_connect":
        Log.d("DEBUG", "meme_connect cancel");
        stopScan();
        handler.removeCallbacksAndMessages(null);
        break;
      default:
        Log.d("DEBUG", "default");
        handler.removeCallbacksAndMessages(null);
        finishAndRemoveTask();
        break;
    }
  }

  IntentFilter getFilter() {
    return (new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  boolean authenticate() {
    //Log.d("DEBUG", "SPOTIFY:: authenticate " + getRedirectUri().toString());

    if (isNetworkEnable && !isAuthenticated && getSavedValue("SPOTIFY_USE", false)) {
      AuthenticationRequest.Builder builder = new Builder(getString(R.string.spotify_client_id),
          Type.TOKEN,
          "jins-meme-bridge-login://callback");
      builder.setShowDialog(false).setScopes(
          new String[]{"user-read-private", "playlist-read", "playlist-read-private",
              "user-follow-read", "user-library-read", "streaming"});
      final AuthenticationRequest request = builder.build();

      AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

      return false;
    } else {
      return true;
    }
  }

  void logout() {
    mPlayer.logout();
  }

  private void onAuthenticationComplete(AuthenticationResponse authResponse, String clientID) {
    Log.d("DEBUG", "Got authentication token");
    if (mPlayer == null) {
      Config playerConfig = new Config(this, authResponse.getAccessToken(), clientID);
      mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
        @Override
        public void onInitialized(SpotifyPlayer player) {
          Log.d("DEBUG", "-- Player initialized --");
          mPlayer
              .setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
          mPlayer.addNotificationCallback(MainActivity.this);
          mPlayer.addConnectionStateCallback(MainActivity.this);
        }

        @Override
        public void onError(Throwable error) {
          Log.d("DEBUG", "Error in initialization: " + error.getMessage());
        }
      });
    } else {
      mPlayer.login(authResponse.getAccessToken());
    }
  }

  private Connectivity getNetworkConnectivity(Context context) {
    ConnectivityManager connectivityManager;
    connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    if (activeNetwork != null && activeNetwork.isConnected()) {
      return Connectivity.fromNetworkType(activeNetwork.getType());
    } else {
      return Connectivity.OFFLINE;
    }
  }

  void setShuffle(boolean flag) {
    mPlayer.setShuffle(mOperationCallback, flag);
  }

  void setPlayUri(String uri) {
    mPlayer.playUri(mOperationCallback, uri, 0, 0);
  }

  void setPause() {
    mPlayer.pause(mOperationCallback);
  }

  boolean isPlaying() {
    return mPlayer.getPlaybackState().isPlaying;
  }

  String processRequestToken(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

      SpotifyConfigFragment.setAccessToken(response.getAccessToken());

      switch (response.getType()) {
        case TOKEN:
          Log.d("DEBUG", "Spotify Token: " + response.getAccessToken());

          onAuthenticationComplete(response, getString(R.string.spotify_client_id));
          SpotifyConfigFragment.setIsLoggedIn(true);
          break;
        case ERROR:
          Log.d("DEBUG", "Spotify Error: " + response.getError());
          break;
        default:
          Log.d("DEBUG", "Spotify Other: " + response.getState());
          break;
      }
    }

    return SpotifyConfigFragment.getAccessToken();
  }

  @Override
  public void onConnectionMessage(String s) {
    Log.d("DEBUG", "SPOTIFY:: Received connection message: " + s);
  }

  @Override
  public void onLoggedIn() {
    Log.d("DEBUG", "SPOTIFY:: User logged in.");

    isAuthenticated = true;
    spotifyConfigFragment.getPlaylist();
  }

  @Override
  public void onLoggedOut() {
    Log.d("DEBUG", "SPOTIFY:: User logged out.");

    isAuthenticated = false;
  }

  @Override
  public void onLoginFailed(Error error) {
    Log.d("DEBUG", "SPOTIFY:: Loggin failed.");
  }

  @Override
  public void onPlaybackEvent(PlayerEvent playerEvent) {
  }

  @Override
  public void onPlaybackError(Error error) {
  }

  @Override
  public void onTemporaryError() {
    Log.d("DEBUG", "SPOTIFY:: Temporary error occurred.");
  }

  @Override
  public void onReceiveMidiMessage() {

  }
}