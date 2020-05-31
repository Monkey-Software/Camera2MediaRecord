package com.brianhoang.recordvideo.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianhoang.recordvideo.R;
import com.brianhoang.recordvideo.ui.LineProgressView;
import com.brianhoang.recordvideo.ui.RecordView;
import com.brianhoang.recordvideo.utils.RecordFileUtil;
import com.brianhoang.recordvideo.viewer.ViewPhotoActivity;
import com.brianhoang.recordvideo.viewer.ViewVideoActivity;

import java.io.File;

public class CameraActivity extends CameraVideoActivity {
    public static final String TAG = "CameraActivity";

    public static final String INTENT_PATH = "intent_path";
    public static final String INTENT_DATA_TYPE = "result_data_type";

    public static final int RESULT_TYPE_VIDEO = 1;
    public static final int RESULT_TYPE_PHOTO = 2;

    public static final int REQUEST_CODE_VIDEO = 100;
    public static final int REQUEST_CODE_PHOTO = 101;

    ImageView ivSwitchCamera;
    LineProgressView lineProgressView;
    ImageView ivSwitchFlash;
    TextView tv_hint;
    RecordView recordView;
    private boolean isCapturePhoto = false;

    private String mOutputFilePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        setUpView();

        File tempPath = new File(getFilesDir().getPath() + "/VideoRecord/");
        RecordFileUtil.setFileDir(tempPath.getPath());
    }

    @Override
    public void onPause() {
        super.onPause();
        cleanRecord();
    }

    @Override
    public int getTextureResource() {
        return R.id.mTextureView;
    }

    @Override
    public void onCameraPreview(SurfaceTexture surfaceTexture) {
        Log.e(TAG, "onCameraPreview");

        if (isCapturePhoto) {
            isCapturePhoto = false;
            Bitmap bitmap = mTextureView.getBitmap();
            savePhoto(bitmap);
        }
    }

    @Override
    protected void setUpView() {
        super.setUpView();
        recordView = findViewById(R.id.recordView);
        ivSwitchCamera = findViewById(R.id.iv_camera_mode);
        lineProgressView = findViewById(R.id.lineProgressView);
        ivSwitchFlash = findViewById(R.id.iv_flash_video);
        tv_hint = findViewById(R.id.tv_hint);

        recordView.setOnGestureListener(new RecordView.OnGestureListener() {
            @Override
            public void onDown() {
                if (mIsRecordingVideo) {
                    try {
                        stopRecordingVideo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    startRecordingVideo();
                    //Receive out put file here
                    mOutputFilePath = getCurrentFile();
                }
            }

            @Override
            public void onUp() {
                upEvent();
                if (mIsRecordingVideo) {
                    try {
                        stopRecordingVideo();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClick() {
                isCapturePhoto = true;
            }
        });

        ivSwitchCamera.setOnClickListener(v -> {
            switchCamera();
            if (isFlashSupported) {
                ivSwitchFlash.setImageResource(R.drawable.ic_flash_off);
                ivSwitchFlash.setClickable(true);
                ivSwitchFlash.setColorFilter(Color.WHITE);
            } else {
                ivSwitchFlash.setImageResource(R.drawable.ic_flash_off);
                ivSwitchFlash.setClickable(false);
                ivSwitchFlash.setColorFilter(getResources().getColor(R.color.colorDisable));
            }
        });
        ivSwitchFlash.setOnClickListener(v -> {
            boolean flash = switchFlash();
            ivSwitchFlash.setImageResource(flash ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        });
    }

    private void upEvent() {
        initRecorderState();
        finishVideo();
    }

    private void initRecorderState() {

        if (mOutputFilePath != null) {
            tv_hint.setText(R.string.hold_for_re_record);
        } else {
            tv_hint.setText(R.string.capture_guide);
        }
        tv_hint.setVisibility(View.VISIBLE);

    }

    private void cleanRecord() {
        recordView.initState();
        mOutputFilePath = null;
        isCapturePhoto = false;
        ivSwitchFlash.setVisibility(View.VISIBLE);
        lineProgressView.setProgress(0);
    }

    public void finishVideo() {
        stopRecordingVideo();
        Intent intent = new Intent(CameraActivity.this, ViewVideoActivity.class);
        intent.putExtra(ViewVideoActivity.INTENT_PATH, Uri.fromFile(new File(mOutputFilePath)));
        intent.putExtra(ViewVideoActivity.INTENT_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VIDEO);
    }

    private void savePhoto(Bitmap bitmap) {
        new AsyncTask<Bitmap, Void, String>() {

            @Override
            protected String doInBackground(Bitmap... bitmaps) {
                Bitmap bm = bitmaps[0];
                return RecordFileUtil.saveBitmap(bitmap);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Intent intent = new Intent(CameraActivity.this, ViewPhotoActivity.class);
                intent.putExtra(ViewPhotoActivity.INTENT_PATH, result);
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }
        }.execute(bitmap);
    }


    /**
     * Create directory and return file
     * returning video file
     */
    protected String getOutputMediaFile() {
        return RecordFileUtil.createMp4FileInBox();
    }
}