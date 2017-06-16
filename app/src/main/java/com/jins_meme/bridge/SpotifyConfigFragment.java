/**
 * SpotifyConfigFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import static com.jins_meme.bridge.R.id.category1;
import static com.jins_meme.bridge.R.id.container;
import static com.jins_meme.bridge.R.id.playlists1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsCursorPager;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.CursorPager;
import kaaes.spotify.webapi.android.models.FeaturedPlaylists;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedAlbum;

public class SpotifyConfigFragment extends ConfigFragmentBase {

  private AsyncSpotifyApi mAsyncSpotifyApi;

  private static boolean isLoggedIn = false;
  private static String accessToken = null;

  private boolean isExecuteFinish = false;
  private boolean isUIInitialized = false;

  private Switch swUse;
  private Switch swShuffle;
  private Spinner[] spCategory = new Spinner[4];
  private Spinner[] spPlaylist = new Spinner[4];

  private ArrayAdapter[] playlistAdapter = new ArrayAdapter[4];

  List<String> userPlaylistNameList = new ArrayList<>();
  List<String> featuredPlaylistNameList = new ArrayList<>();
  List<String> savedAlbumNameList = new ArrayList<>();
  List<String> followedArtistNameList = new ArrayList<>();
  List<String> userPlaylistUriList = new ArrayList<>();
  List<String> featuredPlaylistUriList = new ArrayList<>();
  List<String> savedAlbumUriList = new ArrayList<>();
  List<String> followedArtistUriList = new ArrayList<>();

  public static boolean isLoggedIn() {
    return isLoggedIn;
  }

  public static void setIsLoggedIn(boolean isLoggedIn) {
    SpotifyConfigFragment.isLoggedIn = isLoggedIn;
  }

  public static String getAccessToken() {
    return accessToken;
  }

  public static void setAccessToken(String accessToken) {
    SpotifyConfigFragment.accessToken = accessToken;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_spotifyconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ((MainActivity) getActivity()).setActionBarTitle(R.string.spotify_conf);
    getActivity().invalidateOptionsMenu();

    Log.d("DEBUG", "SPOTIFY:: Config...");

    if (isLoggedIn) {
      mAsyncSpotifyApi = new AsyncSpotifyApi();
      isExecuteFinish = true;
      mAsyncSpotifyApi.execute("user_playlist");

      new Thread(new Runnable() {
        @Override
        public void run() {
          while (isExecuteFinish) {
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          mAsyncSpotifyApi = null;
          mAsyncSpotifyApi = new AsyncSpotifyApi();
          mAsyncSpotifyApi.execute("featured_playlist");
          //mAsyncSpotifyApi.execute("followed_artist");

          /*
          new Thread(new Runnable() {
            @Override
            public void run() {
              while (isExecuteFinish) {
                try {
                  Thread.sleep(10);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }

              mAsyncSpotifyApi = null;
              mAsyncSpotifyApi = new AsyncSpotifyApi();
              mAsyncSpotifyApi.execute("saved_albums");
            }
          }).start();
          */
        }
      }).start();
    }

    swUse = (Switch) view.findViewById(R.id.spotify_use);
    swUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          Log.d("SPOTIFY", "Use Spotify.");
          //Toast.makeText(getActivity(), "SCANNING...", Toast.LENGTH_SHORT).show();

          //((MainActivity) getActivity()).startScan();
        } else {
          Log.d("SPOTIFY", "Not Use Spotify.");
          //Toast.makeText(getActivity(), "SCAN STOPPED.", Toast.LENGTH_SHORT).show();

          //((MainActivity) getActivity()).stopScan();
        }
      }
    });

    swShuffle = (Switch) view.findViewById(R.id.enable_shuffle);
    swShuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          Log.d("SPOTIFY", "Enable Shuffle Play.");
          //Toast.makeText(getActivity(), "SCANNING...", Toast.LENGTH_SHORT).show();

          //((MainActivity) getActivity()).startScan();
        } else {
          Log.d("SPOTIFY", "Disable Shuffle Play.");
          //Toast.makeText(getActivity(), "SCAN STOPPED.", Toast.LENGTH_SHORT).show();

          //((MainActivity) getActivity()).stopScan();
        }
      }
    });

    for (int j = 0; j < 4; j++) {
      switch (j) {
        case 0:
          spCategory[j] = (Spinner) view.findViewById(R.id.category1);
          spPlaylist[j] = (Spinner) view.findViewById(R.id.playlists1);
          break;
        case 1:
          spCategory[j] = (Spinner) view.findViewById(R.id.category2);
          spPlaylist[j] = (Spinner) view.findViewById(R.id.playlists2);
          break;
        case 2:
          spCategory[j] = (Spinner) view.findViewById(R.id.category3);
          spPlaylist[j] = (Spinner) view.findViewById(R.id.playlists3);
          break;
        case 3:
          spCategory[j] = (Spinner) view.findViewById(R.id.category4);
          spPlaylist[j] = (Spinner) view.findViewById(R.id.playlists4);
          break;
      }

      final int jj = j;

      spCategory[j]
          .setSelection(((MainActivity) getActivity()).getSavedValue("SPOTIFY_CAT" + (j + 1), 0));
      spCategory[j].setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          Log.d("DEBUG", "position = " + i);

          ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_CAT" + (jj + 1), i);

          playlistAdapter[jj].clear();
          switch (i) {
            case 0:
              playlistAdapter[jj].addAll(userPlaylistNameList);
              break;
            case 1:
              playlistAdapter[jj].addAll(featuredPlaylistNameList);
              break;
          }
          spPlaylist[jj].setAdapter(playlistAdapter[jj]);
          //spPlaylist[jj].setSelection(0);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
      });

      playlistAdapter[j] = new ArrayAdapter<String>(getContext(),
          android.R.layout.simple_spinner_item);
      spPlaylist[j].setAdapter(playlistAdapter[j]);
      spPlaylist[j].setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          if (isUIInitialized) {
            Log.d("DEBUG", "SPOTIFY:: playlist -> " + spPlaylist[jj].getSelectedItem().toString());

            ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_PL_NAME" + (jj + 1),
                spPlaylist[jj].getSelectedItem().toString());
            ((MainActivity) getActivity())
                .autoSaveValue("SPOTIFY_PL_URI" + (jj + 1), getSelectedPlaylistUri(jj));
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
      });
    }
  }

  private String getSelectedPlaylistUri(int listIndex) {
    switch (spCategory[listIndex].getSelectedItem().toString()) {
      case "USER PLAYLIST":
        return userPlaylistUriList.get(spPlaylist[listIndex].getSelectedItemPosition());
      case "FEATURED PLAYLIST":
        return featuredPlaylistUriList.get(spPlaylist[listIndex].getSelectedItemPosition());
      default:
        return null;
    }
  }

  class AsyncSpotifyApi extends AsyncTask<String, String, String> {

    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private SpotifyService mSpotifyService;

    public AsyncSpotifyApi() {
      Log.d("DEBUG", "SPOTIFY:: CONFIG AccessToken = " + accessToken);

      mSpotifyApi.setAccessToken(accessToken);
      mSpotifyService = mSpotifyApi.getService();
    }

    public AsyncSpotifyApi(String accessToken) {
      mSpotifyApi.setAccessToken(accessToken);
      mSpotifyService = mSpotifyApi.getService();
    }

    @Override
    protected String doInBackground(String... strings) {
      Log.d("DEBUG", "ASYNC SPOTIFY:: " + strings.length);

      isExecuteFinish = true;

      ArrayList[] arrayLists = new ArrayList[2];
      arrayLists[0] = new ArrayList<String>();
      arrayLists[1] = new ArrayList<String>();

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
            userPlaylistNameList.add(list.get(i).name);
            userPlaylistUriList.add(list.get(i).uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
          }
          break;
        case "featured_playlist":
          FeaturedPlaylists featuredPlaylists = mSpotifyService.getFeaturedPlaylists();
          Pager<PlaylistSimple> fpPager = featuredPlaylists.playlists;
          Log.d("DEBUG", "ASYNC SPOTIFY:: Featured Playlist -> " + fpPager.total);
          for (int i = 0; i < fpPager.total; i++) {
            List<PlaylistSimple> list = fpPager.items;
            featuredPlaylistNameList.add(list.get(i).name);
            featuredPlaylistUriList.add(list.get(i).uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
          }
          break;
        case "saved_albums":
          Pager<SavedAlbum> saPager = mSpotifyService.getMySavedAlbums();
          Log.d("DEBUG", "ASYNC SPOTIFY:: My Saved Albums -> " + saPager.total);
          for (int i = 0; i < saPager.total; i++) {
            List<SavedAlbum> list = saPager.items;
            savedAlbumNameList.add(list.get(i).album.name);
            savedAlbumUriList.add(list.get(i).album.uri);
            Log.d("DEBUG",
                "ASYNC SPOTIFY::   --> " + list.get(i).album.name + " " + list.get(i).album.uri);
          }
          break;
        case "followed_artist":
          ArtistsCursorPager acPager = mSpotifyService.getFollowedArtists();
          CursorPager<Artist> cPager = acPager.artists;
          Log.d("DEBUG", "ASYNC SPOTIFY:: My Followed Artist -> " + cPager.total);
          for (int i = 0; i < cPager.total; i++) {
            List<Artist> list = cPager.items;
            followedArtistNameList.add(list.get(i).name);
            followedArtistUriList.add(list.get(i).uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
          }
          break;
        case "search_artist":
          ArtistsPager artistsPager = mSpotifyService.searchArtists(strings[1]);
          Pager<Artist> aPager = artistsPager.artists;
          Log.d("DEBUG", "ASYNC SPOTIFY:: Searched Artist -> " + strings[1] + " " + aPager.total);
          for (int i = 0; i < aPager.total; i++) {
            List<Artist> list = aPager.items;
            //arrayLists[0].add(list.get(i).name);
            //arrayLists[1].add(list.get(i).uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
          }
          break;
        case "search_album":
          AlbumsPager albumsPager = mSpotifyService.searchAlbums(strings[1]);
          Pager<AlbumSimple> albPager = albumsPager.albums;
          Log.d("DEBUG", "ASYNC SPOTIFY:: Searched Album -> " + albPager.total);
          for (int i = 0; i < albPager.total; i++) {
            List<AlbumSimple> list = albPager.items;
            //arrayLists[0].add(list.get(i).name);
            //arrayLists[1].add(list.get(i).uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + list.get(i).name + " " + list.get(i).uri);
          }
          break;
      }

      return strings[0];
    }

    @Override
    protected void onPostExecute(String string) {
      super.onPostExecute(string);

      Log.d("DEBUG", "SPOTIFY:: onPostExecute");

      for (int i = 0; i < 4; i++) {
        Log.d("DEBUG", "SPOTIFY:: onPostExecute -> " + spCategory[i].getSelectedItem().toString());

        if (string.equals("user_playlist") && spCategory[i].getSelectedItem().toString()
            .contains("USER")) {
          Log.d("DEBUG", "SPOTIFY:: user");

          playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
              android.R.layout.simple_spinner_item);
          if (userPlaylistNameList.size() > 0) {
            playlistAdapter[i].addAll(userPlaylistNameList);
          }
          spPlaylist[i].setAdapter(playlistAdapter[i]);
        } else if (string.equals("featured_playlist") && spCategory[i].getSelectedItem().toString()
            .contains("FEATURED")) {
          Log.d("DEBUG", "SPOTIFY:: featured");

          playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
              android.R.layout.simple_spinner_item);
          if (featuredPlaylistNameList.size() > 0) {
            playlistAdapter[i].addAll(featuredPlaylistNameList);
          }
          spPlaylist[i].setAdapter(playlistAdapter[i]);
        }
      }

      if (string.equals("featured_playlist")) {
        for (int i = 0; i < 4; i++) {
          String selectedName = ((MainActivity) getActivity())
              .getSavedValue("SPOTIFY_PL_NAME" + (i + 1));

          Log.d("DEBUG", "SPOTIFY:: load last playlist -> " + selectedName);

          if (selectedName != null) {
            for (int j = 0; j < playlistAdapter[i].getCount(); j++) {
              if (selectedName.equals(playlistAdapter[i].getItem(j))) {
                spPlaylist[i].setSelection(j);
                break;
              }
            }
          } else {
            Log.d("DEBUG", "SPOTIFY:: no selected playlist...");
            spPlaylist[i].setSelection(0);

            ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_PL_NAME" + (i + 1),
                spPlaylist[i].getSelectedItem().toString());
            ((MainActivity) getActivity())
                .autoSaveValue("SPOTIFY_PL_URI" + (i + 1), getSelectedPlaylistUri(i));
          }
        }
        isUIInitialized = true;
      }

      isExecuteFinish = false;
    }
  }
}
