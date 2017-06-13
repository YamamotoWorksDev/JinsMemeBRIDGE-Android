/**
 * MenuFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class SpotifyController implements SpotifyPlayer.NotificationCallback,
    ConnectionStateCallback {

  @Override
  public void onConnectionMessage(String s) {

  }

  @Override
  public void onLoggedIn() {

  }

  @Override
  public void onLoggedOut() {

  }

  @Override
  public void onLoginFailed(Error error) {

  }

  @Override
  public void onPlaybackEvent(PlayerEvent playerEvent) {

  }

  @Override
  public void onPlaybackError(Error error) {

  }

  @Override
  public void onTemporaryError() {

  }
}
