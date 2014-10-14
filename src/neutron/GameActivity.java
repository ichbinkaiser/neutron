package neutron;

import java.util.ArrayList;
import java.util.Random;

import core.neutron.R;
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
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GameActivity extends Activity implements SensorEventListener
{
	private int canvasheight;
	private int canvaswidth;
	private int midpoint; // canvas horizontal midpoint
	private int life = 50;
	private int gamescore = 0;
	private int ballcount = 5;
	private boolean running = true; // game running
	private boolean gameover = false;
	private static String score;
	private int ballsize;
	private boolean reverseposition = false; // AI reverse position in doubles mode
	private boolean sologame = true;
	private int players;
	private static ResourceManager resourcemanager = new ResourceManager(); // global sound manager
	private int smileywidth, smileyheight; // smiley object dimensions
	private ArrayList<Popup> popup = new ArrayList<Popup>(); // popup messages array list
	private ArrayList<Shockwave> shockwave = new ArrayList<Shockwave>(); // shockwave animation list
	private ArrayList<Trail> trail = new ArrayList<Trail>(); // trail animation list
	private ArrayList<BuzzBall> buzzball = new ArrayList<BuzzBall>(); // buzzball fruit array list
	private ArrayList<Ball> ball = new ArrayList<Ball>(); // whiteball array list
	private RollingObjectBitmap[] buzzballbitmaps = resourcemanager.getBuzzballbitmaps(); //buzz ball bitmap
	protected PowerManager.WakeLock wakelock;
	private GameSurfaceThread gamesurfacethread;
	private SurfaceHolder surfaceholder;
	private SensorManager sensormanager;
	private Sensor orientation;
	private float rollangle = 0;
	private Random rnd = new Random();

	private Player[] player; // set Players array
	private AI ai; // set AI

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
			ballcount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve ball count from main activity
		
		sologame = getIntent().getBooleanExtra("SOLO_GAME", false);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

		if (sologame)
			players = 1;
		else
			players = 2;

		LinearLayout lLayout = new LinearLayout(getApplicationContext());
		MyDraw mydraw = new MyDraw(getApplicationContext()); // set SurfaceView
		lLayout.addView(mydraw);
		setContentView(lLayout);
		
		sensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		orientation = sensormanager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensormanager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
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
		sensormanager.unregisterListener(this);
	}

	public void onResume()
	{
		super.onResume();
		sensormanager.registerListener(this,  orientation, SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void showScore() // show score screen
	{
		Intent scoreIntent = new Intent(this, ScoreActivity.class);
		scoreIntent.putExtra(score, Integer.toString(gamescore));
		startActivity(scoreIntent);
		Log.i(getLocalClassName(), "Game ended");
		finish();
	}

	public void doShake(int time) // phone vibrate
	{
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(time);
	}

	public boolean isReversePosition() 
	{
		return reverseposition;
	}

	public void setReversePosition(boolean reverseposition) 
	{
		this.reverseposition = reverseposition;
	}

	public int getMidpoint() 
	{
		return midpoint;
	}

	public void setMidpoint(int midpoint) 
	{
		this.midpoint = midpoint;
	}

	public boolean isRunning() 
	{
		return running;
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

	public int getCanvasWidth() 
	{
		return canvaswidth;
	}

	public void setCanvasWidth(int canvaswidth) 
	{
		this.canvaswidth = canvaswidth;
	}

	public int getCanvasHeight() 
	{
		return canvasheight;
	}

	public void setCanvasHeight(int canvasheight) 
	{
		this.canvasheight = canvasheight;
	}

	public int getBallSize() 
	{
		return ballsize;
	}

	public void setBallSize(int ballsize) 
	{
		this.ballsize = ballsize;
	}

	public ArrayList<Shockwave> getShockwave() 
	{
		return shockwave;
	}

	public void setShockwave(ArrayList<Shockwave> shockwave) 
	{
		this.shockwave = shockwave;
	}

	public int getSmileyHeight() 
	{
		return smileyheight;
	}

	public void setSmileyHeight(int smileyheight) 
	{
		this.smileyheight = smileyheight;
	}

	public int getGameScore() 
	{
		return gamescore;
	}

	public void setGameScore(int score) 
	{
		this.gamescore = score;
	}

	public int getSmileyWidth() 
	{
		return smileywidth;
	}

	public void setSmileyWidth(int smileywidth) 
	{
		this.smileywidth = smileywidth;
	}

	public int getBallCount() 
	{
		return ballcount;
	}

	public void setBallCount(int ballcount) 
	{
		this.ballcount = ballcount;
	}

	public static ResourceManager getResourceManager() 
	{
		return resourcemanager;
	}

	public boolean isSoloGame() 
	{
		return sologame;
	}

	public void setSoloGame(boolean sologame) 
	{
		this.sologame = sologame;
	}

	public ArrayList<Popup> getPopup() 
	{
		return popup;
	}

	public void setPopup(ArrayList<Popup> popup) 
	{
		this.popup = popup;
	}

	public int getLife() 
	{
		return life;
	}

	public void setLife(int life) 
	{
		this.life = life;
	}

	public ArrayList<Trail> getTrail() 
	{
		return trail;
	}

	public void setTrail(ArrayList<Trail> trail) 
	{
		this.trail = trail;
	}

	public static String getScore() 
	{
		return score;
	}

	public ArrayList<BuzzBall> getBuzzBall()
	{
		return buzzball;
	}

	public float getRollAngle() 
	{
		return rollangle;
	}

	public ArrayList<Ball> getBall()
	{
		return ball;
	}

	public Player[] getPlayer()
	{
		return player;
	}

	private class GlobalThread implements Runnable
	{	
		public void start()
		{
			Thread thread = new Thread(this);
			thread.setName("GlobalThread");
			thread.start();
		}
		
		public void run()
		{
			int spawn,jumpAI;
			
			while (running)
			{
				if (ball.size() < ballcount)
					ball.add(new Ball(GameActivity.this, ball, player)); // maintain ball number
				
				spawn = rnd.nextInt(10);
				jumpAI = rnd.nextInt(100);
				
				if (spawn == 0)
					addBuzzBall();
				
				if (jumpAI ==0)
					player[1].jump();
				
				if ((life < 0) && (!gameover)) // game over condition
				{
					running =  false;
					gameover = true;
					gamesurfacethread.setFlag(false);
					resourcemanager.playSound(7, 1);
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

	public class MyDraw extends SurfaceView implements Callback
	{
		private Bitmap smileyright; // right smiley image
		private Bitmap smileyleft; // left smiley image
		private Bitmap smileyshadesright; // right smiley shades image
		private Bitmap smileyshadesleft; // left smiley shades image
		private Bitmap back; // background

		private String[] extralifestrings = new String[] {"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
		private String[] lostlifestrings = new String[] {"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};
		private String[] bumpstrings = new String[] {"BUMP!", "TOINK!", "BOINK!", "BAM!", "WABAM!"};
		private String[] zoomstrings = new String[] {"ZOOM!", "WOOSH!", "SUPER MODE!", "ZOOMBA!", "WARPSPEED!"};
		private Paint pint = new Paint(); // ball paint
		private Paint scoretext = new Paint();
		private Paint popuptext = new Paint();
		private Paint balltrail = new Paint(); // ball trail
		private Paint circlestrokepaint = new Paint();
		private Paint centerlinepaint = new Paint();
		private Paint shadowpaint = new Paint();
		private Paint buzzballpaint = new Paint();
		private GlobalThread globalthread;

		public MyDraw(Context context)
		{
			super(context);

			surfaceholder = getHolder();
			surfaceholder.addCallback(this);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			canvaswidth = metrics.widthPixels;
			canvasheight = metrics.heightPixels;
			midpoint = canvaswidth / 2;

			player = new Player[players]; // set Players array

			back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), canvaswidth, canvasheight, true);
			Log.i(getLocalClassName(), "Portrait background created");

			smileyright = BitmapFactory.decodeResource(getResources(), R.drawable.smiley); // create pong image for player
			Log.i(getLocalClassName(), "Smiley right created");

			smileyheight = smileyright.getHeight(); // store smiley dimensions
			smileywidth = smileyright.getWidth();

			Matrix matrix = new Matrix();
			matrix.preScale(-1.0f, 1.0f);

			smileyleft = Bitmap.createBitmap(smileyright, 0, 0, smileyright.getWidth(), smileyright.getHeight(), matrix, true);
			Log.i(getLocalClassName(), "Smiley left created");

			smileyshadesright = BitmapFactory.decodeResource(getResources(), R.drawable.smiley2); // create pong image for player
			Log.i(getLocalClassName(), "Smileyshades right created");

			smileyshadesleft = Bitmap.createBitmap(smileyshadesright, 0, 0, smileyshadesright.getWidth(), smileyshadesright.getHeight(), matrix, true);
			Log.i(getLocalClassName(), "Smileyshades left created");

			Typeface myType = Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL);
			scoretext.setColor(Color.WHITE);
			scoretext.setTypeface(myType);

			popuptext.setTypeface(myType);
			popuptext.setTextAlign(Align.CENTER);

			centerlinepaint.setStrokeWidth(3);

			if (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) // adjust to low DPI
			{
				popuptext.setTextSize(8);
				scoretext.setTextSize(9);
				ballsize = 2;
				Log.i(getLocalClassName(), "Screen DPI is low, adjustment sizes set to small");
			}
			else
			{
				popuptext.setTextSize(12);
				scoretext.setTextSize(15);
				ballsize = 4;
				Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
			}

			pint.setColor(Color.WHITE);
			circlestrokepaint.setStyle(Paint.Style.STROKE);

			if (ball.size() == 1)
				Log.i(getLocalClassName(), "Ball initialized");
			else
				Log.i(getLocalClassName(), "Balls initialized");

			if (sologame)
			{
				player[0] = new Player(GameActivity.this, canvasheight - canvasheight / 4);
				player[0].getPosition().set(midpoint, canvasheight - canvasheight / 4);
				Log.i(getLocalClassName(), "Player0 initialized");
			}
			else
			{
				int[] ground = new int[2]; // player ground level array
				
				ground[0] = canvasheight - canvasheight / 3;
				ground[1] = canvasheight - canvasheight / 4;
				
				for (int playercounter = 0; playercounter < player.length; playercounter++) // initialize player and AI
				{
					player[playercounter] = new Player(GameActivity.this, ground[playercounter]);
					Log.i(getLocalClassName(), "Player" + Integer.toString(playercounter) + " initialized");

					if (playercounter == 0) // if player
						player[0].getPosition().set(midpoint, ground[playercounter]);
					
					else // if AI
					{
						ai = new AI(GameActivity.this, ball, player[0]);
						ai.start();
						player[playercounter].getPosition().set(midpoint, ground[playercounter]); // set AI start location to 2nd quadrant
						Log.i(getLocalClassName(), "AI initialized");
					}
				}
			}
			
			globalthread = new GlobalThread();
			globalthread.start();
		}

		public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
		{
			gamesurfacethread.setFlag(false);
			running = false ;
			Log.i(getLocalClassName(), "Surface destroyed");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
		{
			Log.i(getLocalClassName(), "Surface changed");
		}

		public void surfaceCreated(SurfaceHolder holder) // when user enters game
		{
			gamesurfacethread = new GameSurfaceThread(GameActivity.this, holder, this);
			gamesurfacethread.setFlag(true);
			gamesurfacethread.start();
			Log.i(getLocalClassName(), "Surface created");
		}
		
		public boolean onTouchEvent(MotionEvent event)
		{
			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN)
			{
				player[0].jump();
			}
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawBitmap(back, 0, 0, null);

			for (int playercounter = 0; playercounter < player.length; playercounter++) // draw player
			{
				shadowpaint.setAlpha(player[playercounter].getShadowOpacity());
				canvas.drawOval(player[playercounter].getShadow(), shadowpaint);
				if (playercounter == 0)
					if (player[playercounter].isRight())
						canvas.drawBitmap(smileyright, player[playercounter].getPosition().x, player[playercounter].getPosition().y, null);
						else
						canvas.drawBitmap(smileyleft, player[playercounter].getPosition().x, player[playercounter].getPosition().y, null); // draw player smiley
				else
					if (player[playercounter].isRight())
						canvas.drawBitmap(smileyshadesright, player[playercounter].getPosition().x, player[playercounter].getPosition().y, null);
					else
						canvas.drawBitmap(smileyshadesleft, player[playercounter].getPosition().x, player[playercounter].getPosition().y, null); // draw AI smiley
			}

			for (int ballcounter = ball.size() - 1; ballcounter >= 0; ballcounter--) // ball drawer
			{
                Ball currentball = ball.get(ballcounter);
				if (currentball.isDead())
					ball.remove(ballcounter);
				else
					canvas.drawCircle(currentball.getPosition().x, currentball.getPosition().y, ballsize, pint);
			}

			for (int trailcounter = trail.size() - 1; trailcounter >= 0; trailcounter--) // trail drawer
			{
                Trail currenttrail = trail.get(trailcounter);
				if (currenttrail.getCounter() > 0)
				{
					balltrail.setStrokeWidth(ballsize - currenttrail.calcSize());
					balltrail.setColor(Color.argb(currenttrail.getCounter() * 25, 255, 255, 255));
					canvas.drawLine(currenttrail.getStartPoint().x, currenttrail.getStartPoint().y, currenttrail.getEndPoint().x, currenttrail.getEndPoint().y, balltrail);
                    currenttrail.setCounter(currenttrail.getCounter() - 1);
				}
				else
					trail.remove(trailcounter); // remove dead trail
			}

			for (int shockwavecounter = shockwave.size() - 1; shockwavecounter >= 0; shockwavecounter--)  // shockwave drawer
			{
                Shockwave currentshockwave = shockwave.get(shockwavecounter);
				if (currentshockwave.getLife() > 0) // bump animation
				{
                    int currentshockwavelife = currentshockwave.getLife();
					switch (currentshockwave.getType())
					{
					case 0: // is small wave animation
						circlestrokepaint.setColor(Color.argb(currentshockwavelife * 23,255, 255, 255));
						circlestrokepaint.setStrokeWidth(1);
						canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,11 - currentshockwavelife, circlestrokepaint);
						break;
					case 1: // is medium wave animation
						circlestrokepaint.setColor(Color.argb(currentshockwavelife * 12, 255, 255, 255));
						circlestrokepaint.setStrokeWidth(2);
						canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,21 - currentshockwavelife, circlestrokepaint);
						break;
					case 2: // is big wave animation
						circlestrokepaint.setColor(Color.argb(currentshockwavelife * 2, 255, 255, 255));
						circlestrokepaint.setStrokeWidth(1);
						canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,128 - currentshockwavelife, circlestrokepaint);
						break;
					case 3: // is super big animation
						circlestrokepaint.setColor(Color.argb(currentshockwavelife, 255, 255, 255));
						circlestrokepaint.setStrokeWidth(1);
						canvas.drawCircle(currentshockwave.getPosition().x, currentshockwave.getPosition().y,252 - currentshockwavelife, circlestrokepaint);
						break;
					}
				}
				else
					shockwave.remove(shockwavecounter); // remove dead shockwave
			}

			for (int popupcounter = popup.size() - 1; popupcounter >= 0; popupcounter--) // popup text drawer
			{
				if (popup.get(popupcounter).getCounter() > 0) // if popup text is to be shown
				{
					popuptext.setColor(Color.argb(popup.get(popupcounter).getCounter(), 255, 255, 255)); // text fade effect
                    Popup currentpopup = popup.get(popupcounter);

					switch (popup.get(popupcounter).getType()) 
					{
					case 0: // scoreup
						canvas.drawText(extralifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y - currentpopup.getCounter(), popuptext);
						break;
					case 1: // lose life
						canvas.drawText(lostlifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y + currentpopup.getCounter(), popuptext);
						break;
					case 2: // solo
						canvas.drawText(extralifestrings[currentpopup.getTextIndex()], currentpopup.getPosition().x, currentpopup.getPosition().y + currentpopup.getCounter(), popuptext);
						break;
					}
				}
				else
					popup.remove(popupcounter); // remove dead popup
			}

			for (int buzzballcounter = buzzball.size() - 1; buzzballcounter >= 0; buzzballcounter--) // draw buzzball
			{
                BuzzBall currentbuzzball = buzzball.get(buzzballcounter);
				buzzballpaint.setAlpha(buzzball.get(buzzballcounter).getOpacity());
				
				if (currentbuzzball.isDead())
					buzzball.remove(buzzballcounter); // cleanup dead buzzballs from array list
				else if(currentbuzzball.getOpacity() < 0)
                    currentbuzzball.kill();
				else
					canvas.drawBitmap(buzzballbitmaps[currentbuzzball.getType()].getFrame(currentbuzzball.getRotation()).getBitmap(), currentbuzzball.getPosition().x + buzzballbitmaps[currentbuzzball.getType()].getFrame(currentbuzzball.getRotation()).getOffset().x, currentbuzzball.getPosition().y + buzzballbitmaps[currentbuzzball.getType()].getFrame(currentbuzzball.getRotation()).getOffset().y, buzzballpaint);
			}
			
			if (life > 0)
				canvas.drawText("Ball Count: " + Integer.toString(ball.size()) + " " + "Score: " + Integer.toString(gamescore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasheight - 10, scoretext);
		}
	}
	
	private void addBuzzBall()
	{
		int type = rnd.nextInt(buzzballbitmaps.length - 1);
		buzzball.add(new BuzzBall(GameActivity.this, type, buzzballbitmaps[type].getHeight(), buzzballbitmaps[type].getWidth(), ball, player));
	}

	public void onAccuracyChanged(Sensor sensor, int integer)
	{
		Log.i(getLocalClassName(), "Accuracy changed");
	}

	public void onSensorChanged(SensorEvent event)
	{
		rollangle = event.values[2];
		player[0].setDestination(rollangle);
	}
}