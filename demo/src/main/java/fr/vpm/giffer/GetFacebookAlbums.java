package fr.vpm.giffer;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Requires user-photos read permission.
 */
public class GetFacebookAlbums {

  public static final String GET_FACEBOOK_ALBUMS = "GetFacebookAlbums";

  interface AlbumListener {

  }

  public void getMyAlbumIds() {
    Log.d(GET_FACEBOOK_ALBUMS, "querying albums");
    new GraphRequest(
        AccessToken.getCurrentAccessToken(),
        "/" + AccessToken.getCurrentAccessToken().getUserId() + "/albums", // "/accounts" for pages
        null,
        HttpMethod.GET,
        new GraphRequest.Callback() {
          public void onCompleted(GraphResponse response) {
            Log.d(GET_FACEBOOK_ALBUMS, "got a response : " + response.toString());
            /* handle the result */
            List<String> albumIds = new ArrayList<String>();
            try {
              JSONArray albums = response.getJSONObject().getJSONArray("data");
              Log.d(GET_FACEBOOK_ALBUMS, albums.toString());
              for (int i = 0; i < albums.length(); i++) {
                albumIds.add(albums.getJSONObject(i).getString("id"));
              }
            } catch (JSONException e) {
              Log.w(GET_FACEBOOK_ALBUMS, "cannot get my albums");
            }
          }
        }
    ).executeAsync();
  }
}
