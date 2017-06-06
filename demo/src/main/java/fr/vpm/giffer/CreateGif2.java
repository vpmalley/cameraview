package fr.vpm.giffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by vince on 06/06/17.
 */

public class CreateGif2 implements CreateGif {

  private static final String TAG = "CreateGif2";

  @Override
  public void createGif(final File pictureSessionDir, final Handler backgroundHandler,
                        final Listener listener) {

    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        File gifFile = new File(pictureSessionDir,
            "gif-" + System.currentTimeMillis() + ".gif");

        GifEncoder gifEncoder = new GifEncoder();
        try {
          gifEncoder.init(1000, 1000, gifFile.getAbsolutePath(), GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY);

          for (File nextPic : pictureSessionDir.listFiles()) {
            if (nextPic.getName().startsWith("picture-")) {
              Log.d(TAG, "getting bitmap for pic " + nextPic.getName());
              Bitmap bitmap = bitmapOf(nextPic);
              Log.d(TAG, "adding frame for pic " + nextPic.getName());
              // Bitmap is MUST ARGB_8888.
              gifEncoder.encodeFrame(bitmap, 300);
            }
          }
        } catch (FileNotFoundException e) {
          Log.w(TAG, "Cannot write to " + gifFile.getAbsolutePath(), e);
        } finally {
          gifEncoder.close();
        }
        Log.d(TAG, "Gif processed");
        listener.onGifCreated(gifFile);
      }
    });
  }

  private Bitmap bitmapOf(File nextPicPath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(nextPicPath.getAbsolutePath(), options);
    return bitmap;
  }
}
