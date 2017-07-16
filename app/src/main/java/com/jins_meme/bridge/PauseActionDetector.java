/**
 * PauseActionDetector.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

import android.graphics.PointF;
import android.widget.FrameLayout;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PauseActionDetector extends FrameLayout implements SimpleTimer.OnResultListener {
  private float allowSlideRadius = 1;
  private float longPressDuration = 1;
  private PointF touchedPoint;
  SimpleTimer longPressTimer;
  private boolean interceptTouchEvent = false;
  public PauseActionDetector(@NonNull Context context) {
    this(context, null);
  }
  public PauseActionDetector(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    longPressTimer = new SimpleTimer(0);
    longPressTimer.setListener(this);
  }
  public void setListaner(OnPauseActionListener listener) {
    this.listener = listener;
  }
  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    switch(event.getAction()) {
      case ACTION_DOWN:
        longPressTimer.startTimer(longPressDuration, true);
        interceptTouchEvent = false;
        touchedPoint = new PointF(event.getX(), event.getY());
        return super.dispatchTouchEvent(event) || true;
      case ACTION_MOVE:
        float dx = event.getX()-touchedPoint.x;
        float dy = event.getY()-touchedPoint.y;
        float distanceSquared = dx*dx+dy*dy;
        if(distanceSquared >= allowSlideRadius*allowSlideRadius) {
          longPressTimer.abortTimer();
        }
        break;
      case ACTION_UP:
        longPressTimer.abortTimer();
        break;
    }
    return super.dispatchTouchEvent(event);
  }
  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    return interceptTouchEvent || super.onInterceptTouchEvent(event);
  }
  @Override
  public void onTimerStarted(int id) {
  }

  @Override
  public void onTimerFinished(int id, boolean completed) {
    if(completed) {
      listener.onPauseAction();
      interceptTouchEvent = true;
    }
  }

  interface OnPauseActionListener {
    void onPauseAction();
  }
  OnPauseActionListener listener;
}
