/**
 * MenuFragment.java
 *
 * Copyright (C) 2017, Nariaki Iwatani(Anno Lab Inc.) and Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jins_meme.bridge.BridgeUIView.Adapter;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VDJMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class VDJMenuFragment extends MenuFragmentBase implements MidiReceiveListener {

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();
  private MemeMIDI memeMIDI;
  private MemeOSC memeOSC;

  private final int noteInterval = 300;

  private int midiChannel = 1;
  private int lastNote = 0;

  private int rPrev = -1;
  private int rSameCount = 0;

  private BidiMap<String, Integer> bidiMap;
  private SparseIntArray trackArray = new SparseIntArray();
  private SparseIntArray effectArray = new SparseIntArray();
  private SparseIntArray logoArray = new SparseIntArray();
  private SparseIntArray subgrpArray = new SparseIntArray();

  private JsonNode vdjRoot;

  public VDJMenuFragment() {
    HashMap<String, Integer> rootMap = new HashMap<>();
    rootMap.put("trackA",  R.string.trackA);
    rootMap.put("trackB",  R.string.trackB);
    rootMap.put("trackC",  R.string.trackC);
    rootMap.put("trackD",  R.string.trackD);
    rootMap.put("trackE",  R.string.trackE);
    rootMap.put("trackF",  R.string.trackF);
    rootMap.put("effectA", R.string.effectA);
    rootMap.put("effectB", R.string.effectB);
    rootMap.put("effectC", R.string.effectC);
    rootMap.put("effectD", R.string.effectD);
    rootMap.put("effectE", R.string.effectE);
    rootMap.put("effectF", R.string.effectF);
    rootMap.put("logoA",   R.string.logoA);
    rootMap.put("logoB",   R.string.logoB);
    rootMap.put("logoC",   R.string.logoC);
    rootMap.put("logoD",   R.string.logoD);
    rootMap.put("logoE",   R.string.logoE);
    rootMap.put("logoF",   R.string.logoF);
    rootMap.put("subgrpA", R.string.subgrpA);
    rootMap.put("subgrpB", R.string.subgrpB);
    rootMap.put("subgrpC", R.string.subgrpC);
    rootMap.put("subgrpD", R.string.subgrpD);
    rootMap.put("subgrpE", R.string.subgrpE);
    rootMap.put("subgrpF", R.string.subgrpF);
    rootMap.put("subgrpG", R.string.subgrpG);
    rootMap.put("subgrpH", R.string.subgrpH);
    rootMap.put("subgrpI", R.string.subgrpI);
    rootMap.put("subgrpJ", R.string.subgrpJ);
    rootMap.put("subgrpK", R.string.subgrpK);
    rootMap.put("subgrpL", R.string.subgrpL);
    rootMap.put("subgrpM", R.string.subgrpM);
    rootMap.put("subgrpN", R.string.subgrpN);
    rootMap.put("subgrpO", R.string.subgrpO);
    rootMap.put("subgrpP", R.string.subgrpP);
    rootMap.put("subgrpQ", R.string.subgrpQ);
    rootMap.put("subgrpR", R.string.subgrpR);
    rootMap.put("track1", R.string.track1);
    rootMap.put("track2", R.string.track2);
    rootMap.put("track3", R.string.track3);
    rootMap.put("track4", R.string.track4);
    rootMap.put("track5", R.string.track5);
    rootMap.put("track6", R.string.track6);
    rootMap.put("track7", R.string.track7);
    rootMap.put("track8", R.string.track8);
    rootMap.put("track9", R.string.track9);
    rootMap.put("track10", R.string.track10);
    rootMap.put("track11", R.string.track11);
    rootMap.put("track12", R.string.track12);
    rootMap.put("track13", R.string.track13);
    rootMap.put("track14", R.string.track14);
    rootMap.put("track15", R.string.track15);
    rootMap.put("track16", R.string.track16);
    rootMap.put("track17", R.string.track17);
    rootMap.put("track18", R.string.track18);
    rootMap.put("effect1", R.string.effect1);
    rootMap.put("effect2", R.string.effect2);
    rootMap.put("effect3", R.string.effect3);
    rootMap.put("effect4", R.string.effect4);
    rootMap.put("effect5", R.string.effect5);
    rootMap.put("effect6", R.string.effect6);
    rootMap.put("effect7", R.string.effect7);
    rootMap.put("effect8", R.string.effect8);
    rootMap.put("effect9", R.string.effect9);
    rootMap.put("effect10", R.string.effect10);
    rootMap.put("effect11", R.string.effect11);
    rootMap.put("effect12", R.string.effect12);
    rootMap.put("effect13", R.string.effect13);
    rootMap.put("effect14", R.string.effect14);
    rootMap.put("effect15", R.string.effect15);
    rootMap.put("effect16", R.string.effect16);
    rootMap.put("logo1", R.string.logo1);
    rootMap.put("logo2", R.string.logo2);
    rootMap.put("logo3", R.string.logo3);
    rootMap.put("logo4", R.string.logo4);
    rootMap.put("logo5", R.string.logo5);
    rootMap.put("logo6", R.string.logo6);
    rootMap.put("logo7", R.string.logo7);
    rootMap.put("logo8", R.string.logo8);
    rootMap.put("logo9", R.string.logo9);
    rootMap.put("logo10", R.string.logo10);
    rootMap.put("logo11", R.string.logo11);
    rootMap.put("logo12", R.string.logo12);
    rootMap.put("logo13", R.string.logo13);
    rootMap.put("logo14", R.string.logo14);
    rootMap.put("logo15", R.string.logo15);
    bidiMap = new DualHashBidiMap<>(rootMap);

    trackArray.append(0, R.string.track1);
    trackArray.append(1, R.string.track2);
    trackArray.append(2, R.string.track3);
    trackArray.append(3, R.string.track4);
    trackArray.append(4, R.string.track5);
    trackArray.append(5, R.string.track6);
    trackArray.append(6, R.string.track7);
    trackArray.append(7, R.string.track8);
    trackArray.append(8, R.string.track9);
    trackArray.append(9, R.string.track10);
    trackArray.append(10, R.string.track11);
    trackArray.append(11, R.string.track12);
    trackArray.append(12, R.string.track13);
    trackArray.append(13, R.string.track14);
    trackArray.append(14, R.string.track15);
    trackArray.append(15, R.string.track16);
    trackArray.append(16, R.string.track17);
    trackArray.append(17, R.string.track18);

    effectArray.append(0, R.string.effect1);
    effectArray.append(1, R.string.effect2);
    effectArray.append(2, R.string.effect3);
    effectArray.append(3, R.string.effect4);
    effectArray.append(4, R.string.effect5);
    effectArray.append(5, R.string.effect6);
    effectArray.append(6, R.string.effect7);
    effectArray.append(7, R.string.effect8);
    effectArray.append(8, R.string.effect9);
    effectArray.append(9, R.string.effect10);
    effectArray.append(10, R.string.effect11);
    effectArray.append(11, R.string.effect12);
    effectArray.append(12, R.string.effect13);
    effectArray.append(13, R.string.effect14);
    effectArray.append(14, R.string.effect15);
    effectArray.append(15, R.string.effect16);

    logoArray.append(0, R.string.logo1);
    logoArray.append(1, R.string.logo2);
    logoArray.append(2, R.string.logo3);
    logoArray.append(3, R.string.logo4);
    logoArray.append(4, R.string.logo5);
    logoArray.append(5, R.string.logo6);
    logoArray.append(6, R.string.logo7);
    logoArray.append(7, R.string.logo8);
    logoArray.append(8, R.string.logo9);
    logoArray.append(9, R.string.logo10);
    logoArray.append(10, R.string.logo11);
    logoArray.append(11, R.string.logo12);
    logoArray.append(12, R.string.logo13);
    logoArray.append(13, R.string.logo14);
    logoArray.append(14, R.string.logo15);

    subgrpArray.append(0, R.string.subgrpA);
    subgrpArray.append(1, R.string.subgrpB);
    subgrpArray.append(2, R.string.subgrpC);
    subgrpArray.append(3, R.string.subgrpD);
    subgrpArray.append(4, R.string.subgrpE);
    subgrpArray.append(5, R.string.subgrpF);
    subgrpArray.append(6, R.string.subgrpG);
    subgrpArray.append(7, R.string.subgrpH);
    subgrpArray.append(8, R.string.subgrpJ);
    subgrpArray.append(9, R.string.subgrpK);
    subgrpArray.append(10, R.string.subgrpL);
    subgrpArray.append(11, R.string.subgrpM);
    subgrpArray.append(12, R.string.subgrpN);
    subgrpArray.append(13, R.string.subgrpO);
    subgrpArray.append(14, R.string.subgrpP);
    subgrpArray.append(15, R.string.subgrpQ);
    subgrpArray.append(16, R.string.subgrpR);
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

    try {
      ObjectMapper mapper = new ObjectMapper();
      InputStream is = ((MainActivity)getActivity()).openLocalorAssets("eye_vdj_structure.json");
      vdjRoot = mapper.readTree(is);
      is.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Initialize MIDI
    memeMIDI = new MemeMIDI(getContext());
    memeMIDI.initPort();
    memeMIDI.setListener(this);

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
      memeMIDI.removeListener();
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
    return getContext().getSharedPreferences("vdj_menu", MODE_PRIVATE);
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
      case R.string.track18:
        ++note;
      case R.string.track17:
        ++note;
      case R.string.track16:
        ++note;
      case R.string.track15:
        ++note;
      case R.string.track14:
        ++note;
      case R.string.track13:
        ++note;
      case R.string.track12:
        ++note;
      case R.string.track11:
        ++note;
      case R.string.track10:
        ++note;
      case R.string.track9:
        ++note;
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
      case R.string.effect16:
        ++noteFx;
      case R.string.effect15:
        ++noteFx;
      case R.string.effect14:
        ++noteFx;
      case R.string.effect13:
        ++noteFx;
      case R.string.effect12:
        ++noteFx;
      case R.string.effect11:
        ++noteFx;
      case R.string.effect10:
        ++noteFx;
      case R.string.effect9:
        ++noteFx;
      case R.string.effect8:
        ++noteFx;
      case R.string.effect7:
        ++noteFx;
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
        ++noteFx;
        /*
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
        */
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

    note = 60;
    switch (id) {
      case R.string.logo15:
        ++note;
      case R.string.logo14:
        ++note;
      case R.string.logo13:
        ++note;
      case R.string.logo12:
        ++note;
      case R.string.logo11:
        ++note;
      case R.string.logo10:
        ++note;
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
        case R.string.trackA:
        case R.string.trackB:
        case R.string.trackC:
        case R.string.trackD:
        case R.string.trackE:
        case R.string.trackF:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track);
          break;
        case R.string.track1:
        case R.string.track2:
        case R.string.track3:
        case R.string.track4:
        case R.string.track5:
        case R.string.track6:
        case R.string.track7:
        case R.string.track8:
        case R.string.track9:
        case R.string.track10:
        case R.string.track11:
        case R.string.track12:
        case R.string.track13:
        case R.string.track14:
        case R.string.track15:
        case R.string.track16:
        case R.string.track17:
        case R.string.track18:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_track);
          break;
        case R.string.effectA:
        case R.string.effectB:
        case R.string.effectC:
        case R.string.effectD:
        case R.string.effectE:
        case R.string.effectF:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect);
          break;
        case R.string.effect1:
        case R.string.effect2:
        case R.string.effect3:
        case R.string.effect4:
        case R.string.effect5:
        case R.string.effect6:
        case R.string.effect7:
        case R.string.effect8:
        case R.string.effect9:
        case R.string.effect10:
        case R.string.effect11:
        case R.string.effect12:
        case R.string.effect13:
        case R.string.effect14:
        case R.string.effect15:
        case R.string.effect16:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_effect);
          break;
        case R.string.logoA:
        case R.string.logoB:
        case R.string.logoC:
        case R.string.logoD:
        case R.string.logoE:
        case R.string.logoF:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo);
          break;
        case R.string.logo1:
        case R.string.logo2:
        case R.string.logo3:
        case R.string.logo4:
        case R.string.logo5:
        case R.string.logo6:
        case R.string.logo7:
        case R.string.logo8:
        case R.string.logo9:
        case R.string.logo10:
        case R.string.logo11:
        case R.string.logo12:
        case R.string.logo13:
        case R.string.logo14:
        case R.string.logo15:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_logo);
          break;
        case R.string.subgrpA:
        case R.string.subgrpB:
        case R.string.subgrpC:
        case R.string.subgrpD:
        case R.string.subgrpE:
        case R.string.subgrpF:
        case R.string.subgrpG:
        case R.string.subgrpH:
        case R.string.subgrpI:
        case R.string.subgrpJ:
        case R.string.subgrpK:
        case R.string.subgrpL:
        case R.string.subgrpM:
        case R.string.subgrpN:
        case R.string.subgrpO:
        case R.string.subgrpP:
        case R.string.subgrpQ:
        case R.string.subgrpR:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
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
        case R.string.trackA:
        case R.string.trackB:
        case R.string.trackC:
        case R.string.trackD:
        case R.string.trackE:
        case R.string.trackF:
        case R.string.effectA:
        case R.string.effectB:
        case R.string.effectC:
        case R.string.effectD:
        case R.string.effectE:
        case R.string.effectF:
        case R.string.logoA:
        case R.string.logoB:
        case R.string.logoC:
        case R.string.logoD:
        case R.string.logoE:
        case R.string.logoF:
        case R.string.subgrpA:
        case R.string.subgrpB:
        case R.string.subgrpC:
        case R.string.subgrpD:
        case R.string.subgrpE:
        case R.string.subgrpF:
        case R.string.subgrpG:
        case R.string.subgrpH:
        case R.string.subgrpI:
        case R.string.subgrpJ:
        case R.string.subgrpK:
        case R.string.subgrpL:
        case R.string.subgrpM:
        case R.string.subgrpN:
        case R.string.subgrpO:
        case R.string.subgrpP:
        case R.string.subgrpQ:
        case R.string.subgrpR:
          return CardFunction.ENTER_MENU;
        default:
          return CardFunction.END;
      }
    }

    @Override
    public int getCardId(int parent_id, int position) {
      int id = NO_ID;
      String parent_name = bidiMap.getKey(parent_id);

      int foundIndex = -1;
      for (int i = 0; i < vdjRoot.get("main").size(); i++) {
        if (parent_name != null && parent_name.equals(vdjRoot.get("main").get(i).get("group").asText())) {
          foundIndex = i;
          break;
        }
      }

      switch (parent_id) {
        case NO_ID:
          if (bidiMap.get(vdjRoot.get("main").get(position).get("group").asText()) != null) {
            id = bidiMap.get(vdjRoot.get("main").get(position).get("group").asText());
          }
          break;
        case R.string.trackA:
        case R.string.trackB:
        case R.string.trackC:
        case R.string.trackD:
        case R.string.trackE:
        case R.string.trackF:
        case R.string.effectA:
        case R.string.effectB:
        case R.string.effectC:
        case R.string.effectD:
        case R.string.effectE:
        case R.string.effectF:
        case R.string.logoA:
        case R.string.logoB:
        case R.string.logoC:
        case R.string.logoD:
        case R.string.logoE:
        case R.string.logoF:
        case R.string.subgrpA:
        case R.string.subgrpB:
        case R.string.subgrpC:
        case R.string.subgrpD:
        case R.string.subgrpE:
        case R.string.subgrpF:
        case R.string.subgrpG:
        case R.string.subgrpH:
        case R.string.subgrpI:
        case R.string.subgrpJ:
        case R.string.subgrpK:
        case R.string.subgrpL:
        case R.string.subgrpM:
        case R.string.subgrpN:
        case R.string.subgrpO:
        case R.string.subgrpP:
        case R.string.subgrpQ:
        case R.string.subgrpR:
          id = bidiMap.get(vdjRoot.get("main").get(foundIndex).get("item").get(position).asText());
          break;
      }
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      String parent_name = bidiMap.getKey(parent_id);

      int foundIndex = -1;
      for (int i = 0; i < vdjRoot.get("main").size(); i++) {
        if (parent_name != null && parent_name.equals(vdjRoot.get("main").get(i).get("group").asText())) {
          foundIndex = i;
          break;
        }
      }

      if (foundIndex >= 0) {
        return vdjRoot.get("main").get(foundIndex).get("item").size();
      } else if (parent_id == NO_ID) {
        return vdjRoot.get("main").size();
      } else {
        return 0;
      }
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

  @Override
  public void onReceiveMidiMessage() {
    Log.d("MIDI", memeMIDI.getMidiType() + " " + memeMIDI.getMidiCh() + " " + memeMIDI.getMidiNum() + " " + memeMIDI.getMidiVal());

    if (memeMIDI.getMidiType() == 0xB0 && memeMIDI.getMidiCh() == 15 && memeMIDI.getMidiNum() == 1 && memeMIDI.getMidiVal() > 0) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          ((MainActivity)getActivity()).cancel(false);
        }
      });

      Log.d("MIDI", "test0");
    } else if (memeMIDI.getMidiType() == 0xB0 && memeMIDI.getMidiCh() == 15 && memeMIDI.getMidiNum() == 2 && memeMIDI.getMidiVal() > 0) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          ((MainActivity)getActivity()).pause();
        }
      });

      Log.d("MIDI", "test1");
    }/* else if (memeMIDI.getMidiType() == 0xB0 && memeMIDI.getMidiCh() == 15 && memeMIDI.getMidiNum() == 3 && memeMIDI.getMidiVal() > 0) {
      Log.d("MIDI", "test2");
    }
    */
  }
}