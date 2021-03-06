package ichbinkaiser.neutron.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import ichbinkaiser.neutron.R;
import ichbinkaiser.neutron.core.ResourceManager;

public class SplashActivity extends Activity {
    private Loader loader = new Loader();

    private ResourceManager resourceManager = ResourceManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        if (!resourceManager.isLoaded()) {
            resourceManager.initSounds(this);
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

        @Override
        public void run() {
            resourceManager.loadSounds();
            while (!(resourceManager.getSoundsLoaded() == resourceManager.getSoundLibrary().length)) {
                pause(1000);
            }

            resourceManager.loadBitmaps();
            while (!(resourceManager.getBitmapsLoaded() == resourceManager.getBitmapsSize())) {
                pause(1500);
            }

            resourceManager.setLoaded(true);
            showMain(); // done loading show go to main
        }
    }
}
