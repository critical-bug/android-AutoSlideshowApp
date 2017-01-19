package jp.techacademy.critical_bug.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    // ?
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Cursor mCursor;
    private Handler mHandler = null;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            moveAndShow(1);
            mHandler.postDelayed(mRunnable, period);
        }
    };
    private static final int period = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() { public void onClick(View v) { moveAndShow(1); } });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() { public void onClick(View v) { moveAndShow(-1); } });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() { public void onClick(View v){
            if (mHandler == null) {
                mHandler = new Handler();
                mHandler.postDelayed(mRunnable, period);
                Log.d("Slideshow", "started");
                findViewById(R.id.button).setEnabled(false);
                findViewById(R.id.button2).setEnabled(false);
            } else {
                mHandler.removeCallbacks(mRunnable);
                mHandler = null;
                Log.d("Slideshow", "stopped");
                findViewById(R.id.button).setEnabled(true);
                findViewById(R.id.button2).setEnabled(true);
            }
        }});

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        } else {
            getContentsInfo();
        }
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    private void getContentsInfo() {
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        moveAndShow(0);
    }

    private void moveAndShow(int direction) {
        boolean result;
        if (direction < 0) {
            result = mCursor.isFirst() ? mCursor.moveToLast() : mCursor.moveToPrevious();
        } else if (direction > 0) {
            result = mCursor.isLast() ? mCursor.moveToFirst() : mCursor.moveToNext();
        } else {
            result = mCursor.moveToFirst();
        }
        if (result) {
            int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = mCursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView iv = (ImageView) findViewById(R.id.imageView);
            iv.setImageURI(imageUri);
        }
    }
}
