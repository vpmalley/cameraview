/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.cameraview.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nbadal.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ShareGifActivity extends AppCompatActivity {

  private static final String TAG = "ShareGifActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private Handler mBackgroundHandler;

  private ImageView mGifVisualization;

  private TextView mTalkingToUser;

  private FloatingActionButton mTakingPictureFab;
  private String gifFolderPath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    mGifVisualization = (ImageView) findViewById(R.id.gif);
    mTalkingToUser = (TextView) findViewById(R.id.talkingToUser);
    mTakingPictureFab = (FloatingActionButton) findViewById(R.id.share_picture);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }
    Intent i = getIntent();
    if (i != null) {
      gifFolderPath = i.getStringExtra("GIF_PATH");
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mTakingPictureFab != null) {
      // mTakingPictureFab.setOnClickListener();
      mTakingPictureFab.hide();
    }
    File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    File picturesSessionDir = new File(externalFilesDir, gifFolderPath);
    File[] files = picturesSessionDir.listFiles();
    if ((files.length > 0) && (mGifVisualization != null)) {
      Glide.with(this)
          .load(files[0].getAbsolutePath())
          .into(mGifVisualization);
    }
    createGif(gifFolderPath);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mBackgroundHandler != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        mBackgroundHandler.getLooper().quitSafely();
      } else {
        mBackgroundHandler.getLooper().quit();
      }
      mBackgroundHandler = null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.aspect_ratio:
        return true;
      case R.id.switch_flash:
        return true;
      case R.id.switch_camera:
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private Handler getBackgroundHandler() {
    if (mBackgroundHandler == null) {
      HandlerThread thread = new HandlerThread("background");
      thread.start();
      mBackgroundHandler = new Handler(thread.getLooper());
    }
    return mBackgroundHandler;
  }

  private void createGif(final String pictureSessionFolder) {
    getBackgroundHandler().post(new Runnable() {
      @Override
      public void run() {
        File pictureSessionDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), pictureSessionFolder);
        if (!pictureSessionDir.isDirectory()) {
          return;
        }
        final File gifFile = new File(pictureSessionDir,
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
        /*
        mTalkingToUser.post(new Runnable() {
          @Override
          public void run() {
            onGifProcessed(gifFile.getAbsolutePath());
          }
        });
        */
      }
    });
  }

  private void onGifProcessed(String gifAbsolutePath) {
    mTalkingToUser.setText("Gif processed");
    Log.d(TAG, "Gif processed");
    mTakingPictureFab.show();
    Glide.with(this)
        .load(gifAbsolutePath)
        .into(mGifVisualization);
  }

  private Bitmap bitmapOf(File nextPicPath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(nextPicPath.getAbsolutePath(), options);
    return bitmap;
  }

}
