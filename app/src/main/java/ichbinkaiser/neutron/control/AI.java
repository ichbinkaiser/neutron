package ichbinkaiser.neutron.control;

import android.graphics.Point;
import android.util.Log;

import java.util.List;

import ichbinkaiser.neutron.activity.GameActivity;
import ichbinkaiser.neutron.entity.Ball;

public class AI implements Runnable {
    GameActivity gameActivity;
    List<Ball> ball;

    Point playerPosition = new Point(); // Player location
    Point target = new Point(); // top balls threat

    public AI(GameActivity gameActivity, List<Ball> ball, Player player) {
        this.gameActivity = gameActivity;
        this.ball = ball;
        playerPosition = player.position;
        start();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("AI");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        target.x = gameActivity.getCanvasWidth() / 2;

        ////////////////////////// AI TARGETING LOGIC ///////////////////////////////////////
        while (gameActivity.isRunning()) // AI Thread
        {
            target.y = 0;
            for (Ball currentBall : ball) {
                currentBall.setValidTarget(currentBall.getPosition().x <= playerPosition.x
                        && currentBall.getPosition().x <= playerPosition.x + gameActivity.getSmileyWidth()); // if not true balls is a valid AI target

                if (!currentBall.isDead() && currentBall.isNotGoingUp() && currentBall.isValidTarget() && currentBall.getPosition().y > target.y) {
                    target.set(currentBall.getPosition().x - gameActivity.getSmileyWidth() / 2, currentBall.getPosition().y - gameActivity.getSmileyHeight() / 2); // check for priority target
                }
            }
            gameActivity.getPlayers()[1].setDestination(target.x); // set AI target

            try {
                Thread.sleep(10);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Log.e("AI", interruptedException.toString());
            }
        }
    }
}