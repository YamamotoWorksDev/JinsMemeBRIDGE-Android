/**
 * AlertDialogFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class ProgressDialogFragment extends DialogFragment {

  ProgressDialog progress;
  private DialogListener mListener = null;

  public static ProgressDialogFragment newInstance(String type) {
    ProgressDialogFragment instance = new ProgressDialogFragment();

    Bundle arguments = new Bundle();
    arguments.putString("type", type);
    instance.setArguments(arguments);

    return instance;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String type = getArguments().getString("type");

    progress = new ProgressDialog(getActivity());

    switch (type) {
      case "meme_scan":
        progress.setMax(100);
        progress.setMessage(getString(R.string.meme_scanning));
        progress.setTitle(getString(R.string.meme_scan_connect_title));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        /*
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
            new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null) {
                  mListener.doNegativeClick("meme_connect");
                }
              }
            });
            */
        break;
      case "meme_connect":
        progress.setMax(100);
        progress.setMessage(getString(R.string.meme_scanning));
        progress.setTitle(getString(R.string.meme_scan_connect_title));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
            new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null) {
                  mListener.doNegativeClick("meme_connect");
                }
              }
            });
        break;
      case "hue_search":
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(getString(R.string.hue_searching));
        progress.setCancelable(false);
        break;
      case "hue_connect":
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(getString(R.string.hue_push_link_button));
        progress.setCancelable(false);
        break;
      case "spotify_loading":
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage(getString(R.string.spotify_loading));
        progress.setCancelable(false);
        break;
    }

    return progress;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public void setMessage(String message) {
    progress.setMessage(message);
  }

  public void setDialogListener(DialogListener listener) {
    mListener = listener;
  }

  public boolean isShowing() {
    return progress.isShowing();
  }

  public void removeDialogListener() {
    mListener = null;
  }
}
