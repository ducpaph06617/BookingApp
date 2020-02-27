package com.dev.bookingapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.firebase.auth.FirebaseAuth;

import spencerstudios.com.bungeelib.Bungee;

public class SplashActivity extends AppCompatActivity {
    private ImageView imageView2;
    private TextView txtLogo;
    private Typeface typeface;
    private LinearLayout liner;
    private TextView txtGround;
    private NumberProgressBar numberProgressBar;

    public Animation animationIMG;

    public Animation animationGR;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imageView2 = findViewById(R.id.imageView2);
        txtLogo = findViewById(R.id.txtLogo);
        liner = findViewById(R.id.liner);
        txtGround = findViewById(R.id.txtGround);
        numberProgressBar = findViewById(R.id.number_progress_bar);
        typeface= Typeface.createFromAsset(this.getAssets(),"bellerose.ttf");
        animationIMG= AnimationUtils.loadAnimation(this,R.anim.anim3);
        animationGR= AnimationUtils.loadAnimation(this,R.anim.anim1);
        txtLogo.setTypeface(typeface);
        imageView2.setAnimation(animationIMG);
        txtLogo.setAnimation(animationIMG);
        txtGround.setTypeface(typeface);
        RunAble runAble=new RunAble(5,this);
        new Thread(runAble).start();



    }
    public class RunAble implements Runnable {
        int seconds;
        Context context;

        public RunAble(int seconds, Context context) {
            this.seconds = seconds;
            this.context = context;
        }

        @Override
        public void run() {
            for (int i = 0; i <=4; i++) {
                Handler handler = new Handler(Looper.getMainLooper());
                final int intI = i;
                handler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        if (intI==2){

                            liner.setVisibility(View.VISIBLE);
                            liner.setAnimation(animationGR);
                        }
                        if (intI==4){

                            RunAble1 runAble1=new RunAble1(5,SplashActivity.this);
                            new Thread(runAble1).start();
                        }
                    }

                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }
    }

    public class RunAble1 implements Runnable {
        int seconds;
        Context context;

        public RunAble1(int seconds, Context context) {
            this.seconds = seconds;
            this.context = context;
        }

        @Override
        public void run() {
            for (int i = 0; i <=100; i++) {
                Handler handler = new Handler(Looper.getMainLooper());
                final int intI = i;
                handler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        numberProgressBar.setVisibility(View.VISIBLE);
                        numberProgressBar.setProgress(intI);
                        if (intI==100){
                            if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                Log.e("KT","KT");
                            }else {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                Log.e("T","T");
                            }
                            Bungee.zoom(context);

                            finish();
                        }
                    }

                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
