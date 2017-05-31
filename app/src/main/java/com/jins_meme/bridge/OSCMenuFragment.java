package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OSCMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class OSCMenuFragment extends MenuFragmentBase implements IResultListener {

  private OnFragmentInteractionListener mListener;
  private MemeOSC mOsc;

  public OSCMenuFragment() {
    // Required empty public constructor
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    CardAdapter myAdapter = new CardAdapter(getContext(), this);
    mView.setAdapter(myAdapter);

    // Initialize OSC
    mOsc = new MemeOSC();
    mOsc.setRemoteIP(
        ((MainActivity) getActivity()).getSavedValue("REMOTE_IP", MemeOSC.getRemoteIPv4Address()));
    mOsc.setRemotePort(((MainActivity) getActivity()).getSavedValue("REMOTE_PORT", 10316));
    //mOsc.setHostPort(((MainActivity) getActivity()).getSavedValue("HOST_PORT", 11316));
    mOsc.initSocket();
  }

  public void destroy() {
    if ((mOsc != null)) {
      mOsc.closeSocket();
      mOsc = null;
    }

    Log.d("FRAGMENT", "onDestroy...");
  }
  @Override
  public void onDestroyView() {
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

  public interface OnFragmentInteractionListener {
    void backToPreviousMenu();
  }

  @Override
  public void onEnterCard(int id) {
  }

  @Override
  public void onExitCard(int id) {
    mListener.backToPreviousMenu();
  }

  @Override
  public void onEndCardSelected(int id) {
    final CardAdapter.MyCardHolder mych = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
    switch (id) {
      case R.string.osc_220hz:
        Log.d("DEBUG", "set freq 220");
        mOsc.setAddress(MemeOSC.PREFIX, "/frequency");
        mOsc.setTypeTag("i");
        mOsc.addArgument(220);
        mOsc.flushMessage();

        mych.select(500);
        break;
      case R.string.osc_440hz:
        Log.d("DEBUG", "set freq 440");
        mOsc.setAddress(MemeOSC.PREFIX, "/frequency");
        mOsc.setTypeTag("i");
        mOsc.addArgument(440);
        mOsc.flushMessage();

        mych.select(500);
        break;
      case R.string.osc_mute_on:
        Log.d("DEBUG", "mute on");
        mOsc.setAddress(MemeOSC.PREFIX, "/volume");
        mOsc.setTypeTag("f");
        mOsc.addArgument(0.);
        mOsc.flushMessage();

        mych.select(500);
        break;
      case R.string.osc_mute_off:
        Log.d("DEBUG", "mute off");
        mOsc.setAddress(MemeOSC.PREFIX, "/volume");
        mOsc.setTypeTag("f");
        mOsc.addArgument(1.);
        mOsc.flushMessage();

        mych.select(500);
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

    private final int CATD_TYPE_ONLY_TITLE = 0;

    @Override
    public CardHolder onCreateCardHolder(ViewGroup parent, int card_type) {
      return new MyCardHolder(mInflater.inflate(R.layout.card_sample, parent, false));
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      ((MyCardHolder) (cardHolder)).mTextView.setText(getResources().getString(id));
    }

    @Override
    public CardFunction getCardFunction(int id) {
      switch (id) {
        case R.string.back:
          return CardFunction.BACK;
        default:
          return CardFunction.END;
      }
    }

    @Override
    public int getCardId(int parent_id, int position) {
      int id = NO_ID;
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
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case NO_ID:
          return 5;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return CATD_TYPE_ONLY_TITLE;
    }

    private class MyCardHolder extends CardHolder {

      TextView mTextView;
      TextView mValue;
      Handler mHandler = new Handler();

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

        mHandler.postDelayed(new Runnable() {
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

  }

}