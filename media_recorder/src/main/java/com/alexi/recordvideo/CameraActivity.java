package com.alexi.recordvideo;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexi.recordvideo.camera.AutoFitTextureView;
import com.alexi.recordvideo.camera.CameraVideoActivity;
import com.alexi.recordvideo.ui.custom.LineProgressView;
import com.alexi.recordvideo.ui.custom.RecordView;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends CameraVideoActivity {

    private static final String TAG = "CameraFragment";
    private static final String VIDEO_DIRECTORY_NAME = "AndroidWave";


    ImageView iv_change_camera;
    LineProgressView lineProgressView;
    ImageView iv_flash_video;
    TextView tv_hint;
    RecordView recordView;

    private String mOutputFilePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        setUpView();
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
    protected void setUpView() {
        super.setUpView();
        recordView = findViewById(R.id.recordView);
        iv_change_camera = findViewById(R.id.iv_camera_mode);
        lineProgressView = findViewById(R.id.lineProgressView);
        iv_flash_video = findViewById(R.id.iv_flash_video);
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
                    mOutputFilePath = getCurrentFile().getAbsolutePath();
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
                // TODO Take take picture
            }
        });
    }


    private String parseVideo(String mFilePath) throws IOException {
        DataSource channel = new FileDataSourceImpl(mFilePath);
        IsoFile isoFile = new IsoFile(channel);
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        boolean isError = false;
        for (TrackBox trackBox : trackBoxes) {
            TimeToSampleBox.Entry firstEntry = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox().getTimeToSampleBox().getEntries().get(0);
            // Detect if first sample is a problem and fix it in isoFile
            // This is a hack. The audio deltas are 1024 for my files, and video deltas about 3000
            // 10000 seems sufficient since for 30 fps the normal delta is about 3000
            if (firstEntry.getDelta() > 10000) {
                isError = true;
                firstEntry.setDelta(3000);
            }
        }
        File file = getOutputMediaFile();
        String filePath = file.getAbsolutePath();
        if (isError) {
            Movie movie = new Movie();
            for (TrackBox trackBox : trackBoxes) {
                movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
            }
            movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
            Container out = new DefaultMp4Builder().build(movie);

            //delete file first!
            FileChannel fc = new RandomAccessFile(filePath, "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
            Log.d(TAG, "Finished correcting raw video");
            return filePath;
        }
        return mFilePath;
    }

    /**
     * Create directory and return file
     * returning video file
     */
    private File getOutputMediaFile() {
        // External sdcard file location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
                VIDEO_DIRECTORY_NAME);
        // Create storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + VIDEO_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "VID_" + timeStamp + ".mp4");
        return mediaFile;
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
        iv_flash_video.setVisibility(View.VISIBLE);
        lineProgressView.setProgress(0);
    }

    public void finishVideo() {
//        cameraRecord.stopRecord();
//        Intent intent = new Intent(RecordActivity.this, ViewVideoActivity.class);
//        intent.putExtra(ViewVideoActivity.INTENT_PATH, Uri.fromFile(new File(videoPath)));
//        intent.putExtra(ViewVideoActivity.INTENT_RESULT, true);
//        startActivityForResult(intent, REQUEST_CODE_VIDEO);
    }
}