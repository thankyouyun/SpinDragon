package com.goodyun.spindragon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {

    ImageView iv;
    //스케줄관리 객체 (비서객체)
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        iv = findViewById(R.id.yun_soft);
        //자바로 애니메이셩 효과
//        Animation ani = new AlphaAnimation(0.0f, 1.0f);
//        ani.setDuration(3000);

        //appear_logo.xml 문서를 읽어서 Animation객체로 생성
        Animation ani = AnimationUtils.loadAnimation(this,R.anim.appear_logo);
        iv.startAnimation(ani);

        //4초후에 MainActivity 실행행
        //스케줄 관리 객체에게 스케줄 등록
        timer.schedule(task,4000);

        //저장된 데이터들 로딩하기..
        loadData();


    }//onCreate

    void loadData(){
        SharedPreferences pref = getSharedPreferences("Data",MODE_PRIVATE);

        G.gem= pref.getInt("Gem", 0);
        G.champion= pref.getInt("Champion", 0);

        G.isMusic= pref.getBoolean("Music", true);
        G.isSound= pref.getBoolean("Sound", true);
        G.isVibrate= pref.getBoolean("Vibrate", true);

        G.championImg= pref.getString("ChampionImg", null);

    }



    //timer의 스케줄링 작업을 수행하는 객체생성
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            //스케줄링에 의해 4초후에 이메소드 실행
            Intent intent = new Intent(IntroActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
    };

}//class
