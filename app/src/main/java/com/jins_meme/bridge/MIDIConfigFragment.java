/**
 * SettingFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MIDIConfigFragment extends Fragment {

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
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.midi_conf) + " SETTING");
    ((MainActivity) getActivity()).setActionBarBack(true);

    Log.d("DEBUG", "flag = " + MemeMIDI.checkUsbMidi(getContext()));
    if (!MemeMIDI.checkUsbMidi(getContext())) {
      AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
      alert.setTitle("Warning");
      alert.setMessage("Please change your USB Connection Type to MIDI and restart.");
      alert.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Quit App...");

          ((MainActivity) getActivity()).finish();
        }
      });
      alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          Log.d("DEBUG", "Close Alert Dialog...");

          ((MainActivity) getActivity()).transitToMain(0);
        }
      });

      alert.create().show();
    }
  }
}
