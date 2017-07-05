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


  private Context context;
  private JmDNS jmdns;
  private WifiManager.MulticastLock multicastLock;
  private boolean isProcessingBonjour;
  private boolean isGetMessages;
  private BonjourServiceListener bonjourServiceListener;
  private OnDevicesListener devicesListener;
  private OnMessagesListener messagesListener;

  private boolean isExist;
  private boolean isError;

  private boolean isFirstReceive;
  private boolean isRestartBonjour = false;
  private boolean isRestopBonjour = false;

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
    isFirstReceive = true;
    getIRMessages(address);
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
      Log.e(TAG, "startBonjourDiscovery: isProcessingBonjour is true");
      isRestartBonjour = true;
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
          jmdns.addServiceListener(REMO_SERVICE_TYPE, bonjourServiceListener);

        }
        isProcessingBonjour = false;
        if (isRestopBonjour) {
          stopBonjourDiscovery();
          isRestopBonjour = false;
        }
        return null;
      }
    }.execute();
  }
  private void stopBonjourDiscovery() {
    if (isProcessingBonjour) {
      Log.e(TAG, "stopBonjourDiscovery: isProcessingBonjour is true");
      isRestopBonjour = true;
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
            jmdns.removeServiceListener(REMO_SERVICE_TYPE, bonjourServiceListener);

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
        if (isRestartBonjour) {
          startBonjourDiscovery();
          isRestartBonjour = false;
        }
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
    isError = false;
    isExist = false;
    String urlString = "http://" + address + "/";
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
        httpURLConnection.setConnectTimeout(10000);
//        httpURLConnection.setReadTimeout(1000);
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
    String urlString = "http://" + address + "/messages";
    isError = false;
    isExist = false;
    if (!isGetMessages) {
      return;
    }

    new AsyncTask<String, Void, String>() {
      @Override
      protected String doInBackground(String... urls) {
        String urlString = urls[0];
        URL url = null;

        try {
          url = new URL(urlString);
        }catch (MalformedURLException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }

        HttpURLConnection httpURLConnection = null;
        StringBuilder result = new StringBuilder();

        try {
          httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }
        try {
          httpURLConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }
        try {
          httpURLConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
          httpURLConnection.setRequestProperty("X-Requested-With", "JinsMemeBRIDGE");
          httpURLConnection.setConnectTimeout(5000);
//          httpURLConnection.setReadTimeout(5000);
          httpURLConnection.connect();

          isExist = true;

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
          e.printStackTrace();
          isError = true;
        } finally {
          httpURLConnection.disconnect();
          return result.toString();
        }
      }
      @Override
      protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: " + result);
        if (isError || !isExist) {
          cancelReceiveMessages();
          messagesListener.onReciveMessages(result ,false);
          return;
        }
        if (!isGetMessages) {
          return;
        }
        if (isFirstReceive) {
          isFirstReceive = false;
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            public void run() {
              getIRMessages(address);
            }
          }, 5000);

        } else if (result.equals("")) {
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            public void run() {
              getIRMessages(address);
            }
          }, 2000);
        } else {
          isGetMessages = false;
          if (messagesListener != null) {
            messagesListener.onReciveMessages(result ,true);
          }
        }
      }
    }.execute(urlString);
  }

  private void postIRMessages(String address, final String messages) {
    String urlString = "http://" + address + "/messages";
    isError = false;
    isExist = false;
    new AsyncTask<String, Void, String>() {
      @Override
      protected String doInBackground(String... urls) {
        String urlString = urls[0];
        URL url = null;
        try {
          url = new URL(urlString);
        }catch (MalformedURLException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }

        HttpURLConnection httpURLConnection = null;
        StringBuilder result = new StringBuilder();

        try {
          httpURLConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }
        try {
          httpURLConnection.setRequestMethod("POST");
        } catch (ProtocolException e) {
          e.printStackTrace();
          isError = true;
          return "";
        }

        try {
          httpURLConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
          httpURLConnection.setRequestProperty("X-Requested-With", "JinsMemeBRIDGE");
          httpURLConnection.setConnectTimeout(5000);
//          httpURLConnection.setReadTimeout(5000);
          httpURLConnection.setDoOutput(true);
          httpURLConnection.connect();

          isExist = true;

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

              while ((line = bufferedReader.readLine()) != null) {
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
//          e.printStackTrace();
          isExist = false;
        } finally {
            httpURLConnection.disconnect();
            return result.toString();
        }
      }
      @Override
      protected void onPostExecute(String result) {
        boolean isSuccess;
        Log.d(TAG, "onPostExecute: " + result);
        if (isExist && !isError) {
          isSuccess = true;
        } else {
          isSuccess = false;
        }
        messagesListener.onSendMessages(result, isSuccess);
      }
    }.execute(urlString);
  }


  public interface OnDevicesListener {
    public void onExist(boolean result);
    public void onServiceAdded(ServiceEvent serviceEvent);
    public void onServiceRemoved(ServiceEvent serviceEvent);
    public void onServiceResolved(ServiceEvent serviceEvent);

  }
  public interface OnMessagesListener {
    public void onReciveMessages(String messages, boolean isSuccess);
    public void onSendMessages(String messages, boolean isSuccess);
  }


}
