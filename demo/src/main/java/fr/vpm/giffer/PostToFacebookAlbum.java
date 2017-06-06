package fr.vpm.giffer;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by vince on 04/06/17.
 */

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public interface Listener {
    void onPicturePublished();
  }

  public void shareGifToFb(String gifFileAbsolutePath, final Listener listener) {
    Log.w(POST_TO_ALBUM, "sharing gif to fb");

    Bundle params = new Bundle();

    try {
      FileInputStream in = new FileInputStream(gifFileAbsolutePath);
      BufferedInputStream buf = new BufferedInputStream(in);
      byte[] bMapArray= new byte[buf.available()];
      buf.read(bMapArray);
      String pictureContent = new String(bMapArray);
      params.putString("source", pictureContent);
    } catch (IOException e) {
      Log.w(POST_TO_ALBUM, "Failed to add pic bytes to the request");
    }

    new GraphRequest(
        AccessToken.getCurrentAccessToken(),
        "/10211351342553032/photos",
        params,
        HttpMethod.POST,
        new GraphRequest.Callback() {
          public void onCompleted(GraphResponse response) {
        /* handle the result */
            Log.d(POST_TO_ALBUM, response.toString());
            try {
              String id = response.getJSONObject().getString("id");

              Log.w(POST_TO_ALBUM, "response contains id " + id);
              if (id != null && listener != null) {
                listener.onPicturePublished();
              }
            } catch (JSONException e) {
              Log.w(POST_TO_ALBUM, "response does not contain id");
            }
          }
        }
    ).executeAsync();
  }
}
