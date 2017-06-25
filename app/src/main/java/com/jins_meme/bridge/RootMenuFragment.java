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
 * {@link RootMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RootMenuFragment extends MenuFragmentBase implements IResultListener {

  private OnFragmentInteractionListener mListener;

  public RootMenuFragment() {
    // Required empty public constructor
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

  @Override
  protected Adapter createAdapter() {
    return new CardAdapter(getContext(), this);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
  }

  public interface OnFragmentInteractionListener {
    void openNextMenu(int card_id);
  }

  @Override
  public void onEnterCard(int id) {
  }

  @Override
  public void onExitCard(int id) {

  }

  @Override
  public void onEndCardSelected(int id) {
    mListener.openNextMenu(id);
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

      ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
      ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
      ((MyCardHolder) cardHolder).mSubtitle.setText("");

    }

    @Override
    public CardFunction getCardFunction(int id) {
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
              id = R.string.spotify;
              break;
            case 2:
              id = R.string.remo;
              break;
            case 3:
              id = R.string.hue;
              break;
            case 4:
              id = R.string.vdj;
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
          return 5;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return getResources().getInteger(R.integer.CARD_TYPE_ONLY_TITLE);
      //return CARD_TYPE_ONLY_TITLE;
    }

    private class MyCardHolder extends CardHolder {

      ImageView mImageView;
      TextView mTitle;
      TextView mSubtitle;
      TextView mValue;
      Handler mHandler = new Handler();

      MyCardHolder(View itemView) {
        super(itemView);

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
