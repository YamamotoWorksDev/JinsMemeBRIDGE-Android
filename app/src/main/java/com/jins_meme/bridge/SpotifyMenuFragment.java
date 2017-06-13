/**
 * MenuFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;

public class SpotifyMenuFragment extends MenuFragmentBase implements IResultListener {

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();
  private SpotifyController mSpotify;

  public SpotifyMenuFragment() {
    // Required empty public constructor
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Log.d("DEBUG", "SPOTIFY:: onViewCreated");

    super.onViewCreated(view, savedInstanceState);

    CardAdapter myAdapter = new CardAdapter(getContext(), this);
    mView.setAdapter(myAdapter);

    mSpotify = new SpotifyController();
  }

  public void destroy() {
    if (mSpotify != null) {
      //mSpotify.turnOff();
      mSpotify = null;
    }

    Log.d("FRAGMENT", "onDestroy...");
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
    Log.d("DEBUG", "SPOTIFY:: onAttach");

    super.onAttach(context);
    if (context instanceof HueMenuFragment.OnFragmentInteractionListener) {
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
  public void onEndCardSelected(int id) {

  }

  @Override
  public void onEnterCard(int id) {

  }

  @Override
  public void onExitCard(int id) {
    mListener.backToPreviousMenu();
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
      return new CardAdapter.MyCardHolder(mInflater.inflate(R.layout.card_default, parent, false));
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
      ((CardAdapter.MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
      ((CardAdapter.MyCardHolder) cardHolder).mSubtitle.setText("");
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
          id = R.string.playlist1;
          break;
        case 1:
          id = R.string.playlist2;
          break;
        case 2:
          id = R.string.playlist3;
          break;
        case 3:
          id = R.string.playlist4;
          break;
      }
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case NO_ID:
          return 4;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return getResources().getInteger(R.integer.CARD_TYPE_LOGO_TITLE);
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
