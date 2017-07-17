package fr.vpm.giffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;

import java.io.File;

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public void shareGifToFb(String gifFileAbsolutePath, Activity activity, final Listener listener) {
    Log.w(POST_TO_ALBUM, "sharing gif to fb");

    Uri uri = Uri.fromFile(new File(gifFileAbsolutePath));

    SharePhoto sharePhoto = new SharePhoto.Builder()
        .setImageUrl(uri)
        .setCaption("some gif")
        .setUserGenerated(true)
        .build();

    SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
        .addPhoto(sharePhoto)
        .build();

    ShareApi shareApi = new ShareApi(sharePhotoContent);
    shareApi.setGraphNode("10211351342553032");
    shareApi.setMessage("Another Gif");
    shareApi.share(new FacebookCallback<Sharer.Result>() {
      @Override
      public void onSuccess(Sharer.Result result) {
        Log.d(POST_TO_ALBUM, "posting succeeded: " + result.toString());
        listener.onPicturePublished();
      }

      @Override
      public void onCancel() {
        Log.w(POST_TO_ALBUM, "posting cancelled");
      }

      @Override
      public void onError(FacebookException error) {
        Log.w(POST_TO_ALBUM, error.getMessage());
      }
    });
  }

  public interface Listener {
    void onPicturePublished();
  }
}
