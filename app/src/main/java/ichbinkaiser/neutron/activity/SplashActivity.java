package ichbinkaiser.neutron.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import ichbinkaiser.neutron.R;

public class SplashActivity extends Activity {
    private Loader loader = new Loader();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        if (!GameActivity.getResourceManager().isLoaded()) {
            GameActivity.resourceManager.initSounds(this);
            loader.start();
        } else {
            showMain(); // done loading show go to main
        }

    }

    public void showMain() {
        Intent scoreIntent = new Intent(this, MainActivity.class);
        startActivity(scoreIntent);
        finish();
    }

    void pause(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("Splash", e.toString());
        }
    }

    private class Loader implements Runnable {
        public void start() {
            Thread thread = new Thread(this);
            thread.setName("Loader");
            thread.setDaemon(true);
            thread.start();
        }

        public void run() {
            GameActivity.resourceManager.loadSounds();
            while (!(GameActivity.resourceManager.getSoundsLoaded() == GameActivity.resourceManager.getSoundLibrary().length)) {
                pause(1000);
            }

            GameActivity.resourceManager.loadBitmaps();
            while (!(GameActivity.resourceManager.getBitmapsLoaded() == GameActivity.resourceManager.getBitmapsSize())) {
                pause(1500);
            }

            GameActivity.getResourceManager().setLoaded(true);
            showMain(); // done loading show go to main
        }
    }
}
