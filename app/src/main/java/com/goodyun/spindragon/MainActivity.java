package com.goodyun.spindragon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mp;
    SoundPool sp;
    int uiBtn;
    Animation ani;
    ToggleButton tbMusic, tbSound, tbVibrete;
    ImageView btnStart;
    View dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int checkSelfPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, 100);

            }

        }

        tbMusic = findViewById(R.id.tb_music);
        tbSound = findViewById(R.id.tb_sound);
        tbVibrete = findViewById(R.id.tb_vibrate);

        btnStart = findViewById(R.id.btn_start);

        mp = MediaPlayer.create(this, R.raw.dragon_flight);
        mp.setLooping(true);

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        uiBtn = sp.load(this, R.raw.ui_button, 1);

        tbMusic.setOnCheckedChangeListener(checkedChangeListener);
        tbSound.setOnCheckedChangeListener(checkedChangeListener);
        tbVibrete.setOnCheckedChangeListener(checkedChangeListener);

        tbMusic.setChecked(G.isMusic);
        tbSound.setChecked(G.isSound);
        tbVibrete.setChecked(G.isVibrate);


    }//onCreate

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

            switch (compoundButton.getId()) {
                case R.id.tb_music:
                    G.isMusic = checked;
                    if (G.isMusic) mp.setVolume(0.5f, 0.5f);
                    else mp.setVolume(0, 0);
                    break;

                case R.id.tb_sound:
                    G.isSound = checked;

                    break;

                case R.id.tb_vibrate:
                    G.isVibrate = checked;

                    break;
            }

        }
    };

    @Override
    protected void onResume() {
        btnStart.setClickable(true);
        if (G.isMusic) {
            mp.setVolume(0.5f, 0.5f);
        } else {
            mp.setVolume(0.0f, 0.0f);
        }

        mp.start();

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mp != null) {
            mp.pause();
        }

        if(dialog!=null) {
            dialog.setVisibility(View.GONE);
            dialog = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;

        }
        super.onDestroy();
    }


    public void clickStart(View v) {
        btnStart.setClickable(false);
        if (G.isSound) sp.play(uiBtn, 0.7f, 0.7f, 2, 1, 1);
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
    }

    public void clicksetting(View v) {

        if (dialog != null) return;

        if (G.isSound) sp.play(uiBtn, 0.7f, 0.7f, 2, 1, 1);

        dialog = findViewById(R.id.dialog_setting);
        dialog.setVisibility(View.VISIBLE);
        ani = AnimationUtils.loadAnimation(this, R.anim.appear_dialog);
        dialog.startAnimation(ani);

    }

    public void clickExit(View v) {
        if (G.isSound) sp.play(uiBtn, 0.7f, 0.7f, 2, 1, 1);
        finish();

    }

    public void clickBtn(View v) {
        if (G.isSound) sp.play(uiBtn, 0.7f, 0.7f, 2, 1, 1);

        ani = AnimationUtils.loadAnimation(this, R.anim.disappear_dialog);

        ani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialog.setVisibility(View.GONE);
                dialog = null;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dialog.startAnimation(ani);

    }
}
