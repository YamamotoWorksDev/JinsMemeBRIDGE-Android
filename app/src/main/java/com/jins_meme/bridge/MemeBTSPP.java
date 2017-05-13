/**
 * MemeBTSPP.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MemeBTSPP {

  static final int MAX_UUID_NUM = 2;

  // for Android to PC/Mac
  private static final UUID[] APP_UUID = {UUID.fromString("2f96a155-0b98-4023-87e7-c6dd3b7a4e1d")};

  private BluetoothAdapter btAdapter;
  private BluetoothSocket btSocket;
  private InputStream btIn = null;
  private PushbackInputStream btPbIn = null;
  private OutputStream btOut = null;
  private BufferedOutputStream btDataOut = null;

  private String[] pairedMachineNames;
  private int uuidIndex = 0;
  private boolean isConnectedMachine = false;
  private String connectedDeviceName;

  MemeBTSPP() {
    btAdapter = BluetoothAdapter.getDefaultAdapter();

    if (btAdapter == null) {
      Log.d("DEBUG", "This device is not implement Bluetooth.");
      return;
    }

    if (!btAdapter.isEnabled()) {
      Log.d("DEBUG", "This device is disabled Bluetooth.");
      return;
    }

    Set<BluetoothDevice> paired_devices = btAdapter.getBondedDevices();
    final BluetoothDevice[] devices = paired_devices.toArray(new BluetoothDevice[0]);
    String[] items = new String[devices.length];

    for (int i = 0; i < devices.length; i++) {
      items[i] = devices[i].getName();
    }
    pairedMachineNames = items.clone();

    Log.d("DEBUG", "LIST: " + Arrays.toString(pairedMachineNames));
  }

  public String[] getPairedDeviceName() {
    return pairedMachineNames;
  }

  public String getConnectedDeviceName() {
    return connectedDeviceName;
  }

  public void connect(String connectDeviceName) {
    if (isConnectedMachine) {
      return;
    }

    Set<BluetoothDevice> paired_devices = btAdapter.getBondedDevices();
    final BluetoothDevice[] devices = paired_devices.toArray(new BluetoothDevice[0]);
    String[] items = new String[devices.length];

    for (int i = 0; i < devices.length; i++) {
      items[i] = devices[i].getName();

      if (connectDeviceName.equals(items[i])) {
        try {
          Log.d("DEBUG",
              "hoge0... " + uuidIndex + " " + APP_UUID[uuidIndex < (MAX_UUID_NUM + 1) ? uuidIndex
                  : 0]);

          btSocket = devices[i].createRfcommSocketToServiceRecord(APP_UUID[uuidIndex]);

          try {
            //debug Log.d("BT CONNECTING...")
            //setDebugTextView("BT CONNECTING...");

            btSocket.connect();

            Log.d("DEBUG", "hoge1...");

            btIn = btSocket.getInputStream();
            btPbIn = new PushbackInputStream(btIn);

            btOut = btSocket.getOutputStream();
            btDataOut = new BufferedOutputStream(btOut);

            connectedDeviceName = connectDeviceName;

            isConnectedMachine = true;
          } catch (Throwable t) {
            //Log.d("DEBUG", "hoge2...");

            btSocket.close();
            btSocket = null;

            isConnectedMachine = false;
          }
        } catch (IOException e) {
          Log.d("DEBUG", e.toString());
        }
      }
    }
    Log.d("DEBUG", Arrays.toString(items));
  }

  public void disconnect() {
    try {
      if (btDataOut != null) {
        //debug Log.d("DEBUG", "bluetooth write...");

        //bt_data_out.writeInt(0xFE000000 | (device_id << 16));
        //bt_data_out.flush();

        btDataOut.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    try {
      if (btIn != null) {
        btIn.close();
      }

      if (btOut != null) {
        btOut.close();
      }

      if (btSocket != null) {
        btSocket.close();
      }

      isConnectedMachine = false;
    } catch (IOException ioe) {
      btIn = null;
      btOut = null;
      btSocket = null;

      ioe.printStackTrace();
    }
  }

  public void sendEyeBlink(int strength, int speed) {
    try {
      if (btDataOut != null && isConnectedMachine) {
        //debug Log.d("DEBUG", "bluetooth write...");

        byte[] byteStrength = ByteBuffer.allocate(4).putInt(strength).array();
        byte[] byteSpeed = ByteBuffer.allocate(4).putInt(speed).array();
        byte[] allData = {(byte) 1,
            byteStrength[0], byteStrength[1], byteStrength[2], byteStrength[3],
            byteSpeed[0], byteSpeed[1], byteSpeed[2], byteSpeed[3]};
        btDataOut.write(allData);
        btDataOut.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendEyeMove(int eyeUp, int eyeDown, int eyeLeft, int eyeRight) {
    try {
      if (btDataOut != null && isConnectedMachine) {
        //debug Log.d("DEBUG", "bluetooth write...");

        byte[] allData = {(byte) 2, (byte) eyeUp, (byte) eyeDown, (byte) eyeLeft, (byte) eyeRight};
        btDataOut.write(allData);
        btDataOut.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendAngle(float yaw, float pitch, float roll) {
    try {
      if (btDataOut != null && isConnectedMachine) {
        //debug Log.d("DEBUG", "bluetooth write...");

        byte[] byteYaw = ByteBuffer.allocate(4).putFloat(yaw).array();
        byte[] bytePitch = ByteBuffer.allocate(4).putFloat(pitch).array();
        byte[] byteRoll = ByteBuffer.allocate(4).putFloat(roll).array();
        ;
        byte[] allData = {(byte) 3,
            byteYaw[0], byteYaw[1], byteYaw[2], byteYaw[3],
            bytePitch[0], bytePitch[1], bytePitch[2], bytePitch[3],
            byteRoll[0], byteRoll[1], byteRoll[2], byteRoll[3]};

        btDataOut.write(allData);
        btDataOut.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendAccel(float ax, float ay, float az) {
    try {
      if (btDataOut != null && isConnectedMachine) {
        //debug Log.d("DEBUG", "bluetooth write...");

        byte[] byteAx = ByteBuffer.allocate(4).putFloat(ax).array();
        byte[] byteAy = ByteBuffer.allocate(4).putFloat(ay).array();
        byte[] byteAz = ByteBuffer.allocate(4).putFloat(az).array();
        ;
        byte[] allData = {(byte) 4,
            byteAx[0], byteAx[1], byteAx[2], byteAx[3],
            byteAy[0], byteAy[1], byteAy[2], byteAy[3],
            byteAz[0], byteAz[1], byteAz[2], byteAz[3]};

        btDataOut.write(allData);
        btDataOut.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
