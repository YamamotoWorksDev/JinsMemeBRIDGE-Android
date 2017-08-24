/**
 * HueController.java
 *
 * Copyright (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;

public class HueController implements PHSDKListener {

  private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
  private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
  private static final String LAST_CONNECTED_IP = "LastConnectedIP";

  private Handler handler;
  private FragmentManager fragmentManager;
  private static PHHueSDK hueSDK;
  private List<PHLight> allLights;
  //private static PHLight currentLight;// = allLights.get(0);
  //private PHLightState currentLightState;// = light.getLastKnownLightState();

  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPreferencesEditor;

  ProgressDialogFragment hueConnectProgressDialog;
  private AlertDialog.Builder alert;

  private boolean isAuthRequired = true;
  static int connectionState = 0;

  static int getConnectionState() {
    return connectionState;
  }

  @Override
  public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
    Log.d("HUE", "Cache Updated...");
  }

  @Override
  public void onBridgeConnected(PHBridge phBridge, String s) {
    Log.d("HUE", "Bridge Connected...");

    hueSDK.setSelectedBridge(phBridge);
    hueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
    hueSDK.getLastHeartbeat()
        .put(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress(),
            System.currentTimeMillis());

    setLastConnectIp(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
    setUsername(s);

    allLights = phBridge.getResourceCache().getAllLights();

    for (PHLight light : allLights) {
      PHLightState lightState = light.getLastKnownLightState();

      Log.d("HUE",
          "id = " + light.getIdentifier() + " " + light.getModelNumber() + " " + light.getUniqueId()
              + " " + lightState.getBrightness() + " " + lightState.getSaturation());
      Log.d("HUE", "type = " + light.getLightType().name() + " " + light.getLightType().ordinal());
    }

    closeProgressDialog();

    turnOn();
  }

  @Override
  public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
    Log.d("HUE", "Authentication Required...");

    hueSDK.startPushlinkAuthentication(phAccessPoint);

    closeProgressDialog();

    if (isAuthRequired) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          hueConnectProgressDialog = ProgressDialogFragment.newInstance("hue_connect");
          //hueConnectProgressDialog.setDialogListener(this);
          hueConnectProgressDialog.setCancelable(false);
          hueConnectProgressDialog.show(fragmentManager, "dialog");
        }
      });

      isAuthRequired = false;
    }
  }

  @Override
  public void onAccessPointsFound(List<PHAccessPoint> list) {
    Log.d("HUE", "Access Point Found... " + list.size());

    isAuthRequired = true;
    connectionState = 2;

    if (list.size() > 0) {
      hueSDK.getAccessPointsFound().clear();
      hueSDK.getAccessPointsFound().addAll(list);
      hueSDK.connect(list.get(0));
    }
  }

  @Override
  public void onError(int i, String s) {
    Log.d("HUE", "Error Called : " + i + ":" + s);

    if (i == PHHueError.NO_CONNECTION) {

      Log.d("HUE", "No Connection...");
    } else if (i == PHHueError.AUTHENTICATION_FAILED
        || i == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
      Log.d("HUE", "Authentication failed... / Pushlink Authentication failed...");

      connectionState = -1;

      hueConnectProgressDialog.dismiss();
    } else if (i == PHHueError.BRIDGE_NOT_RESPONDING) {
      Log.d("HUE", "Bridge Not Responding..");

      connectionState = -2;

      setLastConnectIp("");

    } else if (i == PHMessageType.BRIDGE_NOT_FOUND) {
      Log.d("HUE", "Bridge Not Found...");

      connectionState = -2;

      //if(!lastSearchWasIPScan) {  // Perform an IP Scan (backup mechanism) if UPNP and Portal Search fails.
      //  phHueSDK = PHHueSDK.getInstance();
      //  PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
      //  sm.search(false, false, true);
      //  lastSearchWasIPScan=true;
      //}
    }
  }

  @Override
  public void onConnectionResumed(PHBridge phBridge) {

  }

  @Override
  public void onConnectionLost(PHAccessPoint phAccessPoint) {
    Log.d("HUE", "onConnectionLost : " + phAccessPoint.getIpAddress());

    if (!hueSDK.getDisconnectedAccessPoint().contains(phAccessPoint)) {
      hueSDK.getDisconnectedAccessPoint().add(phAccessPoint);
    }
  }

  @Override
  public void onParsingErrors(List<PHHueParsingError> list) {
    for (PHHueParsingError parsingError : list) {
      Log.d("HUE", "ParsingError : " + parsingError.getMessage());
    }
  }

  private PHLightListener lightListener = new PHLightListener() {
    @Override
    public void onReceivingLightDetails(PHLight phLight) {
      Log.d("HUE", "Receiving Light Details...");
    }

    @Override
    public void onReceivingLights(List<PHBridgeResource> list) {
      Log.d("HUE", "Receiving Lights... " + list.toString());
    }

    @Override
    public void onSearchComplete() {
      Log.d("HUE", "Search Complete...");
    }

    @Override
    public void onSuccess() {
      Log.d("HUE", "Success...");

      connectionState = 4;
    }

    @Override
    public void onError(int i, String s) {
      Log.d("HUE", "Error... " + i + " " + s);
    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
      Log.d("HUE", "State Update...");
    }
  };

  HueController(Context context, FragmentManager fragmentManager) {
    handler = new Handler();

    sharedPreferences = context.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0);
    sharedPreferencesEditor = sharedPreferences.edit();

    this.fragmentManager = fragmentManager;

    alert = new AlertDialog.Builder(context);

    hueSDK = PHHueSDK.create();

    hueSDK.setAppName("JINS MEME BRIDGE");
    hueSDK.setDeviceName(Build.MODEL);

    hueSDK.getNotificationManager().registerSDKListener(this);

    if (getLastConnectedIp() != null && !getLastConnectedIp().equals("")) {
      Log.d("HUE", "connect... " + getLastConnectedIp() + " / " + getUsername());

      PHAccessPoint accessPoint = new PHAccessPoint();
      accessPoint.setIpAddress(getLastConnectedIp());
      accessPoint.setUsername(getUsername());

      if (!hueSDK.isAccessPointConnected(accessPoint)) {
        hueSDK.connect(accessPoint);
      } else {
        PHBridge bridge = hueSDK.getSelectedBridge();
        allLights = bridge.getResourceCache().getAllLights();

        for (PHLight light : allLights) {
          PHLightState lightState = light.getLastKnownLightState();

          Log.d("DEBUG",
              "HUE:: id = " + light.getIdentifier() + " " + light.getModelNumber() + " " + light
                  .getUniqueId() + " " + lightState.getBrightness() + " " + lightState
                  .getSaturation());
          Log.d("DEBUG",
              "HUE:: type = " + light.getLightType().name() + " " + light.getLightType().ordinal());
        }
      }
    }
  }

  void closeProgressDialog() {
    if (hueConnectProgressDialog != null && hueConnectProgressDialog.isShowing()) {
      hueConnectProgressDialog.dismiss();
    }
  }

  void showConnectionAlertDialog(FragmentManager fragmentManager) {
    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("hue");
    alertDialogFragment.setCancelable(false);
    //alertDialogFragment.setDialogListener(this);
    alertDialogFragment.show(fragmentManager, "dialog");
  }

  void connect() {
    if (getLastConnectedIp() != null && !getLastConnectedIp().equals("")) {
      connectionState = 3;

      Log.d("HUE", "connect... " + getLastConnectedIp() + " / " + getUsername());

      PHAccessPoint accessPoint = new PHAccessPoint();
      accessPoint.setIpAddress(getLastConnectedIp());
      accessPoint.setUsername(getUsername());

      if (!hueSDK.isAccessPointConnected(accessPoint)) {
        hueSDK.connect(accessPoint);
      }
    } else {
      Log.d("HUE", "search...");

      connectionState = 1;

      hueConnectProgressDialog = ProgressDialogFragment.newInstance("hue_search");
      //hueConnectProgressDialog.setDialogListener(this);
      hueConnectProgressDialog.setCancelable(false);
      hueConnectProgressDialog.show(fragmentManager, "dialog");

      PHBridgeSearchManager searchManager = (PHBridgeSearchManager) hueSDK
          .getSDKService(PHHueSDK.SEARCH_BRIDGE);
      searchManager.search(true, true);
    }
  }

  void disconnect() {
    turnOff();

    PHBridge bridge = hueSDK.getSelectedBridge();
    if (bridge != null) {
      hueSDK.disableAllHeartbeat();
      hueSDK.disconnect(bridge);
    }
  }

  private String getUsername() {
    return sharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
  }

  private void setUsername(String username) {
    sharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
    sharedPreferencesEditor.apply();
  }

  private String getLastConnectedIp() {
    return sharedPreferences.getString(LAST_CONNECTED_IP, "");
  }

  private void setLastConnectIp(String ipAddress) {
    Log.d("HUE", "setLastConnectIp");

    sharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);
    sharedPreferencesEditor.apply();
  }

  void turnOn() {
    PHBridge bridge = hueSDK.getSelectedBridge();

    for (PHLight light : allLights) {
      PHLightState lightState = new PHLightState();
      lightState.setOn(true);

      if (light.getModelNumber().equals("LWB014")) {
        lightState.setBrightness(50);
      } else {
        float[] xy = PHUtilities.calculateXYFromRGB(255, 255, 255, light.getModelNumber());

        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
        lightState.setBrightness(50);
      }

      bridge.updateLightState(light, lightState, lightListener);
    }
  }

  void turnOff() {
    Log.d("HUE", "turn off...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(false);

    if (allLights != null) {
      for (PHLight light : allLights) {
        PHBridge bridge = hueSDK.getSelectedBridge();

        if (light != null) {
          bridge.updateLightState(light, lightState, lightListener);
        }
      }
    }
  }

  void changeColor(int r, int g, int b) {
    Log.d("HUE", "change light color...");

    PHBridge bridge = hueSDK.getSelectedBridge();

    for (PHLight light : allLights) {
      PHLightState lightState = new PHLightState();
      lightState.setOn(true);

      float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, light.getModelNumber());

      if (!light.getModelNumber().equals("LWB014")) {
        lightState.setX(xy[0]);
        lightState.setY(xy[1]);
      }

      bridge.updateLightState(light, lightState, lightListener);
    }
  }

  void changeColor(int r, int g, int b, int brightness, int time) {
    Log.d("HUE", "change light color...");

    PHBridge bridge = hueSDK.getSelectedBridge();

    if (allLights != null) {
      for (PHLight light : allLights) {
        PHLightState lightState = new PHLightState();
        lightState.setOn(true);

        if (light != null) {
          float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, light.getModelNumber());

          if (!light.getModelNumber().equals("LWB014")) {
            lightState.setX(xy[0]);
            lightState.setY(xy[1]);
          }
          lightState.setBrightness(brightness);
          lightState.setTransitionTime(time);

          bridge.updateLightState(light, lightState, lightListener);
        }
      }
    }
  }
}
