package com.jins_meme.bridge;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragmentBase extends Fragment {


  public ConfigFragmentBase() {
    // Required empty public constructor
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).changeSettingButton(false);
  }

  @Override
  public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.config_in);
    if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
      if (enter) {
        return AnimationUtils.loadAnimation(getContext(), R.anim.config_in);
      } else {
        return AnimationUtils.loadAnimation(getContext(), R.anim.config_out2);
      }
    } else if (transit == FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) {
      if (enter) {
        return AnimationUtils.loadAnimation(getContext(), R.anim.config_in);
      } else {
        return AnimationUtils.loadAnimation(getContext(), R.anim.config_out2);
      }
    }

    return super.onCreateAnimation(transit, enter, nextAnim);
  }
}
