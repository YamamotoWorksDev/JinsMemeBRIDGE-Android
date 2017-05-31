package com.jins_meme.bridge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.jins_jp.meme.MemeRealtimeListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragmentBase extends Fragment implements MemeRealtimeDataFilter.MemeFilteredDataCallback {

  protected BridgeUIView mView = null;

  public MenuFragmentBase() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mView = new BridgeUIView(getContext());
    mView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT));
    return mView;
  }

  @Override
  public void onMemeBlinked() {
    mView.enter();
  }

  @Override
  public void onMemeMoveLeft() {
    mView.move(-1);
  }

  @Override
  public void onMemeMoveRight() {
    mView.move(1);
  }

}
