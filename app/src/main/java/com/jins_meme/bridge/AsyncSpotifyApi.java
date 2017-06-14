package com.jins_meme.bridge;

import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * Created by shun on 2017/06/14.
 */

public class AsyncSpotifyApi extends AsyncTask<String, String, String> {

  //private Activity mainActivity;

  private SpotifyApi mSpotifyApi = new SpotifyApi();
  private SpotifyService mSpotifyService;

  public AsyncSpotifyApi(String accessToken) {
    //this.mainActivity = activity;

    mSpotifyApi.setAccessToken(accessToken);
    mSpotifyService = mSpotifyApi.getService();
  }

  @Override
  protected String doInBackground(String... strings) {
    Log.d("DEBUG", "ASYNC SPOTIFY:: " + strings.length);

    switch (strings[0]) {
      case "me":
        String country = mSpotifyService.getMe().country;
        Log.d("DEBUG", "ASYNC SPOTIFY:: Country -> " + country);
        break;
      case "user_playlist":
        Pager<PlaylistSimple> pager = mSpotifyService.getMyPlaylists();
        Log.d("DEBUG", "ASYNC SPOTIFY:: My Playlist -> " + pager.total);
        for (int i = 0; i < pager.total; i++) {
          List<PlaylistSimple> list = pager.items;
          Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(0).name + " " + list.get(0).uri);
        }
        break;
      case "search_artist":
        ArtistsPager artistsPager = mSpotifyService.searchArtists("tokimonsta");
        Log.d("DEBUG", "ASYNC SPOTIFY:: Artists -> " + artistsPager.toString());
        break;
    }

    return null;
  }
}
