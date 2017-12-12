/**
 * MenuFragment.java
 *
 * Copyright (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.jins_meme.bridge.BridgeUIView.Adapter;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VDJMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class VDJMenuFragment extends MenuFragmentBase {

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();
  private MemeMIDI memeMIDI;
  private MemeOSC memeOSC;

  private final int noteInterval = 300;

  private int midiChannel = 1;
  private int lastNote = 0;

  private int rPrev = -1;
  private int rSameCount = 0;

  public VDJMenuFragment() {
    // Required empty public constructor
  }

  @Override
  protected Adapter createAdapter() {
    return new CardAdapter(getContext(), this);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Log.d("DEBUG", "VDJ:: onViewCreated");

    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).changeMainBackgroud(R.color.no4);
    ((MainActivity) getActivity()).updateActionBar(getString(R.string.actionbar_title), false);
    ((MainActivity) getActivity()).changeSettingButton(false);

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
  }

  public void destroy() {
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
  public void onDestroyView() {
    super.onDestroyView();

    super.onDestroy();
    this.destroy();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.destroy();
  }

  @Override
  public void onAttach(Context context) {
    Log.d("DEBUG", "VDJ:: onAttach");

    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  interface OnFragmentInteractionListener {
    void backToPreviousMenu();
  }

  @Override
  protected SharedPreferences getPreferences() {
    return getContext().getSharedPreferences("vdj_menu", Context.MODE_PRIVATE);
  }

  @Override
  public void onEnterCard(int id) {
    super.onEnterCard(id);
    Log.d("DEBUG", "VDJ:: onEnterCard " + id);
  }

  @Override
  public void onExitCard(int id) {
    super.onExitCard(id);
    Log.d("DEBUG", "VDJ:: onExitCard " + id);
  }

  @Override
  public void onEndCardSelected(int id) {
    super.onEndCardSelected(id);
    final CardAdapter.MyCardHolder mych = (CardAdapter.MyCardHolder) mView
        .findViewHolderForItemId(id);

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
        //final int finalNote = note > 27 ? note + 8 : note;
        final int finalNote = note;

        mych.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            if (finalNote != lastNote) {
              Log.d("DEBUG", "note on " + finalNote);
              memeMIDI.sendNote(midiChannel, finalNote, 127);

              memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_track));
              memeOSC.setTypeTag("i");
              memeOSC.addArgument(finalNote - 23);
              memeOSC.flushMessage();
            }

            try {
              Thread.sleep(noteInterval);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              if (finalNote != lastNote) {
                Log.d("DEBUG", "note off " + finalNote);
                memeMIDI.sendNote(midiChannel, finalNote, 0);
                lastNote = finalNote;
              }

              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  mych.clearText();
                }
              });
            }
          }
        }).start();
        break;
    }

    int noteFx = 48;
    final int finalNoteFx;
    final CardAdapter.MyCardHolder mych_fx;

    switch (id) {
      /*
      case R.string.effect7:
        ++noteFx;
      case R.string.effect6:
        ++noteFx;
      case R.string.effect5:
        ++noteFx;
      case R.string.effect4:
        ++noteFx;
        */
      case R.string.effect3:
        //++noteFx;
      case R.string.effect2:
        //++noteFx;
      case R.string.effect1:
        Random rand = new Random();
        double rnd = rand.nextDouble();
        double rnd1 = 0;
        switch (rPrev) {
          case -1:
            rnd1 = rnd;
            break;
          case 0:
            rnd1 = rand3(rnd, 0.5, 1.0, 1.0);
            break;
          case 1:
            rnd1 = rand3(rnd, 1.0, 0.5, 1.0);
            break;
          case 2:
            rnd1 = rand3(rnd, 1.0, 1.0, 0.5);
            break;
        }

        Log.d("DEBUG", "random" + rPrev + " = " + rnd + " " + rnd1);

        int r;
        if (rnd1 < 0.3333) {
          r = 0;
        } else if (rnd1 >= 0.3333 && rnd1 < 0.6666) {
          r = 1;
        } else {
          r = 2;
        }
        if (r == rPrev) {
          rSameCount++;
        } else {
          rSameCount = 0;
        }
        rPrev = r;

        switch (id) {
          case R.string.effect1:
            noteFx = r + 48;
            break;
          case R.string.effect2:
            noteFx = r + 51;
            break;
          case R.string.effect3:
            noteFx = r + 54;
            break;
        }

        finalNoteFx = noteFx;

        mych_fx = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
        mych_fx.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            Log.d("DEBUG", "note on " + finalNoteFx);
            memeMIDI.sendNote(midiChannel, finalNoteFx, 127);

            memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_effect));
            memeOSC.setTypeTag("i");
            memeOSC.addArgument(finalNoteFx - 47);
            memeOSC.flushMessage();

            try {
              Thread.sleep(noteInterval);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              Log.d("DEBUG", "note off " + finalNoteFx);
              memeMIDI.sendNote(midiChannel, finalNoteFx, 0);

              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  mych_fx.clearText();
                }
              });
            }
          }
        }).start();
        break;
    }

    ///* MFT
    note = 60;
    switch (id) {
      case R.string.logo9:
        ++note;
      case R.string.logo8:
        ++note;
      case R.string.logo7:
        ++note;
      case R.string.logo6:
        ++note;
      case R.string.logo5:
        ++note;
      case R.string.logo4:
        ++note;
      case R.string.logo3:
        ++note;
      case R.string.logo2:
        ++note;
      case R.string.logo1:
        final int finalNote = note;

        final CardAdapter.MyCardHolder mych_lg = (CardAdapter.MyCardHolder) mView
            .findViewHolderForItemId(id);
        mych_lg.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            Log.d("DEBUG", "note on " + finalNote);
            memeMIDI.sendNote(midiChannel, finalNote, 127);

            memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_logo));
            memeOSC.setTypeTag("i");
            memeOSC.addArgument(finalNote - 59);
            memeOSC.flushMessage();

            try {
              Thread.sleep(noteInterval);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              Log.d("DEBUG", "note off " + finalNote);
              memeMIDI.sendNote(midiChannel, finalNote, 0);

              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  mych_lg.clearText();
                }
              });
            }
          }
        }).start();
        break;
    }
    //*/
  }

  private class CardAdapter extends BridgeUIView.Adapter<BridgeUIView.CardHolder> {

    Context mContext;
    LayoutInflater mInflater;

    CardAdapter(Context context, IResultListener listener) {
      super(listener);
      mContext = context;
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public CardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
      return new MyCardHolder(mInflater.inflate(R.layout.card_default, parent, false));
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      if (((MainActivity) getActivity()).getSavedValue("ENABLE_DARK", true)) {
        ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.no5));
      } else {
        ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.no4));
      }

      switch (id) {
        case R.string.track13:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track);
          break;
        case R.string.track45:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track);
          break;
        case R.string.track67:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track);
          break;
        case R.string.track1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track1);
          break;
        case R.string.track2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track2);
          break;
        case R.string.track3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track3);
          break;
        case R.string.track4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track4);
          break;
        case R.string.track5:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track5);
          break;
        case R.string.track6:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track6);
          break;
        case R.string.track7:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track7);
          break;
        case R.string.effect:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect);
          break;
        case R.string.effect1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect1);
          break;
        case R.string.effect2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect2);
          break;
        case R.string.effect3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect3);
          break;
        case R.string.logo:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo);
          break;
        case R.string.logo1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo1);
          break;
        case R.string.logo2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo2);
          break;
        case R.string.logo3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo3);
          break;
        case R.string.logo4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo4);
          break;
        case R.string.logo5:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo5);
          break;
      }

      if (isAdded()) {
        ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
      } else {
        ((MyCardHolder) cardHolder).mTitle.setText("");
      }
      ((MyCardHolder) cardHolder).mTitle
          .setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
      ((MyCardHolder) cardHolder).mSubtitle.setText("");
      ((MyCardHolder) cardHolder).mSubtitle
          .setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
      ((MyCardHolder) cardHolder).mValue
          .setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
    }

    @Override
    public CardFunction getCardFunction(int id) {
      switch (id) {
        case R.string.back:
          return CardFunction.BACK;
        /*
        case R.string.track14:
        case R.string.track58:
        */
        case R.string.track13:
        case R.string.track45:
        case R.string.track67:
          /*
        case R.string.guiterist:
        case R.string.rapper:
        case R.string.session:
        */
        case R.string.effect:
        case R.string.logo: // MFT comment out
        //case R.string.guiterist:
        //case R.string.rapper:
          return CardFunction.ENTER_MENU;
        default:
          return CardFunction.END;
      }
    }

    @Override
    public int getCardId(int parent_id, int position) {
      int id = NO_ID;
      switch (parent_id) {
        case NO_ID:
          switch (position) {
            // default
            /*
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
            */
            // for MOVE FES 9/9
            case 0:
              id = R.string.track13;
              break;
            case 1:
              id = R.string.track45;
              break;
            case 2:
              id = R.string.track67;
              break;
              /*
            case 2:
              id = R.string.session;
              break;
            case 3:
              id = R.string.rapper;
              break;
              */
            case 3:
              id = R.string.effect;
              break;
            case 4:
              id = R.string.logo;
              break;
          }
          break;
        case R.string.effect:
          switch (position) {
            /*
            case 3:
              id = R.string.effect1;
              break;
            case 4:
              id = R.string.effect2;
              break;
            case 5:
              id = R.string.effect3;
              break;
            case 6:
              id = R.string.effect4;
              break;
            case 0:
              id = R.string.effect5;
              break;
            case 1:
              id = R.string.effect6;
              break;
            case 2:
              id = R.string.effect7;
              break;
              */
            case 0:
              id = R.string.effect1;
              break;
            case 1:
              id = R.string.effect2;
              break;
            case 2:
              id = R.string.effect3;
              break;
          }
          break;
        ///* MFT
        case R.string.logo:
          switch (position) {
            /*
            case 4:
              id = R.string.logo6;
              break;
            case 5:
              id = R.string.logo7;
              break;
            case 6:
              id = R.string.logo8;
              break;
            case 7:
              id = R.string.logo9;
              break;
              */
            case 4:
              id = R.string.logo1;
              break;
            case 0:
              id = R.string.logo2;
              break;
            case 1:
              id = R.string.logo3;
              break;
            case 2:
              id = R.string.logo4;
              break;
            case 3:
              id = R.string.logo5;
              break;
          }
          break;
        //*/
        /*
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
          break;
        case R.string.track58:
          switch (position) {
            case 1:
              id = R.string.track5;
              break;
            case 2:
              id = R.string.track6;
              break;
            case 3:
              id = R.string.track7;
              break;
            case 0:
              id = R.string.track8;
              break;
          }
          break;
        */
        case R.string.track13:
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
          }
          break;
        case R.string.track45:
          switch (position) {
            case 0:
              id = R.string.track4;
              break;
            case 1:
              id = R.string.track5;
              break;
          }
          break;
        case R.string.track67:
          switch (position) {
            case 0:
              id = R.string.track6;
              break;
            case 1:
              id = R.string.track7;
              break;
          }
          break;
        /*
        case R.string.session:
          switch (position) {
            case 0:
              id = R.string.track6;
              break;
            case 1:
              id = R.string.guiterist;
              break;
            case 2:
              id = R.string.rapper;
              break;
          }
          break;
        case R.string.guiterist:
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
            case 3:
              id = R.string.logo4;
              break;
            case 4:
              id = R.string.logo5;
              break;
            case 5:
              id = R.string.logo6;
              break;
            case 6:
              id = R.string.logo7;
              break;
            case 7:
              id = R.string.effect3;
              break;
          }
          break;
        case R.string.rapper:
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
            case 3:
              id = R.string.logo4;
              break;
            case 4:
              id = R.string.logo5;
              break;
            case 5:
              id = R.string.logo6;
              break;
            case 6:
              id = R.string.logo8;
              break;
            case 7:
              id = R.string.logo9;
              break;
          }
          break;
          */
      }
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case NO_ID:
          //return 3;// MFT
          return 5;// MOVE FES 9/9
        case R.string.track13:
          return 3;
        case R.string.track45:
          return 2;
        case R.string.track67:
          return 2;
        //case R.string.session:
        //  return 3;
        case R.string.logo: // MFT comment out
          return 5;
        case R.string.effect:
          return 3;
        //return 4;// MFT
        //case R.string.guiterist:
        //  return 8;
        //case R.string.rapper:
        //  return 8;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      if (isAdded()) {
        return getResources().getInteger(R.integer.CARD_TYPE_LOGO_TITLE);
      } else {
        return 0;
      }
    }

    private class MyCardHolder extends CardHolder {

      CardView mCardView;
      ImageView mImageView;
      TextView mTitle;
      TextView mSubtitle;
      TextView mValue;
      Handler mHandler = new Handler();

      MyCardHolder(View itemView) {
        super(itemView);

        mCardView = (CardView) itemView.findViewById(R.id.card_view);
        mImageView = (ImageView) itemView.findViewById(R.id.funcicon);
        mTitle = (TextView) itemView.findViewById(R.id.card_text);
        mSubtitle = (TextView) itemView.findViewById(R.id.card_subtext);
        mValue = (TextView) itemView.findViewById(R.id.card_select);
      }

      void setText(String text) {
        //mValue.setText(getString(R.string.selected));
        mValue.setText(text);
      }

      void setText(String text, int msec) {
        //mValue.setText(getString(R.string.selected));
        mValue.setText(text);

        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            mValue.setText(" ");
          }
        }, msec);
      }

      void clearText() {
        mValue.setText(" ");
      }
    }

  }

  double rand3(double x, double a, double b, double c) {
    double y = 0.0;
    Random r = new Random();

    if (x < 1.0 / 3.0) {
      if (r.nextDouble() < a) {
        y = x;
      } else {
        y = 2.0 * x + 1.0 / 3.0;
      }
    } else if (x >= 1.0 / 3.0 && x < 2.0 / 3.0) {
      if (r.nextDouble() < b) {
        y = x;
      } else {
        if (x < 1.0 / 2.0) {
          y = 2.0 * x - 2.0 / 3.0;
        } else {
          y = 2.0 * x - 1.0 / 3.0;
        }
        y = 2.0 * x + 1.0 / 3.0;
      }
    } else if (x >= 2.0 / 3.0 && x < 1.0) {
      if (r.nextDouble() < c) {
        y = x;
      } else {
        y = 2.0 * x - 4.0 / 3.0;
      }
    }

    return y;
  }
}