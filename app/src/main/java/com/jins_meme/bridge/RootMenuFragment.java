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
import android.widget.Switch;
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

    Log.d("DEBUG", "ROOT:: onAttach");

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

    Log.d("DEBUG", "ROOT:: onDetach");

    mListener = null;
  }

  @Override
  protected Adapter createAdapter() {
    return new CardAdapter(getContext(), this);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("DEBUG", "ROOT:: onViewCreated");

    ((MainActivity) getActivity()).changeMainBackgroud(R.color.no4);
    ((MainActivity) getActivity()).updateActionBar(getString(R.string.actionbar_title), false);
    //((MainActivity) getActivity()).updateActionBarLogo(false);
    ((MainActivity) getActivity()).setIsCameraMenuFragment(false);

    ((MainActivity) getActivity()).changeSettingButton(false);
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
      switch (id) {
        case R.string.camera:
          ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
          ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.no3));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_camera);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.spotify:
          ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
          ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.spotify));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_spotify);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.remo:
          ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
          ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.remo));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_remo);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.hue:
          ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
          ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.hue));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_hue);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        case R.string.vdj:
          ((MyCardHolder) cardHolder).mCardView.setCardBackgroundColor(Color.WHITE);
          ((MyCardHolder) cardHolder).mTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.eyevdj));
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_eyevdj);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
        /*
        default:
          ((MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
          ((MyCardHolder) cardHolder).mTitle.setText(getResources().getString(id));
          ((MyCardHolder) cardHolder).mSubtitle.setText("");
          break;
          */
      }
    }

    @Override
    public CardFunction getCardFunction(int id) {
      return CardFunction.END;
    }

    @Override
    public int getCardId(int parent_id, int position) {
      Log.d("DEBUG", "ROOT:: getCardId");

      int id = NO_ID;

      switch (parent_id) {
        case NO_ID:
          switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
              id = ((MainActivity) getActivity()).getRootCardId(position);
              break;
          }
          break;
      }

      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      Log.d("DEBUG", "ROOT:: getChildCardCount");

      switch (parent_id) {
        case NO_ID:
          return ((MainActivity) getActivity()).getEnabledCardNum();
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return getResources().getInteger(R.integer.CARD_TYPE_ONLY_TITLE);
      //return CARD_TYPE_ONLY_TITLE;
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
