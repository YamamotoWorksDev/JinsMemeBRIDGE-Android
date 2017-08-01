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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VDJMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class VDJMenuFragment extends MenuFragmentBase implements IResultListener {

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();
  private MemeMIDI memeMIDI;
  private MemeOSC memeOSC;

  private int midiChannel = 1;
  private int lastNote = 0;

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
    Log.d("DEBUG", "VDJ:: onEnterCard " + id);

    moveToFit();
  }

  @Override
  public void onExitCard(int id) {
    Log.d("DEBUG", "VDJ:: onExitCard " + id);

    moveToFit();
  }

  @Override
  public void onEndCardSelected(int id) {
    final CardAdapter.MyCardHolder mych = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);

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

        mych.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            //if (finalNote != lastNote) {
              Log.d("DEBUG", "note on " + finalNote);
              memeMIDI.sendNote(midiChannel, finalNote, 127);

              memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_track));
              memeOSC.setTypeTag("ii");
              memeOSC.addArgument(midiChannel);
              memeOSC.addArgument(finalNote);
              memeOSC.flushMessage();
            //}

            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } finally {
              //if (finalNote != lastNote) {
                Log.d("DEBUG", "note off " + finalNote);
                memeMIDI.sendNote(midiChannel, finalNote, 0);
                lastNote = finalNote;
              //}

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
      ///* MFT
      case R.string.effect6:
        ++noteFx;
      case R.string.effect5:
        ++noteFx;
        //*/
      case R.string.effect4:
        ++noteFx;
      case R.string.effect3:
        ++noteFx;
      case R.string.effect2:
        ++noteFx;
      case R.string.effect1:
        finalNoteFx = noteFx;

        mych_fx = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
        mych_fx.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            Log.d("DEBUG", "note on " + finalNoteFx);
            memeMIDI.sendNote(midiChannel, finalNoteFx, 127);
            memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_effect));
            memeOSC.setTypeTag("ii");
            memeOSC.addArgument(midiChannel);
            memeOSC.addArgument(finalNoteFx);
            memeOSC.flushMessage();

            try {
              Thread.sleep(500);
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
      case R.string.logo4:
        ++note;
      case R.string.logo3:
        ++note;
      case R.string.logo2:
        ++note;
      case R.string.logo1:
        final int finalNote = note;

        final CardAdapter.MyCardHolder mych_lg = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
        mych_lg.setText(getString(R.string.selected));

        new Thread(new Runnable() {
          @Override
          public void run() {
            Log.d("DEBUG", "note on " + finalNote);
            memeMIDI.sendNote(midiChannel, finalNote, 127);
            memeOSC.setAddress(getString(R.string.osc_prefix), getString(R.string.osc_logo));
            memeOSC.setTypeTag("ii");
            memeOSC.addArgument(midiChannel);
            memeOSC.addArgument(finalNote);
            memeOSC.flushMessage();

            try {
              Thread.sleep(500);
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
      ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
      ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));

      ((MyCardHolder) cardHolder).mSubtitle.setText("");

      switch (id) {
        case R.string.track14:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track);
          break;
        case R.string.track58:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track);
          break;
        case R.string.track1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track1);
          break;
        case R.string.track2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track2);
          break;
        case R.string.track3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track3);
          break;
        case R.string.track4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track4);
          break;
        case R.string.track5:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track5);
          break;
        case R.string.track6:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track6);
          break;
        case R.string.track7:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track7);
          break;
        case R.string.track8:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.track8);
          break;
        case R.string.effect:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect);
          break;
        case R.string.effect1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect1);
          break;
        case R.string.effect2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect2);
          break;
        case R.string.effect3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect3);
          break;
        case R.string.effect4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect4);
          break;
        ///* MFT
        case R.string.effect5:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect5);
          break;
        case R.string.effect6:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.effect6);
          break;
        case R.string.logo:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo);
          break;
        case R.string.logo1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo1);
          break;
        case R.string.logo2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo2);
          break;
        case R.string.logo3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo3);
          break;
        case R.string.logo4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.logo4);
          break;
          //*/
      }

      ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
      ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
      ((MyCardHolder) cardHolder).mSubtitle.setText("");
      ((MyCardHolder) cardHolder).mSubtitle.setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
      ((MyCardHolder) cardHolder).mValue.setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
    }

    @Override
    public CardFunction getCardFunction(int id) {
      switch (id) {
        case R.string.back:
          return CardFunction.BACK;
        case R.string.track14:
        case R.string.track58:
        case R.string.effect:
        case R.string.logo: // MFT comment out
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
        case R.string.effect:
          switch (position) {
            case 2:
              id = R.string.effect1;
              break;
            case 3:
              id = R.string.effect2;
              break;
            case 4:
              id = R.string.effect3;
              break;
            case 5:
              id = R.string.effect4;
              break;
            ///* MFT
            case 0:
              id = R.string.effect5;
              break;
            case 1:
              id = R.string.effect6;
              break;
              //*/
          }
          break;
        ///* MFT
        case R.string.logo:
          switch (position) {
            case 3:
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
          }
          break;
          //*/
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

      }
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case NO_ID:
          //return 3;// MFT
        case R.string.logo: // MFT comment out
        case R.string.track14:
        case R.string.track58:
          return 4;
        case R.string.effect:
          return 6;// MFT
          //return 4;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return getResources().getInteger(R.integer.CARD_TYPE_LOGO_TITLE);
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

}