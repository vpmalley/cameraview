package fr.vpm.giffer;

import android.net.Uri;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;

import java.io.File;

public class PostToFacebookAlbum {

  private static final String POST_TO_ALBUM = "PostToFacebookAlbum";

  public void shareGifToFb(String gifFileAbsolutePath, FacebookCallback<Sharer.Result> callback) {
    Log.d(POST_TO_ALBUM, "sharing gif to fb");

    Uri uri = Uri.fromFile(new File(gifFileAbsolutePath));
    ShareVideo shareVideo = new ShareVideo.Builder()
        .setLocalUrl(uri)
        .build();

    ShareVideoContent shareVideoContent= new ShareVideoContent.Builder()
        .setVideo(shareVideo)
        .setContentTitle("5 août selfie")
        .build();

    ShareApi shareApi = new ShareApi(shareVideoContent);
    //shareApi.setGraphNode("10211351342553032"); // album
    //shareApi.setGraphNode("1475683019176601"); // page
    shareApi.setGraphNode("1472278586199579"); // group
    shareApi.setMessage("Another Gif");
    Log.d(POST_TO_ALBUM, "can share video to group : " + shareApi.canShare());
    shareApi.share(callback);
  }
}
