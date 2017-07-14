package fr.vpm.giffer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.android.giffer.giffer.R;
import com.tumblr.jumblr.JumblrClient;

import java.io.File;
import java.io.IOException;

public class PostToTumblrBlog {

  private OAuth10aService service;

  private Context context;

  private File fileToPost;

  private OAuth10aService getService(Resources resources) {
    final OAuth10aService service = new ServiceBuilder()
        .apiKey(resources.getString(R.string.tumblr_consumer_key))
        .apiSecret(resources.getString(R.string.tumblr_consumer_secret))
        .callback("vpmgiffer://loginok")
        .build(TumblrApi.instance());
    return service;
  }

  public void post(final Context context, Handler backgroundHandler, final File file) {
    Log.d("POST-PHOTO", "start posting");
    this.context = context;
    this.fileToPost = file;
    service = getService(context.getResources());
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        OAuth1RequestToken requestToken = null;
        try {
          requestToken = service.getRequestToken();
        } catch (IOException e) {
          Log.w("GET-OAUTH-TOKEN", e);
        }
        if (requestToken != null) {
          SharedPreferences sharedPref = context.getSharedPreferences(
              context.getString(R.string.oauth_preference_file_key), Context.MODE_PRIVATE);
          SharedPreferences.Editor edition = sharedPref.edit();
          edition.putString("rt", requestToken.getToken());
          edition.putString("rts", requestToken.getTokenSecret());
          edition.apply();
          authorize(requestToken);
        }
      }
    });
  }

  private void authorize(OAuth1RequestToken oAuth1RequestToken) {
    Log.d("POST-PHOTO", "start authorizing");
    String authorizationUrl = service.getAuthorizationUrl(oAuth1RequestToken);

    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl));
    context.startActivity(browserIntent);
  }

  public void getOAuthToken(String verifier) {
    Log.d("POST-PHOTO", "start getting token with verifier");
    SharedPreferences sharedPref = context.getSharedPreferences(
        context.getString(R.string.oauth_preference_file_key), Context.MODE_PRIVATE);
    String requestToken = sharedPref.getString("rt", null);
    String requestTokenSecret = sharedPref.getString("rts", null);
    OAuth1RequestToken oAuth1RequestToken = new OAuth1RequestToken(requestToken, requestTokenSecret);
    new AsyncGetOAuthToken(service, new AsyncGetOAuthToken.Listener() {
      @Override
      public void onOAuthTokenRetrieved(OAuth1AccessToken token) {
        SharedPreferences sharedPref = context.getSharedPreferences(
            context.getString(R.string.oauth_preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor edition = sharedPref.edit();
        edition.putString("at", token.getToken());
        edition.putString("ats", token.getTokenSecret());
        edition.apply();
        postWithToken(token);
      }
    }).execute(oAuth1RequestToken, verifier);
  }

  private void postWithToken(OAuth1AccessToken oAuthToken) {
    Log.d("POST-PHOTO", "start posting with token");
    if (oAuthToken != null) {
      Log.i("POST-PHOTO", "failed posting the picture to the blog");
      return;
    }
    JumblrClient client = getJumblrClient(oAuthToken);

//    new AsyncPostToTumblr(client, new AsyncPostToTumblr.Listener() {
//      @Override
//      public void onPosted() {
//        Log.d("POST-PHOTO", "posted photo");
//      }
//    }).execute(fileToPost);
  }

  @NonNull
  private JumblrClient getJumblrClient(OAuth1AccessToken oAuthToken) {
    JumblrClient jumblrClient = new JumblrClient(context.getResources().getString(R.string.tumblr_consumer_key),
        context.getResources().getString(R.string.tumblr_consumer_secret));
    jumblrClient.setToken(oAuthToken.getToken(), oAuthToken.getTokenSecret());
    return jumblrClient;
  }

}
