package ichbinkaiser.neutron.entity;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ichbinkaiser.neutron.activity.GameActivity;
import ichbinkaiser.neutron.control.Player;
import lombok.Getter;

public class BuzzBall implements Runnable {

    private int height, width;
    private float gravity = 0.0f;
    private float climb = 3; // upward force
    private int xMovement = 0;
    private float inertia = 0;
    private int ground; // ground bounce for buzzBalls

    private Player[] player; // playerCount pointer
    private GameActivity gameActivity;
    private Random rnd = new Random();
    private List<Ball> balls;
    private List<Ball> ballsCollided = new ArrayList<>(); // array list of balls that have collided with this buzzBalls

    @Getter
    private int type; // buzzBalls type

    @Getter
    private boolean dead = false, dying = false;

    @Getter
    private int rotation = 0;

    @Getter
    private int opacity = 255;

    @Getter
    private Point position = new Point();

    @Getter
    private Point previousPosition = new Point();

    public BuzzBall(GameActivity gameActivity, int type, int height, int width, List<Ball> balls, Player[] player) {
        this.gameActivity = gameActivity;
        this.type = type;
        this.height = height;
        this.width = width;
        this.balls = balls;
        this.player = player;
        position.x = rnd.nextInt(gameActivity.getCanvasWidth() - width);
        position.y = 0 - height;
        start();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("BuzzBall");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        int roll;
        ground = (gameActivity.getGround()[1] - rnd.nextInt(gameActivity.getGround()[1] - gameActivity.getGround()[0])); // set random ground for buzzBalls in a specific range

        while (gameActivity.isRunning() && (!dead)) {
            ///////////////////////////// BUZZBALL ROTATION AND MOVEMENT //////////////////////////////////////
            roll = (int) gameActivity.getRollAngle();

            if ((roll > 0) && (inertia < 10)) {
                inertia += .05;
            } else if ((roll < 0) && (inertia > -10)) {
                inertia -= .05;
            }

            rotation += (inertia); // rotation to positive target rotation

            if (rotation > 359) {
                rotation -= 360;
            } else if (rotation < 0) {
                rotation += 360;
            }

            position.y += climb + gravity;
            gravity += 0.025f;

            if (!dying) {
                if (position.x < 0) // left or right movement responding to collision with screen boundaries
                {
                    xMovement = 2;
                } else if (position.x > gameActivity.getCanvasWidth() - width) {
                    xMovement = -2;
                }
            }
            position.x += xMovement;

            if ((position.y > ground) && (!dying)) {
                dying = true;
                climb = -1; // emulate gravity
                gravity = 0.0f;

                if (rnd.nextBoolean()) {
                    inertia = 10;
                    xMovement = 2;
                } else {
                    inertia = -10;
                    xMovement = -2;
                }
            }
            ////////////////////// BUZZBALL TO BALL COLLISION DETECTION /////////////////////////////////
            for (Ball ball : balls) {
                if (ball.getPosition().x >= position.x
                        && ball.getPosition().x <= position.x + width
                        && ball.getPosition().y >= position.y
                        && ball.getPosition().y <= position.y + height) {
                    boolean collided = false;
                    for (Ball ballCollided : ballsCollided) {
                        if (ballCollided == ball) {
                            collided = true;
                        }
                    }

                    if (!collided && !dying) {

                        if (ball.isGoingLeft()) {
                            ball.setGoingLeft(false);
                            xMovement = 2;
                        } else {
                            ball.setGoingLeft(true);
                            xMovement = -2;
                        }
                        ballsCollided.add(ball);
                    }
                }
            }

            previousPosition.x = position.x;
            previousPosition.y = position.y;

            if (dying) {
                opacity--;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Log.e("BuzzBall", interruptedException.toString());
            }
        }
    }

    public void kill() {
        this.dead = true;
    }

}
