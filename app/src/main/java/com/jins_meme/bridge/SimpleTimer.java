package com.jins_meme.bridge;

import android.os.Handler;

/**
 * Created by nariakiiwatani on 2017/07/04.
 */

public class SimpleTimer {
  SimpleTimer(int id) {
    this.id = id;
  }
  public void setListener(OnResultListener listener) {
    this.listener = listener;
  }
  public void startTimer(float durationInSeconds, boolean overwrite) {
    if(isActive && overwrite) {
      abortTimer();
    }
    isActive = true;
    isFinished = false;
    handler.postDelayed(callback, (long) (durationInSeconds * 1000));
    if(listener != null) {
      listener.onTimerStarted(id);
    }
  }
  public void abortTimer() {
    if(isActive) {
      isActive = false;
      isFinished = false;
      handler.removeCallbacks(callback);
      if(listener != null) {
        listener.onTimerFinished(id, false);
      }
    }
  }
  public boolean isTimerActive() { return isActive; }
  public boolean isTimerFinished() { return isFinished; }
  private Handler handler = new Handler();
  private Runnable callback = new Runnable() {
    @Override
    public void run() {
      isActive = false;
      isFinished = true;
      if(listener != null) {
        listener.onTimerFinished(id, true);
      }
    }
  };
  private boolean isActive = false;
  private boolean isFinished = false;
  private int id;

  interface OnResultListener {
    void onTimerStarted(int id);
    void onTimerFinished(int id, boolean completed);
  }
  private OnResultListener listener = null;
}
