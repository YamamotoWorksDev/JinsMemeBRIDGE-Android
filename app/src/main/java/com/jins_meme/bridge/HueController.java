/**
 * HueController.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
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

public class HueController {

  private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
  private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
  private static final String LAST_CONNECTED_IP = "LastConnectedIP";

  private Handler handler;
  private static PHHueSDK hueSDK;
  private List<PHLight> allLights;// = phBridge.getResourceCache().getAllLights();
  private static PHLight currentLight;// = allLights.get(0);
  private PHLightState currentLightState;// = light.getLastKnownLightState();
  //private HueSharedPreferences huePrefs;
  //private AccesPointListAdapter adapter;

  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor sharedPreferencesEditor;

  private ProgressDialog progressDialog;
  private AlertDialog.Builder alert;
  private AlertDialog alertDialog;

  private boolean isAuthRequired = true;
  static int connectionState = 0;

  static int getConnectionState() {
    return connectionState;
  }

  private PHSDKListener phListener = new PHSDKListener() {
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
      Log.d("HUE",
          currentLightState.isOn() + " (r,g,b) = (" + Color.red(color) + ", " + Color.green(color)
              + ", " + Color.blue(color) + ") ");

      connectionState = 4;
      handler.post(new Runnable() {
        @Override
        public void run() {
          progressDialog.dismiss();
        }
      });

      turnOn();
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
      Log.d("HUE", "Authentication Required...");

      hueSDK.startPushlinkAuthentication(phAccessPoint);

      if (isAuthRequired) {
        handler.post(new Runnable() {
          @Override
          public void run() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Found Hue Bridge.\nPlease push the LINK Button...");
            progressDialog.setCancelable(false);
            progressDialog.show();
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

      handler.post(new Runnable() {
        @Override
        public void run() {
          progressDialog.dismiss();
        }
      });

      if (list.size() > 0) {
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
      Log.d("HUE", "Error Called : " + i + ":" + s);

      if (i == PHHueError.NO_CONNECTION) {

        Log.d("HUE", "No Connection...");
      } else if (i == PHHueError.AUTHENTICATION_FAILED
          || i == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED) {
        Log.d("HUE", "Authentication failed... / Pushlink Authentication failed...");

        connectionState = -1;

        handler.post(new Runnable() {
          @Override
          public void run() {
            progressDialog.dismiss();
          }
        });
      } else if (i == PHHueError.BRIDGE_NOT_RESPONDING) {
        Log.d("HUE", "Bridge Not Responding..");

        setLastConnectIp("");
      } else if (i == PHMessageType.BRIDGE_NOT_FOUND) {
        Log.d("HUE", "Bridge Not Found...");

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
  };

  private PHLightListener lightListener = new PHLightListener() {
    @Override
    public void onReceivingLightDetails(PHLight phLight) {
      Log.d("HUE", "Receiving Light Details...");
    }

    @Override
    public void onReceivingLights(List<PHBridgeResource> list) {
      Log.d("HUE", "Receiving Lights...");
    }

    @Override
    public void onSearchComplete() {
      Log.d("HUE", "Search Complete...");
    }

    @Override
    public void onSuccess() {
      Log.d("HUE", "Success...");
    }

    @Override
    public void onError(int i, String s) {
      Log.d("HUE", "Error...");
    }

    @Override
    public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {
      Log.d("HUE", "State Update...");
    }
  };

  HueController(Context context) {
    handler = new Handler();

    sharedPreferences = context.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0);
    sharedPreferencesEditor = sharedPreferences.edit();

    progressDialog = new ProgressDialog(context);
    alert = new AlertDialog.Builder(context);

    hueSDK = PHHueSDK.create();

    hueSDK.setAppName("JINS MEME BRIDGE");
    hueSDK.setDeviceName(Build.MODEL);

    hueSDK.getNotificationManager().registerSDKListener(phListener);

    if (getLastConnectedIp() != null && !getLastConnectedIp().equals("")) {
      Log.d("HUE", "connect... " + getLastConnectedIp() + " / " + getUsername());

      PHAccessPoint accessPoint = new PHAccessPoint();
      accessPoint.setIpAddress(getLastConnectedIp());
      accessPoint.setUsername(getUsername());

      if (!hueSDK.isAccessPointConnected(accessPoint)) {
        hueSDK.connect(accessPoint);
      }
    }
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

      handler.post(new Runnable() {
        @Override
        public void run() {
          progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          progressDialog.setMessage("Searching Hue Bridge...");
          progressDialog.setCancelable(false);
          progressDialog.show();
        }
      });

      PHBridgeSearchManager searchManager = (PHBridgeSearchManager) hueSDK
          .getSDKService(PHHueSDK.SEARCH_BRIDGE);
      searchManager.search(true, true);
    }
  }

  void disconnect() {
    turnOff();

    PHBridge bridge = hueSDK.getSelectedBridge();
    if (bridge != null) {
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
    Log.d("HUE", "turn on...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(true);

    float[] xy = PHUtilities.calculateXYFromRGB(255, 255, 255, currentLight.getModelNumber());

    lightState.setX(xy[0]);
    lightState.setY(xy[1]);

    PHBridge bridge = hueSDK.getSelectedBridge();
    if (currentLight != null) {
      bridge.updateLightState(currentLight, lightState, lightListener);
    }
  }

  void turnOff() {
    Log.d("HUE", "turn off...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(false);

    PHBridge bridge = hueSDK.getSelectedBridge();
    if (currentLight != null) {
      bridge.updateLightState(currentLight, lightState, lightListener);
    }
  }

  void changeColor(int r, int g, int b) {
    Log.d("HUE", "change light color...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(true);

    float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, currentLight.getModelNumber());

    lightState.setX(xy[0]);
    lightState.setY(xy[1]);
    //lightState.setBrightness();

    PHBridge bridge = hueSDK.getSelectedBridge();
    bridge.updateLightState(currentLight, lightState, lightListener);
  }

  void changeColor(int r, int g, int b, int brightness, int time) {
    Log.d("HUE", "change light color...");

    PHLightState lightState = new PHLightState();
    lightState.setOn(true);

    if (currentLight != null) {
      float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, currentLight.getModelNumber());

      lightState.setX(xy[0]);
      lightState.setY(xy[1]);
      lightState.setBrightness(brightness);
      lightState.setTransitionTime(time);

      PHBridge bridge = hueSDK.getSelectedBridge();
      bridge.updateLightState(currentLight, lightState, lightListener);
    }
  }
}
