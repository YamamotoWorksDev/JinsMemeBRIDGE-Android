/**
 * MIDIConfigFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

public class MIDIConfigFragment extends ConfigFragmentBase implements DialogListener {

  private Spinner spMidiCh;
  private SeekBar sbCC72;
  private SeekBar sbCC73;
  private SeekBar sbCC74;
  private SeekBar sbCC75;
  private Button btnNote72;
  private Button btnNote73;
  private Button btnNote74;
  private Button btnNote75;

  MemeMIDI testMIDI;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_midiconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    spMidiCh = null;
    sbCC72 = null;
    sbCC73 = null;
    sbCC74 = null;
    sbCC75 = null;
    btnNote72 = null;
    btnNote73 = null;
    btnNote74 = null;
    btnNote75 = null;

    if(testMIDI != null) {
      testMIDI.closePort();
      testMIDI = null;
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).setActionBarTitle(R.string.midi_conf);
    //((MainActivity) getActivity()).setActionBarBack(true);
    getActivity().invalidateOptionsMenu();

    Log.d("DEBUG", "flag = " + MemeMIDI.checkUsbMidi(getContext()));
    if (!MemeMIDI.checkUsbMidi(getContext())) {
      AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance("midi");
      alertDialogFragment.setCancelable(false);
      alertDialogFragment.setDialogListener(this);
      alertDialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    spMidiCh = (Spinner) view.findViewById(R.id.midi_ch);
    spMidiCh.setSelection(((MainActivity) getActivity()).getSavedValue("MIDI_CH", 0));
    spMidiCh.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("DEBUG", "position = " + i);

        ((MainActivity) getActivity()).autoSaveValue("MIDI_CH", i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    sbCC72 = (SeekBar) view.findViewById(R.id.cc_72);
    sbCC72.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        testMIDI.sendControlChange(spMidiCh.getSelectedItemPosition() + 1, 72, i);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    sbCC73 = (SeekBar) view.findViewById(R.id.cc_73);
    sbCC73.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        testMIDI.sendControlChange(spMidiCh.getSelectedItemPosition() + 1, 73, i);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    sbCC74 = (SeekBar) view.findViewById(R.id.cc_74);
    sbCC74.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        testMIDI.sendControlChange(spMidiCh.getSelectedItemPosition() + 1, 74, i);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    sbCC75 = (SeekBar) view.findViewById(R.id.cc_75);
    sbCC75.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        testMIDI.sendControlChange(spMidiCh.getSelectedItemPosition() + 1, 75, i);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    btnNote72 = (Button) view.findViewById(R.id.note_72);
    btnNote72.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          Log.d("DEBUG", "72 down");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 72, 127);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          Log.d("DEBUG", "72 up");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 72, 0);
        }

        return false;
      }
    });

    btnNote73 = (Button) view.findViewById(R.id.note_73);
    btnNote73.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          Log.d("DEBUG", "73 down");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 73, 127);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          Log.d("DEBUG", "73 up");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 73, 0);
        }

        return false;
      }
    });

    btnNote74 = (Button) view.findViewById(R.id.note_74);
    btnNote74.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          Log.d("DEBUG", "74 down");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 74, 127);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          Log.d("DEBUG", "74 up");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 74, 0);
        }

        return false;
      }
    });

    btnNote75 = (Button) view.findViewById(R.id.note_75);
    btnNote75.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          Log.d("DEBUG", "75 down");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 75, 127);
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
          Log.d("DEBUG", "75 up");

          testMIDI.sendNote(spMidiCh.getSelectedItemPosition() + 1, 75, 0);
        }

        return false;
      }
    });

    testMIDI = new MemeMIDI(getContext());
    testMIDI.initPort();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    spMidiCh = null;
    sbCC72 = null;
    sbCC73 = null;
    sbCC74 = null;
    sbCC75 = null;
    btnNote72 = null;
    btnNote73 = null;
    btnNote74 = null;
    btnNote75 = null;

    testMIDI.closePort();
    testMIDI = null;
  }

  @Override
  public void doPositiveClick(String type) {
    switch (type) {
      case "midi":
        ((MainActivity) getActivity()).backToPreviousMenu();
        break;
    }
  }

  @Override
  public void doNegativeClick(String type) {
    getActivity().finishAndRemoveTask();
  }
}
