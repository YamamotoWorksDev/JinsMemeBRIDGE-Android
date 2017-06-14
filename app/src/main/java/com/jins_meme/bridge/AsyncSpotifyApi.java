package com.jins_meme.bridge;

import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.FeaturedPlaylists;
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
        Pager<PlaylistSimple> upPager = mSpotifyService.getMyPlaylists();
        Log.d("DEBUG", "ASYNC SPOTIFY:: My Playlist -> " + upPager.total);
        for (int i = 0; i < upPager.total; i++) {
          List<PlaylistSimple> list = upPager.items;
          Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
        }
        break;
      case "featured_playlist":
        FeaturedPlaylists featuredPlaylists = mSpotifyService.getFeaturedPlaylists();
        Pager<PlaylistSimple> fpPager = featuredPlaylists.playlists;
        Log.d("DEBUG", "ASYNC SPOTIFY:: Featured Playlist -> " + fpPager.total);
        for (int i = 0; i < fpPager.total; i++) {
          List<PlaylistSimple> list = fpPager.items;
          Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
        }
        break;
      case "search_artist":
        ArtistsPager artistsPager = mSpotifyService.searchArtists(strings[1]);
        Pager<Artist> aPager =  artistsPager.artists;
        Log.d("DEBUG", "ASYNC SPOTIFY:: Searched Artist -> " + strings[1] + " " + aPager.total);
        for (int i = 0; i < aPager.total; i++) {
          List<Artist> list = aPager.items;
          Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
        }
        break;
      case "search_album":
        AlbumsPager albumsPager = mSpotifyService.searchAlbums(strings[1]);
        Pager<AlbumSimple> albPager =  albumsPager.albums;
        Log.d("DEBUG", "ASYNC SPOTIFY:: Searched Album -> " + albPager.total);
        for (int i = 0; i < albPager.total; i++) {
          List<AlbumSimple> list = albPager.items;
          Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
        }
        break;
    }

    return null;
  }
}
