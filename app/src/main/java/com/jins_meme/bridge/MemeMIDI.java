/**
 * MemeMIDI.java
 *
 * Copyright (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.util.Log;

import java.io.IOException;

public class MemeMIDI {

  public static final int EYE_BLINK = 30;
  public static final int EYE_UP = 31;
  public static final int EYE_DOWN = 32;
  public static final int EYE_LEFT = 33;
  public static final int EYE_RIGHT = 34;

  private Context context;

  private boolean initializedMidi = false;
  private int midiType;
  private int midiCh;
  private int midiNum;
  private int midiVal;

  private MidiReceiveListener listener = null;
  private MidiManager midiManager;
  private MidiInputPort midiInputPort;
  private MidiOutputPort midiOutputPort;
  private MidiReceiver midiReceiver = new MidiReceiver() {
    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) throws IOException {
      Log.d("MIDI", "receive midi... " + count);

      for (int i = 0; i < count + 1; i++) {
        int type = msg[i] & 0x00F0;
        int ch = 0;
        int number = 0;
        int value = 0;
        if (type == 0x80 || type == 0x90 || type == 0xB0) {
          ch = msg[i] & 0x000F;
          number = msg[i + 1];
          value = msg[i + 2];

          Log.d("MIDI", i + "* : " + (msg[i] & 0x00FF) + "(0x" + Integer.toHexString(msg[i] & 0x00FF) + ")" + " / " + ch + " / " + number + " / " + value);

          if (listener != null) {
            midiType = type;
            midiCh = ch;
            midiNum = number;
            midiVal = value;

            listener.onReceiveMidiMessage();
          }

          i += 2;
        } else {
          Log.d("MIDI", i + "  : " + (msg[i] & 0x00FF) + "(0x" + Integer.toHexString(msg[i] & 0x00FF) + ")");
        }
      }
    }
  };

  public void setMidiType(int midiType) {
    this.midiType = midiType;
  }

  public int getMidiType() {
    return midiType;
  }

  public void setMidiCh(int midiCh) {
    this.midiCh = midiCh;
  }

  public int getMidiCh() {
    return midiCh;
  }

  public void setMidiNum(int midiNum) {
    this.midiNum = midiNum;
  }

  public int getMidiNum() {
    return midiNum;
  }

  public void setMidiVal(int midiVal) {
    this.midiVal = midiVal;
  }

  public int getMidiVal() {
    return midiVal;
  }

  public MemeMIDI(Context context) {
    this.context = context;
  }

  public boolean isInitializedMidi() {
    return initializedMidi;
  }

  public static boolean checkUsbMidi(Context context) {
    MidiManager midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
    if (midiManager != null) {
      final MidiDeviceInfo[] infos = midiManager.getDevices();
      if (infos.length > 0) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public void initPort() {
    Log.d("MIDI", "initPort...");

    midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
    if (midiManager != null) {
      final MidiDeviceInfo[] infos = midiManager.getDevices();

      for (final MidiDeviceInfo info : infos) {
        final int numInputs = info.getInputPortCount();
        final int numOutputs = info.getOutputPortCount();

        if (numOutputs == 0)
          continue;

        Log.d("MIDI", "in port num = " + numInputs);
        Log.d("MIDI", "out port num = " + numOutputs);

        int portIndex = 0;
        for (MidiDeviceInfo.PortInfo portInfo : info.getPorts()) {
          Log.d("MIDI", "name: " + portInfo.getName());

          String portName = portInfo.getName();

          final int pi = portIndex;
          midiManager.openDevice(info, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
              if (device == null) {
                Log.d("MIDI", "could not open device " + info);
              } else {
                Log.d("MIDI", "a onDeviceOpend... " + pi);

                switch (pi) {
                  case 0:
                    midiInputPort = device.openInputPort(numInputs - 1);

                    if (midiInputPort == null) {
                      Log.d("MIDI", "midi input port is null...");
                    }
                    break;
                  case 1:
                    midiOutputPort = device.openOutputPort(numOutputs - 1);

                    if (midiOutputPort == null) {
                      Log.d("MIDI", "midi output port is null...");
                    }

                    midiOutputPort.onConnect(midiReceiver);
                    break;
                }
                initializedMidi = true;
              }
            }
          }, null);

          portIndex++;
        }
      }
      //Log.d("DEBUG", "midi:" + infos.length);
    }
  }

  public void closePort() {
    try {
      if (midiInputPort != null) {
        midiInputPort.close();
        midiInputPort = null;
      }

      if (midiOutputPort != null) {
        midiOutputPort.onDisconnect(midiReceiver);
        midiOutputPort.close();
        midiOutputPort = null;
        midiReceiver = null;
      }

      midiManager = null;
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendNote(int channel, int pitch, int velocity) {
    if (channel < 1 || channel > 16) {
      return;
    }

    if (pitch < 0 || pitch > 127) {
      return;
    }

    if (velocity < 0) {
      velocity = 0;
    } else if (velocity > 127) {
      velocity = 127;
    }

    // MIDI
    byte[] buffer = new byte[32];
    int numBytes = 0;
    if (velocity > 0) {
      buffer[numBytes++] = (byte) (0x90 + (channel - 1)); // Note On
      buffer[numBytes++] = (byte) pitch;
      buffer[numBytes++] = (byte) velocity;
    } else if (velocity == 0) {
      buffer[numBytes++] = (byte) (0x80 + (channel - 1)); // Note Off
      buffer[numBytes++] = (byte) pitch;
      buffer[numBytes++] = (byte) 0;
    }
    int offset = 0;

    try {
      if (midiInputPort != null) {
        midiInputPort.send(buffer, offset, numBytes);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void sendControlChange(int channel, int number, int value) {
    if (channel < 1 || channel > 16) {
      return;
    }

    if (number < 0 || number > 127) {
      return;
    }

    if (value < 0) {
      value = 0;
    } else if (value > 119) {
      value = 119;
    }

    // MIDI
    byte[] buffer = new byte[32];
    int numBytes = 0;
    buffer[numBytes++] = (byte) (0xB0 + (channel - 1)); // Control Change
    buffer[numBytes++] = (byte) number;
    buffer[numBytes++] = (byte) value;
    int offset = 0;

    try {
      if (midiInputPort != null) {
        midiInputPort.send(buffer, offset, numBytes);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void setListener(MidiReceiveListener listener) {
    this.listener = listener;
  }

  public void removeListener() {
    this.listener = null;
  }
}
