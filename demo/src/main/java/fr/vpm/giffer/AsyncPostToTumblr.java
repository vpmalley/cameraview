package fr.vpm.giffer;

import android.os.AsyncTask;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;

import java.io.File;
import java.io.IOException;

/**
 * Created by vince on 22/06/17.
 */

class AsyncPostToTumblr extends AsyncTask<File, Integer, Boolean> {

  private final JumblrClient client;
  private final Listener listener;

  AsyncPostToTumblr(JumblrClient client, Listener listener) {
    this.client = client;
    this.listener = listener;
  }

  @Override
  protected Boolean doInBackground(File... files) {
    PhotoPost post;
    try {
      post = client.newPost("vincetraveller", PhotoPost.class);
      post.setCaption("some gif");
      post.setData(files[0]);
      post.save();
      Log.i("POST-PHOTO", "posted the picture to the blog");
      return true;
    } catch (IllegalAccessException | InstantiationException | IOException e) {
      Log.w("POST-PHOTO", e);
    }
    return false;
  }

  @Override
  protected void onPostExecute(Boolean isPosted) {
    super.onPostExecute(isPosted);
    if (isPosted) {
      listener.onPosted();
    }
  }

  interface Listener {
    void onPosted();
  }
}
