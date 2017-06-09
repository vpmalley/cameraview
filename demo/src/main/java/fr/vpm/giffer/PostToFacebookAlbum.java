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

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public interface Listener {
    void onPicturePublished();
  }

  public void shareGifToFb(String gifFileAbsolutePath, final Listener listener) {
    Log.w(POST_TO_ALBUM, "sharing gif to fb");

    Bundle params = new Bundle();

    FileInputStream in = null;
    try {
      in = new FileInputStream(gifFileAbsolutePath);
      BufferedInputStream buf = new BufferedInputStream(in);
      byte[] bMapArray= new byte[buf.available()];
      buf.read(bMapArray);
      String pictureContent = new String(bMapArray);
      params.putString("source", pictureContent);
    } catch (IOException e) {
      Log.w(POST_TO_ALBUM, "Failed to add pic bytes to the request");
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }

    new GraphRequest(
        AccessToken.getCurrentAccessToken(),
       // "/10211351342553032/photos",
        "/116200818975426/photos",
        params,
        HttpMethod.POST,
        new GraphRequest.Callback() {
          public void onCompleted(GraphResponse response) {
        /* handle the result */
            Log.d(POST_TO_ALBUM, response.toString());
            try {
              if (response.getJSONObject() != null && response.getJSONObject().has("id")) {
                String id = response.getJSONObject().getString("id");
                Log.d(POST_TO_ALBUM, "response contains id " + id);
                if (id != null && listener != null) {
                  listener.onPicturePublished();
                }
              }
            } catch (JSONException e) {
              Log.w(POST_TO_ALBUM, "response does not contain id");
            }
          }
        }
    ).executeAsync();
  }
}
