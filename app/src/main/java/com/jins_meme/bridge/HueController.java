package com.jins_meme.bridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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

/**
 *
 * HueController.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class HueController {
  private static final String DEBUG = "TEST";
  private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
  private static final String LAST_CONNECTED_USERNAME      = "LastConnectedUsername";
  private static final String LAST_CONNECTED_IP            = "LastConnectedIP";

  private Context context;
  private PHHueSDK hueSDK;
  private List<PHLight> allLights;// = phBridge.getResourceCache().getAllLights();
  private PHLight currentLight;// = allLights.get(0);
  private PHLightState currentLightState;// = light.getLastKnownLightState();
  //private HueSharedPreferences huePrefs;
  //private AccesPointListAdapter adapter;
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPreferencesEditor;

  private PHSDKListener phListener = new PHSDKListener() {
    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
      Log.d(DEBUG, "Cache Updated...");
    }

    @Override
    public void onBridgeConnected(PHBridge phBridge, String s) {
      Log.d(DEBUG, "Bridge Connected...");

      hueSDK.setSelectedBridge(phBridge);
      hueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
      hueSDK.getLastHeartbeat().put(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress(), System.currentTimeMillis());

      setLastConnectIp(phBridge.getResourceCache().getBridgeConfiguration().getIpAddress());
      setUsername(s);

      /*
      List<PHLight> allLights = phBridge.getResourceCache().getAllLights();
      PHLight light = allLights.get(0);
      PHLightState lightState = light.getLastKnownLightState();
      */
      allLights = phBridge.getResourceCache().getAllLights();
      currentLight = allLights.get(0);
      currentLightState = currentLight.getLastKnownLightState();

      float[] xy = {currentLightState.getX(), currentLightState.getY()};
      int color = PHUtilities.colorFromXY(xy, currentLight.getModelNumber());
      Log.d(DEBUG, currentLightState.isOn() + " (r,g,b) = (" + Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color) + ") ");

      turnOn();
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
      Log.d(DEBUG, "Authentication Required...");

      hueSDK.startPushlinkAuthentication(phAccessPoint);
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {
      Log.d(DEBUG, "Access Point Found... " + list.size());

      if(list.size() > 0) {
        hueSDK.getAccessPointsFound().clear();
        hueSDK.getAccessPointsFound().addAll(list);

        /*
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            adapter.updateData(hueSDK.getAccessPointsFound());
          }
        });
        */

        hueSDK.connect(list.get(0));
      }
    }

    @Override
    public void onError(int i, String s) {
      Log.d(DEBUG, "Error Called : " + i + ":" + s);

      if(i == PHHueError.NO_CONNECTION) {
        Log.d(DEBUG, "No Connection...");
      }
      else if(i == PHHueError.AUTHENTICATION_FAILED || i == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
        Log.d(DEBUG, "Authentication failed... / Pushlink Authentication failed...");
      }
      else if(i == PHHueError.BRIDGE_NOT_RESPONDING) {
        Log.d(DEBUG, "Bridge Not Responding..");
      }
      else if(i == PHMessageType.BRIDGE_NOT_FOUND) {
        Log.d(DEBUG, "Bridge Not Found...");

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
      Log.d(DEBUG, "onConnectionLost : " + phAccessPoint.getIpAddress());

      if(!hueSDK.getDisconnectedAccessPoint().contains(phAccessPoint)) {
        hueSDK.getDisconnectedAccessPoint().add(phAccessPoint);
      }
    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {
      for(PHHueParsingError parsingError: list) {
        Log.d(DEBUG, "ParsingError : " + parsingError.getMessage());
      }
    }
  };

  private PHLightListener lightListener = new PHLightListener() {
    @Override
    public void onReceivingLightDetails(PHLight phLight) {
      Log.d(DEBUG, "Receiving Light Details...");
    }

    @Override
    public void onReceivingLights(List<PHBridgeResource> list) {
      Log.d(DEBUG, "Receiving Lights...");
    }

    @Override
    public void onSearchComplete() {
      Log.d(DEBUG, "Search Complete...");
    }

    @Override
    public void onSuccess() {
      Log.d(DEBUG, "Success...");
    }

    @Override
    public void onError(int i, String s) {
      Log.d(DEBUG, "Error...");
    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
      Log.d(DEBUG, "State Update...");
    }
  };

  HueController(Context context) {
    this.context = context;

    sharedPreferences = this.context.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0);
    //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    sharedPreferencesEditor = sharedPreferences.edit();

    hueSDK = PHHueSDK.create();
    //hueSDK = PHHueSDK.getInstance();

    hueSDK.setAppName("MemeBRIDGE");
    hueSDK.setDeviceName(Build.MODEL);

    hueSDK.getNotificationManager().registerSDKListener(phListener);

    if(getLastConnectedIp() != null && !getLastConnectedIp().equals("")) {
      Log.d("TEST", "connect... " + getLastConnectedIp() + " / " + getUsername());

      PHAccessPoint accessPoint = new PHAccessPoint();
      accessPoint.setIpAddress(getLastConnectedIp());
      accessPoint.setUsername(getUsername());

      if(!hueSDK.isAccessPointConnected(accessPoint)) {
        hueSDK.connect(accessPoint);
      }
    }
    else {
      Log.d("TEST", "search...");

      PHBridgeSearchManager searchManager = (PHBridgeSearchManager) hueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
      searchManager.search(true, true);
    }
  }

  private String getUsername() {
    String username = sharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
    return username;
  }

  private boolean setUsername(String username) {
    sharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
    return (sharedPreferencesEditor.commit());
  }

  private String getLastConnectedIp() {
    String ipAddress = sharedPreferences.getString(LAST_CONNECTED_IP, "");
    return ipAddress;
  }

  private boolean setLastConnectIp(String ipAddress) {
    sharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);
    return (sharedPreferencesEditor.commit());
  }

  void turnOn() {
    Log.d(DEBUG, "turn on...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(true);

    float[] xy = PHUtilities.calculateXYFromRGB(255, 255, 255, currentLight.getModelNumber());

    lightState.setX(xy[0]);
    lightState.setY(xy[1]);

    PHBridge bridge = hueSDK.getSelectedBridge();
    bridge.updateLightState(currentLight, lightState, lightListener);
  }

  void turnOff() {
    Log.d(DEBUG, "turn off...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(false);

    PHBridge bridge = hueSDK.getSelectedBridge();
    bridge.updateLightState(currentLight, lightState, lightListener);
  }

  void changeColor(int r, int g, int b) {
    Log.d(DEBUG, "change light color...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(true);

    float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, currentLight.getModelNumber());

    lightState.setX(xy[0]);
    lightState.setY(xy[1]);

    PHBridge bridge = hueSDK.getSelectedBridge();
    bridge.updateLightState(currentLight, lightState, lightListener);
  }
}
