/**
 * MenuFragment.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jins_jp.meme.MemeFitStatus;
import com.jins_jp.meme.MemeRealtimeData;
import com.jins_jp.meme.MemeRealtimeListener;
import java.util.Random;

import static com.jins_meme.bridge.BridgeUIView.CardHolder;
import static com.jins_meme.bridge.BridgeUIView.IResultListener;

public class MenuFragment extends Fragment implements IResultListener, MemeRealtimeListener {

  private static final int PAUSE_MAX = 50;
  private static final int REFRACTORY_PERIOD_MAX = 20;

  private BridgeUIView mView = null;

  private Handler handler = new Handler();

  private HueController hueController;
  private MemeMIDI memeMIDI;
  private MemeOSC memeOSC;
  private MemeBTSPP memeBTSPP;

  private MemeRealtimeDataFilter memeFilter;

  private int midiChannel = 1;

  private boolean cancelFlag = false;
  private int pauseCount = 0;
  private boolean pauseFlag = false;
  private int refractoryPeriod = 0;
  private int batteryCheckCount = 2000;

  // MenuFragmentからactivityへの通知イベント関連
  enum MenuFragmentEvent {
    LAUNCH_CAMERA {
      @Override
      public void apply(AppCompatActivity activity, Fragment parent) {

        activity.getSupportFragmentManager().beginTransaction()
            .addToBackStack(null)
            .replace(R.id.container, new CameraFragment())
            .commit();
      }
    };

    abstract public void apply(AppCompatActivity activity, Fragment parent);
  }

  interface MenuFragmentListener {

    void onMenuFragmentEnd(MenuFragmentEvent event);
  }

  private MenuFragmentListener mListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof MenuFragmentListener) {
      mListener = (MenuFragmentListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement MenuFragmentListener");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    cancelFlag = false;
    pauseCount = 0;
    pauseFlag = false;
    refractoryPeriod = 0;

    mView = new BridgeUIView(getContext());
    mView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT));
    return mView;
  }

  public boolean menuBack() {
    return mView.back();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    MyAdapter myAdapter = new MyAdapter(getContext(), this);
    mView.setAdapter(myAdapter);

    // Initialize Hue
    hueController = new HueController(getContext());

    // Initialize MIDI
    memeMIDI = new MemeMIDI(getContext());
    memeMIDI.initPort();
    midiChannel = ((MainActivity) getActivity()).getSavedValue("MIDI_CH", 0) + 1;

    // Initialize OSC
    memeOSC = new MemeOSC();
    memeOSC.setRemoteIP(
        ((MainActivity) getActivity()).getSavedValue("REMOTE_IP", MemeOSC.getRemoteIPv4Address()));
    memeOSC.setRemotePort(((MainActivity) getActivity()).getSavedValue("REMOTE_PORT", 10316));
    //memeOSC.setHostPort(((MainActivity) getActivity()).getSavedValue("HOST_PORT", 11316));
    memeOSC.initSocket();

    // Initialize BTSPP
    memeBTSPP = new MemeBTSPP();

    memeFilter = new MemeRealtimeDataFilter();
//        memeFilter.setMoveType(MemeRealtimeDataFilter.MoveType.HEAD);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    Log.d("FRAGMENT", "onDestroyView");

    if (hueController != null) {
      hueController.turnOff();
      hueController = null;
    }

    if (memeMIDI != null) {
      memeMIDI.closePort();
      memeMIDI = null;
    }

    if ((memeOSC != null)) {
      memeOSC.closeSocket();
      memeOSC = null;
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    Log.d("FRAGMENT", "onStop...");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (hueController != null) {
      hueController.turnOff();
      hueController = null;
    }

    if (memeMIDI != null) {
      memeMIDI.closePort();
      memeMIDI = null;
    }

    if (memeOSC != null) {
      memeOSC.closeSocket();
      memeOSC = null;
    }

    Log.d("FRAGMENT", "onDestroy...");
  }

  @Override
  public void onEnterCard(int id) {
    Log.d("ENTER", getResources().getString(id));

    if (pauseCount >= PAUSE_MAX) {
      final MyCardHolder mych = (MyCardHolder) mView.findViewHolderForItemId(id);

      if (pauseFlag) {
        pauseFlag = false;

        mych.reset();
      } else {
        pauseFlag = true;

        mych.pause();
      }
    }
  }

  @Override
  public void onExitCard(int id) {
    Log.d("EXIT", getResources().getString(id));
  }

  @Override
  public void onEndCardSelected(int id) {
    Log.d("RESULT", getResources().getString(id));

    if (pauseCount >= PAUSE_MAX) {
      final MyCardHolder mych = (MyCardHolder) mView.findViewHolderForItemId(id);

      if (pauseFlag) {
        pauseFlag = false;

        mych.reset();
      } else {
        pauseFlag = true;

        mych.pause();
      }

      return;
    }

    final MyCardHolder mych = (MyCardHolder) mView.findViewHolderForItemId(id);

    {
      // MIDI?
      int note = 60;
      switch (id) {
        case R.string.noteon_67:
          ++note;
        case R.string.noteon_66:
          ++note;
        case R.string.noteon_65:
          ++note;
        case R.string.noteon_64:
          ++note;
        case R.string.noteon_63:
          ++note;
        case R.string.noteon_62:
          ++note;
        case R.string.noteon_61:
          ++note;
        case R.string.noteon_60:
          mych.select();

          final int finalNote = note;
          new Thread(new Runnable() {
            @Override
            public void run() {
              Log.d("DEBUG", "note on " + finalNote);
              memeMIDI.sendNote(midiChannel, finalNote, 127);
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              } finally {
                Log.d("DEBUG", "note off " + finalNote);
                memeMIDI.sendNote(midiChannel, finalNote, 0);

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    mych.reset();
                  }
                });
              }
            }
          }).start();
          break;
      }
    }
    {
      // OSC?
      switch (id) {
        case R.string.osc_220hz:
          Log.d("DEBUG", "set freq 220");
          memeOSC.setAddress(MemeOSC.PREFIX, "/frequency");
          memeOSC.setTypeTag("i");
          memeOSC.addArgument(220);
          memeOSC.flushMessage();

          mych.select(500);
          break;
        case R.string.osc_440hz:
          Log.d("DEBUG", "set freq 440");
          memeOSC.setAddress(MemeOSC.PREFIX, "/frequency");
          memeOSC.setTypeTag("i");
          memeOSC.addArgument(440);
          memeOSC.flushMessage();

          mych.select(500);
          break;
        case R.string.osc_mute_on:
          Log.d("DEBUG", "mute on");
          memeOSC.setAddress(MemeOSC.PREFIX, "/volume");
          memeOSC.setTypeTag("f");
          memeOSC.addArgument(0.);
          memeOSC.flushMessage();

          mych.select(500);
          break;
        case R.string.osc_mute_off:
          Log.d("DEBUG", "mute off");
          memeOSC.setAddress(MemeOSC.PREFIX, "/volume");
          memeOSC.setTypeTag("f");
          memeOSC.addArgument(1.);
          memeOSC.flushMessage();

          mych.select(500);
          break;
      }
    }
    {
      // functions?
      switch (id) {
        case R.string.blink_count:
          CountCardHolder ch = (CountCardHolder) mView.findViewHolderForItemId(id);
          ch.countUp();
          break;
      }
    }
    {
      // camera?
      switch (id) {
        case R.string.camera:
          mListener.onMenuFragmentEnd(MenuFragmentEvent.LAUNCH_CAMERA);
          break;
      }
    }
    {
      // Hue
      switch (id) {
        case R.string.random:
          Random rand = new Random();
          hueController.changeColor(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
          break;
        case R.string.light1:
          hueController.changeColor(255, 0, 0);
          break;
        case R.string.light2:
          hueController.changeColor(0, 255, 0);
          break;
        case R.string.light3:
          hueController.changeColor(0, 0, 255);
          break;
        case R.string.light4:
          hueController.changeColor(255, 255, 255);
          break;
      }
    }
  }

  @Override
  public void memeRealtimeCallback(MemeRealtimeData memeRealtimeData) {
    float accelX = memeRealtimeData.getAccX();
    float accelY = memeRealtimeData.getAccY();
    float accelZ = memeRealtimeData.getAccZ();

    int eyeBlinkStrength = memeRealtimeData.getBlinkStrength();
    int eyeBlinkSpeed = memeRealtimeData.getBlinkSpeed();

    int eyeUp = memeRealtimeData.getEyeMoveUp();
    int eyeDown = memeRealtimeData.getEyeMoveDown();
    int eyeLeft = memeRealtimeData.getEyeMoveLeft();
    int eyeRight = memeRealtimeData.getEyeMoveRight();

    float yaw = memeRealtimeData.getYaw();
    float pitch = memeRealtimeData.getPitch();
    float roll = memeRealtimeData.getRoll();

    memeBTSPP.sendAccel(accelX, accelY, accelZ);
    memeBTSPP.sendAngle(yaw, pitch, roll);

    //debug Log.d("DEBUG", "accel  = " + accelX + ", " + accelY + ", " + accelZ);
    //debug Log.d("DEBUG", "rotation  = " + yaw + ", " + pitch + ", " + roll);

    if (memeOSC.initializedSocket()) {
      //debug Log.d("DEBUG", "osc bundle.");

      memeOSC.createBundle();

      memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.ACCEL);
      memeOSC.setTypeTag("fff");
      memeOSC.addArgument(accelX);
      memeOSC.addArgument(accelY);
      memeOSC.addArgument(accelZ);
      memeOSC.setMessageSizeToBundle();
      memeOSC.addMessageToBundle();

      memeOSC.setAddress(MemeOSC.PREFIX, MemeOSC.ANGLE);
      memeOSC.setTypeTag("fff");
      memeOSC.addArgument(yaw);
      memeOSC.addArgument(pitch);
      memeOSC.addArgument(roll);
      memeOSC.setMessageSizeToBundle();
      memeOSC.addMessageToBundle();

      memeOSC.flushBundle();
    }

    int iyaw = (int) ((yaw / 90.0) * 127.0);
    int ipitch = (int) ((pitch / 90.0) * 127.0);
    int iroll = (int) ((roll / 90.0) * 127.0);

    //debug Log.d("DEBUG", "irotation = " + iyaw + ", " + ipitch + ", " + iroll);

    if (iyaw >= 0) {
      memeMIDI.sendControlChange(midiChannel, 30, iyaw);
    } else {
      memeMIDI.sendControlChange(midiChannel, 31, -iyaw);
    }

    if (ipitch >= 0) {
      memeMIDI.sendControlChange(midiChannel, 32, ipitch);
    } else {
      memeMIDI.sendControlChange(midiChannel, 33, -ipitch);
    }

    if (iroll >= 0) {
      memeMIDI.sendControlChange(midiChannel, 34, iroll);
    } else {
      memeMIDI.sendControlChange(midiChannel, 35, -iroll);
    }

    if (++batteryCheckCount > 2000) {
      Log.d("DEBUG", "battery status = " + memeRealtimeData.getPowerLeft());
      ((MainActivity) getActivity()).renewBatteryState(memeRealtimeData.getPowerLeft());

      batteryCheckCount = 0;
    }

    if (memeRealtimeData.getFitError() == MemeFitStatus.MEME_FIT_OK) {
      if (Math.abs(roll) > ((MainActivity) getActivity()).getRollThreshold()) {
        cancelFlag = true;
        //Log.d("DEBUG", "menu = " + getResources().getString(currentEnteredMenu) + " / item = " + getResources().getString(currentSelectedItem));

        if (pauseCount < PAUSE_MAX) {
          pauseCount++;

          if (pauseCount == PAUSE_MAX) {
            handler.post(new Runnable() {
              @Override
              public void run() {
                mView.enter();
              }
            });
          }
        }
      } else if (Math.abs(roll) <= ((MainActivity) getActivity()).getRollThreshold()) {
        if (!pauseFlag) {
          if (cancelFlag && pauseCount < PAUSE_MAX) {
            refractoryPeriod = REFRACTORY_PERIOD_MAX;

            resetCard();
          } else {
            if (refractoryPeriod > 0) {
              refractoryPeriod--;
            } else {
              memeFilter.update(memeRealtimeData,
                  ((MainActivity) getActivity()).getBlinkThreshold(),
                  ((MainActivity) getActivity()).getUpDownThreshold(),
                  ((MainActivity) getActivity()).getLeftRightThreshold());

              if (memeFilter.isBlink()) {
                Log.d("EYE", "blink = " + eyeBlinkStrength + " " + eyeBlinkSpeed);
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    mView.enter();
                  }
                });
              } else if (memeFilter.isLeft()) {
                Log.d("EYE", "left = " + eyeLeft);
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    mView.move(-1);
                  }
                });
              } else if (memeFilter.isRight()) {
                Log.d("EYE", "right = " + eyeRight);
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    mView.move(1);
                  }
                });
              }
            }
          }
        }

        cancelFlag = false;
        pauseCount = 0;
      }
    }

    if (eyeBlinkStrength > 0 || eyeBlinkSpeed > 0) {
      Log.d("EYE", String.format("meme: BLINK = %d/%d", eyeBlinkStrength, eyeBlinkSpeed));

      memeBTSPP.sendEyeBlink(eyeBlinkStrength, eyeBlinkSpeed);
    }

    if (eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
      Log.d("EYE", String
          .format("meme: UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeUp, eyeDown, eyeLeft,
              eyeRight));

      memeBTSPP.sendEyeMove(eyeUp, eyeDown, eyeLeft, eyeRight);
    }
  }

  void resetCard() {
    handler.post(new Runnable() {
      @Override
      public void run() {
        mView.reset();
      }
    });
  }

  // for Bluetooth SPP
  /*
  public boolean isEnabledBLE() {
    return memeBTSPP.isEnabled();
  }
  public void btConnect(String name) {
    memeBTSPP.connect(name);
  }
  public void btDisconnect() {
    memeBTSPP.disconnect();
  }
  public String[] getBtPairedDeviceName() {
    return memeBTSPP.getPairedDeviceName();
  }
  public String getBtConnectedDeviceName() {
    return memeBTSPP.getConnectedDeviceName();
  }
  */

  private class MyAdapter extends BridgeUIView.Adapter<BridgeUIView.CardHolder> {

    Context mContext;
    LayoutInflater mInflater;

    MyAdapter(Context context, IResultListener listener) {
      super(listener);
      mContext = context;
      mInflater = LayoutInflater.from(context);
    }

    private final int CATD_TYPE_ONLY_TITLE = 0;
    private final int CATD_TYPE_COUNTER = 1;

    @Override
    public CardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
      switch (card_type) {
        case CATD_TYPE_COUNTER:
          return new CountCardHolder(mInflater.inflate(R.layout.card_count, parent, false));
        default:
          return new MyCardHolder(mInflater.inflate(R.layout.card_sample, parent, false));
      }
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      switch (id) {
        case R.string.blink_count:
          ((CountCardHolder) (cardHolder)).mTitle.setText(getResources().getString(id));
          ((CountCardHolder) (cardHolder)).reset();
          break;
        default:
          ((MyCardHolder) (cardHolder)).mTextView.setText(getResources().getString(id));
          break;
      }
    }

    @Override
    public CardFunction getCardFunction(int id) {
      switch (id) {
        case R.string.back:
          return CardFunction.BACK;
        case R.string.midi:
        case R.string.osc:
        case R.string.functions:
        case R.string.hue:
          if (pauseCount < PAUSE_MAX) {
            return CardFunction.ENTER_MENU;
          }
      }
      return CardFunction.END;
    }

    @Override
    public int getCardId(int parent_id, int position) {
      int id = NO_ID;
      switch (parent_id) {
        case NO_ID:
          switch (position) {
            case 0:
              id = R.string.midi;
              break;
            case 1:
              id = R.string.osc;
              break;
            case 2:
              id = R.string.functions;
              break;
            case 3:
              id = R.string.camera;
              break;
            case 4:
              id = R.string.hue;
              break;
          }
          break;
        case R.string.midi:
          if (position < 8) {
            id = getResources()
                .getIdentifier("noteon_6" + position, "string", mContext.getPackageName());
          } else {
            id = R.string.back;
          }
          break;
        case R.string.osc:
          switch (position) {
            case 0:
              id = R.string.osc_220hz;
              break;
            case 1:
              id = R.string.osc_440hz;
              break;
            case 2:
              id = R.string.osc_mute_on;
              break;
            case 3:
              id = R.string.osc_mute_off;
              break;
            case 4:
              id = R.string.back;
              break;
          }
          break;
        case R.string.functions:
          switch (position) {
            case 0:
              id = R.string.blink_count;
              break;
            case 1:
              id = R.string.back;
              break;
            case 2:
              id = R.string.back;
              break;
          }
          break;
        case R.string.hue:
          switch (position) {
            case 0:
              id = R.string.random;
              break;
            case 1:
              id = R.string.light1;
              break;
            case 2:
              id = R.string.light2;
              break;
            case 3:
              id = R.string.light3;
              break;
            case 4:
              id = R.string.light4;
              break;
          }
          break;
      }

      if (pauseCount >= PAUSE_MAX) {
        id = parent_id;
      }

      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case R.string.midi:
          return 9;
        case R.string.osc:
          return 5;
        case R.string.functions:
          return 3;
        case R.string.hue:
          return 5;
        case NO_ID:
          return 5;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      switch (id) {
        case R.string.blink_count:
          return CATD_TYPE_COUNTER;
        default:
          return CATD_TYPE_ONLY_TITLE;
      }
    }
  }

  private class MyCardHolder extends CardHolder {

    TextView mTextView;
    TextView mValue;

    MyCardHolder(View itemView) {
      super(itemView);
      mTextView = (TextView) itemView.findViewById(R.id.card_text);
      mValue = (TextView) itemView.findViewById(R.id.card_select);
    }

    void select() {
      mValue.setText(getString(R.string.selected));
    }

    void select(int msec) {
      mValue.setText(getString(R.string.selected));

      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          mValue.setText(" ");
        }
      }, msec);
    }

    void pause() {
      mValue.setText(getString(R.string.pause));
    }

    void reset() {
      mValue.setText(" ");
    }
  }

  private class CountCardHolder extends CardHolder {

    TextView mTitle;
    TextView mValue;
    int count = 0;

    CountCardHolder(View itemView) {
      super(itemView);
      mTitle = (TextView) itemView.findViewById(R.id.count_title);
      mValue = (TextView) itemView.findViewById(R.id.count);
    }

    private void reset() {
      count = 0;
      mValue.setText(getString(R.string.count, count));
    }

    private void countUp() {
      ++count;
      mValue.setText(getString(R.string.count, count));
    }
  }
}