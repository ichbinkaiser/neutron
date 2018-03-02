package ichbinkaiser.neutron.entity;

import android.graphics.Point;
import android.util.Log;

import java.util.List;
import java.util.Random;

import ichbinkaiser.neutron.activity.GameActivity;
import ichbinkaiser.neutron.control.Player;
import ichbinkaiser.neutron.core.ResourceManager;
import lombok.Getter;
import lombok.Setter;

public class Ball implements Runnable {

    private float climb; // upward force
    private float gravity; // downward force
    private int sidewaysSpeed;
    private boolean collide; // has collided
    private int spawnWave = 0;

    private GameActivity gameActivity;
    private List<Ball> balls; // balls pointer
    private Player[] players; // playerCount pointer
    private ResourceManager resourceManager = ResourceManager.getInstance();
    private Point previousPosition = new Point(); // previous position
    private Random rnd = new Random();

    @Getter
    @Setter
    private boolean validTarget = true;

    @Getter
    @Setter
    private boolean isGoingLeft; // is left balls direction

    @Getter
    private boolean isDead = false; // balls is removed from balls list

    @Getter
    private Point position = new Point();

    public Ball(GameActivity gameActivity, List<Ball> balls, Player[] players) {
        this.gameActivity = gameActivity;
        this.balls = balls;
        this.players = players;

        sidewaysSpeed = rnd.nextInt(5);
        position.x = rnd.nextInt(gameActivity.getCanvasWidth());
        position.y = gameActivity.getCanvasHeight();
        climb = -(float) (rnd.nextInt(3) + 12); // upward force
        gravity = 0;

        int number = rnd.nextInt(3);
        isGoingLeft = number > 1;
        start();
    }

    private boolean checkCollision(Point object) // balls collision detection
    {
        return object.x <= position.x + gameActivity.getBallSize() - 1
                && object.x >= position.x - gameActivity.getBallSize() - 1
                && object.y <= position.y + gameActivity.getBallSize() - 1
                && object.y >= position.y - gameActivity.getBallSize() - 1;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("Ball");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while (gameActivity.isRunning() && !isDead) {
            ///////////////////////////// BALL TO PLAYER COLLISION DETECTION //////////////////////////
            for (Player currentPlayer : players) {
                if (position.y >= currentPlayer.getPosition().y
                        && position.y <= currentPlayer.getPosition().y + gameActivity.getSmileyHeight()
                        && position.x >= currentPlayer.getPosition().x
                        && position.x <= currentPlayer.getPosition().x + gameActivity.getSmileyWidth()
                        && isNotGoingUp()) // playerCount to balls collision detection
                {
                    climb = -(gravity * 2); // emulate gravity
                    gravity = 0.0f;

                    gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.EXTRA_SMALL_WAVE));
                    isGoingLeft = currentPlayer.isRight();

                    if (currentPlayer == players[0]) // if human players
                    {
                        gameActivity.addGameScore(1);
                        gameActivity.doShake(40);
                    }

                    resourceManager.playSound(Sound.POP, 1);

                    sidewaysSpeed = rnd.nextInt(5);
                    gameActivity.getPopups().add(new Popup(position, PopupType.BUMP, gameActivity.getYeyStrings().length)); // popups text in score++
                }
            }
            //////////////////////// BALL TO BALL COLLISION DETECTION ///////////////////////////////
            for (Ball currentBall : balls) {
                if (this != currentBall && !collide) // if balls is not compared to itself and has not yet collided
                {
                    if (checkCollision(currentBall.position)) // balls collision detected
                    {
                        resourceManager.playSound(Sound.RESTART, 1);
                        isGoingLeft = !isGoingLeft && !currentBall.isGoingLeft; // go right if bumped balls is going left
                        currentBall.isGoingLeft = !isGoingLeft; // reverse direction of the bumped balls
                        sidewaysSpeed = rnd.nextInt(5);
                        currentBall.collide = true;
                    }
                }
            }
            collide = false;
            ////////////////////////// BALL MOVEMENT AND WORLD BOUNDARY INTERACTION //////////////////////////////////////
            previousPosition.x = position.x;
            previousPosition.y = position.y;

            position.y += climb + gravity * gravity;
            gravity += 0.05f;

            if (isGoingLeft) {
                position.x += sidewaysSpeed;
            } else {
                position.x -= sidewaysSpeed; // balls horizontal movement
            }

            if (spawnWave > 0) // spawn_wave animation
            {
                gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.SMALL_WAVE));
                spawnWave--;
            }

            if (position.x < 0) // balls has reached left wall
            {
                isGoingLeft = true;
                resourceManager.playSound(Sound.POP_WALL, 1);
            }

            if (position.x > gameActivity.getCanvasWidth()) // balls has reached right wall
            {
                isGoingLeft = false;
                resourceManager.playSound(Sound.POP_WALL, 1);
            }

            if (position.y > gameActivity.getCanvasHeight()) // balls has fallen off screen
            {
                gameActivity.decrementLife();
                gameActivity.getPopups().add(new Popup(position, PopupType.YEY, gameActivity.getBooStrings().length));
                resourceManager.playSound(Sound.DOWN, 1);
                gameActivity.doShake(100);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                    Log.e("Ball", interruptedException.toString());
                }
                isDead = true;
                resourceManager.playSound(Sound.DOWN, 1);
            } else // balls trailer effects
            {
                gameActivity.getTrails().add(new Trail(previousPosition, position));
                gameActivity.getShockWaves().add(new ShockWave(position, ShockWaveType.EXTRA_SMALL_WAVE));
            }

            try {
                Thread.sleep(40);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Log.e("Ball", interruptedException.toString());
            }
        }
    }

    public boolean isNotGoingUp() {
        return (previousPosition.y <= position.y);
    }
}