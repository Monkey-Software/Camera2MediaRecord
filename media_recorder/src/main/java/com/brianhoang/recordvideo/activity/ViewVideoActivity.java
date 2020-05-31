package com.brianhoang.recordvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.brianhoang.recordvideo.R;
import com.brianhoang.recordvideo.ui.UniversalMediaController;
import com.brianhoang.recordvideo.ui.UniversalVideoView;

public class ViewVideoActivity extends AppCompatActivity implements UniversalVideoView.VideoViewCallback {
    public static final String TAG = "ViewVideoActivity";

    public static final String INTENT_PATH = "intent_path";
    public static final String INTENT_RESULT = "INTENT_RESULT";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

    ImageView ivSend;
    ConstraintLayout mVideoLayout;
    UniversalVideoView mVideoView;
    UniversalMediaController mMediaController;

    private int mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;

    private Uri videoUri;
    boolean shouldReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_video);

        // Get data
        videoUri = getIntent().getParcelableExtra(INTENT_PATH);
        if (videoUri == null) {
            onBackPressed();
        }

        shouldReturn = getIntent().getBooleanExtra(INTENT_RESULT, false);

        // Init views

        mVideoLayout = findViewById(R.id.video_layout);
        mVideoView = findViewById(R.id.videoView);
        mMediaController = findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
        ivSend = findViewById(R.id.ivSend);


        // Init callback
        mVideoView.setVideoViewCallback(this);

        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        ivSend.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(CameraActivity.INTENT_PATH, videoUri);
            intent.putExtra(CameraActivity.INTENT_DATA_TYPE, CameraActivity.RESULT_TYPE_VIDEO);
            setResult(RESULT_OK, intent);
            finish();
        });

        initVideoView();

        if (mSeekPosition > 0) {
            mVideoView.seekTo(mSeekPosition);
        }
        mVideoView.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            mVideoView.pause();
        }
    }

    private void initVideoView() {
        if (!shouldReturn) {
            // Hide sent button
            ivSend.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mMediaController.getLayoutParams();
            params.setMarginEnd(0);
            mMediaController.setLayoutParams(params);
        }


        mVideoLayout.post(() -> {
            cachedHeight = mVideoLayout.getHeight();
            ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
            videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoLayoutParams.height = cachedHeight;
            mVideoLayout.setLayoutParams(videoLayoutParams);
            mVideoView.setVideoURI(videoUri);
            mVideoView.requestFocus();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState != null) {
            mSeekPosition = savedInstanceState.getInt(SEEK_POSITION_KEY);
        }
        Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);

        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
        }

        switchTitleBar(!isFullscreen);
    }

    private void switchTitleBar(boolean show) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPause UniversalVideoView callback");
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingEnd UniversalVideoView callback");
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            if (shouldReturn) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }
}
