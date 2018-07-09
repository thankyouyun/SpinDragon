package com.goodyun.spindragon;

import android.graphics.Bitmap;

import java.util.Random;

/**
 * Created by alfo6-19 on 2018-03-30.
 */

public class Item {
    int width, height;

    Bitmap img;
    int x, y;
    int w, h;
    boolean isDead;
    int kind;
    int dx, dy;
    int life = 240;

    public Item(int width, int height, Bitmap[] imgs, int ex, int ey) {
        this.width = width;
        this.height = height;
        x = ex; y = ey;
        Random rnd = new Random();
        //0:coin, 1:gem, 2:fast, 3:protect 4:magnet 5:bomb, 6:strong

        int n = rnd.nextInt(100);
        kind = n<66?0:n<67?1:n<77?2:n<79?3:n<89?4:n<90?5:6;

        img = imgs[kind];
        w = img.getWidth()/2;
        h = img.getHeight()/2;

        int k = rnd.nextBoolean()?1:-1;
        dx = w/6*k;
        k = rnd.nextBoolean()?1:-1;
        dy = w/6*k;

    }//constructor


    void move(int px,int py){
        double radian = Math.atan2(y-py,px-x);
        x = (int) (x+Math.cos(radian)*w);
        y = (int) (y-Math.sin(radian)*w);


    }

    void move(){
        life--;
        if(life<=0) isDead =true;


        x+=dx;
        y+=dy;
        if(x<=w){
            dx= -dx;
            x= w;
        }

        if(x>=width-w){
            dx= -dx;
            x= width-w;
        }

        if(y<=h){
            dy= -dy;
            y= h;
        }

        if(y>=height-h){
            dy= -dy;
            y= height-h;
        }



    }//move
}

