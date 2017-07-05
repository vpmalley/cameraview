package fr.vpm.giffer;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.android.giffer.giffer.R;
import com.tumblr.jumblr.JumblrClient;

import java.io.File;


/**
 * Created by vince on 15/06/17.
 */

public class PostToTumblrBlog {

  private OAuth10aService service;

  private Context context;

  private File fileToPost;

  private OAuth10aService getService(Resources resources) {
    final OAuth10aService service = new ServiceBuilder()
        .apiKey(resources.getString(R.string.tumblr_consumer_key))
        .apiSecret(resources.getString(R.string.tumblr_consumer_secret))
        .build(TumblrApi.instance());
    return service;
  }

  public void post(final Context context, final File file) {
    this.context = context;
    this.fileToPost = file;
    service = getService(context.getResources());
    new AsyncGetRequestToken(service, new AsyncGetRequestToken.Listener() {
      @Override
      public void onOAuthTokenRetrieved(OAuth1RequestToken token) {
        authorize(token);
      }
    });
  }

  private void authorize(OAuth1RequestToken oAuth1RequestToken) {
    String authorizationUrl = service.getAuthorizationUrl(oAuth1RequestToken);
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(context, Uri.parse(authorizationUrl));
  }

  private void getOAuthToken(String verifier) {
    new AsyncGetOAuthToken(service, new AsyncGetOAuthToken.Listener() {
      @Override
      public void onOAuthTokenRetrieved(OAuth1AccessToken token) {
        postWithToken(token);
      }
    }).execute();
  }

  private void postWithToken(OAuth1AccessToken oAuthToken) {
    if (oAuthToken != null) {
      Log.i("POST-PHOTO", "failed posting the picture to the blog");
      return;
    }
    JumblrClient client = getJumblrClient(oAuthToken);

    new AsyncPostToTumblr(client, new AsyncPostToTumblr.Listener() {
      @Override
      public void onPosted() {

      }
    }).execute(fileToPost);
  }

  @NonNull
  private JumblrClient getJumblrClient(OAuth1AccessToken oAuthToken) {
    JumblrClient jumblrClient = new JumblrClient(context.getResources().getString(R.string.tumblr_consumer_key),
        context.getResources().getString(R.string.tumblr_consumer_secret));
    jumblrClient.setToken(oAuthToken.getToken(), oAuthToken.getTokenSecret());
    return jumblrClient;
  }

}
