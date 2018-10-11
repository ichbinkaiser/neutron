package ichbinkaiser.neutron.activity;

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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import ichbinkaiser.neutron.R;
import ichbinkaiser.neutron.control.AI;
import ichbinkaiser.neutron.control.Player;
import ichbinkaiser.neutron.core.GameSurfaceThread;
import ichbinkaiser.neutron.core.ResourceManager;
import ichbinkaiser.neutron.entity.Ball;
import ichbinkaiser.neutron.entity.BuzzBall;
import ichbinkaiser.neutron.entity.Popup;
import ichbinkaiser.neutron.entity.RollingObjectBitmap;
import ichbinkaiser.neutron.entity.ShockWave;
import ichbinkaiser.neutron.entity.Sound;
import ichbinkaiser.neutron.entity.Trail;
import lombok.Getter;

public class GameActivity extends Activity implements SensorEventListener {

    private int midpoint; // canvas horizontal midpoint
    private int life = 50;
    private int gameScore = 0;
    private int ballCount = 5;
    private boolean gameOver = false;
    private boolean soloGame = true;
    private int playerCount;

    private RollingObjectBitmap[] buzzBallBitmaps; //buzz balls bitmap
    private PowerManager.WakeLock wakelock;
    private GameSurfaceThread gameSurfaceThread;
    private SurfaceHolder surfaceHolder;
    private SensorManager sensorManager;
    private Sensor orientation;
    private Random rnd = new Random();
    private List<BuzzBall> buzzBalls = new CopyOnWriteArrayList<>(); // buzzBalls fruit array list
    private List<Ball> balls = new CopyOnWriteArrayList<>(); // white ball array list
    private ResourceManager resourceManager = ResourceManager.getInstance(); // global resource manager

    @Getter
    private static String score;

    @Getter
    private int canvasHeight, canvasWidth;

    @Getter
    private boolean isRunning = true; // game isRunning

    @Getter
    private int ballSize;

    @Getter
    private int smileyWidth, smileyHeight; // smiley object dimensions

    @Getter
    private float rollAngle = 0;

    @Getter
    private int[] ground = new int[2]; // playerCount ground level array

    @Getter
    private List<Popup> popups = new CopyOnWriteArrayList<>(); // popups messages array list

    @Getter
    private List<ShockWave> shockWaves = new CopyOnWriteArrayList<>(); // shockWaves animation list

    @Getter
    private List<Trail> trails = new CopyOnWriteArrayList<>(); // trails animation list

    @Getter
    private Player[] players; // set Players array

    @Getter
    private AI ai; // set AI

    @Getter
    private String[] yeyStrings = new String[]{
            "OH YEAH!",
            "WOHOOO!",
            "YEAH BABY!",
            "WOOOT!",
            "AWESOME!",
            "COOL!",
            "GREAT!",
            "YEAH!!",
            "WAY TO GO!",
            "YOU ROCK!"
    };

    @Getter
    private String[] booStrings = new String[]{
            "YOU SUCK!",
            "LOSER!",
            "GO HOME!",
            "REALLY?!",
            "WIMP!",
            "SUCKER!",
            "HAHAHA!",
            "YOU MAD?!",
            "DIE!",
            "BOOM!"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buzzBallBitmaps = resourceManager.getBuzzBallBitmaps();
        Log.i(getLocalClassName(), "Activity started");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:neutron");
        this.wakelock.acquire();

        if (getIntent().getIntExtra("BALLS_COUNT", -1) > 0) {
            ballCount = getIntent().getIntExtra("BALLS_COUNT", -1); // retrieve balls count from main activity
        }

        soloGame = getIntent().getBooleanExtra("SOLO_GAME", false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        playerCount = soloGame ? 1 : 2;

        LinearLayout lLayout = new LinearLayout(getApplicationContext());
        GameScreen myDraw = new GameScreen(getApplicationContext()); // set SurfaceView
        lLayout.addView(myDraw);
        setContentView(lLayout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(getLocalClassName(), "Activity stopped");
        this.wakelock.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish(); // disallow pausing
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
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

    private void addBuzzBall() {
        int type = rnd.nextInt(buzzBallBitmaps.length - 1);
        buzzBalls.add(new BuzzBall(GameActivity.this,
                type,
                buzzBallBitmaps[type].getHeight(),
                buzzBallBitmaps[type].getWidth(),
                balls,
                players));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int integer) {
        Log.i(getLocalClassName(), "Accuracy changed");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        rollAngle = event.values[2];
        players[0].setDestination(rollAngle);
    }

    public void addGameScore(int addend) {
        gameScore += addend;
    }

    public void decrementLife() {
        life--;
    }

    private class GlobalThread implements Runnable {
        GlobalThread() {
            start();
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.setName("GlobalThread");
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void run() {
            while (isRunning) {
                if (balls.size() < ballCount) {
                    balls.add(new Ball(GameActivity.this, balls, players)); // maintain balls number
                }

                if (rnd.nextInt(10) == 0) {
                    addBuzzBall();
                }

                if (rnd.nextInt(100) == 0) {
                    players[1].jump();
                }

                if (life < 0 && !gameOver) // game over condition
                {
                    isRunning = false;
                    gameOver = true;
                    resourceManager.playSound(Sound.SPAWN, 1);
                    showScore();
                }

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("GlobalThread", e.toString());
                }
            }
        }
    }

    public class GameScreen extends SurfaceView implements Callback {
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

        public GameScreen(Context context) {
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

            back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.back),
                    canvasWidth,
                    canvasHeight,
                    true);
            Log.i(getLocalClassName(), "Portrait background created");

            smileyRight = BitmapFactory.decodeResource(getResources(), R.drawable.smiley); // create pong image for playerCount
            Log.i(getLocalClassName(), "Smiley right created");

            smileyHeight = smileyRight.getHeight(); // store smiley dimensions
            smileyWidth = smileyRight.getWidth();

            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);

            smileyLeft = Bitmap.createBitmap(smileyRight,
                    0,
                    0,
                    smileyRight.getWidth(),
                    smileyRight.getHeight(),
                    matrix,
                    true);
            Log.i(getLocalClassName(), "Smiley left created");

            smileyShadesRight = BitmapFactory.decodeResource(getResources(), R.drawable.smiley2); // create pong image for playerCount
            Log.i(getLocalClassName(), "Smiley shades right created");

            smileyShadesLeft = Bitmap.createBitmap(smileyShadesRight,
                    0,
                    0,
                    smileyShadesRight.getWidth(),
                    smileyShadesRight.getHeight(),
                    matrix, true);
            Log.i(getLocalClassName(), "Smiley shades left created");

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
            } else {
                popupText.setTextSize(12);
                scoreText.setTextSize(15);
                ballSize = 4;
                Log.i(getLocalClassName(), "Screen DPI is not low, adjustment sizes set to normal");
            }

            pint.setColor(Color.WHITE);
            circleStrokePaint.setStyle(Paint.Style.STROKE);

            if (balls.size() == 1) {
                Log.i(getLocalClassName(), "Ball initialized");
            } else {
                Log.i(getLocalClassName(), "Balls initialized");
            }

            if (soloGame) {
                players[0] = new Player(GameActivity.this,
                        canvasHeight - canvasHeight / 4,
                        midpoint,
                        canvasHeight - canvasHeight / 4);
                Log.i(getLocalClassName(), "Player0 initialized");
            } else {
                for (int playerCounter = 0; playerCounter < players.length; playerCounter++) // initialize playerCount and AI
                {
                    players[playerCounter] = new Player(GameActivity.this,
                            ground[playerCounter],
                            midpoint,
                            ground[playerCounter]);
                    Log.i(getLocalClassName(), "Player" + Integer.toString(playerCounter) + " initialized");
                }
                ai = new AI(GameActivity.this, balls, players[0]);
                Log.i(getLocalClassName(), "AI initialized");
            }
            globalthread = new GlobalThread();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) { // when user leaves game
            isRunning = false;
            Log.i(getLocalClassName(), "Surface destroyed");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(getLocalClassName(), "Surface changed");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) { // when user enters game
            gameSurfaceThread = new GameSurfaceThread(GameActivity.this, holder, this);
            Log.i(getLocalClassName(), "Surface created");
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                players[0].jump();
            }

            return true;
        }

        public void screenDraw(Canvas canvas) {
            canvas.drawBitmap(back, 0, 0, null);
            for (Player player : players) { // draw playerCount
                shadowPaint.setAlpha(player.getShadowOpacity());
                canvas.drawOval(player.getShadow(), shadowPaint);
                if (player == players[0]) {
                    if (player.isRight()) {
                        canvas.drawBitmap(smileyRight,
                                player.getPosition().x,
                                player.getPosition().y, null);
                    } else {
                        canvas.drawBitmap(smileyLeft,
                                player.getPosition().x,
                                player.getPosition().y,
                                null); // draw playerCount smiley
                    }
                } else if (player.isRight()) {
                    canvas.drawBitmap(smileyShadesRight,
                            player.getPosition().x,
                            player.getPosition().y,
                            null);
                } else {
                    canvas.drawBitmap(smileyShadesLeft,
                            player.getPosition().x,
                            player.getPosition().y,
                            null); // draw AI smiley
                }
            }

            for (int index = 0; index < balls.size(); index++) { // balls drawer
                Ball ball = balls.get(index);
                if (ball.isDead()) {
                    balls.remove(index);
                } else {
                    canvas.drawCircle(ball.getPosition().x,
                            ball.getPosition().y,
                            ballSize,
                            pint);
                }
            }

            for (int index = 0; index < trails.size(); index++) { // trails drawer
                Trail trail = trails.get(index);
                if (trail.getLife() > 0) {
                    ballTrail.setStrokeWidth(ballSize - trail.calcSize());
                    ballTrail.setColor(Color.argb(trail.getLife() * 25, 255, 255, 255));
                    canvas.drawLine(trail.getStartPoint().x,
                            trail.getStartPoint().y,
                            trail.getEndPoint().x,
                            trail.getEndPoint().y,
                            ballTrail);
                    trail.decrementLife();
                } else {
                    trails.remove(index); // remove dead trails
                }
            }

            for (int index = 0; index < shockWaves.size(); index++) { // shockWaves drawer
                ShockWave shockWave = shockWaves.get(index);
                if (shockWave.getLife() > 0) // bump animation
                {
                    int currentShockWaveLife = shockWave.getLife();
                    switch (shockWave.getType()) {
                        case EXTRA_SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 23, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(shockWave.getPosition().x,
                                    shockWave.getPosition().y,
                                    11 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case SMALL_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 12, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(2);
                            canvas.drawCircle(shockWave.getPosition().x,
                                    shockWave.getPosition().y,
                                    21 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case MEDIUM_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife * 2, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(shockWave.getPosition().x,
                                    shockWave.getPosition().y,
                                    128 - currentShockWaveLife,
                                    circleStrokePaint);
                            break;
                        case LARGE_WAVE:
                            circleStrokePaint.setColor(Color.argb(currentShockWaveLife, 255, 255, 255));
                            circleStrokePaint.setStrokeWidth(1);
                            canvas.drawCircle(shockWave.getPosition().x,
                                    shockWave.getPosition().y,
                                    252 - currentShockWaveLife,
                                    circleStrokePaint);
                    }
                } else {
                    shockWaves.remove(index); // remove dead shockWaves
                }
            }

            for (int index = 0; index < popups.size(); index++) { // popups text drawer
                if (popups.get(index).getLife() > 0) // if popups text is to be shown
                {
                    popupText.setColor(Color.argb(popups.get(index).getLife(), 255, 255, 255)); // text fade effect
                    Popup popup = popups.get(index);

                    switch (popups.get(index).getType()) {
                        case YEY:
                            canvas.drawText(yeyStrings[popup.getTextIndex()],
                                    popup.getPosition().x,
                                    popup.getPosition().y - popup.getLife(),
                                    popupText);
                            break;
                        case BOO:
                            canvas.drawText(booStrings[popup.getTextIndex()],
                                    popup.getPosition().x,
                                    popup.getPosition().y + popup.getLife(),
                                    popupText);
                            break;
                        case BUMP:
                            canvas.drawText(yeyStrings[popup.getTextIndex()],
                                    popup.getPosition().x,
                                    popup.getPosition().y + popup.getLife(),
                                    popupText);
                    }
                } else {
                    popups.remove(index); // remove dead popups
                }
            }

            for (int index = 0; index < buzzBalls.size(); index++) { // draw buzzBalls
                BuzzBall buzzBall = buzzBalls.get(index);
                buzzBallPaint.setAlpha(buzzBalls.get(index).getOpacity());

                if (buzzBall.isDead()) {
                    buzzBalls.remove(index); // cleanup dead buzzBalls from array list
                } else if (buzzBall.getOpacity() < 0) {
                    buzzBall.kill();
                } else {
                    canvas.drawBitmap(buzzBallBitmaps[buzzBall.getType()].getFrame(buzzBall.getRotation()).getBitmap(),
                            buzzBall.getPosition().x + buzzBallBitmaps[buzzBall.getType()].getFrame(buzzBall.getRotation()).getOffset().x,
                            buzzBall.getPosition().y + buzzBallBitmaps[buzzBall.getType()].getFrame(buzzBall.getRotation()).getOffset().y,
                            buzzBallPaint);
                }
            }
            if (life > 0) {
                canvas.drawText("Ball Count: " + Integer.toString(balls.size()) + " " + "Score: " + Integer.toString(gameScore) + "  " + "Extra Life: " + Integer.toString(life),
                        10,
                        canvasHeight - 10,
                        scoreText);
            }
        }
    }
}