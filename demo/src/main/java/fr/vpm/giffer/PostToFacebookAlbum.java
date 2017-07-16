package fr.vpm.giffer;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.ShareVideo;

import java.io.File;

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public void shareGifToFb(String gifFileAbsolutePath, Activity activity, final Listener listener) {
    Log.w(POST_TO_ALBUM, "sharing gif to fb");

    Uri uri = Uri.fromFile(new File(gifFileAbsolutePath));
    ShareVideo shareVideo = new ShareVideo.Builder()
        .setLocalUrl(uri)
        .build();

    ShareContent shareContent = new ShareMediaContent.Builder()
        .addMedium(shareVideo)
        .build();
    ShareApi shareApi = new ShareApi(shareContent);
    shareApi.setGraphNode("10211351342553032");
    shareApi.setMessage("Another Gif");
    shareApi.share(null);
    listener.onPicturePublished();
  }

  public interface Listener {
    void onPicturePublished();
  }
}
