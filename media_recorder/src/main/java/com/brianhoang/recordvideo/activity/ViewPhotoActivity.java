package com.brianhoang.recordvideo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.brianhoang.recordvideo.R;
import com.bumptech.glide.Glide;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity {

    public static final String INTENT_PATH = "intent_path";
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_photo);

        String videoPath = getIntent().getStringExtra(INTENT_PATH);
        if (videoPath != null) {
            imageUri = Uri.fromFile(new File(videoPath));
        } else {
            onBackPressed();
        }

        ImageView ivImage = findViewById(R.id.imageView);

        Glide.with(this)
                .load(imageUri)
                .into(ivImage);


        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.ivSend).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(CameraActivity.INTENT_PATH, imageUri);
            intent.putExtra(CameraActivity.INTENT_DATA_TYPE, CameraActivity.RESULT_TYPE_PHOTO);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
