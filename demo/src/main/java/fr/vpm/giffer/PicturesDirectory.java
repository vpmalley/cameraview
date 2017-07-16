package fr.vpm.giffer;

import android.os.Environment;

import java.io.File;

/**
 * Created by vince on 16/07/17.
 */

public class PicturesDirectory {

  public static File get(String pictureSessionFolderPath) {
    File externalFilesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    File gifferFilesDir = new File(externalFilesDir, "Giffer");
    return new File(gifferFilesDir, pictureSessionFolderPath);
  }
}
