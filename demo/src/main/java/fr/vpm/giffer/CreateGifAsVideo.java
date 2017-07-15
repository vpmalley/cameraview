package fr.vpm.giffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import org.jcodec.api.android.SequenceEncoder;

import java.io.File;
import java.io.IOException;

/**
 * Created by vince on 15/07/17.
 */

public class CreateGifAsVideo implements CreateGif {

  public static final String TAG = "CreateGifAsVideo";

  @Override
  public void createGif(File pictureSessionDir, Handler backgroundHandler, Listener listener) {
    if (!pictureSessionDir.isDirectory()) {
      return;
    }
    File videoFile = new File(pictureSessionDir,
        "video-" + System.currentTimeMillis() + ".mp4");
    SequenceEncoder enc = null;
    try {
      enc = new SequenceEncoder(videoFile);
      //enc.getEncoder().setKeyInterval(25);

      for(File nextPic : pictureSessionDir.listFiles()) {
        if (nextPic.getName().startsWith("picture-")) {
          Log.d(TAG, "getting bitmap for pic " + nextPic.getName());
          for (int i = 0; i < 2; i++) {
            Bitmap frameBitmap = bitmapOf(nextPic);
            Log.d(TAG, "adding frame for pic " + nextPic.getName());
            enc.encodeImage(frameBitmap);
          }
        }
      }
      enc.finish();
    } catch (IOException e) {
      Log.w(TAG, e);
    }
    listener.onGifCreated(videoFile);
  }

  private Bitmap bitmapOf(File nextPicPath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(nextPicPath.getAbsolutePath(), options);
    return bitmap;
  }
}
