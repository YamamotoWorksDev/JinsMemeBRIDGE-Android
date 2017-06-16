package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.jins_meme.bridge.BridgeUIView.CardHolder;
import com.jins_meme.bridge.BridgeUIView.IResultListener;
import java.util.HashMap;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RemoMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 */
public class RemoMenuFragment extends MenuFragmentBase implements IResultListener {

  private OnFragmentInteractionListener mListener;

  private RemoController remoController;

  private HashMap<String, String> signalNameMap;

  MainActivity mainActivity;

  public RemoMenuFragment() {
    // Required empty public constructor
  }


  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mainActivity = ((MainActivity) getActivity());

    signalNameMap = new HashMap<>();
    signalNameMap.put(getResources().getString(R.string.signal1), "REMO_SIGNAL_1_NAME");
    signalNameMap.put(getResources().getString(R.string.signal2), "REMO_SIGNAL_2_NAME");
    signalNameMap.put(getResources().getString(R.string.signal3), "REMO_SIGNAL_3_NAME");
    signalNameMap.put(getResources().getString(R.string.signal4), "REMO_SIGNAL_4_NAME");
    signalNameMap.put(getResources().getString(R.string.signal5), "REMO_SIGNAL_5_NAME");

    CardAdapter cardAdapter = new CardAdapter(getContext(), this);
    mView.setAdapter(cardAdapter);

    remoController = new RemoController(getContext());

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

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
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
    String address;
    String messages;
    final RemoMenuFragment.CardAdapter.MyCardHolder mych = (RemoMenuFragment.CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
    switch (id) {
      case R.string.signal1:
        address = ((MainActivity) getActivity()).getSavedValue("REMO_DEVICE_ADDRESS");
        messages = ((MainActivity) getActivity()).getSavedValue("REMO_SIGNAL_1");
        remoController.sendMessages(address, messages);
        break;
      case R.string.signal2:
        address = ((MainActivity) getActivity()).getSavedValue("REMO_DEVICE_ADDRESS");
        messages = ((MainActivity) getActivity()).getSavedValue("REMO_SIGNAL_2");
        remoController.sendMessages(address, messages);
        break;
      case R.string.signal3:
        address = ((MainActivity) getActivity()).getSavedValue("REMO_DEVICE_ADDRESS");
        messages = ((MainActivity) getActivity()).getSavedValue("REMO_SIGNAL_3");
        remoController.sendMessages(address, messages);
        break;
      case R.string.signal4:
        address = ((MainActivity) getActivity()).getSavedValue("REMO_DEVICE_ADDRESS");
        messages = ((MainActivity) getActivity()).getSavedValue("REMO_SIGNAL_4");
        remoController.sendMessages(address, messages);
        break;
      case R.string.signal5:
        address = ((MainActivity) getActivity()).getSavedValue("REMO_DEVICE_ADDRESS");
        messages = ((MainActivity) getActivity()).getSavedValue("REMO_SIGNAL_5");
        remoController.sendMessages(address, messages);
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
      return new CardAdapter.MyCardHolder(mInflater.inflate(R.layout.card_default, parent, false));
    }

    @Override
    public void onBindCardHolder(CardHolder cardHolder, int id) {
      String title = getResources().getString(id);
      String subTitle = mainActivity.getSavedValue(signalNameMap.get(title));

      ((CardAdapter.MyCardHolder) cardHolder).mImageView.setImageResource(R.drawable.card_default);
      ((CardAdapter.MyCardHolder) cardHolder).mTitle.setText(title);
      ((CardAdapter.MyCardHolder) cardHolder).mSubtitle.setText(subTitle);
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
          id = R.string.signal1;
          break;
        case 1:
          id = R.string.signal2;
          break;
        case 2:
          id = R.string.signal3;
          break;
        case 3:
          id = R.string.signal4;
          break;
        case 4:
          id = R.string.signal5;
          break;
        case 5:
          id = R.string.back;
          break;
      }
      return id;
    }

    @Override
    public int getChildCardCount(int parent_id) {
      switch (parent_id) {
        case NO_ID:
          return 6;
      }
      return 0;
    }

    @Override
    public int getCardType(int id) {
      return CATD_TYPE_ONLY_TITLE;
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
