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

/**
 * Created by shun on 2017/06/26.
 */

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
      case "meme_connect":
        progress.setMax(100);
        progress.setMessage("Scannig...");
        progress.setTitle("SCAN & CONNECT");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
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
        progress.setMessage("Searching Hue Bridge...");
        progress.setCancelable(false);
        break;
      case "hue_connect":
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Found Hue Bridge.\nPlease push the LINK Button...");
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
