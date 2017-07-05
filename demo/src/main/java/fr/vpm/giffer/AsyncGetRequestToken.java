package fr.vpm.giffer;

import android.os.AsyncTask;
import android.util.Log;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;

/**
 * Created by vince on 22/06/17.
 */

class AsyncGetRequestToken extends AsyncTask<String, Integer, OAuth1RequestToken> {

  interface Listener {
    void onOAuthTokenRetrieved(OAuth1RequestToken token);
  }

  private final OAuth10aService service;

  private final Listener listener;

  AsyncGetRequestToken(OAuth10aService service, Listener listener) {
    this.service = service;
    this.listener = listener;
  }

  @Override
  protected OAuth1RequestToken doInBackground(String... strings) {
    try {
      return service.getRequestToken();
    } catch (IOException e) {
      Log.w("GET-OAUTH-TOKEN", e);
    }
    return null;
  }

  @Override
  protected void onPostExecute(OAuth1RequestToken oAuth1RequestToken) {
    super.onPostExecute(oAuth1RequestToken);
    listener.onOAuthTokenRetrieved(oAuth1RequestToken);
  }
}
