package com.example.tmh.servicereceivernotify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    private final static int REQUESTCODE = 911;
    private final static int MILLION = 3000;
    private ImageView mImageCd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mImageCd = (ImageView) findViewById(R.id.image_cd);
        startAnimation();
    }

    @Override
    protected void onResume() {
        checkPermision();
        super.onResume();
    }

    private void checkPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUESTCODE);
            } else {
                autoChimCut(); // Tình huống 3: đã cấp quyền rồi => CÚT
            }
        } else {
            autoChimCut(); // Tình huống 2: Dưới phiên bản M => CÚT
        }
    }

    private void autoChimCut() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(MILLION);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUESTCODE) {
            autoChimCut(); // Tình huống 1: cung cấp xong => CÚT
        }
    }
    //Animation
    private void startAnimation() {
        mImageCd.setImageDrawable(getResources().getDrawable(R.drawable.ic_cd_80));
        Animation rotateLoading = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        mImageCd.clearAnimation();
        mImageCd.setAnimation(rotateLoading);
    }
}
