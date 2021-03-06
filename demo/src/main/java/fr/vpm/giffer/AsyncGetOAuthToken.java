package fr.vpm.giffer;

import android.os.AsyncTask;
import android.util.Log;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;

/**
 * Created by vince on 22/06/17.
 */

class AsyncGetOAuthToken extends AsyncTask<Object, Integer, OAuth1AccessToken> {

  interface Listener {
    void onOAuthTokenRetrieved(OAuth1AccessToken token);
  }

  private final OAuth10aService service;

  private final Listener listener;

  AsyncGetOAuthToken(OAuth10aService service, Listener listener) {
    this.service = service;
    this.listener = listener;
  }

  @Override
  protected OAuth1AccessToken doInBackground(Object... params) {
    final OAuth1RequestToken requestToken = (OAuth1RequestToken) params[0];
    final String verifier = (String) params[1];
    try {
      return service.getAccessToken(requestToken, verifier);
    } catch (IOException e) {
      Log.w("GET-OAUTH-TOKEN", e);
    }
    return null;
  }

  @Override
  protected void onPostExecute(OAuth1AccessToken oAuth1AccessToken) {
    super.onPostExecute(oAuth1AccessToken);
    listener.onOAuthTokenRetrieved(oAuth1AccessToken);
  }
}
