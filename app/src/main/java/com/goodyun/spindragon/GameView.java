package com.goodyun.spindragon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by alfo6-19 on 2018-03-28.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    Context context;
    SurfaceHolder holder;
    int width, height;
    GameThread gameThread;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        holder = getHolder();
        holder.addCallback(this);

    }//constructor


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //생성자가 끝나고 이 gameView가 화면에 보여지면 자동 호출
        //Game진행 작업 시작!!


    }//Create..

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //GameVIew가 화면에서 보이지 않으면 자동 실행

        if (gameThread == null) {
            width = getWidth();
            height = getHeight();

            gameThread = new GameThread();
            gameThread.start();


        } else {
            //게임을 재시작(resume) !
            gameThread.resumeThread();
        }


    }//changed..

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }//destroyed..


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x, y;

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
                gameThread.touchDown(x, y);


                break;
            case MotionEvent.ACTION_UP:
                x = (int) event.getX();
                y = (int) event.getY();
                gameThread.touchUp(x, y);

                break;
            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();
                y = (int) event.getY();
                gameThread.touchMove(x, y);
                break;


        }//switch


        return true;
    }//onTouchEvent

    void stopGame() {
        gameThread.stopThread();
    }

    void pauseGame() {
        gameThread.pauseThread();
    }

    void resumeGame() {
        gameThread.resumeThread();
    }


    //innnerclass
    class GameThread extends Thread {
        boolean isRun = true;
        boolean isWait = false;
        Bitmap imgBack;
        Bitmap imgJoypad;
        Bitmap[] imgMissile = new Bitmap[3];
        Bitmap[][] imgPlayer = new Bitmap[3][4];
        Bitmap[][] imgEnemy = new Bitmap[3][4];
        Bitmap[][] imgGauge = new Bitmap[2][];
        Bitmap[] imgDust = new Bitmap[6];
        Bitmap[] imgItem = new Bitmap[7];
        Bitmap imgProtect;
        Bitmap imgStrong;
        Bitmap imgBomb;

        //폭탄 버튼 변수들..
        Rect recBomb;
        boolean isBomb = false;//폭탄 버튼사용


        int protectRad;//보호막 이미지의 반지름
        int protectAng;//보호막 이미지 회전각도

        int posBack = 0; //배경이미지의 x좌표
        //조이패드 변수들..
        int jpx, jpy, jpr;//조이패드의 중심좌표..
        boolean isJaypad = false;//조이패드를 눌렀는가?

        //Bit맵의 투명도(alpha)를 적용하기 위한 paint객체
        Paint paint = new Paint();

        //플레이어 객체 참조변수
        Player me;
        int playerkind = 0;
        //미사일들 리스트 객체 참조변수
        ArrayList<Missaile> missailes = new ArrayList<>();
        //적군들 리스트
        ArrayList<Enermy> enermies = new ArrayList<>();
        //Dust들 리스트
        ArrayList<Dust> dusts = new ArrayList<>();
        //Item들 리스트
        ArrayList<Item> items = new ArrayList<>();

        Random rnd = new Random();


        //미사일 발사 간격
        int missileGap = 3;

        //적군 생성 간격
        int level = 1;

        //아이템의 지속 시간..
        int fastItem = 0;
        int protectItem = 0;
        int magnetItem = 0;
        int strongItem = 0;

        int bomb = 3;//폭탄개수
        int score = 0;
        int coin = 0;

        SoundPool sp;
        int sdChdie, sdFire, sdCoin, sdGem, sdProtect, sdItem, sdMondie, sdPumbkin;

        Vibrator vibrator;//진동 관리자..

        //초기값 설정 작업 메소드(마치 .. 생성자처럼..)
        void initial() {
            //그림들을 Bitmap객체로 생성 !
            createBitmaps();
            //플레이어 객체 생성..
            me = new Player(width, height, imgPlayer, playerkind);

            //조이패드의 초기값..
            jpx = jpr * 2;
            jpy = height - jpr * 2;

            //TextView들에 값 설정..
            setTextview();

            //효과음 변수들 작업
            sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            sdChdie = sp.load(context, R.raw.ch_die, 1);
            sdFire = sp.load(context, R.raw.fireball, 1);
            sdCoin = sp.load(context, R.raw.get_coin, 1);
            sdGem = sp.load(context, R.raw.get_gem, 1);
            sdProtect = sp.load(context, R.raw.get_invincible, 1);
            sdItem = sp.load(context, R.raw.get_item, 1);
            sdMondie = sp.load(context, R.raw.mon_die, 1);
            sdPumbkin = sp.load(context, R.raw.pumkin, 1);

            //진동관리자 객체 얻어오기..
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        }

        //GameActivity의 Textview에 값 설정
        void setTextview() {
            //Activity 클래스의 runOnUiThread()와 같은 역할을 하는 메소드..
            post(new Runnable() {
                @Override
                public void run() {
                    GameActivity ga = (GameActivity) context;
                    String s;

                    s = String.format("%07d", score);
                    ga.tvScore.setText(s);

                    s = String.format("%04d", coin);
                    ga.tvCoin.setText(s);

                    s = String.format("%04d", G.gem);
                    ga.tvGem.setText(s);

                    s = String.format("%04d", bomb);
                    ga.tvBomb.setText(s);

                    s = String.format("%07d", G.champion);
                    ga.tvChampion.setText(s);

                }
            });

        }

        //그림을 비트맵객체로 만들어내는 작업 메소드..
        void createBitmaps() {
            Resources res = context.getResources();

            //폭탙 버튼 이미지..
            int sizeBomb = height / 5;
            imgBomb = BitmapFactory.decodeResource(res, R.drawable.btn_bomb);
            imgBomb = Bitmap.createScaledBitmap(imgBomb, sizeBomb, sizeBomb, true);
            recBomb = new Rect(width - sizeBomb - imgBomb.getWidth() / 2, height - sizeBomb - imgBomb.getWidth() / 4, width - imgBomb.getWidth() / 2, height - imgBomb.getWidth() / 4);

            //보호막 이미지..
            imgProtect = BitmapFactory.decodeResource(res, R.drawable.effect_protect);
            imgProtect = Bitmap.createScaledBitmap(imgProtect, height / 4, height / 4, true);
            protectRad = imgProtect.getWidth() / 2;


            //강화 미사일
            imgStrong = BitmapFactory.decodeResource(res, R.drawable.bullet_04);
            imgStrong = Bitmap.createScaledBitmap(imgStrong, height / 10, height / 10, true);


            //item 이미지..
            for (int i = 0; i < 7; i++) {
                imgItem[i] = BitmapFactory.decodeResource(res, R.drawable.item_0_coin + i);
                imgItem[i] = Bitmap.createScaledBitmap(imgItem[i], height / 16, height / 16, true);

            }


            //Dust 이미지
            Bitmap img = BitmapFactory.decodeResource(res, R.drawable.dust);
            float[] ratio = new float[]{1.0f, 0.4f, 1.2f, 0.6f, 0.3f, 0.8f};
            for (int i = 0; i < 6; i++) {
                int size = (int) (height / 9 * ratio[i]);
                imgDust[i] = Bitmap.createScaledBitmap(img, size, size, true);

            }


            //조이패드 이미지..
            imgJoypad = BitmapFactory.decodeResource(res, R.drawable.img_joypad);
            imgJoypad = Bitmap.createScaledBitmap(imgJoypad, height / 4, height / 4, true);
            jpr = imgJoypad.getWidth() / 2;

            //Gauge 이미지..
            imgGauge[0] = new Bitmap[5];
            for (int i = 0; i < 5; i++) {
                imgGauge[0][i] = BitmapFactory.decodeResource(res, R.drawable.gauge_step5_01 + i);
                imgGauge[0][i] = Bitmap.createScaledBitmap(imgGauge[0][i], height / 9, height / 36, true);

            }

            imgGauge[1] = new Bitmap[3];
            for (int i = 0; i < 3; i++) {
                imgGauge[1][i] = BitmapFactory.decodeResource(res, R.drawable.gauge_step3_01 + i);
                imgGauge[1][i] = Bitmap.createScaledBitmap(imgGauge[1][i], height / 9, height / 36, true);

            }


            //미사일 이미지 만들기
            for (int i = 0; i < 3; i++) {
                imgMissile[i] = BitmapFactory.decodeResource(res, R.drawable.bullet_01 + i);
                imgMissile[i] = Bitmap.createScaledBitmap(imgMissile[i], height / 10, height / 10, true);
            }


            //배경이미지
            imgBack = BitmapFactory.decodeResource(res, R.drawable.back_01 + rnd.nextInt(8));
            imgBack = Bitmap.createScaledBitmap(imgBack, width, height, true);


            //적군 이미지..
            for (int i = 0; i < 3; i++) {//종류가 3종류
                for (int j = 0; j < 3; j++) {//날개가 4종류
                    imgEnemy[i][j] = BitmapFactory.decodeResource(res, R.drawable.enemy_a_01 + (i * 3) + j);
                    imgEnemy[i][j] = Bitmap.createScaledBitmap(imgEnemy[i][j], height / 9, height / 9, true);
                }
                imgEnemy[i][3] = imgEnemy[i][1];
            }//적군이미지..for문


            //플레이어 이미지..
            for (int i = 0; i < 3; i++) {
                imgPlayer[0][i] = BitmapFactory.decodeResource(res, R.drawable.char_a_01 + i);
                imgPlayer[0][i] = Bitmap.createScaledBitmap(imgPlayer[0][i], height / 8, height / 8, true);

            }
            imgPlayer[0][3] = imgPlayer[0][1];

            for (int i = 0; i < 3; i++) {
                imgPlayer[1][i] = BitmapFactory.decodeResource(res, R.drawable.char_b_01 + i);
                imgPlayer[1][i] = Bitmap.createScaledBitmap(imgPlayer[1][i], height / 8, height / 8, true);
            }
            imgPlayer[1][3] = imgPlayer[1][1];
            for (int i = 0; i < 3; i++) {
                imgPlayer[2][i] = BitmapFactory.decodeResource(res, R.drawable.char_c_01 + i);
                imgPlayer[2][i] = Bitmap.createScaledBitmap(imgPlayer[2][i], height / 8, height / 8, true);
            }
            imgPlayer[2][3] = imgPlayer[2][1];


        }

        //Resource 제거하기..
        void removeResource() {
            if (sp != null) {
                sp.release();
                sp = null;
            }

            imgJoypad.recycle();
            imgJoypad = null;
            imgBack.recycle();
            imgBack = null;
            imgProtect.recycle();
            imgProtect = null;
            imgStrong.recycle();
            imgStrong = null;
            imgBomb.recycle();
            imgBomb = null;

            for (int i = 0; i < imgItem.length; i++) {
                imgItem[i].recycle();
                imgItem[i] = null;
            }

            for (int i = 0; i < imgDust.length; i++) {
                imgDust[i].recycle();
                imgDust[i] = null;
            }

            for (int i = 0; i < imgGauge.length; i++) {
                for (int j = 0; j < imgGauge[i].length; j++) {
                    imgGauge[i][j].recycle();
                    imgGauge[i][j] = null;
                }
            }

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    imgPlayer[i][j].recycle();
                    imgPlayer[i][j] = null;
                }
                imgPlayer[i][3] = null;
            }//imgplayer


            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    imgEnemy[i][j].recycle();
                    imgEnemy[i][j] = null;
                }
                imgEnemy[i][3] = null;
            }//imgEnemy


            for (int i = 0; i < imgMissile.length; i++) {
                imgMissile[i].recycle();
                imgMissile[i] = null;
            }

        }//removeResource


        //2.1 화면에 보이는 모든 객체들 만들기...
        void makeAll() {


            //적군 만들기..
            int p = rnd.nextInt(8 - level);
            if (p == 0) {
                enermies.add(new Enermy(width, height, imgEnemy, me.x, me.y, imgGauge));
            }
            //미사일 만들기..
            if (fastItem > 0) {
                if (G.isSound) sp.play(sdFire, 0.1f, 0.1f, 0, 0, 1);
                missailes.add(new Missaile(width, height, imgMissile, me.x, me.y, me.angle, me.kind));
            } else {
                missileGap--;
                if (missileGap == 0) {
                    if (G.isSound) sp.play(sdFire, 0.1f, 0.1f, 0, 0, 1);
                    missailes.add(new Missaile(width, height, imgMissile, me.x, me.y, me.angle, me.kind));
                    missileGap = 3;
                }
            }
        }

        //2.2 움직이는 작업들...
        void moveAll() {

            //아이템들 움직이기..
            for (int i = items.size() - 1; i >= 0; i--) {
                Item t = items.get(i);

                if (magnetItem > 0 && t.kind < .2) {
                    t.move(me.x, me.y);
                } else {
                    t.move();
                }

                if (t.isDead) items.remove(i);
            }

            //Dust들 움직이기..
            for (int i = dusts.size() - 1; i >= 0; i--) {
                Dust t = dusts.get(i);
                t.move();
                if (t.isDead) {
                    dusts.remove(i);
                }

            }

            //적군들
            for (int i = enermies.size() - 1; i >= 0; i--) {
                Enermy t = enermies.get(i);
                t.move(me.x, me.y);
                if (t.isOut) {
                    enermies.remove(i);
                } else if (t.isDead) {
                    //점수 획득..
                    score += ((t.kind + 1) * 10)*(level/2==0?1:level);
                    setTextview();
                    //폭발 효과 생성..
                    dusts.add(new Dust(imgDust, t.x, t.y));
                    //효과음
                    if (G.isSound) sp.play(sdMondie, 0.7f, 0.7f, 1, 0, 1);
                    //아이템 생성..

                    items.add(new Item(width, height, imgItem, t.x, t.y));


                    enermies.remove(i);
                }
            }//적군들


            //미사일들 움직이기..
            for (int i = missailes.size() - 1; i >= 0; i--) {
                Missaile t = missailes.get(i);
                t.move();
                if (t.isDead) {
                    missailes.remove(i);
                }
            }


            //플레이어 움직이기..
            me.move();

            //배경 움직이기..
            posBack -= width / 600;
            if (posBack <= -width) posBack += width;


            //아이템 지속 시간 체크 메소드 호출
            checkItemTime();

        }

        //아이템 지속시간 체크 작업 메소드
        void checkItemTime() {
            if (fastItem > 0) {
                fastItem--;
                if (fastItem == 0) {
                    me.da = 3;
                }
            }

            if (protectItem > 0) protectItem--;
            if (magnetItem > 0) magnetItem--;
            if (strongItem > 0) strongItem--;


        }

        //아이템의 종류에 따른 작업 메소드
        void actionItem(int kind) {
            switch (kind) {
                case 0://coin
                    if (G.isSound) sp.play(sdCoin, 1, 1, 2, 0, 1);
                    coin++;
                    setTextview();
                    break;
                case 1://gem
                    if (G.isSound) sp.play(sdGem, 1, 1, 2, 0, 1);
                    G.gem++;
                    setTextview();
                    break;
                case 2://fast
                    if (G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    me.da = 9;//회전 변화량
                    fastItem = 200;
                    break;
                case 3://protect
                    if (G.isSound) sp.play(sdProtect, 0.7f, 0.7f, 4, 0, 1);
                    protectItem = 200;
                    break;
                case 4://magnet
                    if (G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    magnetItem = 200;
                    break;
                case 5://bomb
                    if (G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    bomb++;
                    setTextview();
                    break;
                case 6://strong
                    if (G.isSound) sp.play(sdItem, 1, 1, 3, 0, 1);
                    strongItem = 200;
                    break;

            }

        }//actionItem method

        //2.3 모든 충돌체크 작업
        void checkCollision() {

            //플레이어와 적군의 충돌 ..
            for (Enermy t : enermies) {
                //보호막이 있는가...
                if (protectItem > 0) {
                    if (Math.pow(me.x - t.x, 2) + Math.pow(me.y - t.y, 2) <= Math.pow(protectRad + t.w, 2)) {
                        t.isDead = true;
                    }
                } else {
                    if (Math.pow(me.x - t.x, 2) + Math.pow(me.y - t.y, 2) <= Math.pow(me.w + t.w, 2)) {
                        t.isDead = true;
                        me.HP--;
                        if (G.isVibrate) {
                            vibrator.vibrate(700);
                        }
                        if (me.HP <= 0) {
                            if (G.isSound) sp.play(sdChdie, 0.7f, 0.7f, 5, 0, 1);
                            //GAME OVER
                            //GameoverActivity 실행
                            //본사(GameActivity)에 요청 !
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putInt("Score", score);
                            data.putInt("Coin", coin);
                            msg.setData(data);
                            ((GameActivity) context).handler.sendMessage(msg);

                        }
                    }
                }// else문


            }


            //플레이어와 아이템의 충돌
            for (Item t : items) {
                if (Math.pow(me.x - t.x, 2) + Math.pow(me.y - t.y, 2) <= Math.pow(me.w + t.w, 2)) {
                    //아이템의 종류에 따른 동작수행
                    actionItem(t.kind);
                    t.isDead = true;
                    break;
                }
            }


            //미사일과 적군의 충돌
            for (Missaile t : missailes) {
                for (Enermy et : enermies) {
                    if (Math.pow(t.x - et.x, 2) + Math.pow(t.y - et.y, 2) <= Math.pow(t.w + et.w, 2)) {


                        //적군의 HP줄이기..
                        et.damaged(t.kind + 1);

                        //미사일 없애기..
                        if (strongItem == 0) t.isDead = true;
                        score += 5;

                        if (score >= 5000&&me.kind==0) {
                                me.kind++;
                        }

                        if (score >= 10000&&me.kind==1) {
                            me.kind++;
                        }

                        if(score>3000&&level==1){
                            level++;
                        }else if(score>5000&&level==2){
                            level++;
                        }else if(score>7000&&level==3){
                            level++;
                        }else if(score>10000&&level==4){
                            level++;
                        }else if(score>20000&&level==5){
                            level++;
                        }else if(score>25000&&level==6){
                            level++;
                        }

                        setTextview();
                        break;
                    }
                }
            }


        }//checkCollision

        //2.4 화면에 그려내는 모든 작업
        void drawAll(Canvas canvas) {

            //배경그리기..
            canvas.drawBitmap(imgBack, posBack, 0, null);
            canvas.drawBitmap(imgBack, posBack + width, 0, null);

            //적군 그리기
            for (Enermy t : enermies) {
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(t.img, t.x - t.w, t.y - t.h, null);
                canvas.restore();
                if (t.kind > 0) {
                    canvas.drawBitmap(t.imgG, t.x - t.w, t.y + t.h, null);

                }
            }

            //미사일 그리기
            for (Missaile t : missailes) {
                canvas.save();
                canvas.rotate(t.angle, t.x, t.y);
                canvas.drawBitmap(strongItem > 0 ? imgStrong : t.img, t.x - t.w, t.y - t.h, null);
                canvas.restore();
            }

            //아이템 그리기
            for (Item t : items) {
                canvas.drawBitmap(t.img, t.x - t.w, t.y - t.h, null);

            }


            //플레이어 그리기!
            canvas.save();
            canvas.rotate(me.angle, me.x, me.y);
            canvas.drawBitmap(me.img, me.x - me.w, me.y - me.h, null);
            canvas.restore();

            //보호막 이미지 그리기..
            if (protectItem > 0) {
                protectAng += 15;
                canvas.save();
                canvas.rotate(protectAng, me.x, me.y);
                canvas.drawBitmap(imgProtect, me.x - protectRad, me.y - protectRad, null);
                canvas.restore();
            }


            //Dust들 그리기..
            for (Dust t : dusts) {
                for (int i = 0; i < 6; i++) {
                    canvas.drawBitmap(t.img[i], t.x[i] - t.rad[i], t.y[i] - t.rad[i], null);

                }

            }


            //조이패드 그리기
            paint.setAlpha(isJaypad ? 200 : 130);//0~255 0=투명.. 255=불투명
            canvas.drawBitmap(imgJoypad, jpx - jpr, jpy - jpr, paint);

            //폭탄 버튼 그리기...
            paint.setAlpha(isBomb ? 200 : 130);//0~255 0=투명.. 255=불투명
            canvas.drawBitmap(imgBomb, recBomb.left, recBomb.top, paint);

        }

        //터치 작업 메소드..
        void touchDown(int x, int y) {


            //터치다운한 x,y 지점이 조이패드안인가??
            if (Math.pow(x - jpx, 2) + Math.pow(y - jpy, 2) <= Math.pow(jpr, 2)) {
                //터치다운한 x,y와 조이패드의 중심 좌표(jpx,jpy)사이의 각도 계산
                me.radian = Math.atan2(jpy - y, x - jpx);
                isJaypad = true;
                me.canMove = isJaypad;

            }


            //터치다운한 x,y지점이 폭탄버튼의 안에 있는가...
            if (recBomb.contains(x, y)) {
                isBomb = true;
                if (bomb > 0) {//폭탄개수가 1개이상인가..
                    if (G.isSound) sp.play(sdPumbkin, 1, 1, 4, 1, 1);
                    bomb--;
                    setTextview();
                    for (Enermy t : enermies) {
                        if (t.wasShow) t.isDead = true;
                    }
                }
            }


        }

        void touchUp(int x, int y) {

            isJaypad = false;
            me.canMove = isJaypad;

            isBomb = false;
        }

        void touchMove(int x, int y) {
            //조이패드의 각도를 계산..
            if (isJaypad) {
                me.radian = Math.atan2(jpy - y, x - jpx);
            }

        }


        @Override
        public void run() {
            //초기값 설정!!
            initial();

            Canvas canvas = null;

            while (isRun) {
                //1.canvas얻어오기
                canvas = holder.lockCanvas();

                //2.canvas에 원하는 작업 수행
                try {
                    synchronized (holder) {
                        //2.1 화면에 보여질 적군(객체)들 만들기 ..
                        makeAll();
                        //2.2 적군(객체 움직이기)
                        moveAll();
                        //2.3 충돌 체크 작업
                        checkCollision();
                        //2.4 그림 그리기...
                        drawAll(canvas);
                    }
                } finally {
                    //3.holder에게 canvas를 본사 post
                    holder.unlockCanvasAndPost(canvas);
                }

                //일시정지(pause)에 대한 체크..
                if (isWait) {
                    try {
                        synchronized (this) {
                            wait();//게임스레드 일시정지..
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }//while
            removeResource();

        }//run


        void stopThread() {
            isRun = false;
            synchronized (this) {
                this.notify();
            }
        }

        void pauseThread() {
            isWait = true;
        }

        void resumeThread() {
            isWait = false;
            synchronized (this) {
                this.notify();

            }
        }


    }//gameTHread...


}//gameView class..
