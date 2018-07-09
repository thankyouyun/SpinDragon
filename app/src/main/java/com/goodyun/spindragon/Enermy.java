package com.goodyun.spindragon;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by alfo6-19 on 2018-03-30.
 */

public class Enermy {
    int width, height;
    Bitmap img;
    int x, y;
    int w, h;
    boolean isDead = false;//미사일 맞아 죽은것..
    boolean isOut = false;//화면 밖에 나갔는가..
    boolean wasShow = false; //화면 안에 보여진적 있는가..?
    Rect rect;//화면사이즈 사각형 영역
    Bitmap[] imgs;
    int index; //이미지 날개짓 번호 ..
    int loop;
    int kind;//종류..

    double radian;//이동 각도..
    int speed;     //이동속도..

    int angle; //회전각도..
    int HP;

    Bitmap[] imgGs;
    Bitmap imgG;

    public Enermy(int width, int height, Bitmap[][] imgSrc, int px, int py, Bitmap[][] imgGauge) {
        this.width = width;
        this.height = height;

        Random rnd = new Random();
        //캐릭터 종류 0:white 1:yellow 2:pink
        int n = rnd.nextInt(10);
        kind = n < 6 ? 0 : n < 9 ? 1 : 2;

        //체력 white:1 ,yellow:5 ,pink:3
        HP = kind == 0 ? 1 : kind == 1 ? 5 : 3;

        imgs = imgSrc[kind];
        img = imgs[index];
        w = img.getWidth() / 2;
        h = img.getHeight() / 2;

        double r = Math.toRadians(rnd.nextInt(360));
        x = (int) (width / 2 + Math.cos(r) * width);
        y = (int) (height / 2 - Math.sin(r) * width);

        radian = Math.atan2(y - py, px - x);
        angle = (int) (270 - Math.toDegrees(radian));
        //적군 속도..
        speed = kind == 0 ? w / 5 : kind == 1 ? w / 7 : 8;

        rect = new Rect(0, 0, width, height);
        if (kind > 0) {
            imgGs = imgGauge[kind - 1];
            imgG = imgGs[0];
        }
    }//contructor


    void damaged(int n) {
        HP -= n;
        if (HP <= 0) {
            isDead = true;
            return;
        }
        imgG = imgGs[imgGs.length-HP];

    }
    void move(int px, int py) {

        //pink인 적군은 각도를 다시 계산
        if (kind == 2) {
            radian = Math.atan2(y - py, px - x);
            angle = (int) (270 - Math.toRadians(radian));

        }

        //날개짓 애니메이션..
        loop++;
        if (loop % 3 == 0) {
            index++;
            if (index > 3) index = 0;

            img = imgs[index];
        }

        // 이동...
        x = (int) (x + Math.cos(radian) * speed);
        y = (int) (y - Math.sin(radian) * speed);

        if (rect.contains(x, y)) {
            wasShow = true;
        }

        //화면에 한번이라도 보인적이 있는가..?
        if (wasShow) {
            //화면밖으로 나갔는가..
            if (x < -w || x > width + w || y < -h || y > height + h) {
                isOut = true;
            }//if

        }
    }//move


}//class..
