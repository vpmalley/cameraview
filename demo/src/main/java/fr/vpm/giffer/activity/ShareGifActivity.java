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
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.File;

import fr.vpm.giffer.CreateGif;
import fr.vpm.giffer.CreateGifAsVideo;
import fr.vpm.giffer.PicturesDirectory;
import fr.vpm.giffer.PostToFacebookAlbum;
import fr.vpm.giffer.PostToTumblrBlog;
import fr.vpm.giffer.R;

public class ShareGifActivity extends AppCompatActivity {

  private static final String TAG = "ShareGifActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private Handler mBackgroundHandler;

  private ImageView mGifVisualization;

  private VideoView mGifShow;

  private TextView mTalkingToUser;

  private FloatingActionButton mShareToFbFab;

  private FloatingActionButton mBackToCamera;

  private ProgressBar mProgress;

  private String gifFolderPath;
  private CallbackManager callbackManager;
  private String gifFileAbsolutePath;
  private PostToFacebookAlbum postToFacebookAlbum = new PostToFacebookAlbum();
  private PostToTumblrBlog postToTumblr;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("SHARE-GIF", "onCreate");
    setContentView(R.layout.activity_share_gif);
    postToTumblr = new PostToTumblrBlog(this);
    mGifVisualization = (ImageView) findViewById(R.id.gif);
    mGifShow = (VideoView) findViewById(R.id.gif_video);
    mTalkingToUser = (TextView) findViewById(R.id.talkingToUser);
    mShareToFbFab = (FloatingActionButton) findViewById(R.id.share_picture);
    mBackToCamera = (FloatingActionButton) findViewById(R.id.back_to_camera);
    mProgress = (ProgressBar) findViewById(R.id.gif_progress);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }
    setFabClickListeners();
    extractPathFromIntent();
    prepareFacebookLogin();
  }

  private void setFabClickListeners() {
    mShareToFbFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mProgress.setVisibility(View.VISIBLE);
        postToFacebookAlbum.shareGifToFb(ShareGifActivity.this.gifFileAbsolutePath, ShareGifActivity.this, new PostToFacebookAlbum.Listener() {
          @Override
          public void onPicturePublished() {
            Toast.makeText(ShareGifActivity.this, "Your video is shared on Facebook :) Check the page", Toast.LENGTH_LONG).show();
            mProgress.setVisibility(View.GONE);
            onBackPressed();
          }
        });
        //postToTumblr.post(new File(ShareGifActivity.this.gifFileAbsolutePath));
      }
    });
    mBackToCamera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });
  }

  private void prepareFacebookLogin() {
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

  private void extractPathFromIntent() {
    Intent i = getIntent();
    if (i != null) {
      gifFolderPath = i.getStringExtra("GIF_PATH");
    }
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
    File pictureSessionDir = PicturesDirectory.get(gifFolderPath);;
    File[] files = pictureSessionDir.listFiles();
    if ((files != null) && (files.length > 0) && (mGifVisualization != null)) {
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
    Log.d("SHARE-GIF", "onActivityResult with " + requestCode + " " + resultCode);
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
    File pictureSessionDir = PicturesDirectory.get(pictureSessionFolder);
    new CreateGifAsVideo().createGif(pictureSessionDir, getBackgroundHandler(), new CreateGif.Listener() {
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
    Log.d(TAG, "Gif processed : " + gifAbsolutePath);
    mShareToFbFab.show();
//    Uri uri = Uri.parse(gifAbsolutePath);
//    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//    intent.setDataAndType(uri, "video/mp4");
//    startActivity(intent);
  }
}
