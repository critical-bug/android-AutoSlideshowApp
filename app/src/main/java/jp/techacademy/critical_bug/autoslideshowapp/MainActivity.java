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
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int PERIOD = 2000;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        public void run() {
            moveAndShow(1);
        }
    };
    private Cursor mCursor;
    private Timer mTimer;
    private Button nextButton;
    private Button prevButton;
    private Button autoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nextButton = (Button) findViewById(R.id.button);
        prevButton = (Button) findViewById(R.id.button2);
        autoButton = (Button) findViewById(R.id.button3);

        nextButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { moveAndShow(1); } });
        prevButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { moveAndShow(-1); } });
        autoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mTimer == null) {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(runnable);
                        }
                    }, 0, PERIOD);
                    Log.d("Slideshow", "started");
                    nextButton.setEnabled(false);
                    prevButton.setEnabled(false);
                } else {
                    mTimer.cancel();
                    mTimer = null;
                    Log.d("Slideshow", "stopped");
                    nextButton.setEnabled(true);
                    prevButton.setEnabled(true);
                }
            }
        });

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
        initializeCursorAndView();
    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        super.onDestroy();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Slideshow", "許可された");
                    initializeCursorAndView();
                } else {
                    Log.d("Slideshow", "許可されなかった");
                }
                break;
            default:
                break;
        }
    }

    private void initializeCursorAndView() {
        mCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor == null) {
            Log.d("Slideshow", "getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null) returned null");
        } else {
            if (mCursor.getCount() > 0) {
                moveAndShow(0);

                nextButton.setEnabled(true);
                prevButton.setEnabled(true);
                autoButton.setEnabled(true);
            }
        }
    }
}
