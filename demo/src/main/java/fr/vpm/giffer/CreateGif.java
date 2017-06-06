package fr.vpm.giffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.nbadal.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by vince on 06/06/17.
 */

public class CreateGif {

  public interface Listener {
    void onGifCreated(final File gifFile);
  }

  private static final String TAG = "CreateGif";

  public void createGif(final File pictureSessionDir, final Handler backgroundHandler,
                        final Listener listener) {
    backgroundHandler.post(new Runnable() {
      @Override
      public void run() {
        if (!pictureSessionDir.isDirectory()) {
          return;
        }
        File gifFile = new File(pictureSessionDir,
            "gif-" + System.currentTimeMillis() + ".gif");
        OutputStream os = null;
        ByteArrayOutputStream bos = null;
        try {
          AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
          bos = new ByteArrayOutputStream();
          gifEncoder.setDelay(300);
          gifEncoder.start(bos);
          for (File nextPic : pictureSessionDir.listFiles()) {
            if (nextPic.getName().startsWith("picture-")) {
              Log.d(TAG, "getting bitmap for pic " + nextPic.getName());
              Bitmap bitmap = bitmapOf(nextPic);
              Log.d(TAG, "adding frame for pic " + nextPic.getName());
              gifEncoder.addFrame(bitmap);
            }
          }
          gifEncoder.finish();

          os = new FileOutputStream(gifFile);
          os.write(bos.toByteArray());
          os.close();
          bos.close();
          Log.d(TAG, "Saved gif to " + gifFile.getAbsolutePath());
        } catch (IOException e) {
          Log.w(TAG, "Cannot write to " + gifFile, e);
        } finally {
          if (os != null) {
            try {
              os.close();
            } catch (IOException e) {
              // Ignore
            }
          }
        }
        if (bos != null) {
          try {
            bos.close();
          } catch (IOException e) {
            // Ignore
          }
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
