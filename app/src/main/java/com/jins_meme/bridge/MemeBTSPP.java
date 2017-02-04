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

/**
 * Created by shun on 2017/02/04.
 */

public class MemeBTSPP {
  static final int MAX_UUID_NUM = 2;

  // for Android to PC/Mac
  private static final UUID[] APP_UUID = {UUID.fromString("2f96a155-0b98-4023-87e7-c6dd3b7a4e1d")};

  private BluetoothAdapter bt_adapter;
  private BluetoothSocket bt_socket;
  private InputStream bt_in = null;
  private PushbackInputStream bt_pb_in = null;
  private OutputStream bt_out = null;
  private BufferedOutputStream bt_data_out = null;

  private String[] paired_machine_names;
  private int paired_machine_index = 2;// pc/mac index
  private int uuid_index = 0;
  private boolean is_connected_machine = false;

  MemeBTSPP() {
    bt_adapter = BluetoothAdapter.getDefaultAdapter();

    if(bt_adapter == null) {
      Log.d("DEBUG", "This device is not implement Bluetooth.");
      return;
    }

    if(!bt_adapter.isEnabled()) {
      Log.d("DEBUG", "This device is disabled Bluetooth.");
      return;
    }

    Set<BluetoothDevice> paired_devices = bt_adapter.getBondedDevices();
    final BluetoothDevice[] devices = paired_devices.toArray(new BluetoothDevice[0]);
    String[] items = new String[devices.length];

    for(int i = 0; i < devices.length; i++) {
      items[i] = devices[i].getName();
    }
    paired_machine_names = items.clone();

    Log.d("DEBUG", "LIST: " + Arrays.toString(paired_machine_names));
  }

  public void connect() {
    if(is_connected_machine)
      return;

    Set<BluetoothDevice> paired_devices = bt_adapter.getBondedDevices();
    final BluetoothDevice[] devices = paired_devices.toArray(new BluetoothDevice[0]);
    String[] items = new String[devices.length];

    for(int i = 0; i < devices.length; i++) {
      items[i] = devices[i].getName();

      Log.d("DEBUG", "hoge0" + i + " : " + paired_machine_index + " " + items[i]);

      if(paired_machine_names[paired_machine_index].equals(items[i])) {
        try {
          Log.d("DEBUG", "hoge0... " + uuid_index + " " + APP_UUID[uuid_index < (MAX_UUID_NUM + 1) ? uuid_index : 0]);

          bt_socket = devices[i].createRfcommSocketToServiceRecord(APP_UUID[uuid_index]);

          try {
            //debug Log.d("BT CONNECTING...")
            //setDebugTextView("BT CONNECTING...");

            bt_socket.connect();

            Log.d("DEBUG", "hoge1...");

            bt_in = bt_socket.getInputStream();
            bt_pb_in = new PushbackInputStream(bt_in);

            bt_out = bt_socket.getOutputStream();
            bt_data_out = new BufferedOutputStream(bt_out);

            is_connected_machine = true;
          }
          catch(Throwable t) {
            Log.d("DEBUG", "hoge2...");

            bt_socket.close();
            bt_socket = null;

            is_connected_machine = false;
          }
        }
        catch(IOException e) {
          Log.d("DEBUG", e.toString());
        }
      }
    }
    Log.d("DEBUG", Arrays.toString(items));
  }

  public void disconnect() {
    try {
      if(bt_data_out != null) {
        //debug Log.d("DEBUG", "bluetooth write...");

        //bt_data_out.writeInt(0xFE000000 | (device_id << 16));
        //bt_data_out.flush();

        bt_data_out.flush();
      }
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }

    try {
      if(bt_in != null)
        bt_in.close();

      if(bt_out != null)
        bt_out.close();

      if(bt_socket != null)
        bt_socket.close();

      is_connected_machine = false;
    }
    catch(IOException ioe) {
      bt_in = null;
      bt_out = null;
      bt_socket = null;

      ioe.printStackTrace();
    }
  }
}
