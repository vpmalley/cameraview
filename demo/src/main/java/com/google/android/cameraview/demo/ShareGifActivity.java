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
import android.graphics.Movie;
import android.os.Build;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShareGifActivity extends AppCompatActivity {

  private static final String TAG = "ShareGifActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private Handler mBackgroundHandler;

  private ImageView mGifVisualization;

  private TextView mTalkingToUser;

  private FloatingActionButton mTakingPictureFab;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    mGifVisualization = (ImageView) findViewById(R.id.gif);
    mTalkingToUser = (TextView) findViewById(R.id.talkingToUser);
    mTakingPictureFab = (FloatingActionButton) findViewById(R.id.share_picture);
    if (mTakingPictureFab != null) {
      // mTakingPictureFab.setOnClickListener();
    }
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }
    Intent i = getIntent();
    if (i != null) {
      String gifAbsolutePath = i.getStringExtra("GIF_PATH");
      /*
      FileInputStream gifFis = null;
      try {
        gifFis = new FileInputStream(new File(gifAbsolutePath));
        //Movie gifMovie = Movie.decodeStream(gifFis);
      } catch (FileNotFoundException e) {
        Log.w("ShareGifActivity", "failed getting gif", e);
      } finally {
        if (gifFis != null) {
          try {
            gifFis.close();
          } catch (IOException e) {
          }
        }
      }
      */
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
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

}
