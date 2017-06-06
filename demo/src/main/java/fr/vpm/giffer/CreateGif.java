package fr.vpm.giffer;

import android.os.Handler;

import java.io.File;

/**
 * Created by vince on 06/06/17.
 */

public interface CreateGif {
  void createGif(final File pictureSessionDir, final Handler backgroundHandler,
                 final CreateGif.Listener listener);

  interface Listener {
    void onGifCreated(final File gifFile);
  }
}
