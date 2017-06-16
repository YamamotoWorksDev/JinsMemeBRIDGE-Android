package com.jins_meme.bridge;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Created by ni on 2017/06/11.
 */

public class RemoController {
  public String TAG = "RemoController";
  public static final String IRKIT_SERVICE_TYPE = "_irkit._tcp.local.";
  public static final String REMO_SERVICE_TYPE = "_remo._tcp.local.";
  public static final String DEBUG_SERVICE_TYPE = "_ipp._tcp.local.";

  private Context context;
  private JmDNS jmdns;
  private WifiManager.MulticastLock multicastLock;
  private boolean isProcessingBonjour;
  private boolean isGetMessages;
  private BonjourServiceListener bonjourServiceListener;
  private OnDevicesListener devicesListener;
  private OnMessagesListener messagesListener;

  private String address;
  private String messages;

  private enum RemoState {

  }

  RemoController(Context context) {
    this.context = context;
  }

  public void setDevicesListener(OnDevicesListener listener) {
    this.devicesListener = listener;
  }
  public void setMessagesListener(OnMessagesListener listener) {
    this.messagesListener = listener;
  }
  public void removeDevicesListener() {
    this.devicesListener = null;
  }
  public void removeMessagesListener() {
    this.messagesListener = null;
  }
  public void startDiscovery() {
    startBonjourDiscovery();
  }
  public void stopDiscovery() {
    stopBonjourDiscovery();
  }
  public void recevieMessages(final String address) {
    isGetMessages = true;
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        getIRMessages(address);
      }
    }, 5000);
  }
  public void cancelReceiveMessages() {
    isGetMessages = false;
  }
  public void sendMessages(String address, String messages) {
    postIRMessages(address, messages);
  }

  public void checkExist(String address) {
    getExist(address);
  }


  private void startBonjourDiscovery() {
    if (isProcessingBonjour) {
      Log.e(TAG, "isProcessingBonjour is true");
      return;
    }
    isProcessingBonjour = true;
    // Do network tasks in background. We can't use Handler here.
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock(getClass().getName());
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        InetAddress deviceIPAddress = getWifiIPv4Address();
        try {
          // Do not use default constructor i.e. JmDNS.create()
          jmdns = JmDNS.create(deviceIPAddress);
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (jmdns != null) {
          // Started zeroconf probe
          bonjourServiceListener = new BonjourServiceListener();
          jmdns.addServiceListener(IRKIT_SERVICE_TYPE, bonjourServiceListener);
          jmdns.addServiceListener(DEBUG_SERVICE_TYPE, bonjourServiceListener);
        }
        isProcessingBonjour = false;

        return null;
      }
    }.execute();
  }
  private void stopBonjourDiscovery() {
    if (isProcessingBonjour) {
      Log.e(TAG, "isProcessingBonjour is true");
      return;
    }
    isProcessingBonjour = true;
    // Do network tasks in another thread
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (jmdns != null) {
          if (bonjourServiceListener != null) {
            jmdns.removeServiceListener(IRKIT_SERVICE_TYPE, bonjourServiceListener);
            jmdns.removeServiceListener(DEBUG_SERVICE_TYPE, bonjourServiceListener);
            bonjourServiceListener = null;
          }
          try {
            jmdns.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          jmdns = null;
        }
        if (multicastLock != null) {
          multicastLock.release();
          multicastLock = null;
        }
        // Stopped zeroconf probe
        isProcessingBonjour = false;
      }
    }).start();
  }

  class BonjourServiceListener implements ServiceListener {

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {
      Log.d(TAG, "serviceAdded: " + serviceEvent.getInfo().getQualifiedName());
      jmdns.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName());
      if (devicesListener != null) {
        devicesListener.onServiceAdded(serviceEvent);
      }
    }
    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {
      Log.d(TAG, "serviceRemoved: " + serviceEvent.getInfo().getQualifiedName());
      if (devicesListener != null) {
        devicesListener.onServiceRemoved(serviceEvent);
      }
    }
    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
      Log.d(TAG, "serviceResolved: " + serviceEvent.getInfo().getQualifiedName());
      if (devicesListener != null) {
        devicesListener.onServiceResolved(serviceEvent);
      }
    }
  }

  private InetAddress getWifiIPv4Address() {
    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

    // http://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
      ipAddress = Integer.reverseBytes(ipAddress);
    }

    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

    InetAddress inetAddress = null;
    try {
      inetAddress = InetAddress.getByAddress(ipByteArray);
    } catch (UnknownHostException ex) {
      Log.e(TAG, "Failed to get Wi-Fi IP address");
    }

    return inetAddress;
  }
  private void getExist(String address) {
    String urlString = "http:/" + address + "/";
    URL url;
    try {
      url = new URL(urlString);
    }catch (MalformedURLException e) {
      return;
    }

    new AsyncTask<URL, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(URL... urls) {
        final URL url = urls[0];
        Boolean isExist = false;
        HttpURLConnection httpURLConnection = null;

        try {
          httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
          e.printStackTrace();
          return isExist;
        }
        try {
          httpURLConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
          e.printStackTrace();
          return isExist;
        }
        httpURLConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpURLConnection.setRequestProperty("X-Requested-With", "JinsMemeBRIDGE");
        try {
          httpURLConnection.connect();
          int responseCode = httpURLConnection.getResponseCode();
          Log.d(TAG, "responseCode: " + responseCode);
          isExist = true;
        } catch (IOException e) {
          e.printStackTrace();
          isExist = false;
        } finally {
          if (httpURLConnection != null) {
            httpURLConnection.disconnect();
          }
          return isExist;
        }
      }
      @Override
      protected void onPostExecute(Boolean result) {
        if (devicesListener != null) {
          devicesListener.onExist((boolean)result);
        }
      }
    }.execute(url);
  }
  private void getIRMessages(final String address) {
    String urlString = "http:/" + address + "/messages";
    URL url;
    if (!isGetMessages) {
      return;
    }
    try {
      url = new URL(urlString);
    }catch (MalformedURLException e) {
      return;
    }

    new AsyncTask<URL, Void, String>() {
      @Override
      protected String doInBackground(URL... urls) {
        final URL url = urls[0];
        HttpURLConnection httpURLConnection = null;
        StringBuilder result = new StringBuilder();
        try {
          httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          httpURLConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {

        }
        try {
          httpURLConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
          httpURLConnection.setRequestProperty("X-Requested-With", "JinsMemeBRIDGE");
          httpURLConnection.connect();

          int responseCode = httpURLConnection.getResponseCode();
          switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
              InputStream inputStream = httpURLConnection.getInputStream();
              String encoding = httpURLConnection.getContentEncoding();
              if (encoding == null) {
                encoding = "UTF-8";
              }
              InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
              BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
              String line = null;

              while((line = bufferedReader.readLine()) != null) {
                result.append(line);
              }
              bufferedReader.close();
              inputStreamReader.close();
              inputStream.close();

              break;
            default:
              break;
          }
        } catch (IOException e) {

        } finally {
          if (httpURLConnection != null) {
            httpURLConnection.disconnect();
          }
        }
        return result.toString();
      }
      @Override
      protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: " + result);
        if (!isGetMessages) {
          return;
        }
        if (result.equals("")) {
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            public void run() {
              getIRMessages(address);
            }
          }, 1000);
        } else {
          isGetMessages = false;
          if (messagesListener != null) {
            messagesListener.onReciveMessages(result);
          }
        }
      }
    }.execute(url);
  }

  private void postIRMessages(String address, final String messages) {
    String urlString = "http:/" + address + "/messages";
    URL url;
    try {
      url = new URL(urlString);
    }catch (MalformedURLException e) {
      return;
    }

    new AsyncTask<URL, Void, String>() {
      @Override
      protected String doInBackground(URL... urls) {
        final URL url = urls[0];
        HttpURLConnection httpURLConnection = null;
        StringBuilder result = new StringBuilder();
        try {
          httpURLConnection = (HttpURLConnection) url.openConnection();
          httpURLConnection.setRequestMethod("POST");
          httpURLConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
          httpURLConnection.setRequestProperty("X-Requested-With", "JinsMemeBRIDGE");
          httpURLConnection.setDoOutput(true);
          httpURLConnection.connect();

          OutputStream outputStream = httpURLConnection.getOutputStream();
          PrintStream printStream = new PrintStream(outputStream);
          printStream.print(messages);
          printStream.close();
          outputStream.close();

          int responseCode = httpURLConnection.getResponseCode();
          switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
              InputStream inputStream = httpURLConnection.getInputStream();
              String encoding = httpURLConnection.getContentEncoding();
              if (encoding == null) {
                encoding = "UTF-8";
              }
              InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
              BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
              String line = null;

              while((line = bufferedReader.readLine()) != null) {
                result.append(line);
              }
              bufferedReader.close();
              inputStreamReader.close();
              inputStream.close();

              break;
            default:
              break;
          }
        } catch (MalformedURLException e) {

        } catch (ProtocolException e) {

        } catch (IOException e) {

        } finally {
          if (httpURLConnection != null) {
            httpURLConnection.disconnect();
          }
        }
        return result.toString();
      }
      @Override
      protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: " + result);
        if (messagesListener != null) {
          messagesListener.onSendMessages(result);
        }
      }
    }.execute(url);
  }


  public interface OnDevicesListener {
    public void onExist(boolean result);
    public void onServiceAdded(ServiceEvent serviceEvent);
    public void onServiceRemoved(ServiceEvent serviceEvent);
    public void onServiceResolved(ServiceEvent serviceEvent);

  }
  public interface OnMessagesListener {
    public void onReciveMessages(String messages);
    public void onSendMessages(String messages);
  }


}