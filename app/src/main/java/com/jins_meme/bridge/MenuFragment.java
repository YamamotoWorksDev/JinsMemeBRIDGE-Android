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
import android.widget.ImageView;
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
  //private MemeBTSPP memeBTSPP;

  private MemeRealtimeDataFilter memeFilter;

  private int midiChannel = 1;
  private int lastNote = 0;

  private boolean cancelFlag = false;
  private int pauseCount = 0;
  private boolean pauseFlag = false;
  private int refractoryPeriod = 0;
  private int batteryCheckCount = 2000;

  private int currentEnteredMenu = 0;
  private int currentSelectedItem = 0;

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
    //memeBTSPP = new MemeBTSPP();

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
      // camera
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
          hueController
              .changeColor(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 255, 1);
          break;
        case R.string.light1:
          hueController.changeColor(((MainActivity) getActivity()).getSavedValue("HUE_L1_R", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L1_G", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L1_B", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L1_BRI", 127),
              ((MainActivity) getActivity()).getSavedValue("HUE_L1_TTIME", 10));
          break;
        case R.string.light2:
          hueController.changeColor(((MainActivity) getActivity()).getSavedValue("HUE_L2_R", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L2_G", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L2_B", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L2_BRI", 127),
              ((MainActivity) getActivity()).getSavedValue("HUE_L2_TTIME", 10));
          break;
        case R.string.light3:
          hueController.changeColor(((MainActivity) getActivity()).getSavedValue("HUE_L3_R", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L3_G", 0),
              ((MainActivity) getActivity()).getSavedValue("HUE_L3_B", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L3_BRI", 127),
              ((MainActivity) getActivity()).getSavedValue("HUE_L3_TTIME", 10));
          break;
        case R.string.light4:
          hueController.changeColor(((MainActivity) getActivity()).getSavedValue("HUE_L4_R", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L4_G", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L4_B", 255),
              ((MainActivity) getActivity()).getSavedValue("HUE_L4_BRI", 127),
              ((MainActivity) getActivity()).getSavedValue("HUE_L4_TTIME", 10));
          break;
      }
      mych.select();
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            handler.post(new Runnable() {
              @Override
              public void run() {
                mych.reset();
              }
            });
          }
        }
      }).start();
    }
    {
      // VDJ
      int note = 24;
      switch (id) {
        case R.string.track8:
          ++note;
        case R.string.track7:
          ++note;
        case R.string.track6:
          ++note;
        case R.string.track5:
          ++note;
        case R.string.track4:
          ++note;
        case R.string.track3:
          ++note;
        case R.string.track2:
          ++note;
        case R.string.track1:
          final int finalNote = note > 27 ? note + 8 : note;

          //final MyCardHolder mych = (MyCardHolder) mView.findViewHolderForItemId(id);
          mych.select();

          new Thread(new Runnable() {
            @Override
            public void run() {
              if (finalNote != lastNote) {
                Log.d("DEBUG", "note on " + finalNote);
                memeMIDI.sendNote(midiChannel, finalNote, 127);
              }

              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              } finally {
                if (finalNote != lastNote) {
                  Log.d("DEBUG", "note off " + finalNote);
                  memeMIDI.sendNote(midiChannel, finalNote, 0);
                  lastNote = finalNote;
                }

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

      int noteFx = 48;
      final int finalNoteFx;
      final MyCardHolder mych_fx;

      switch (id) {
        case R.string.effect6:
          ++noteFx;
        case R.string.effect5:
          ++noteFx;
        case R.string.effect4:
          ++noteFx;
        case R.string.effect3:
          ++noteFx;
        case R.string.effect2:
          ++noteFx;
        case R.string.effect1:
          finalNoteFx = noteFx;

          mych_fx = (MyCardHolder) mView.findViewHolderForItemId(id);
          mych_fx.select();

          new Thread(new Runnable() {
            @Override
            public void run() {
              Log.d("DEBUG", "note on " + finalNoteFx);
              memeMIDI.sendNote(midiChannel, finalNoteFx, 127);

              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              } finally {
                Log.d("DEBUG", "note off " + finalNoteFx);
                memeMIDI.sendNote(midiChannel, finalNoteFx, 0);

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    mych_fx.reset();
                  }
                });
              }
            }
          }).start();
          break;
      }

      note = 60;
      switch (id) {
        /*
        case R.string.logo5:
          ++note;
        case R.string.logo4:
          ++note;
         */
        case R.string.logo3:
          ++note;
        case R.string.logo2:
          ++note;
        case R.string.logo1:
          final int finalNote = note;

          final MyCardHolder mych_lg = (MyCardHolder) mView.findViewHolderForItemId(id);
          mych_lg.select();

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
                    mych_lg.reset();
                  }
                });
              }
            }
          }).start();
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

    //memeBTSPP.sendAccel(accelX, accelY, accelZ);
    //memeBTSPP.sendAngle(yaw, pitch, roll);

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

      //memeBTSPP.sendEyeBlink(eyeBlinkStrength, eyeBlinkSpeed);
    }

    if (eyeUp > 0 || eyeDown > 0 || eyeLeft > 0 || eyeRight > 0) {
      Log.d("EYE", String
          .format("meme: UP = %d, DOWN = %d, LEFT = %d, RIGHT = %d", eyeUp, eyeDown, eyeLeft,
              eyeRight));

      //memeBTSPP.sendEyeMove(eyeUp, eyeDown, eyeLeft, eyeRight);
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

    private final int CARD_TYPE_ONLY_TITLE = 0;
    private final int CARD_TYPE_LOGO_TITLE = 1;
    private final int CARD_TYPE_ONLY_LOGO  = 2;

    @Override
    public CardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
      switch (card_type) {
        case CARD_TYPE_LOGO_TITLE:
          return new MyCardHolder(mInflater.inflate(R.layout.card_vdj, parent, false));
        default:
          return new MyCardHolder(mInflater.inflate(R.layout.card_default, parent, false));
      }
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      switch (id) {
        case R.string.track14:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("1-4");
          break;
        case R.string.track58:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("5-8");
          break;
        case R.string.track1:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track1);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track2:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track2);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track3:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track3);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track4:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track4);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track5:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track5);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track6:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track6);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track7:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track7);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.track8:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track8);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect1:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect1);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect2:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect2);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect3:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect3);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect4:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect4);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect5:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect5);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.effect6:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect6);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.logo:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.logo1:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo1);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.logo2:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo2);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.logo3:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo3);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        default:
          //((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
      }
    }

    @Override
    public CardFunction getCardFunction(int id) {
      switch (id) {
        case R.string.back:
          return CardFunction.BACK;
        case R.string.hue:
        case R.string.vdj:
        case R.string.track14:
        case R.string.track58:
        case R.string.effect:
        case R.string.logo:
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
              id = R.string.camera;
              break;
            case 1:
              id = R.string.hue;
              break;
            case 2:
              id = R.string.vdj;
              break;
          }
          currentSelectedItem = id;
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
          currentSelectedItem = id;
          break;
        case R.string.vdj:
          switch (position) {
            case 0:
              id = R.string.track14;
              break;
            case 1:
              id = R.string.track58;
              break;
            case 2:
              id = R.string.effect;
              break;
            case 3:
              id = R.string.logo;
              break;
          }
          break;
        case R.string.track14:
          switch (position) {
            case 0:
              id = R.string.track1;
              break;
            case 1:
              id = R.string.track2;
              break;
            case 2:
              id = R.string.track3;
              break;
            case 3:
              id = R.string.track4;
              break;
          }
          currentSelectedItem = id;
          break;
        case R.string.track58:
          switch (position) {
            case 0:
              id = R.string.track5;
              break;
            case 1:
              id = R.string.track6;
              break;
            case 2:
              id = R.string.track7;
              break;
            case 3:
              id = R.string.track8;
              break;
          }
          currentSelectedItem = id;
          break;
        case R.string.effect:
          switch (position) {
            case 0:
              id = R.string.effect1;
              break;
            case 1:
              id = R.string.effect2;
              break;
            case 2:
              id = R.string.effect3;
              break;
            case 3:
              id = R.string.effect4;
              break;
            case 4:
              id = R.string.effect5;
              break;
            case 5:
              id = R.string.effect6;
              break;
          }
          currentSelectedItem = id;
          break;
        case R.string.logo:
          switch (position) {
            case 0:
              id = R.string.logo1;
              break;
            case 1:
              id = R.string.logo2;
              break;
            case 2:
              id = R.string.logo3;
              break;
          }
          currentSelectedItem = id;
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
        case R.string.hue:
          return 5;
        case R.string.vdj:
          return 4;
        case R.string.track14:
          return 4;
        case R.string.track58:
          return 4;
        case R.string.effect:
          return 6;
        case R.string.logo:
          return 3;
        case NO_ID:
          return 3;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      switch (id) {
        case R.string.track1:
        case R.string.track14:
        case R.string.track2:
        case R.string.track3:
        case R.string.track4:
        case R.string.track5:
        case R.string.track58:
        case R.string.track6:
        case R.string.track7:
        case R.string.track8:
        case R.string.effect:
        case R.string.effect1:
        case R.string.effect2:
        case R.string.effect3:
        case R.string.effect4:
        case R.string.effect5:
        case R.string.effect6:
        case R.string.logo:
        case R.string.logo1:
        case R.string.logo2:
        case R.string.logo3:
          return CARD_TYPE_LOGO_TITLE;
        default:
          return CARD_TYPE_ONLY_TITLE;
      }
    }
  }

  private class MyCardHolder extends CardHolder {

    ImageView mImageView;
    TextView mTitle;
    TextView mSubtitle;
    TextView mValue;

    MyCardHolder(View itemView) {
      super(itemView);
      mImageView = (ImageView) itemView.findViewById(R.id.funcicon);
      mTitle = (TextView) itemView.findViewById(R.id.card_text);
      mSubtitle = (TextView) itemView.findViewById(R.id.card_subtext);
      mValue = (TextView) itemView.findViewById(R.id.card_select);
    }

    void select() {
      //mValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
      mValue.setText(getString(R.string.selected));
    }

    void pause() {
      //mValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
      mValue.setText(getString(R.string.pause));
    }

    void reset() {
      //mValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
      mValue.setText(" ");
    }
  }
}