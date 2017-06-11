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

package fr.vpm.giffer.activity;

import android.content.Intent;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.giffer.giffer.R;

import java.io.File;

import fr.vpm.giffer.CreateGif;
import fr.vpm.giffer.CreateGif1;
import fr.vpm.giffer.PostToFacebookAlbum;

public class ShareGifActivity extends AppCompatActivity {

  private static final String TAG = "ShareGifActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private Handler mBackgroundHandler;

  private ImageView mGifVisualization;

  private TextView mTalkingToUser;

  private FloatingActionButton mShareToFbFab;

  private ProgressBar mProgress;

  private String gifFolderPath;
  private CallbackManager callbackManager;
  private String gifFileAbsolutePath;
  private PostToFacebookAlbum postToFacebookAlbum = new PostToFacebookAlbum();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_share_gif);
    mGifVisualization = (ImageView) findViewById(R.id.gif);
    mTalkingToUser = (TextView) findViewById(R.id.talkingToUser);
    mShareToFbFab = (FloatingActionButton) findViewById(R.id.share_picture);
    mShareToFbFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        postToFacebookAlbum.shareGifToFb(ShareGifActivity.this.gifFileAbsolutePath, new PostToFacebookAlbum.Listener() {
          @Override
          public void onPicturePublished() {
            Toast.makeText(ShareGifActivity.this, "Gif uploaded to album", Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
    mProgress = (ProgressBar) findViewById(R.id.gif_progress);
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

    callbackManager = CallbackManager.Factory.create();

    LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
    loginButton.setPublishPermissions("publish_actions");
    loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(LoginResult loginResult) {
        Toast.makeText(ShareGifActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onCancel() {
        // App code
      }

      @Override
      public void onError(FacebookException exception) {
        // App code
      }
    });

  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mTalkingToUser != null) {
      mTalkingToUser.setText("Processing your pics");
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
    if (mShareToFbFab != null) {
      mShareToFbFab.hide();
    }
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    callbackManager.onActivityResult(requestCode, resultCode, data);
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
    File pictureSessionDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        pictureSessionFolder);
    new CreateGif1().createGif(pictureSessionDir, getBackgroundHandler(), new CreateGif.Listener() {
      @Override
      public void onGifCreated(final File gifFile) {
        mProgress.post(new Runnable() {
          @Override
          public void run() {
            onGifProcessed(gifFile.getAbsolutePath());
          }
        });
      }
    });
  }

  private void onGifProcessed(String gifAbsolutePath) {
    this.gifFileAbsolutePath = gifAbsolutePath;
    mProgress.setVisibility(View.GONE);
    mTalkingToUser.setText("Gif processed");
    Log.d(TAG, "Gif processed");
    mShareToFbFab.show();
//    Glide.with(this)
//        .load(gifAbsolutePath)
//        .into(mGifVisualization);
  }
}
