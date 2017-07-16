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

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.vpm.giffer.PicturesDirectory;
import fr.vpm.giffer.PostToTumblrBlog;
import fr.vpm.giffer.R;
import fr.vpm.giffer.giffer.CameraView;

/**
 * This demo app saves the taken picture to a constant file.
 * $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
 */
public class CameraActivity extends AppCompatActivity implements
    ActivityCompat.OnRequestPermissionsResultCallback {

  private static final String TAG = "CameraActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private CameraView mCameraView;

  private Handler mBackgroundHandler;

  private TextView mTalkingToUser;

  private FloatingActionButton mTakingPictureFab;

  private ProgressBar mProgress;

  private PostToTumblrBlog postToTumblr;
  private String mPictureSessionFolder;
  private View.OnClickListener mOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.take_picture:
          if (!postToTumblr.hasAccessToken()) {
            Log.d("CameraA", "authenticating");
            postToTumblr.authenticate(getBackgroundHandler());
            return;
          }
          Log.d("CameraA", "taking pics");
          if (mCameraView != null) {
            mTakingPictureFab.hide();
            mPictureSessionFolder = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.FRANCE).format(new Date());
            File pictureSessionDir = PicturesDirectory.get(mPictureSessionFolder);
            boolean mkdir = pictureSessionDir.mkdir();
            Log.d(TAG, "Creating session folder : " + mkdir);

            new CountDownTimer(4000, 1000) {

              @Override
              public void onTick(long l) {
                long leftSeconds = l / 1000;
                mTalkingToUser.setText(String.valueOf(leftSeconds));
              }

              @Override
              public void onFinish() {
                mTalkingToUser.setText("Cheeeese!");
                mCameraView.takePicture();
              }
            }.start();
          }
          break;
      }
    }
  };
  private CameraView.Callback mCallback
      = new CameraView.Callback() {

    @Override
    public void onCameraOpened(CameraView cameraView) {
      Log.d(TAG, "onCameraOpened");
    }

    @Override
    public void onCameraClosed(CameraView cameraView) {
      Log.d(TAG, "onCameraClosed");
    }

    @Override
    public void onPictureTaken(CameraView cameraView, final byte[] data) {
      Log.d(TAG, "onPictureTaken " + data.length);
      getBackgroundHandler().post(new Runnable() {
        @Override
        public void run() {
          File pictureSessionDir = PicturesDirectory.get(mPictureSessionFolder);
          Log.d(TAG, pictureSessionDir.getAbsolutePath());
          File file = new File(pictureSessionDir,
              "picture-" + System.currentTimeMillis() + ".jpg");
          Log.d(TAG, "Saving to " + file.getAbsolutePath());
          OutputStream os = null;
          try {
            os = new FileOutputStream(file);
            os.write(data);
            os.close();
          } catch (IOException e) {
            Log.w(TAG, "Cannot write to " + file, e);
          } finally {
            if (os != null) {
              try {
                os.close();
              } catch (IOException e) {
                // Ignore
              }
            }
          }
        }
      });
    }

    @Override
    public void onProgress(long percentage) {
      Log.d(TAG, "made progress at " + percentage);
      mProgress.setProgress((int) (100 - percentage));
      if (percentage > 90) {
        mProgress.setVisibility(View.VISIBLE);
      }
    }

    @Override
    public void onAllPicturesTaken() {
      Log.d(TAG, "onAllPicturesTaken");
      mProgress.setVisibility(View.GONE);

      Intent i = new Intent(CameraActivity.this, ShareGifActivity.class);
      i.putExtra("GIF_PATH", mPictureSessionFolder);
      startActivity(i);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    postToTumblr = new PostToTumblrBlog(this);
    mCameraView = (CameraView) findViewById(R.id.camera);
    if (mCameraView != null) {
      mCameraView.addCallback(mCallback);
    }
    mTalkingToUser = (TextView) findViewById(R.id.talkingToUser);
    mTakingPictureFab = (FloatingActionButton) findViewById(R.id.take_picture);
    if (mTakingPictureFab != null) {
      mTakingPictureFab.setOnClickListener(mOnClickListener);
    }
    mProgress = (ProgressBar) findViewById(R.id.picture_progress);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
    }
    Intent intent = getIntent();
    if ((intent != null) && (intent.getData() != null)) {
      Uri uri = intent.getData();
      String oauthVerifier = uri.getQueryParameter("oauth_verifier");
      Log.d("POST-PHOTO", oauthVerifier);
      postToTumblr.getOAuthToken(oauthVerifier);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      mCameraView.start();
    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.CAMERA)) {
      ConfirmationDialogFragment
          .newInstance(R.string.camera_permission_confirmation,
              new String[]{Manifest.permission.CAMERA},
              REQUEST_CAMERA_PERMISSION,
              R.string.camera_permission_not_granted)
          .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
    } else {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
          REQUEST_CAMERA_PERMISSION);
    }
    if (mTalkingToUser != null) {
      mTalkingToUser.setText("");
    }
    if (mTakingPictureFab != null) {
      mTakingPictureFab.show();
    }
  }

  @Override
  protected void onPause() {
    mCameraView.stop();
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_CAMERA_PERMISSION:
        if (permissions.length != 1 || grantResults.length != 1) {
          throw new RuntimeException("Error on requesting camera permission.");
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
          Toast.makeText(this, R.string.camera_permission_not_granted,
              Toast.LENGTH_SHORT).show();
        }
        // No need to start camera here; it is handled by onResume
        break;
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
      case R.id.switch_camera:
        if (mCameraView != null) {
          int facing = mCameraView.getFacing();
          mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
              CameraView.FACING_BACK : CameraView.FACING_FRONT);
        }
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

  public static class ConfirmationDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_PERMISSIONS = "permissions";
    private static final String ARG_REQUEST_CODE = "request_code";
    private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

    public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                         String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
      ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_MESSAGE, message);
      args.putStringArray(ARG_PERMISSIONS, permissions);
      args.putInt(ARG_REQUEST_CODE, requestCode);
      args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
      fragment.setArguments(args);
      return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Bundle args = getArguments();
      return new AlertDialog.Builder(getActivity())
          .setMessage(args.getInt(ARG_MESSAGE))
          .setPositiveButton(android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                  if (permissions == null) {
                    throw new IllegalArgumentException();
                  }
                  ActivityCompat.requestPermissions(getActivity(),
                      permissions, args.getInt(ARG_REQUEST_CODE));
                }
              })
          .setNegativeButton(android.R.string.cancel,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  Toast.makeText(getActivity(),
                      args.getInt(ARG_NOT_GRANTED_MESSAGE),
                      Toast.LENGTH_SHORT).show();
                }
              })
          .create();
    }

  }

}
