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
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_splash);

		if (!GameActivity.RESOURCEMANAGER.loaded)
		{
			GameActivity.RESOURCEMANAGER.initSounds(this);
			loader.start();
		}

		else
		{
			showMain(); // done loading show go to main
		}

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
			thread.setDaemon(true);
			thread.start();
		}

		public void run()
		{
			GameActivity.RESOURCEMANAGER.loadSounds();
			while (!(GameActivity.RESOURCEMANAGER.soundsLoaded == GameActivity.RESOURCEMANAGER.soundLibrary.length))
			{
				pause(1000);
			}

			GameActivity.RESOURCEMANAGER.loadBitmaps();
			while (!(GameActivity.RESOURCEMANAGER.bitmapsLoaded == GameActivity.RESOURCEMANAGER.getBitmapsSize()))
			{
				pause(1500);
			}

			GameActivity.RESOURCEMANAGER.loaded = true;
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
