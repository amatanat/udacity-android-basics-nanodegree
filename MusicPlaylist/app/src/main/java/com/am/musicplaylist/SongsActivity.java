package com.am.musicplaylist;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.airbnb.lottie.LottieAnimationView;
import com.am.musicplaylist.data.PlaylistContract.PlaylistEntry;

public class SongsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

  private final int LOADER_INIT = 300;
  private final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 200;
  private final int REQUEST_CODE = 1;
  private final String PLAYLIST_NAME = "playlistName";

  private LottieAnimationView mLottieAnimationView;
  private String mPlaylistName;
  private ListView mListView;
  private Uri mContentUri;
  private long mId;
  private SongCursorAdapter mSongCursorAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_songs);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    // get LottieAnimationView and set its animation
    mLottieAnimationView = (LottieAnimationView) findViewById(R.id.song_emptyview_image);
    mLottieAnimationView.setAnimation("EmptyState.json");
    mLottieAnimationView.loop(true);
    mLottieAnimationView.playAnimation();

    mSongCursorAdapter = new SongCursorAdapter(this, null);
    mListView = (ListView) findViewById(R.id.song_listview);
    mListView.setAdapter(mSongCursorAdapter);

    // get intent which has started this Activity
    Intent intent = getIntent();
    if (intent != null) {
      mContentUri = intent.getData();
      // get id of the clicked item from MainActivity
      mId = ContentUris.parseId(mContentUri);

      if (intent.hasExtra(PLAYLIST_NAME)) {

        mPlaylistName = intent.getStringExtra(PLAYLIST_NAME);

        // set title of the ActionBar as the playlist name
        getSupportActionBar().setTitle(mPlaylistName);
      }
    }

    // get RecyclerView id and set it as emptyview in ListView
    View emptyView = findViewById(R.id.song_empty_view);
    mListView.setEmptyView(emptyView);

    // initialize loader
    getSupportLoaderManager().initLoader(LOADER_INIT, null, this);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_song, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
      case R.id.new_song:
        // source https://developer.android.com/training/permissions/requesting.html
        if (ContextCompat.checkSelfPermission(SongsActivity.this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

          if (ActivityCompat.shouldShowRequestPermissionRationale(SongsActivity.this,
              Manifest.permission.READ_EXTERNAL_STORAGE)) {

          } else {

            // Request the permission.

            ActivityCompat.requestPermissions(SongsActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

          }
        } else {
          addSong();
        }

        return true;
      case R.id.delete_song:
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  //source https://developer.android.com/training/permissions/requesting.html
  @Override
  public void onRequestPermissionsResult(int requestCode,
      String permissions[], int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          // permission was granted so allow user to add song
          addSong();
        }
        return;
      }
    }
  }

  /**
   * This method is used to allow the user to pick an audio file
   */
  private void addSong() {
    Intent intent = new Intent(Intent.ACTION_PICK,
        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    if (intent.resolveActivity(getPackageManager()) != null){
      startActivityForResult(intent, REQUEST_CODE);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Uri uri;
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
      if (data.getData() != null) {
        uri = data.getData();
        Cursor musicCursor = getContentResolver().query(uri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
          //get columns of the song
          int titleColumn = musicCursor.getColumnIndex
              (android.provider.MediaStore.Audio.Media.TITLE);
          int artistColumn = musicCursor.getColumnIndex
              (android.provider.MediaStore.Audio.Media.ARTIST);

          //add song to ListView and db
          do {
            String thisTitle = musicCursor.getString(titleColumn);
            String thisArtist = musicCursor.getString(artistColumn);
            insertDataIntoDatabase(thisTitle, thisArtist);
          }
          while (musicCursor.moveToNext());
        }
      }
    }
  }

  /**
   * Insert song info into db
   */
  private void insertDataIntoDatabase(String title, String artist) {

    ContentValues values = new ContentValues();

    values.put(PlaylistEntry.COLUMN_SONG_TITLE, title);
    values.put(PlaylistEntry.COLUMN_SONG_ARTIST, artist);
    values.put(PlaylistEntry.COLUMN_SONG_PLAYLIST_ID, mId);

    Uri resultUri = getContentResolver().insert(PlaylistEntry.CONTENT_URI_SONG, values);

    Log.i("SongsActivity", "Result uri in insert method......" + resultUri);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] projection = {
        PlaylistEntry._ID,
        PlaylistEntry.COLUMN_SONG_TITLE,
        PlaylistEntry.COLUMN_SONG_ARTIST,
        PlaylistEntry.COLUMN_SONG_PLAYLIST_ID
    };

    // This loader will execute ContentProvider's query method in a background thread and show corresponding songs for
    // the clicked playlist
    return new CursorLoader(this, ContentUris.withAppendedId(PlaylistEntry.CONTENT_URI_SONG, mId), projection, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mSongCursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mSongCursorAdapter.swapCursor(null);
  }
}
