/**
 * SpotifyConfigFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

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

  private List<String> userPlaylistNameList = new ArrayList<>();
  private List<String> featuredPlaylistNameList = new ArrayList<>();
  private List<String> savedAlbumNameList = new ArrayList<>();
  private List<String> followedArtistNameList = new ArrayList<>();
  private List<String> userPlaylistUriList = new ArrayList<>();
  private List<String> featuredPlaylistUriList = new ArrayList<>();
  private List<String> savedAlbumUriList = new ArrayList<>();
  private List<String> followedArtistUriList = new ArrayList<>();

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

    Log.d("DEBUG", "SPOTIFY_CONFIG:: onAttach");
  }

  @Override
  public void onDetach() {
    super.onDetach();

    Log.d("DEBUG", "SPOTIFY_CONFIG:: onDetach");
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.d("DEBUG", "SPOTIFY_CONFIG:: onResume");

    ((MainActivity) getActivity())
        .updateActionBar(getResources().getString(R.string.spotify_conf_title));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d("DEBUG", "SPOTIFY_CONFIG:: onDestroy");

    isExecuteFinish = false;
    isUIInitialized = false;

    mAsyncSpotifyApi = null;

    swUse = null;
    swShuffle = null;

    for (int i = 0; i < spCategory.length; i++) {
      spCategory[i] = null;
      spPlaylist[i] = null;

      playlistAdapter[i].clear();
      playlistAdapter[i] = null;
    }

    userPlaylistNameList.clear();
    featuredPlaylistNameList.clear();
    savedAlbumNameList.clear();
    followedArtistNameList.clear();
    userPlaylistUriList.clear();
    featuredPlaylistUriList.clear();
    savedAlbumUriList.clear();
    followedArtistUriList.clear();

    userPlaylistNameList = null;
    featuredPlaylistNameList = null;
    savedAlbumNameList = null;
    followedArtistNameList = null;
    userPlaylistUriList = null;
    featuredPlaylistUriList = null;
    savedAlbumUriList = null;
    followedArtistUriList = null;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Log.d("DEBUG", "SPOTIFY_CONFIG:: onViewCreated");

    userPlaylistNameList = new ArrayList<>();
    featuredPlaylistNameList = new ArrayList<>();
    savedAlbumNameList = new ArrayList<>();
    followedArtistNameList = new ArrayList<>();
    userPlaylistUriList = new ArrayList<>();
    featuredPlaylistUriList = new ArrayList<>();
    savedAlbumUriList = new ArrayList<>();
    followedArtistUriList = new ArrayList<>();

    if (((MainActivity) getActivity()).authenticate()) {
      getPlaylist();
    }

    swUse = (Switch) view.findViewById(R.id.spotify_use);
    swUse.setChecked(((MainActivity) getActivity()).getSavedValue("SPOTIFY_USE", false));
    swUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_USE", b);
        
        if (b) {
          Log.d("SPOTIFY", "Use Spotify.");

          ((MainActivity) getActivity()).authenticate();
        } else {
          Log.d("SPOTIFY", "Not Use Spotify.");
        }
      }
    });

    swShuffle = (Switch) view.findViewById(R.id.enable_shuffle);
    swShuffle.setChecked(((MainActivity) getActivity()).getSavedValue("SPOTIFY_SHUFFLE", false));
    swShuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          Log.d("SPOTIFY", "Enable Shuffle Play.");
        } else {
          Log.d("SPOTIFY", "Disable Shuffle Play.");
        }

        ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_SHUFFLE", b);
      }
    });

    Log.d("DEBUG", "SPOTIFY:: onViewCreated");
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
          .setSelection(((MainActivity) getActivity()).getSavedValue("SPOTIFY_CAT" + (j + 1), 0),
              false);
      spCategory[j].setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          Log.d("DEBUG", "set position = " + i);

          ((MainActivity) getActivity()).autoSaveValue("SPOTIFY_CAT" + (jj + 1), i);

          playlistAdapter[jj].clear();
          switch (i) {
            case 0:
              playlistAdapter[jj].addAll(userPlaylistNameList);
              break;
            case 1:
              playlistAdapter[jj].addAll(featuredPlaylistNameList);
              break;
            case 2:
              playlistAdapter[jj].addAll(followedArtistNameList);
              break;
            case 3:
              playlistAdapter[jj].addAll(savedAlbumNameList);
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
            Log.d("DEBUG",
                "SPOTIFY:: set playlist -> " + spPlaylist[jj].getSelectedItem().toString());

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
      case "FOLLOWED ARTIST":
        return followedArtistUriList.get(spPlaylist[listIndex].getSelectedItemPosition());
      case "SAVED ALBUM":
        return savedAlbumUriList.get(spPlaylist[listIndex].getSelectedItemPosition());
      default:
        return null;
    }
  }

  void getPlaylist() {
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
              mAsyncSpotifyApi.execute("followed_artist");

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
            }
          }).start();

        }
      }).start();
    }
  }

  private class AsyncSpotifyApi extends AsyncTask<String, String, String> {

    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private SpotifyService mSpotifyService;

    private AsyncSpotifyApi() {
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
      Log.d("DEBUG", "ASYNC SPOTIFY:: " + strings.length + " " + strings[0]);

      isExecuteFinish = true;

      if (!isLoggedIn) {
        return null;
      }

      //ArrayList[] arrayLists = new ArrayList[2];
      //arrayLists[0] = new ArrayList<>();
      //arrayLists[1] = new ArrayList<>();

      switch (strings[0]) {
        case "me":
          String country = mSpotifyService.getMe().country;
          Log.d("DEBUG", "ASYNC SPOTIFY:: Country -> " + country);
          break;
        case "user_playlist":
          Pager<PlaylistSimple> upPager = mSpotifyService.getMyPlaylists();
          Log.d("DEBUG", "ASYNC SPOTIFY:: My Playlist -> " + upPager.total);
          for (PlaylistSimple playlistSimple : upPager.items) {
            userPlaylistNameList.add(playlistSimple.name);
            userPlaylistUriList.add(playlistSimple.uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + playlistSimple.name + " " + playlistSimple.uri);
          }
          break;
        case "featured_playlist":
          FeaturedPlaylists featuredPlaylists = mSpotifyService.getFeaturedPlaylists();
          Pager<PlaylistSimple> fpPager = featuredPlaylists.playlists;
          Log.d("DEBUG", "ASYNC SPOTIFY:: Featured Playlist -> " + fpPager.total);
          for (PlaylistSimple playlistSimple : fpPager.items) {
            featuredPlaylistNameList.add(playlistSimple.name);
            featuredPlaylistUriList.add(playlistSimple.uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + playlistSimple.name + " " + playlistSimple.uri);
          }
          break;
        case "saved_albums":
          Pager<SavedAlbum> saPager = mSpotifyService.getMySavedAlbums();
          Log.d("DEBUG", "ASYNC SPOTIFY:: My Saved Albums -> " + saPager.total);
          for (SavedAlbum savedAlbum : saPager.items) {
            savedAlbumNameList.add(savedAlbum.album.name);
            savedAlbumUriList.add(savedAlbum.album.uri);
            Log.d("DEBUG",
                "ASYNC SPOTIFY::   --> " + savedAlbum.album.name + " " + savedAlbum.album.uri);
          }
          break;
        case "followed_artist":
          ArtistsCursorPager acPager = mSpotifyService.getFollowedArtists();
          CursorPager<Artist> cPager = acPager.artists;
          Log.d("DEBUG", "ASYNC SPOTIFY:: My Followed Artist -> " + cPager.total);
          for (Artist artist : cPager.items) {
            followedArtistNameList.add(artist.name);
            followedArtistUriList.add(artist.uri);
            Log.d("DEBUG", "ASYNC SPOTIFY::   --> " + artist.name + " " + artist.uri);
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

      Log.d("DEBUG", "SPOTIFY:: onPostExecute " + string);

      if (string == null) {
        return;
      }

      for (int i = 0; i < 4; i++) {
        if (spCategory[i] != null) {
          Log.d("DEBUG",
              "SPOTIFY:: onPostExecute -> " + spCategory[i].getSelectedItem().toString() + " " + string);

          if (string.equals("user_playlist") && spCategory[i].getSelectedItem().toString()
              .contains("USER")) {
            Log.d("DEBUG", "SPOTIFY:: user");

            playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item);
            if (userPlaylistNameList.size() > 0) {
              playlistAdapter[i].addAll(userPlaylistNameList);
            }
            spPlaylist[i].setAdapter(playlistAdapter[i]);
          } else if (string.equals("featured_playlist") && spCategory[i].getSelectedItem()
              .toString()
              .contains("FEATURED")) {
            Log.d("DEBUG", "SPOTIFY:: featured");

            playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item);
            if (featuredPlaylistNameList.size() > 0) {
              playlistAdapter[i].addAll(featuredPlaylistNameList);
            }
            spPlaylist[i].setAdapter(playlistAdapter[i]);
          } else if (string.equals("followed_artist") && spCategory[i].getSelectedItem()
              .toString()
              .contains("FOLLOWED")) {
            Log.d("DEBUG", "SPOTIFY:: followed_artist " + followedArtistNameList.size());

            playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item);
            if (followedArtistNameList.size() > 0) {
              playlistAdapter[i].addAll(followedArtistNameList);
            }
            spPlaylist[i].setAdapter(playlistAdapter[i]);
          } else if (string.equals("saved_albums") && spCategory[i].getSelectedItem()
              .toString()
              .contains("SAVED")) {
            Log.d("DEBUG", "SPOTIFY:: saved_albums " + savedAlbumNameList.size());

            playlistAdapter[i] = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item);
            if (savedAlbumNameList.size() > 0) {
              playlistAdapter[i].addAll(savedAlbumNameList);
            }
            spPlaylist[i].setAdapter(playlistAdapter[i]);
          }
        }
      }

      if (string.equals("saved_albums")) {
        for (int i = 0; i < 4; i++) {
          if (getActivity() != null) {
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
          } else {
            Log.d("DEBUG", "SPOTIFY_CONFIG:: activity null...");
          }
        }
        isUIInitialized = true;
      }

      isExecuteFinish = false;
    }
  }
}
