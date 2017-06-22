package fr.vpm.giffer;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.android.giffer.giffer.R;
import com.tumblr.jumblr.JumblrClient;

import java.io.File;


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

  public void post(final Resources resources, final File file) {
    OAuth10aService service = getService(resources);
    new AsyncGetOAuthToken(service, new AsyncGetOAuthToken.Listener() {
      @Override
      public void onOAuthTokenRetrieved(OAuth1AccessToken token) {
        postWithToken(resources, file, token);
      }
    }).execute();
  }

  private void postWithToken(Resources resources, File file, OAuth1AccessToken oAuthToken) {
    if (oAuthToken != null) {
      Log.i("POST-PHOTO", "failed posting the picture to the blog");
      return;
    }
    JumblrClient client = getJumblrClient(resources, oAuthToken);

    new AsyncPostToTumblr(client, new AsyncPostToTumblr.Listener() {
      @Override
      public void onPosted() {

      }
    }).execute(file);
  }

  @NonNull
  private JumblrClient getJumblrClient(Resources resources, OAuth1AccessToken oAuthToken) {
    JumblrClient jumblrClient = new JumblrClient(resources.getString(R.string.tumblr_consumer_key),
        resources.getString(R.string.tumblr_consumer_secret));
    jumblrClient.setToken(oAuthToken.getToken(), oAuthToken.getTokenSecret());
    return jumblrClient;
  }

}
