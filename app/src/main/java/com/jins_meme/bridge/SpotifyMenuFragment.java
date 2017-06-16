/**
 * SpotifyMenuFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationRequest.Builder;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.AuthenticationResponse.Type;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Player.OperationCallback;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class SpotifyMenuFragment extends MenuFragmentBase implements IResultListener,
    SpotifyPlayer.NotificationCallback,
    ConnectionStateCallback {

  private static final int REQUEST_CODE = 1337;

  private OnFragmentInteractionListener mListener;
  private Handler mHandler = new Handler();

  private Player mPlayer;
  private PlaybackState mCurrentPlaybackState;
  private BroadcastReceiver mNetworkStateReceiver;
  //private AsyncSpotifyApi mAsyncSpotifyApi;

  private final Player.OperationCallback mOperationCallback = new OperationCallback() {
    @Override
    public void onSuccess() {
      Log.d("DEBUG", "SPOTIFY:: OperationCallback -> onSuccess");
    }

    @Override
    public void onError(Error error) {
      Log.d("DEBUG", "SPOTIFY:: OperationCallback -> onError:" + error);
    }
  };

  public SpotifyMenuFragment() {
    // Required empty public constructor
  }

  IntentFilter getFilter() {
    return (new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  void authenticate() {
    //Log.d("DEBUG", "SPOTIFY:: authenticate " + getRedirectUri().toString());

    AuthenticationRequest.Builder builder = new Builder(getString(R.string.spotify_client_id), Type.TOKEN,
        "jins-meme-bridge-login://callback");
    //builder.setShowDialog(false).setScopes(new String[]{"user-read-email"});
    builder.setShowDialog(false).setScopes(
        new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"});
    final AuthenticationRequest request = builder.build();

    AuthenticationClient.openLoginActivity(getActivity(), REQUEST_CODE, request);
  }

  private void onAuthenticationComplete(AuthenticationResponse authResponse, String clientID) {
    // Once we have obtained an authorization token, we can proceed with creating a Player.
    Log.d("DEBUG", "Got authentication token");
    if (mPlayer == null) {
      Config playerConfig = new Config(getContext(), authResponse.getAccessToken(), clientID);
      // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
      // the second argument in order to refcount it properly. Note that the method
      // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
      // one passed in here. If you pass different instances to Spotify.getPlayer() and
      // Spotify.destroyPlayer(), that will definitely result in resource leaks.
      mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
        @Override
        public void onInitialized(SpotifyPlayer player) {
          Log.d("DEBUG", "-- Player initialized --");
          mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(getContext()));
          //mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(getActivity().getApplicationContext()));
          mPlayer.addNotificationCallback(SpotifyMenuFragment.this);
          mPlayer.addConnectionStateCallback(SpotifyMenuFragment.this);
          // Trigger UI refresh
          //updateView();
        }

        @Override
        public void onError(Throwable error) {
          Log.d("DEBUG", "Error in initialization: " + error.getMessage());
        }
      });
    } else {
      mPlayer.login(authResponse.getAccessToken());
    }
  }

  private Connectivity getNetworkConnectivity(Context context) {
    ConnectivityManager connectivityManager;
    connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    if (activeNetwork != null && activeNetwork.isConnected()) {
      return Connectivity.fromNetworkType(activeNetwork.getType());
    } else {
      return Connectivity.OFFLINE;
    }
  }

  @Override
  public void onConnectionMessage(String s) {
    Log.d("DEBUG", "SPOTIFY:: Received connection message: " + s);
  }

  @Override
  public void onLoggedIn() {
    Log.d("DEBUG", "SPOTIFY:: User logged in.");

    //mAsyncSpotifyApi.execute("me");
    //mAsyncSpotifyApi.execute("search_artist", "hoge");
    //mAsyncSpotifyApi.execute("search_album", "hoge");
    //mAsyncSpotifyApi.execute("user_playlist");
    //mAsyncSpotifyApi.execute("featured_playlist");
  }

  @Override
  public void onLoggedOut() {
    Log.d("DEBUG", "SPOTIFY:: User logged out.");
  }

  @Override
  public void onLoginFailed(Error error) {
    Log.d("DEBUG", "SPOTIFY:: Loggin failed.");
  }

  @Override
  public void onPlaybackEvent(PlayerEvent playerEvent) {

  }

  @Override
  public void onPlaybackError(Error error) {

  }

  @Override
  public void onTemporaryError() {
    Log.d("DEBUG", "SPOTIFY:: Temporary error occurred.");
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    Log.d("DEBUG", "SPOTIFY:: onViewCreated");

    super.onViewCreated(view, savedInstanceState);

    CardAdapter myAdapter = new CardAdapter(getContext(), this);
    mView.setAdapter(myAdapter);

    //mSpotify = new SpotifyController();
    //mSpotify.authenticate((MainActivity) getActivity(), getString(R.string.spotify_client_id));

    mNetworkStateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (mPlayer != null) {
          Connectivity connectivity = getNetworkConnectivity(context);
          mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
        }
      }
    };

    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    getActivity().registerReceiver(mNetworkStateReceiver, filter);

    authenticate();
  }

  public void destroy() {
    //if (mSpotify != null) {
    //  mSpotify = null;
    //}
    if(mPlayer != null) {
      mPlayer.destroy();
    }



    getActivity().unregisterReceiver(mNetworkStateReceiver);

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
    String uri = null;
    final CardAdapter.MyCardHolder mych = (CardAdapter.MyCardHolder) mView.findViewHolderForItemId(id);
    switch (id) {
      case R.string.playlist1:
        uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI1");
        break;
      case R.string.playlist2:
        uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI2");
        break;
      case R.string.playlist3:
        uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI3");
        break;
      case R.string.playlist4:
        uri = ((MainActivity) getActivity()).getSavedValue("SPOTIFY_PL_URI4");
        break;
    }

    if (uri != null) {
      mPlayer.setShuffle(mOperationCallback, true);
      if (!mPlayer.getPlaybackState().isPlaying) {
        mPlayer.playUri(mOperationCallback, uri, 0, 0);
      } else {
        mPlayer.pause(mOperationCallback);
      }
    }

    mych.setText(getString(R.string.selected), 300);
  }

  @Override
  public void onEnterCard(int id) {

  }

  @Override
  public void onExitCard(int id) {
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

  String processRequestToken(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
      //AsyncSpotifyApi.setAccessToken(response.getAccessToken());
      SpotifyConfigFragment.setAccessToken(response.getAccessToken());

      switch (response.getType()) {
        case TOKEN:
          Log.d("DEBUG", "Spotify Token: " + response.getAccessToken());

          onAuthenticationComplete(response, getString(R.string.spotify_client_id));
          //mAsyncSpotifyApi = new AsyncSpotifyApi(AsyncSpotifyApi.getAccessToken());
          SpotifyConfigFragment.setIsLoggedIn(true);
          break;
        case ERROR:
          Log.d("DEBUG", "Spotify Error: " + response.getError());
          break;
        default:
          Log.d("DEBUG", "Spotify Other: " + response.getState());
          break;
      }
    }

    //return AsyncSpotifyApi.getAccessToken();
    return SpotifyConfigFragment.getAccessToken();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("DEBUG", "SPOTIFY:: onActivityResult");


  }
}
