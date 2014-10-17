package buzzballfruits.neutron;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity 
{
	private Loader loader = new Loader();
    @Override
    public void onCreate(Bundle savedinstancestate) 
    {
        super.onCreate(savedinstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        GameActivity.resourcemanager.initSounds(this);
        
        loader.start();
    }
    
    public void showMain()
	{	
		Intent scoreIntent = new Intent(this, MainActivity.class);
		startActivity(scoreIntent);
		finish();
	}
    
    private class Loader implements Runnable
    {
    	public void start()
    	{
    		Thread thread = new Thread(this);
    		thread.setName("Loader");
    		thread.start();
    	}

		public void run() 
		{
            GameActivity.resourcemanager.loadSounds();
			while (!(GameActivity.resourcemanager.soundsloaded == GameActivity.resourcemanager.soundlibrary.length))
            {
                pause(1000);
            }

            GameActivity.resourcemanager.loadBitmaps();
            while (!(GameActivity.resourcemanager.bitmapsloaded == GameActivity.resourcemanager.getBitmapsSize()))
            {
                pause(1500);
            }

            showMain(); // done loading show go to main
		}
    }

    void pause(int duration)
    {
        try
        {
            Thread.sleep(duration);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Log.e("Splash", e.toString());
        }
    }
}
