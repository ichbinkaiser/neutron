package buzzballfruits.neutron;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends Activity implements SensorEventListener
{
	int canvasheight, canvaswidth;
    int midpoint; // canvas horizontal midpoint
    int life = 50;
    int gamescore = 0;
    int ballcount = 5;
	boolean running = true; // game running
	boolean gameover = false;
	static String score;
	int ballsize;
	boolean reverseposition = false; // AI reverse position in doubles mode
	boolean sologame = true;
    int players;
	static ResourceManager resourcemanager = new ResourceManager(); // global sound manager
    int smileywidth, smileyheight; // smiley object dimensions
    float rollangle = 0;

	ArrayList<Popup> popup = new ArrayList<Popup>(); // popup messages array list
	ArrayList<Shockwave> shockwave = new ArrayList<Shockwave>(); // shockwave animation list
	ArrayList<Trail> trail = new ArrayList<Trail>(); // trail animation list
	ArrayList<BuzzBall> buzzball = new ArrayList<BuzzBall>(); // buzzball fruit array list
	ArrayList<Ball> ball = new ArrayList<Ball>(); // whiteball array list
	RollingObjectBitmap[] buzzballbitmaps = resourcemanager.buzzballbitmaps; //buzz ball bitmap
	PowerManager.WakeLock wakelock;
	GameSurfaceThread gamesurfacethread;
	SurfaceHolder surfaceholder;
	SensorManager sensormanager;
	Sensor orientation;
	Random rnd = new Random();

    String[] extralifestrings = new String[] {"OH YEAH!", "WOHOOO!", "YEAH BABY!", "WOOOT!", "AWESOME!", "COOL!", "GREAT!", "YEAH!!", "WAY TO GO!", "YOU ROCK!"};
    String[] lostlifestrings = new String[] {"YOU SUCK!", "LOSER!", "GO HOME!", "REALLY?!", "WIMP!", "SUCKER!", "HAHAHA!", "YOU MAD?!", "DIE!", "BOOM!"};
    String[] bumpstrings = new String[] {"BUMP!", "TOINK!", "BOINK!", "BAM!", "WABAM!"};
    String[] zoomstrings = new String[] {"ZOOM!", "WOOSH!", "SUPER MODE!", "ZOOMBA!", "WARPSPEED!"};

	Player[] player; // set Players array
	AI ai; // set AI

    int[] ground = new int[2]; // player ground level array

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
					gamesurfacethread.running = false;
					resourcemanager.playSound(ResourceManager.SPAWN, 1);
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
		Bitmap smileyright; // right smiley image
		Bitmap smileyleft; // left smiley image
		Bitmap smileyshadesright; // right smiley shades image
		Bitmap smileyshadesleft; // left smiley shades image
		Bitmap back; // background

		Paint pint = new Paint(); // ball paint
		Paint scoretext = new Paint();
		Paint popuptext = new Paint();
		Paint balltrail = new Paint(); // ball trail
		Paint circlestrokepaint = new Paint();
		Paint centerlinepaint = new Paint();
		Paint shadowpaint = new Paint();
		Paint buzzballpaint = new Paint();
		GlobalThread globalthread;

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

            ground[0] = canvasheight - canvasheight / 3;
            ground[1] = canvasheight - canvasheight / 4;

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
				player[0] = new Player(GameActivity.this, canvasheight - canvasheight / 4, midpoint, canvasheight - canvasheight / 4);
				Log.i(getLocalClassName(), "Player0 initialized");
			}

			else
			{
				for (int playercounter = 0; playercounter < player.length; playercounter++) // initialize player and AI
				{
                    player[playercounter] = new Player(GameActivity.this, ground[playercounter], midpoint, ground[playercounter]);
                    Log.i(getLocalClassName(), "Player" + Integer.toString(playercounter) + " initialized");
				}
                ai = new AI(GameActivity.this, ball, player[0]);
                Log.i(getLocalClassName(), "AI initialized");
			}
			globalthread = new GlobalThread();
		}

		public void surfaceDestroyed(SurfaceHolder holder) // when user leaves game
		{
			gamesurfacethread.running = false;
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
				shadowpaint.setAlpha(player[playercounter].shadowOpacity);
				canvas.drawOval(player[playercounter].shadow, shadowpaint);
				if (playercounter == 0)
					if (player[playercounter].right)
						canvas.drawBitmap(smileyright, player[playercounter].position.x, player[playercounter].position.y, null);
						else
						canvas.drawBitmap(smileyleft, player[playercounter].position.x, player[playercounter].position.y, null); // draw player smiley
				else
					if (player[playercounter].right)
						canvas.drawBitmap(smileyshadesright, player[playercounter].position.x, player[playercounter].position.y, null);
					else
						canvas.drawBitmap(smileyshadesleft, player[playercounter].position.x, player[playercounter].position.y, null); // draw AI smiley
			}

			for (int ballcounter = 0; ballcounter < ball.size(); ballcounter++) // ball drawer
			{
                Ball currentball = ball.get(ballcounter);
				if (currentball.dead)
					ball.remove(ballcounter);
				else
					canvas.drawCircle(currentball.position.x, currentball.position.y, ballsize, pint);
			}

			for (int trailcounter = 0; trailcounter < trail.size(); trailcounter++) // trail drawer
			{
                Trail currenttrail = trail.get(trailcounter);
				if (currenttrail.life > 0)
				{
					balltrail.setStrokeWidth(ballsize - currenttrail.calcSize());
					balltrail.setColor(Color.argb(currenttrail.life * 25, 255, 255, 255));
					canvas.drawLine(currenttrail.startpoint.x, currenttrail.startpoint.y, currenttrail.endpoint.x, currenttrail.endpoint.y, balltrail);
                    currenttrail.life--;
				}

				else
					trail.remove(trailcounter); // remove dead trail
			}

			for (int shockwavecounter = 0; shockwavecounter < shockwave.size(); shockwavecounter++)  // shockwave drawer
			{
                Shockwave currentshockwave = shockwave.get(shockwavecounter);
				if (currentshockwave.getLife() > 0) // bump animation
				{
                    int currentshockwavelife = currentshockwave.getLife();
					switch (currentshockwave.type)
					{
                        case 0: // is small wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 23,255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y,11 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 1: // is medium wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 12, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(2);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y,21 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 2: // is big wave animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife * 2, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y,128 - currentshockwavelife, circlestrokepaint);
                            break;
                        case 3: // is super big animation
                            circlestrokepaint.setColor(Color.argb(currentshockwavelife, 255, 255, 255));
                            circlestrokepaint.setStrokeWidth(1);
                            canvas.drawCircle(currentshockwave.position.x, currentshockwave.position.y,252 - currentshockwavelife, circlestrokepaint);
					}
				}

				else
					shockwave.remove(shockwavecounter); // remove dead shockwave
			}

			for (int popupcounter = 0; popupcounter < popup.size(); popupcounter++) // popup text drawer
			{
				if (popup.get(popupcounter).getLife() > 0) // if popup text is to be shown
				{
					popuptext.setColor(Color.argb(popup.get(popupcounter).getLife(), 255, 255, 255)); // text fade effect
                    Popup currentpopup = popup.get(popupcounter);

					switch (popup.get(popupcounter).type)
					{
                        case 0: // scoreup
                            canvas.drawText(extralifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y - currentpopup.getLife(), popuptext);
                            break;
                        case 1: // lose life
                            canvas.drawText(lostlifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y + currentpopup.getLife(), popuptext);
                            break;
                        case 2: // solo
                            canvas.drawText(extralifestrings[currentpopup.textindex], currentpopup.position.x, currentpopup.position.y + currentpopup.getLife(), popuptext);
					}
				}

				else
					popup.remove(popupcounter); // remove dead popup
			}

			for (int buzzballcounter = 0; buzzballcounter < buzzball.size(); buzzballcounter++) // draw buzzball
			{
                BuzzBall currentbuzzball = buzzball.get(buzzballcounter);
				buzzballpaint.setAlpha(buzzball.get(buzzballcounter).opacity);
				
				if (currentbuzzball.dead)
					buzzball.remove(buzzballcounter); // cleanup dead buzzballs from array list
				else if(currentbuzzball.opacity < 0)
                    currentbuzzball.dead = true;
				else
					canvas.drawBitmap(buzzballbitmaps[currentbuzzball.type].getFrame(currentbuzzball.rotation).bitmap, currentbuzzball.position.x + buzzballbitmaps[currentbuzzball.type].getFrame(currentbuzzball.rotation).offset.x, currentbuzzball.position.y + buzzballbitmaps[currentbuzzball.type].getFrame(currentbuzzball.rotation).offset.y, buzzballpaint);
			}
			
			if (life > 0)
				canvas.drawText("Ball Count: " + Integer.toString(ball.size()) + " " + "Score: " + Integer.toString(gamescore) + "  " + "Extra Life: " + Integer.toString(life), 10, canvasheight - 10, scoretext);
		}
	}
	
	private void addBuzzBall()
	{
		int type = rnd.nextInt(buzzballbitmaps.length - 1);
		buzzball.add(new BuzzBall(GameActivity.this, type, buzzballbitmaps[type].height, buzzballbitmaps[type].width, ball, player));
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