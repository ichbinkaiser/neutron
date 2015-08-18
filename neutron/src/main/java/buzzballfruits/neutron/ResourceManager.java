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
		POP, LIFE_UP, DING, POP_WALL, DOWN, HIT, RESTART, SPAWN
	}
	boolean loaded = false;
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

		for (int soundIndex = 0; soundIndex < soundLibrary.length; soundIndex++)
		{
			sounds.put(soundIndex + 1, soundpool.load(context, soundLibrary[soundIndex], 1));
		}
	}

	void loadBitmaps()
	{
		Matrix buzzBallMatrix = new Matrix();
		for (int bitmapIndex = 0; bitmapIndex < buzzBallBitmaps.length; bitmapIndex++)
		{
			int centerX, centerY, source_width, source_height;
			Bitmap source;

			centerX = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapIndex]).getWidth() / 2;
			centerY = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapIndex]).getHeight() / 2;

			source = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapIndex]);
			source_width = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapIndex]).getWidth();
			source_height = BitmapFactory.decodeResource(context.getResources(), drawableLibrary[bitmapIndex]).getHeight();

			Log.i("ResourceManager", "Source buzz ball " + bitmapIndex + " created.");
			buzzBallBitmaps[bitmapIndex] = new RollingObjectBitmap(centerX, centerY, source_width, source_height);

			for (int frame = 0; frame < 360; frame++)
			{
				buzzBallMatrix.setRotate(frame, centerX, centerY);

				if (frame % 2 == 0)
				{
					buzzBallBitmaps[bitmapIndex].setFrame(Bitmap.createBitmap(source, 0, 0, source_width, source_height, buzzBallMatrix, true), frame);
				}
				else
				{
					buzzBallBitmaps[bitmapIndex].setFrame(buzzBallBitmaps[bitmapIndex].getFrame(frame - 1), frame);
				}

				bitmapsLoaded++;
			}
			Log.i("ResourceManager", "Successfully created buzz ball bitmap " + Integer.toString(bitmapIndex));
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
