package com.jins_meme.bridge;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.util.Log;

import java.io.IOException;

/**
 *
 * MemeMIDI.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 **/

public class MemeMIDI {
  public static final int EYE_BLINK = 30;
  public static final int EYE_UP    = 31;
  public static final int EYE_DOWN  = 32;
  public static final int EYE_LEFT  = 33;
  public static final int EYE_RIGHT = 34;

  private Context context;

  private MidiManager midiManager;
  private MidiInputPort midiInputPort;
  private MidiOutputPort midiOutputPort;

  private boolean initializedMidi = false;

  public MemeMIDI(Context context) {
    this.context = context;
  }

  public boolean isInitializedMidi() {
    return initializedMidi;
  }

  public static boolean checkUsbMidi(Context context) {
    MidiManager midiManager = (MidiManager)context.getSystemService(Context.MIDI_SERVICE);
    if(midiManager != null) {
      final MidiDeviceInfo[] infos = midiManager.getDevices();
      if(infos.length > 0)
        return true;
      else
        return false;
    }
    else
      return false;
  }

  public void initPort() {
    midiManager = (MidiManager)context.getSystemService(Context.MIDI_SERVICE);
    final MidiDeviceInfo[] infos = midiManager.getDevices();
    if(infos.length > 0) {
      final int numInputs = infos[0].getInputPortCount();
      final int numOutputs = infos[0].getOutputPortCount();
      MidiDeviceInfo.PortInfo[] portInfos = infos[0].getPorts();
      String portName = portInfos[0].getName();
      midiManager.openDevice(infos[0], new MidiManager.OnDeviceOpenedListener() {
        @Override
        public void onDeviceOpened(MidiDevice device) {
          if(device == null) {
            Log.d("DEBUG", "could not open device " + infos[0]);
          }
          else {
            Log.d("DEBUG", "a onDeviceOpend...");

            midiInputPort = device.openInputPort(numInputs - 1);
            midiOutputPort = device.openOutputPort(numOutputs - 1);

            if(midiInputPort == null)
              Log.d("DEBUG", "midi input port is null...");

            if(midiOutputPort == null)
              Log.d("DEBUG", "midi output port is null...");

            initializedMidi = true;
          }
        }
      }, null);

      Log.d("DEBUG", "MIDI: " + numInputs + " " + numOutputs + " " + portName);

      /*
      midiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
        public void onDeviceAdded(MidiDeviceInfo info) {

        }

        public void onDeviceRemoved(MidiDeviceInfo info) {

        }
      });
      */
    }
    Log.d("DEBUG", "midi:" + infos.length);
  }

  public void closePort() {
    try {
      midiInputPort.close();
      midiOutputPort.close();

      midiInputPort = null;
      midiOutputPort = null;

      midiManager = null;
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendNote(int channel, int pitch, int velocity) {
    if(channel < 1 || channel > 16)
      return;

    if(pitch < 0 || pitch > 127)
      return;

    if(velocity < 0) {
      velocity = 0;
    }
    else if(velocity > 127) {
      velocity = 127;
    }

    // MIDI
    byte[] buffer = new byte[32];
    int numBytes = 0;
    if(velocity > 0) {
      buffer[numBytes++] = (byte)(0x90 + (channel - 1)); // Note On
      buffer[numBytes++] = (byte)pitch;
      buffer[numBytes++] = (byte)velocity;
    }
    else if(velocity == 0) {
      buffer[numBytes++] = (byte)(0x80 + (channel - 1)); // Note Off
      buffer[numBytes++] = (byte)pitch;
      buffer[numBytes++] = (byte)0;
    }
    int offset = 0;

    try {
      midiInputPort.send(buffer, offset, numBytes);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendControlChange(int channel, int number, int value) {
    if(channel < 1 || channel > 16)
      return;

    if(number < 0 || number > 127)
      return;

    if(value < 0) {
      value = 0;
    }
    else if(value > 119) {
      value = 119;
    }

    // MIDI
    byte[] buffer = new byte[32];
    int numBytes = 0;
    buffer[numBytes++] = (byte)(0xB0 + (channel - 1)); // Control Change
    buffer[numBytes++] = (byte)number;
    buffer[numBytes++] = (byte)value;
    int offset = 0;

    try {
      midiInputPort.send(buffer, offset, numBytes);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
