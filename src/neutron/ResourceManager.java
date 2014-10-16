package neutron;

import core.neutron.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseIntArray;

final class ResourceManager
{
    final static int POP = 0, LIFE_UP = 1, DING = 2, POPWALL = 3, DOWN = 4, HIT = 5, RESTART = 6, SPAWN = 7;
	SoundPool soundpool;
	SparseIntArray sounds;
	AudioManager  audiomanager;
	Context context;
	int soundsloaded = 0;
    int bitmapsloaded = 0;

    int[] drawablelibrary = new int[] {R.drawable.buzzball1, R.drawable.buzzball2, R.drawable.buzzball3, R.drawable.buzzball4, R.drawable.buzzball5, R.drawable.buzzball6};
	int[] soundlibrary = new int[] {R.raw.pop, R.raw.lifeup, R.raw.ding, R.raw.popwall, R.raw.down, R.raw.hit, R.raw.restart, R.raw.spawn};

    RollingObjectBitmap[] buzzballbitmaps = new RollingObjectBitmap[drawablelibrary.length]; //buzz ball bitmap

	public int getBitmapsSize()
	{
		return buzzballbitmaps.length * 360;
	}

	public void initSounds(Context context)
	{
		this.context = context;
		soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds = new SparseIntArray();
		audiomanager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void loadSounds() // load sounds to IntArray
	{
		soundsloaded = 0;
		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				soundsloaded++;
				Log.i("ResourceManager", "Sample" + Integer.toString(sampleId) + " loaded");
			}
		});

		for (int soundindex = 0; soundindex < soundlibrary.length; soundindex++)
		{
			sounds.put(soundindex + 1, soundpool.load(context, soundlibrary[soundindex], 1));
		}
	}

	public void loadBitmaps()
	{
		Matrix buzzball_matrix = new Matrix();
		for (int bitmapindex = 0; bitmapindex < buzzballbitmaps.length; bitmapindex++)
		{
			int centerX, centerY, source_width, source_height;
			Bitmap source;

			centerX = BitmapFactory.decodeResource(context.getResources(), drawablelibrary[bitmapindex]).getWidth() / 2;
			centerY = BitmapFactory.decodeResource(context.getResources(), drawablelibrary[bitmapindex]).getHeight() / 2;

			source = BitmapFactory.decodeResource(context.getResources(), drawablelibrary[bitmapindex]);
			source_width = BitmapFactory.decodeResource(context.getResources(), drawablelibrary[bitmapindex]).getWidth();
			source_height = BitmapFactory.decodeResource(context.getResources(), drawablelibrary[bitmapindex]).getHeight();

			Log.i("ResourceManager", "Source buzzball " + bitmapindex + " created.");
			buzzballbitmaps[bitmapindex] = new RollingObjectBitmap(centerX , centerY, source_width, source_height);

			for (int frame = 0; frame < 360; frame++)
			{
				buzzball_matrix.setRotate(frame, centerX, centerY);
				
				if (frame % 2 == 0)
					buzzballbitmaps[bitmapindex].setFrame(Bitmap.createBitmap(source, 0, 0, source_width, source_height, buzzball_matrix, true), frame);
				else
					buzzballbitmaps[bitmapindex].setFrame(buzzballbitmaps[bitmapindex].getFrame(frame -1), frame);
				
				bitmapsloaded++;
			}
			Log.i("ResourceManager", "Successfully created Buzzball bitmap " + Integer.toString(bitmapindex));
		}
	}

	public void playSound(int index, float speed)
	{
		if (soundsloaded==8)
		{
			float streamVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			soundpool.play(sounds.get(index), streamVolume, streamVolume, 1, 0, speed);
		}
	}

	public void doCleanup()
	{
		soundpool.release();
	}
}
