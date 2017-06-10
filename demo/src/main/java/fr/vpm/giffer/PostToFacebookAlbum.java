package fr.vpm.giffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public interface Listener {
    void onPicturePublished();
  }

  public void shareGifToFb(String gifFileAbsolutePath, final Listener listener) {
    Log.w(POST_TO_ALBUM, "sharing gif to fb");

    Bundle params = new Bundle();
    Bitmap bitmap = BitmapFactory.decodeFile(gifFileAbsolutePath);
    params.putParcelable("source", bitmap);

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
