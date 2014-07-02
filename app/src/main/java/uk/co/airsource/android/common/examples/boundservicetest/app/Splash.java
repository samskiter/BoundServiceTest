package uk.co.airsource.android.common.examples.boundservicetest.app;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class Splash extends Activity {
    private int SPLASH_MILLISECONDS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        runTimer();
    }

    private void runTimer() {
        Timer timeout = new Timer();
        timeout.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        goToMainActivity();
                    }
                });
            }
        }, SPLASH_MILLISECONDS);
    }

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        FrameLayout layout = (FrameLayout) findViewById(R.id.splashRoot); //HERE USE YOUR ROOT LAYOUT
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        int width = outSize.x;
        Animation moveRighttoLeft = AnimationUtils.loadAnimation(this, R.anim.right_slide_in);
        //apply the animation
        layout.startAnimation(moveRighttoLeft);
    }


}
