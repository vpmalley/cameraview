package fr.vpm.giffer;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.android.giffer.giffer.R;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.PhotoPost;

import java.io.File;
import java.io.IOException;


/**
 * Created by vince on 15/06/17.
 */

public class PostToTumblrBlog {

  private OAuth10aService getService(Resources resources) {
    final OAuth10aService service = new ServiceBuilder()
        .apiKey(resources.getString(R.string.tumblr_consumer_key))
        .apiSecret(resources.getString(R.string.tumblr_consumer_secret))
        .build(TumblrApi.instance());
    return service;
  }

  public void post(Resources resources, File file) {
    OAuth10aService service = getService(resources);
    OAuth1AccessToken oAuthToken = getOAuthToken(service);
    if (oAuthToken != null) {
      Log.i("POST-PHOTO", "failed posting the picture to the blog");
      return;
    }
    JumblrClient client = getJumblrClient(resources, oAuthToken);

    PhotoPost post;
    try {
      post = client.newPost("vincetraveller", PhotoPost.class);
      post.setCaption("some gif");
      post.setData(file);
      post.save();
      Log.i("POST-PHOTO", "posted the picture to the blog");
    } catch (IllegalAccessException e) {
      Log.w("POST-PHOTO", e);
    } catch (InstantiationException e) {
      Log.w("POST-PHOTO", e);
    } catch (IOException e) {
      Log.w("POST-PHOTO", e);
    }
  }

  @NonNull
  private JumblrClient getJumblrClient(Resources resources, OAuth1AccessToken oAuthToken) {
    JumblrClient jumblrClient = new JumblrClient(resources.getString(R.string.tumblr_consumer_key),
        resources.getString(R.string.tumblr_consumer_secret));
    jumblrClient.setToken(oAuthToken.getToken(), oAuthToken.getTokenSecret());
    return jumblrClient;
  }

  private OAuth1AccessToken getOAuthToken(OAuth10aService service) {
    try {
      final OAuth1RequestToken requestToken = service.getRequestToken();
      final String authUrl = service.getAuthorizationUrl(requestToken);
      return service.getAccessToken(requestToken, "verifier you got from the user/callback");
    } catch (IOException e) {
      Log.w("GET-OAUTH-TOKEN", e);
    }
    return null;
  }

}
