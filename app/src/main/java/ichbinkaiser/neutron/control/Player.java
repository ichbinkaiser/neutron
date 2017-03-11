package ichbinkaiser.neutron.control;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

import ichbinkaiser.neutron.activity.GameActivity;

public class Player implements Runnable {
    GameActivity gameActivity;
    Point position = new Point();
    Point previousPosition = new Point(); // last position

    float climb; // upward force
    float gravity = 0.0f; // downward force
    int destination;

    boolean right = true; // is left direction
    boolean jumping = false; // is jumping
    int ground; // base point for playerCount

    Random rnd = new Random();
    float shadowL, shadowT, shadowR, shadowB, shadowEdge; // shadow properties
    int shadowOpacity;
    RectF shadow = new RectF();
    int jumpHeight;

    public Player(GameActivity gameActivity, int ground, int x, int y) {
        this.gameActivity = gameActivity;
        this.ground = ground;
        position.set(x, y);
        start();
    }

    private void start() {
        Thread thread = new Thread(this);
        thread.setName("Player");
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        int edge = gameActivity.getCanvasWidth() - gameActivity.getSmileyWidth();
        shadowT = ground + gameActivity.getSmileyHeight() - gameActivity.getSmileyHeight() / 4;
        shadowB = ground + gameActivity.getSmileyHeight();
        while (gameActivity.isRunning()) {
            if (position.x < destination && position.x < edge) // playerCount move right
            {
                if (Math.abs(destination - position.x) > 10) {
                    position.x += 5;
                } else {
                    position.x++;
                }
            } else if (position.x > destination && position.x > 0) // playerCount move left
            {
                if (Math.abs(destination - position.x) > 10) {
                    position.x -= 5;
                } else {
                    position.x--;
                }
            }

            if (position.x > previousPosition.x) {
                right = true; // playerCount has moved right
            } else if (position.x < previousPosition.x) {
                right = false; // playerCount has moved left
            }

            previousPosition.set(position.x, position.y);

            if (jumping) {
                position.y += climb + gravity * gravity;
                gravity += 0.05f;
            }

            if (jumping && position.y > ground) {
                jumping = false;
                position.y = ground;
                gravity = 0.0f;
            }

            jumpHeight = (ground - position.y) / 10;

            if (jumpHeight < gameActivity.getSmileyWidth() / 4) {
                shadowEdge = jumpHeight;
                shadowOpacity = 50 - jumpHeight * 2;
            }

            shadowL = position.x + shadowEdge;
            shadowR = position.x + gameActivity.getSmileyWidth() - shadowEdge;

            shadow.set(shadowL, shadowT, shadowR, shadowB);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Player", e.toString());
            }
        }
    }

    public void jump() // do jump
    {
        climb = -(float) (rnd.nextInt(2) + 6); // upward force
        jumping = true;
    }

    public void setDestination(float roll) {
        if (roll < 0) {
            destination = (int) (position.x + Math.abs(roll * 5));
        } else if (roll > 0) {
            destination = (int) (position.x - Math.abs(roll * 5));
        }
    }

    public void setDestination(int target) {
        destination = target;
    }

    public Point getPosition() {
        return position;
    }

    public boolean isRight() {
        return right;
    }

    public int getShadowOpacity() {
        return shadowOpacity;
    }

    public RectF getShadow() {
        return shadow;
    }
}