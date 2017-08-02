/**
 * SpotifyMenuFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

public class SpotifyMenuFragment extends MenuFragmentBase {

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();

  private static int currentPlayingPlaylistID = -1;
  private static int prevPlayingPlaylistID = -1;

  public SpotifyMenuFragment() {
    // Required empty public constructor
  }

  @Override
  protected Adapter createAdapter() {
    return new CardAdapter(getContext(), this);
  }

  @Override
  protected SharedPreferences getPreferences() {
    return getContext().getSharedPreferences("spotify_menu", Context.MODE_PRIVATE);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Log.d("DEBUG", "SPOTIFY:: onViewCreated");

    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).changeMainBackgroud(R.color.no4);
    ((MainActivity) getActivity()).changeSettingButton(false);
    ((MainActivity) getActivity()).authenticate();
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d("DEBUG", "SPOTIFY:: onResume");
  }

  @Override
  public void onPause() {
    super.onPause();

    Log.d("DEBUG", "SPOTIFY:: onPause");
  }

  public void destroy() {
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
    super.onEndCardSelected(id);
    final CardAdapter.MyCardHolder mych = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);

    if (((MainActivity) getActivity()).isAuthenticated()) {
      String uri = null;
      switch (id) {
        case R.string.playlist1:
          uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI1");
          currentPlayingPlaylistID = 0;
          break;
        case R.string.playlist2:
          uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI2");
          currentPlayingPlaylistID = 1;
          break;
        case R.string.playlist3:
          uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI3");
          currentPlayingPlaylistID = 2;
          break;
        case R.string.playlist4:
          uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI4");
          currentPlayingPlaylistID = 3;
          break;
      }

      if (uri != null) {
        if (((MainActivity) getActivity()).getSavedValue("SPOTIFY_SHUFFLE", false)) {
          Log.d("DEBUG", "SPOTIFY:: SHUFFLE ON");

          ((MainActivity) getActivity()).setShuffle(true);
        } else {
          Log.d("DEBUG", "SPOTIFY:: SHUFFLE OFF");

          ((MainActivity) getActivity()).setShuffle(false);
        }

        if (currentPlayingPlaylistID == prevPlayingPlaylistID) {
          if (!((MainActivity) getActivity()).isPlaying()) {
            ((MainActivity) getActivity()).setPlayUri(uri);
          } else {
            ((MainActivity) getActivity()).setPause();
          }
        } else {
          ((MainActivity) getActivity()).setPlayUri(uri);
        }
        prevPlayingPlaylistID = currentPlayingPlaylistID;
      }
      mych.setText(getString(R.string.selected), 300);
    } else {
      mych.setText("not authenticated", 300);
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
      return new CardAdapter.MyCardHolder(mInflater.inflate(R.layout.card_default, parent, false));
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      ((CardAdapter.MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);

      switch (id) {
        case R.string.playlist1:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_spotify_playlist1);
          break;
        case R.string.playlist2:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_spotify_playlist2);
          break;
        case R.string.playlist3:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_spotify_playlist3);
          break;
        case R.string.playlist4:
          ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_spotify_playlist4);
          break;
      }

      ((CardAdapter.MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
      ((CardAdapter.MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.spotify));
      ((CardAdapter.MyCardHolder) cardHolder).mSubtitle.setText("");
      ((CardAdapter.MyCardHolder) cardHolder).mSubtitle.setTextColor(ContextCompat.getColor(getContext(), R.color.spotify));
      ((CardAdapter.MyCardHolder) cardHolder).mValue.setTextColor(ContextCompat.getColor(getContext(), R.color.spotify));
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
        mValue.setText(text);
      }

      void setText(String text, int msec) {
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

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("DEBUG", "SPOTIFY:: onActivityResult");


  }
}
