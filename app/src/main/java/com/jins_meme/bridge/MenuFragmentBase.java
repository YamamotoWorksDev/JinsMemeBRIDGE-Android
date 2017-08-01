/**
 * MenuFragmentBase.java
 *
 * Copylight (C) 2017, Nariaki Iwatani(Anno Lab Inc.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.jins_meme.bridge.BridgeUIView.Adapter;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class MenuFragmentBase extends Fragment implements MemeRealtimeDataFilter.MemeFilteredDataCallback {

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
    mView.setAdapter(createAdapter());
    return mView;
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d("DEBUG", "BASE:: onResume");

    ((MainActivity)getActivity()).updateActionBarLogo(((MainActivity) getActivity()).isCameraMenuFragment());

    String id_str = ((Integer)mView.getCurrentParentCardId()).toString();
    SharedPreferences pref = getPreferences();
    if(pref.contains(id_str)) {
      int offset = getPreferences().getInt(id_str, 0);
      mView.setPosition(offset);
    }
    else {
      mView.setToDefaultPosition(0);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    String id_str = ((Integer)mView.getCurrentParentCardId()).toString();
    SharedPreferences.Editor editor = getPreferences().edit();
    editor.putInt(id_str, mView.getCurrentCenteredItemPosition());
    editor.apply();
  }

  public void setTouchEnabled(boolean enabled) {
    mView.setTouchEnabled(enabled);
  }

  protected abstract Adapter createAdapter();

  public boolean menuBack() {
    return mView.back();
  }
  public void menuReset() {
    mView.reset();
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

  public void moveToFit() {
    mView.moveToFit();
  }

  abstract protected SharedPreferences getPreferences();
}
