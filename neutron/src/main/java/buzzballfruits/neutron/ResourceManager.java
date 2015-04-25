package buzzballfruits.neutron;

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
	enum Sound
	{
		POP, LIFE_UP, DING, POPWALL, DOWN, HIT, RESTART, SPAWN
	}

	SoundPool soundpool;
	SparseIntArray sounds;
	AudioManager audioManager;
	Context context;
	int soundsLoaded = 0;
	int bitmapsLoaded = 0;

	int[] drawableLibrary = new int[]{R.drawable.buzzball1, R.drawable.buzzball2, R.drawable.buzzball3, R.drawable.buzzball4, R.drawable.buzzball5, R.drawable.buzzball6};
	int[] soundLibrary = new int[]{R.raw.pop, R.raw.lifeup, R.raw.ding, R.raw.popwall, R.raw.down, R.raw.hit, R.raw.restart, R.raw.spawn};

	RollingObjectBitmap[] buzzBallBitmaps = new RollingObjectBitmap[drawableLibrary.length]; //buzz balls bitmap

	int getBitmapsSize()
	{
		return buzzBallBitmaps.length * 360;
	}

	void initSounds(Context context)
	{
		this.context = context;
		soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds = new SparseIntArray();
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	void loadSounds() // load sounds to IntArray
	{
		soundsLoaded = 0;
		soundpool.setOnLoadCompleteListener(new OnLoadCompleteListener()
		{
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
			{
				soundsLoaded++;
				Log.i("ResourceManager", "Sample" + Integer.toString(sampleId) + " loaded");
			}
		});

		for (int soundindex = 0; soundindex < soundLibrary.length; soundindex++)
		{
			sounds.put(soundindex + 1, soundpool.load(context, soundLibrary[soundindex], 1));
		}
	}

	void loadBitmaps()
	{
		Matrix buzzball_matrix = new Matrix();
		for (int bitmapindex = 0; bitmapindex < buzzBallBitmaps.length; bitmapindex++)
		{
			int centerX, centerY, source_width, source_height;
			Bitmap source;

			centerX = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapindex]).getWidth() / 2;
			centerY = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapindex]).getHeight() / 2;

			source = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapindex]);
			source_width = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapindex]).getWidth();
			source_height = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapindex]).getHeight();

			Log.i("ResourceManager", "Source buzzball " + bitmapindex + " created.");
			buzzBallBitmaps[bitmapindex] = new RollingObjectBitmap(centerX, centerY, source_width, source_height);

			for (int frame = 0; frame < 360; frame++)
			{
				buzzball_matrix.setRotate(frame, centerX, centerY);

				if (frame % 2 == 0)
				{
					buzzBallBitmaps[bitmapindex].setFrame(Bitmap.createBitmap(source, 0, 0, source_width, source_height, buzzball_matrix, true), frame);
				}
				else
				{
					buzzBallBitmaps[bitmapindex].setFrame(buzzBallBitmaps[bitmapindex].getFrame(frame - 1), frame);
				}

				bitmapsLoaded++;
			}
			Log.i("ResourceManager", "Successfully created Buzzball bitmap " + Integer.toString(bitmapindex));
		}
	}

	void playSound(Sound sound, float speed)
	{
		float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		soundpool.play(sounds.get(sound.ordinal() + 1), streamVolume, streamVolume, 1, 0, speed);

	}

	void doCleanup()
	{
		soundpool.release();
	}
}
