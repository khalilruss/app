package com.kierigby.bountyhunter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

public class HotColdActivity extends Activity {

    ImageView icon;
    ImageView icon2;
    ImageView imageView;
    ImageView imageView2;
    float val;
    float val2;
    ObjectAnimator animation;
    ObjectAnimator anim2;

    @Override
    protected void onCreate (Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);

        setContentView(R.layout.activity_hot_cold);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int) (height*0.8));

        startAnimation();
        addListenerToIcons();
    }

    private void addListenerToIcons() {
        icon2 = findViewById(R.id.icon2);
        imageView2 = findViewById(R.id.imageView2);

        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (icon.getVisibility() == View.VISIBLE) {
                    icon2.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                    icon.setVisibility(View.INVISIBLE);
                } else {
                    icon2.clearColorFilter();
                    icon.setVisibility(View.VISIBLE);
                }


            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getVisibility() == View.VISIBLE) {
                    imageView2.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                    imageView.setVisibility(View.INVISIBLE);
                } else {
                    imageView2.clearColorFilter();
                    imageView.setVisibility(View.VISIBLE);
                }


            }
        });
    }

    private void startAnimation() {
        icon = findViewById(R.id.icon);
        imageView = findViewById(R.id.imageView);

        animation = ObjectAnimator.ofFloat(icon, "translationY", val);
        animation.setDuration(5000);
        animation.start();

        anim2 = ObjectAnimator.ofFloat(imageView, "translationY", val2);
        anim2.setDuration(5000);
        anim2.start();
    }

}
