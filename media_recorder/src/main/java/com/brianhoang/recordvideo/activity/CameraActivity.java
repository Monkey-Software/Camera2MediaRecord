package com.brianhoang.recordvideo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianhoang.recordvideo.R;
import com.brianhoang.recordvideo.camera.CameraLogicActivity;
import com.brianhoang.recordvideo.ui.LineProgressView;
import com.brianhoang.recordvideo.ui.RecordView;
import com.brianhoang.recordvideo.utils.ProgressUpdate;
import com.brianhoang.recordvideo.utils.RecordFileUtil;

import java.io.File;
import java.util.TimerTask;

public class CameraActivity extends CameraLogicActivity {

    public static final String INTENT_PATH = "intent_path";
    public static final String INTENT_DATA_TYPE = "result_data_type";

    public static final int RESULT_TYPE_VIDEO = 1;
    public static final int RESULT_TYPE_PHOTO = 2;

    public static final int REQUEST_CODE_VIDEO = 100;
    public static final int REQUEST_CODE_PHOTO = 101;

    private static final int INTERVAL_UPDATE = 100;

    public static final float MAX_VIDEO_TIME = 10f * 1000;

    private LineProgressView lineProgressView;
    private ImageView ivSwitchFlash;
    private TextView tv_hint;
    private RecordView recordView;
    private boolean isCapturePhoto = false;

    private String mOutputFilePath;
    ProgressUpdate progressUpdate;


    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setUpView();

        File tempPath = new File(getFilesDir().getPath() + "/VideoRecord/");
        RecordFileUtil.setFileDir(tempPath.getPath());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing");
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
        if (isCapturePhoto) {
            isCapturePhoto = false;
            Bitmap bitmap = mTextureView.getBitmap();
            savePhoto(bitmap);
        }
    }

    @Override
    public Size maxCameraSize() {
        return new Size(1920, 1080);
    }

    @Override
    protected void setUpView() {
        super.setUpView();
        recordView = findViewById(R.id.recordView);
        ImageView ivSwitchCamera = findViewById(R.id.iv_camera_mode);
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
                }
            }

            @Override
            public void onUp() {
                upEvent();
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

        findViewById(R.id.close).setOnClickListener(v -> cancelRecord());
    }

    private void upEvent() {
        runOnUiThread(() -> {
            initRecorderState();
            finishVideo();
        });
    }

    @Override
    public void startRecordingVideo() {
        super.startRecordingVideo();

        //Receive out put file here
        mOutputFilePath = getCurrentFile();

        videoDuration = 0;
        recordTime = System.currentTimeMillis();
        progressUpdate = new ProgressUpdate();
        progressUpdate.startUpdate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                videoDuration += currentTime - recordTime;
                recordTime = currentTime;
                if (videoDuration <= MAX_VIDEO_TIME) {
                    lineProgressView.setProgress(videoDuration / MAX_VIDEO_TIME);
                } else {
                    upEvent();
                }
            }
        }, INTERVAL_UPDATE);
    }

    @Override
    public void stopRecordingVideo() {
        if (null != progressUpdate) {
            progressUpdate.stopUpdate();
        }

        videoDuration = 0;
        recordTime = System.currentTimeMillis();
        super.stopRecordingVideo();
    }

    private long videoDuration;
    private long recordTime;

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
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Bitmap... bitmaps) {
                Bitmap bm = bitmaps[0];
                return RecordFileUtil.saveBitmap(bm);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelRecord();
    }

    private void cancelRecord() {
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_VIDEO) {
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            } else if (requestCode == REQUEST_CODE_PHOTO) {
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            cleanRecord();
            initRecorderState();
        }
    }

}