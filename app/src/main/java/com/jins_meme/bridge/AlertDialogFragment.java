/**
 * AlertDialogFragment.java
 *
 * Copyright (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class AlertDialogFragment extends DialogFragment {

  private DialogListener mListener = null;

  public static AlertDialogFragment newInstance(String type) {
    AlertDialogFragment instance = new AlertDialogFragment();

    Bundle arguments = new Bundle();
    arguments.putString("type", type);
    instance.setArguments(arguments);

    return instance;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String type = getArguments().getString("type");

    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

    switch (type) {
      case "welcome":
        alert.setTitle(getString(R.string.welcome_title));
        alert.setMessage(getString(R.string.welcome_message));
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            if (mListener != null) {
              mListener.doPositiveClick("welcome");
            }
          }
        });
        break;
      case "airplane":
        alert.setTitle(getString(R.string.airplane_mode_on_title));
        alert.setMessage(getString(R.string.airplane_mode_on_explain));
        alert.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Quit App...");

            if (mListener != null) {
              mListener.doNegativeClick("airplane");
            }
          }
        });
        break;
      case "network":
        alert.setTitle(getString(R.string.not_connected_network_title));
        alert.setMessage(getString(R.string.not_connected_network_explain));
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            if (mListener != null) {
              mListener.doPositiveClick("network");
            }
          }
        });
        alert.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Quit App...");

            if (mListener != null) {
              mListener.doNegativeClick("network");
            }
          }
        });
        break;
      case "meme":
        alert.setTitle(getString(R.string.not_found_meme_title));
        alert.setMessage(getString(R.string.not_found_meme_explain));
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Close Alert Dialog...");

            if (mListener != null) {
              mListener.doPositiveClick("meme");
            }
          }
        });
        alert.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Quit App...");

            if (mListener != null) {
              mListener.doNegativeClick("meme");
            }
          }
        });
        break;
      case "app_id_secret":
        alert.setTitle(getString(R.string.incorrect_app_id_secret_title));
        alert.setMessage(getString(R.string.incorrect_app_id_secret_explain));
        alert.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Quit App...");

            if (mListener != null) {
              mListener.doNegativeClick("meme");
            }
          }
        });
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Close Alert Dialog...");

            if (mListener != null) {
              mListener.doPositiveClick("meme");
            }
          }
        });
        break;
      case "midi":
        alert.setTitle(getString(R.string.not_selected_midi_title));
        alert.setMessage(getString(R.string.not_selected_midi_explain));
        alert.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Quit App...");

            if (mListener != null) {
              mListener.doNegativeClick("midi");
            }
          }
        });
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            Log.d("DEBUG", "Close Alert Dialog...");

            if (mListener != null) {
              mListener.doPositiveClick("midi");
            }
          }
        });
        break;
      case "hue":
        alert.setTitle(getString(R.string.hue_error_title));
        alert.setMessage(getString(R.string.hue_error_message));
        alert.setNegativeButton(getString(R.string.ok), new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
        break;
    }

    Dialog dialog = alert.create();

    Window window = dialog.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    //return alert.create();
    return dialog;
  }

  public void setDialogListener(DialogListener listener) {
    mListener = listener;
  }

  public void removeDialogListener() {
    mListener = null;
  }
}
