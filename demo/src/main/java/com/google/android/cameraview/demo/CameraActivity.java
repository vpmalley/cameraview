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

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.nbadal.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * This demo app saves the taken picture to a constant file.
 * $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
 */
public class CameraActivity extends AppCompatActivity implements
    ActivityCompat.OnRequestPermissionsResultCallback,
    AspectRatioFragment.Listener {

  private static final String TAG = "CameraActivity";

  private static final int REQUEST_CAMERA_PERMISSION = 1;

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final int[] FLASH_OPTIONS = {
      CameraView.FLASH_AUTO,
      CameraView.FLASH_OFF,
      CameraView.FLASH_ON,
  };

  private static final int[] FLASH_ICONS = {
      R.drawable.ic_flash_auto,
      R.drawable.ic_flash_off,
      R.drawable.ic_flash_on,
  };

  private static final int[] FLASH_TITLES = {
      R.string.flash_auto,
      R.string.flash_off,
      R.string.flash_on,
  };

  private int mCurrentFlash;

  private CameraView mCameraView;

  private Handler mBackgroundHandler;

  private TextView mTalkingToUser;

  private FloatingActionButton mTakingPictureFab;

  private ProgressBar mProgress;

  private View.OnClickListener mOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.take_picture:
          if (mCameraView != null) {
            mTakingPictureFab.hide();
            mPictureSessionFolder = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.FRANCE).format(new Date());
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
  private String mPictureSessionFolder;
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
          File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
          File pictureSessionDir = new File(externalFilesDir, mPictureSessionFolder);
          pictureSessionDir.mkdir();
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
      if (percentage > 95) {
        mProgress.setVisibility(View.VISIBLE);
      }
    }

    @Override
    public void onAllPicturesTaken() {
      mProgress.setVisibility(View.GONE);
      mTalkingToUser.setText("Processing gif");
      createGif(mPictureSessionFolder);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
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
      case R.id.aspect_ratio:
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mCameraView != null
            && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
          final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
          final AspectRatio currentRatio = mCameraView.getAspectRatio();
          AspectRatioFragment.newInstance(ratios, currentRatio)
              .show(fragmentManager, FRAGMENT_DIALOG);
        }
        return true;
      case R.id.switch_flash:
        if (mCameraView != null) {
          mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
          item.setTitle(FLASH_TITLES[mCurrentFlash]);
          item.setIcon(FLASH_ICONS[mCurrentFlash]);
          mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
        return true;
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

  @Override
  public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
    if (mCameraView != null) {
      Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
      mCameraView.setAspectRatio(ratio);
    }
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
              Log.d(TAG, "adding frame for pic " + nextPic.getName());
              gifEncoder.addFrame(bitmapOf(nextPic));
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
        mTalkingToUser.post(new Runnable() {
          @Override
          public void run() {
            onGifProcessed(gifFile.getAbsolutePath());
          }
        });
      }
    });
  }

  private void onGifProcessed(String gifAbsolutePath) {
    mTalkingToUser.setText("Gif processed");
    mTakingPictureFab.show();
    Intent i = new Intent(this, ShareGifActivity.class);
    i.putExtra("GIF_PATH", gifAbsolutePath);
    startActivity(i);
  }

  private Bitmap bitmapOf(File nextPicPath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(nextPicPath.getAbsolutePath(), options);
    return bitmap;
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
