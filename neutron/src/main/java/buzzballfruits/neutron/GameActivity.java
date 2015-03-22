package buzzballfruits.neutron;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends Activity implements SensorEventListener
{
	int canvasHeight, canvasWidth;
	int midpoint; // canvas horizontal midpoint
	int life = 50;
	int gameScore = 0;
	int ballCount = 5;
	boolean running = true; // game running
	boolean gameOver = false;
	static String score;
	int ballSize;
	boolean soloGame = true;
	int playerCount;
	static ResourceManager RESOURCEMANAGER = new ResourceManager(); // global resource manager
	int smileyWidth, smileyHeight; // smiley object dimensions
	float rollAngle = 0;

	ArrayList<Popup> popups = new ArrayList<>(); // popups messages array list
	ArrayList<ShockWave> shockWaves = new ArrayList<>(); // shockWaves animation list
	ArrayList<Trail> trails = new ArrayList<>(); // trails animation list
	ArrayList<BuzzBall> buzzBalls = new ArrayList<>(); // buzzBalls fruit array list
	ArrayList<Ball> balls = new ArrayList<>(); // whiteball array list
	RollingObjectBitmap[] buzzBallBitmaps = RESOURCEMANAGER.buzzBallBitmaps; //buzz balls bitmap
	PowerManager.WakeLock wakelock;
	GameSurfaceThread gameSurfaceThread;
	SurfaceHolder surfaceHolder;
	SensorManager sensorManager;
	Sensor orientation;
	Random rnd = new Random();

	String[] yeyStrings = new String[]{"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
	String[] booStrings = new String[]{"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};

	Player[] players; // set Players array
	AI ai; // set AI

	int[] ground = new int[2]; // playerCount ground level array

	@Override
	public void onCreate(Bundle savedinstancestate)
	{
		super.onCreate(savedinstancestate);
		Log.i(getLocalClassName(), "Activity started");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		this.wakelock.acquire();

		if (getIntent().getIntExtra("BALLS_COUNT", -1) > 0)
		{
			ballCount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve balls count from main activity
		}

		soloGame = getIntent().getBooleanExtra("SOLO_GAME", false);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		if (soloGame)
		{
			playerCount = 1;
		}
		else
		{
			playerCount = 2;
		}

		LinearLayout lLayout = new LinearLayout(getApplicationContext());
		GameScreen mydraw = new GameScreen(getApplicationContext()); // set SurfaceView
		lLayout.addView(mydraw);
		setContentView(lLayout);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(getLocalClassName(), "Activity stopped");
		this.wakelock.release();
	}

	public void onPause()
	{
		super.onPause();
		finish(); // disallow pausing
		sensorManager.unregisterListener(this);
	}

	public void onResume()
	{
		super.onResume();
		sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void showScore() // show score screen
	{
		Intent scoreIntent = new Intent(this, ScoreActivity.class);
		scoreIntent.putExtra(score, Integer.toString(gameScore));
		startActivity(scoreIntent);
		Log.i(getLocalClassName(), "Game ended");
		finish();
	}

	public void doShake(int time) // phone vibrate
	{
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	private class GlobalThread implements Runnable
	{
		GlobalThread()
		{
			start();
		}

		public void start()
		{
			Thread thread = new Thread(this);
			thread.setName("GlobalThread");
			thread.start();
		}

		public void run()
		{
			while (running)
			{
				if (balls.size() < ballCount)
				{
					balls.add(new Ball(GameActivity.this, balls, players)); // maintain balls number
				}

				if (rnd.nextInt(10) == 0)
				{
					addBuzzBall();
				}

				if (rnd.nextInt(100) == 0)
				{
					players[1].jump();
				}

				if (life < 0 && !gameOver) // game over condition
				{
					running = false;
					gameOver = true;
					RESOURCEMANAGER.playSound(ResourceManager.Sound.SPAWN, 1);
					showScore();
				}

				try
				{
					Thread.sleep(40);
				}

				catch (InterruptedException e)
				{
					e.printStackTrace();
					Log.e("GlobalThread", e.toString());
				}
			}
		}
	}

	public class GameScreen extends SurfaceView implements Callback
	{
		Bitmap smileyRight; // right smiley image
		Bitmap smileyLeft; // left smiley image
		Bitmap smileyShadesRight; // right smiley shades image
		Bitmap smileyShadesLeft; // left smiley shades image
		Bitmap back; // background

		Paint pint = new Paint(); // balls paint
		Paint scoreText = new Paint();
		Paint popupText = new Paint();
		Paint ballTrail = new Paint(); // balls trails
		Paint circleStrokePaint = new Paint();
		Paint shadowPaint = new Paint();
		Paint buzzBallPaint = new Paint();
		GlobalThread globalthread;

		public GameScreen(Context context)
		{
			super(context);

			surfaceHolder = getHolder();
			surfaceHolder.addCallback(this);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			canvasWidth = metrics.widthPixels;
			canvasHeight = metrics.heightPixels;
			midpoint = canvasWidth / 2;

			ground[0] = canvasHeight - canvasHeight / 3;
			ground[1] = canvasHeight - canvasHeight / 4;

			players = new Player[playerCount]; // set Players array

			back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), canvasWidth, canvasHeight, true);
			Log.i(getLocalClassName(), "Portrait background created");

			smileyRight = BitmapFactory.decodeResource(getResources(), R.drawable.smiley); // create pong image for playerCount
			Log.i(getLocalClassName(), "Smiley right created");

			smileyHeight = smileyRight.getHeight(); // store smiley dimensions
			smileyWidth = smileyRight.getWidth();

			Matrix matrix = new Matrix();
			matrix.preScale(-1.0f, 1.0f);

			smileyLeft = Bitmap.createBitmap(smileyRight, 0, 0, smileyRight.getWidth(), smileyRight.getHeight(), matrix, true);
			Log.i(getLocalClassName(), "Smiley left created");

			smileyShadesRight = BitmapFactory.decodeResource(getResources(), R.drawable.smiley2); // create pong image for playerCount
			Log.i(getLocalClassName(), "Smileyshades right created");

			smileyShadesLeft = Bitmap.createBitmap(smileyShadesRight, 0, 0, smileyShadesRight.getWidth(), smileyShadesRight.getHeight(), matrix, true);
			Log.i(getLocalClassName(), "Smileyshades left created");

			Typeface myType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
			scoreText.setColor(Color.WHITE);
			scoreText.setTypeface(myType);

			popupText.setTypeface(myType);
			popupText.setTextAlign(Align.CENTER);

			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
			{
				popupText.setTextSize(8);
				scoreText.setTextSize(9);
				ballSize = 2;
				Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
			}

			else
			{
				popupText.setTextSize(12);
				scoreText.setTextSize(15);
				ballSize = 4;
				Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
			}

			pint.setColor(Color.WHITE);
			circleStrokePaint.setStyle(Paint.Style.STROKE);

			if (balls.size() == 1)
			{
				Log.i(getLocalClassName(), "Ball initialized");
			}
			else
			{
				Log.i(getLocalClassName(), "Balls initialized");
			}

			if (soloGame)
			{
				players[0] = new Player(GameActivity.this, canvasHeight - canvasHeight / 4, midpoint, canvasHeight - canvasHeight / 4);
				Log.i(getLocalClassName(), "Player0 initialized");
			}

			else
			{
				for (int playerCounter = 0; playerCounter < players.length; playerCounter++) // initialize playerCount and AI
				{
					players[playerCounter] = new Player(GameActivity.this, ground[playerCounter], midpoint, ground[playerCounter]);
					Log.i(getLocalClassName(), "Player" + Integer.toString(playerCounter) + " initialized");
				}
				ai = new AI(GameActivity.this, balls, players[0]);
				Log.i(getLocalClassName(), "AI initialized");
			}
			globalthread = new GlobalThread();
		}

		public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
		{
			running = false;
			Log.i(getLocalClassName(), "Surface destroyed");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			Log.i(getLocalClassName(), "Surface changed");
		}

		public void surfaceCreated(SurfaceHolder holder) // when user enters game
		{
			gameSurfaceThread = new GameSurfaceThread(GameActivity.this, holder, this);
			Log.i(getLocalClassName(), "Surface created");
		}

		public boolean onTouchEvent(MotionEvent event)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				players[0].jump();
			}

			return true;
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawBitmap(back, 0, 0, null);
			for (Player player : players) // draw playerCount
			{
				shadowPaint.setAlpha(player.shadowOpacity);
				canvas.drawOval(player.shadow, shadowPaint);
				if (player == players[0])
				{
					if (player.right)
					{
						canvas.drawBitmap(smileyRight, player.position.x, player.position.y, null);
					}
					else
					{
						canvas.drawBitmap(smileyLeft, player.position.x, player.position.y, null); // draw playerCount smiley
					}
				}
				else if (player.right)
				{
					canvas.drawBitmap(smileyShadesRight, player.position.x, player.position.y, null);
				}
				else
				{
					canvas.drawBitmap(smileyShadesLeft, player.position.x, player.position.y, null); // draw AI smiley
				}
			}

			for (int index = 0; index < balls.size(); index++) // balls drawer
			{
				Ball ball = balls.get(index);
				if (ball.dead)
				{
					balls.remove(index);
				}
				else
				{
					canvas.drawCircle(ball.position.x, ball.position.y, ballSize, pint);
				}
			}

			for (int index = 0; index < trails.size(); index++) // trails drawer
			{
				Trail trail = trails.get(index);
				if (trail.life > 0)
				{
					ballTrail.setStrokeWidth(ballSize - trail.calcSize());
					ballTrail.setColor(Color.argb(trail.life * 25, 255, 255, 255));
					canvas.drawLine(trail.startPoint.x, trail.startPoint.y, trail.endPoint.x, trail.endPoint.y, ballTrail);
					trail.life--;
				}
				else
				{
					trails.remove(index); // remove dead trails
				}
			}

			for (int index = 0; index < shockWaves.size(); index++)  // shockWaves drawer
			{
				ShockWave shockWave = shockWaves.get(index);
				if (shockWave.getLife() > 0) // bump animation
				{
					int currentShockWaveLife = shockWave.getLife();
					switch (shockWave.type)
					{
						case ShockWave.EXTRA_SMALL_WAVE:
							circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
							circleStrokePaint.setStrokeWidth(1);
							canvas.drawCircle(shockWave.position.x, shockWave.position.y, 11 - currentShockWaveLife, circleStrokePaint);
							break;
						case ShockWave.SMALL_WAVE:
							circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
							circleStrokePaint.setStrokeWidth(2);
							canvas.drawCircle(shockWave.position.x, shockWave.position.y, 21 - currentShockWaveLife, circleStrokePaint);
							break;
						case ShockWave.MEDIUM_WAVE:
							circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
							circleStrokePaint.setStrokeWidth(1);
							canvas.drawCircle(shockWave.position.x, shockWave.position.y, 128 - currentShockWaveLife, circleStrokePaint);
							break;
						case ShockWave.LARGE_WAVE:
							circleStrokePaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
							circleStrokePaint.setStrokeWidth(1);
							canvas.drawCircle(shockWave.position.x, shockWave.position.y, 252 - currentShockWaveLife, circleStrokePaint);
					}
				}
				else
				{
					shockWaves.remove(index); // remove dead shockWaves
				}
			}

			for (int index = 0; index < popups.size(); index++) // popups text drawer
			{
				if (popups.get(index).getLife() > 0) // if popups text is to be shown
				{
					popupText.setColor(Color.argb(popups.get(index).getLife(), 255, 255, 255)); // text fade effect
					Popup popup = popups.get(index);

					switch (popups.get(index).type)
					{
						case YEY:
							canvas.drawText(yeyStrings[popup.textIndex], popup.position.x, popup.position.y - popup.getLife(), popupText);
							break;
						case BOO:
							canvas.drawText(booStrings[popup.textIndex], popup.position.x, popup.position.y + popup.getLife(), popupText);
							break;
						case BUMP:
							canvas.drawText(yeyStrings[popup.textIndex], popup.position.x, popup.position.y + popup.getLife(), popupText);
					}
				}
				else
				{
					popups.remove(index); // remove dead popups
				}
			}

			for (int index = 0; index < buzzBalls.size(); index++) // draw buzzBalls
			{
				BuzzBall buzzBall = buzzBalls.get(index);
				buzzBallPaint.setAlpha(buzzBalls.get(index).opacity);

				if (buzzBall.dead)
				{
					buzzBalls.remove(index); // cleanup dead buzzBalls from array list
				}
				else if (buzzBall.opacity < 0)
				{
					buzzBall.dead = true;
				}
				else
				{
					canvas.drawBitmap(buzzBallBitmaps[buzzBall.type].getFrame(buzzBall.rotation).bitmap, buzzBall.position.x + buzzBallBitmaps[buzzBall.type].getFrame(buzzBall.rotation).offset.x, buzzBall.position.y + buzzBallBitmaps[buzzBall.type].getFrame(buzzBall.rotation).offset.y, buzzBallPaint);
				}
			}
			if (life > 0)
			{
				canvas.drawText("Ball Count: " + Integer.toString(balls.size()) + " " + "Score: " + Integer.toString(gameScore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasHeight - 10, scoreText);
			}
		}
	}

	private void addBuzzBall()
	{
		int type = rnd.nextInt(buzzBallBitmaps.length - 1);
		buzzBalls.add(new BuzzBall(GameActivity.this, type, buzzBallBitmaps[type].height, buzzBallBitmaps[type].width, balls, players));
	}

	public void onAccuracyChanged(Sensor sensor, int integer)
	{
		Log.i(getLocalClassName(), "Accuracy changed");
	}

	public void onSensorChanged(SensorEvent event)
	{
		rollAngle = event.values[2];
		players[0].setDestination(rollAngle);
	}
}