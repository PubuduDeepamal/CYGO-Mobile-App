package com.example.cygoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class activity_main extends AppCompatActivity {
    String prevStarted = "yes";
    private static int SPLASH_SCREEN = 3000;

    Animation topAnim;
    ImageView cygo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if(!sharedPreferences.getBoolean(prevStarted,false)){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(prevStarted,Boolean.TRUE);
            editor.apply();

            topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);

            cygo = findViewById(R.id.logo);

            cygo.setAnimation(topAnim);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(activity_main.this, activity_get_start.class);
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View,String>(cygo,"logo");

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity_main.this,pairs);
                    startActivity(intent,options.toBundle());
                    finish();
                }
            },SPLASH_SCREEN);
        }else{
            topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);

            cygo = findViewById(R.id.logo);

            cygo.setAnimation(topAnim);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(activity_main.this,activity_sign_in.class);
                    Pair[] pairs = new Pair[1];
                    pairs[0] = new Pair<View,String>(cygo,"logo");

                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity_main.this,pairs);
                    startActivity(intent,options.toBundle());
                    finish();
                }
            },SPLASH_SCREEN);
        }



    }


}